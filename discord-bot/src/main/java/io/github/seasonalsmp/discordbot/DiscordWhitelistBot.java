package io.github.seasonalsmp.discordbot;

import io.github.seasonalsmp.discordbot.config.BotConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DiscordWhitelistBot extends ListenerAdapter {
    private final BotConfig config;
    private JDA jda;
    private WhitelistAPIClient apiClient;

    public DiscordWhitelistBot() {
        this.config = loadConfig();
        this.apiClient = new WhitelistAPIClient(config.apiBaseUrl, config.apiKey);
    }

    public void start() {
        try {
            jda = JDABuilder.createDefault(config.token)
                    .addEventListeners(this)
                    .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                    .build();
            jda.awaitReady();
            log("Discord bot started successfully.");
        } catch (LoginException e) {
            log("Failed to login to Discord: " + e.getMessage());
        } catch (Exception e) {
            log("Failed to start Discord bot: " + e.getMessage());
        }
    }

    public void stop() {
        if (jda != null) {
            jda.shutdown();
            log("Discord bot stopped.");
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        if (event.getChannel().getId().equals(config.whitelistChannelId)) {
            handleWhitelistRequest(event.getMessage(), event.getAuthor());
        }
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        if (event.getChannel().getId().equals(config.whitelistChannelId)) {
            event.getMessage().addReaction("❌").queue();
        }
    }

    private void handleWhitelistRequest(Message message, User author) {
        String content = message.getContentRaw().trim();
        String username = content;

        if (username.isEmpty() || username.length() < 3 || username.length() > 16) {
            message.addReaction("❌").queue();
            message.reply("Invalid username format. Username must be 3-16 characters.").queue();
            log("Invalid username format from " + author.getId() + ": " + username);
            return;
        }

        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            message.addReaction("❌").queue();
            message.reply("Invalid username format. Only letters, numbers, and underscores are allowed.").queue();
            log("Invalid username characters from " + author.getId() + ": " + username);
            return;
        }

        message.addReaction("⏳").queue();

        CompletableFuture.supplyAsync(() -> fetchUuidFromMojang(username))
                .thenAccept(mojangResponse -> {
                    if (mojangResponse == null || mojangResponse.uuid == null) {
                        message.clearReactions().queue();
                        message.addReaction("❌").queue();
                        message.reply("Minecraft account not found. Please check the username and try again.").queue();
                        log("Mojang API returned null for username: " + username);
                        return;
                    }

                    apiClient.addPlayer(author.getId(), mojangResponse.username)
                            .thenAccept(apiResponse -> {
                                if (apiResponse.isSuccess()) {
                                    message.clearReactions().queue();
                                    message.addReaction("✅").queue();

                                    Role whitelistRole = message.getGuild().getRoleById(config.whitelistRoleId);
                                    if (whitelistRole != null) {
                                        message.getGuild().retrieveMember(author).queue(
                                                member -> {
                                                    if (member != null) {
                                                        message.getGuild().addRoleToMember(member, whitelistRole).queue(
                                                                success -> log("Assigned whitelist role to " + author.getName()),
                                                                error -> log("Failed to assign role: " + error.getMessage())
                                                        );
                                                    }
                                                },
                                                error -> log("Failed to retrieve member: " + error.getMessage())
                                        );
                                    }

                                    String dmMessage = "🎉 " + mojangResponse.username + " has been whitelisted!\n\n" +
                                            "You're officially ready for Seasonal SMP.\n\n" +
                                            "Keep an eye on the Discord for launch announcements. The server IP and launch time will be posted there.\n\n" +
                                            "See you in the first season!";
                                    author.openPrivateChannel().queue(
                                            privateChannel -> privateChannel.sendMessage(dmMessage).queue(
                                                    success -> log("Sent DM to " + author.getName()),
                                                    error -> {
                                                        log("Failed to send DM to " + author.getName() + ": " + error.getMessage());
                                                    }
                                            ),
                                            error -> log("Failed to open DM channel for " + author.getName() + ": " + error.getMessage())
                                    );
                                } else {
                                    message.clearReactions().queue();
                                    message.addReaction("❌").queue();
                                    message.reply("Failed to whitelist: " + apiResponse.getMessage()).queue();
                                    log("Whitelist API error for " + author.getId() + " (" + username + "): " + apiResponse.getMessage());
                                }
                            });
                });
    }

    private MojangResponse fetchUuidFromMojang(String username) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            int responseCode = connection.getResponseCode();
            if (responseCode == 204) {
                return null;
            }
            if (responseCode != 200) {
                return null;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
                if (json == null || !json.has("id") || !json.has("name")) {
                    return null;
                }
                String id = json.get("id").getAsString();
                String name = json.get("name").getAsString();
                return new MojangResponse(name, id);
            }
        } catch (Exception e) {
            log("Failed to fetch UUID from Mojang for " + username + ": " + e.getMessage());
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private BotConfig loadConfig() {
        BotConfig config = new BotConfig();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config.yml")) {
            if (is == null) {
                throw new FileNotFoundException("config.yml not found in resources.");
            }
            Properties properties = new Properties();
            properties.load(is);
            config.token = properties.getProperty("bot.token", "");
            config.guildId = properties.getProperty("bot.guild-id", "");
            config.whitelistChannelId = properties.getProperty("bot.whitelist-channel-id", "");
            config.whitelistRoleId = properties.getProperty("bot.whitelist-role-id", "");
            config.apiBaseUrl = properties.getProperty("bot.api-base-url", "http://localhost:8080");
            config.apiKey = properties.getProperty("bot.api-key", "changeme");
        } catch (Exception e) {
            log("Failed to load config: " + e.getMessage());
            System.exit(1);
        }
        return config;
    }

    private void log(String message) {
        System.out.println("[DiscordBot] " + message);
    }

    private static class BotConfig {
        String token;
        String guildId;
        String whitelistChannelId;
        String whitelistRoleId;
        String apiBaseUrl;
        String apiKey;
    }

    private static class MojangResponse {
        String name;
        String uuid;

        MojangResponse(String name, String uuid) {
            this.name = name;
            this.uuid = uuid;
        }
    }

    public static void main(String[] args) {
        DiscordWhitelistBot bot = new DiscordWhitelistBot();
        Runtime.getRuntime().addShutdownHook(new Thread(bot::stop));
        bot.start();
    }
}

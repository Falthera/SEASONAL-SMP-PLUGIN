package io.github.seasonalsmp.discordbot;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.security.auth.login.LoginException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DiscordWhitelistBot extends ListenerAdapter {
    private final String token;
    private final String guildId;
    private final String whitelistChannelId;
    private final String whitelistRoleId;
    private final String logChannelId;
    private final String apiBaseUrl;
    private final String apiKey;

    private JDA jda;
    private WhitelistAPIClient apiClient;

    public DiscordWhitelistBot(Map<String, String> env) {
        List<String> missing = new ArrayList<>();
        this.token = require(env, "DISCORD_TOKEN", missing);
        this.guildId = require(env, "GUILD_ID", missing);
        this.whitelistChannelId = require(env, "WHITELIST_CHANNEL_ID", missing);
        this.whitelistRoleId = require(env, "WHITELIST_ROLE_ID", missing);
        this.logChannelId = require(env, "LOG_CHANNEL_ID", missing);
        this.apiBaseUrl = require(env, "PLUGIN_API_URL", missing);
        this.apiKey = require(env, "PLUGIN_API_KEY", missing);

        if (!missing.isEmpty()) {
            System.err.println("[DiscordBot] Missing required environment variables:");
            for (String var : missing) {
                System.err.println("[DiscordBot]   - " + var);
            }
            System.exit(1);
        }

        this.apiClient = new WhitelistAPIClient(apiBaseUrl, apiKey);
    }

    public void start() {
        try {
            jda = JDABuilder.createDefault(token)
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
        if (event.getChannel().getId().equals(whitelistChannelId)) {
            handleWhitelistRequest(event.getMessage(), event.getAuthor());
        }
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        if (event.getChannel().getId().equals(whitelistChannelId)) {
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

                                    Role whitelistRole = message.getGuild().getRoleById(whitelistRoleId);
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

    private static String require(Map<String, String> env, String key, List<String> missing) {
        String value = env.get(key);
        if (value == null || value.isBlank()) {
            missing.add(key);
        }
        return value != null ? value : "";
    }

    private void log(String message) {
        System.out.println("[DiscordBot] " + message);
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
        DotenvBuilder builder = Dotenv.configure()
                .directory("./")
                .filename(".env")
                .ignoreIfMissing()
                .systemProperties();

        Dotenv dotenv = builder.load();

        Map<String, String> env = new HashMap<>();
        for (Dotenv.Entry entry : dotenv.entries()) {
            env.put(entry.getKey(), entry.getValue());
        }

        System.getenv().forEach((k, v) -> env.putIfAbsent(k, v));

        DiscordWhitelistBot bot = new DiscordWhitelistBot(env);
        Runtime.getRuntime().addShutdownHook(new Thread(bot::stop));
        bot.start();
    }
}

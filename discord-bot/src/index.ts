import { Client, GatewayIntent, Message, User, Partials } from 'discord.js';
import dotenv from 'dotenv';
import { WhitelistAPIClient } from './api';
import { ApiResponse } from './types';

dotenv.config();

interface Env {
  DISCORD_TOKEN: string;
  GUILD_ID: string;
  WHITELIST_CHANNEL_ID: string;
  WHITELIST_ROLE_ID: string;
  LOG_CHANNEL_ID: string;
  PLUGIN_API_URL: string;
  PLUGIN_API_KEY: string;
}

function requireEnv(key: string, env: Env): string {
  const value = env[key];
  if (!value || value.trim() === '') {
    console.error(`[DiscordBot] Missing required environment variable: ${key}`);
    process.exit(1);
  }
  return value;
}

class DiscordWhitelistBot {
  private token: string;
  private guildId: string;
  private whitelistChannelId: string;
  private whitelistRoleId: string;
  private logChannelId: string;
  private apiBaseUrl: string;
  private apiKey: string;

  private client: Client;
  private apiClient: WhitelistAPIClient;

  constructor(env: Env) {
    this.token = requireEnv('DISCORD_TOKEN', env);
    this.guildId = requireEnv('GUILD_ID', env);
    this.whitelistChannelId = requireEnv('WHITELIST_CHANNEL_ID', env);
    this.whitelistRoleId = requireEnv('WHITELIST_ROLE_ID', env);
    this.logChannelId = requireEnv('LOG_CHANNEL_ID', env);
    this.apiBaseUrl = requireEnv('PLUGIN_API_URL', env);
    this.apiKey = requireEnv('PLUGIN_API_KEY', env);

    this.apiClient = new WhitelistAPIClient(this.apiBaseUrl, this.apiKey);

    this.client = new Client({
      intents: [
        GatewayIntent.Guilds,
        GatewayIntent.GuildMessages,
        GatewayIntent.MessageContent,
      ],
      partials: [Partials.Channel],
    });

    this.client.on('messageCreate', this.handleMessageCreate.bind(this));
    this.client.on('messageUpdate', this.handleMessageUpdate.bind(this));
  }

  async start(): Promise<void> {
    try {
      await this.client.login(this.token);
      this.log('Discord bot started successfully.');
    } catch (error: any) {
      this.log(`Failed to login to Discord: ${error.message}`);
    }
  }

  stop(): void {
    if (this.client) {
      this.client.destroy();
      this.log('Discord bot stopped.');
    }
  }

  private async handleMessageCreate(message: Message): Promise<void> {
    if (message.author.bot) return;
    if (message.channelId !== this.whitelistChannelId) return;

    await this.handleWhitelistRequest(message);
  }

  private async handleMessageUpdate(oldMessage: Message | undefined, newMessage: Message): Promise<void> {
    if (newMessage.channelId !== this.whitelistChannelId) return;
    try {
      await newMessage.react('❌');
    } catch {
      // ignore reaction errors
    }
  }

  private async handleWhitelistRequest(message: Message): Promise<void> {
    const username = message.content.trim();

    if (!username || username.length < 3 || username.length > 16) {
      await message.react('❌');
      await message.reply('Invalid username format. Username must be 3-16 characters.');
      this.log(`Invalid username format from ${message.author.id}: ${username}`);
      return;
    }

    if (!/^[a-zA-Z0-9_]+$/.test(username)) {
      await message.react('❌');
      await message.reply('Invalid username format. Only letters, numbers, and underscores are allowed.');
      this.log(`Invalid username characters from ${message.author.id}: ${username}`);
      return;
    }

    await message.react('⏳');

    const mojangResponse = await this.fetchUuidFromMojang(username);
    if (!mojangResponse || !mojangResponse.uuid) {
      await message.reactions.removeAll();
      await message.react('❌');
      await message.reply('Minecraft account not found. Please check the username and try again.');
      this.log(`Mojang API returned null for username: ${username}`);
      return;
    }

    const apiResponse = await this.apiClient.addPlayer(message.author.id, mojangResponse.username);
    if (apiResponse.isSuccess()) {
      await message.reactions.removeAll();
      await message.react('✅');

      const guild = this.client.guilds.cache.get(this.guildId);
      if (guild) {
        const member = await guild.members.fetch(message.author.id).catch(() => null);
        if (member) {
          const role = guild.roles.cache.get(this.whitelistRoleId);
          if (role) {
            await member.roles.add(role).catch((error) => {
              this.log(`Failed to assign role: ${error.message}`);
            });
          }
        }
      }

      const dmMessage = `🎉 ${mojangResponse.username} has been whitelisted!\n\nYou're officially ready for Seasonal SMP.\n\nKeep an eye on the Discord for launch announcements. The server IP and launch time will be posted there.\n\nSee you in the first season!`;
      await message.author.send(dmMessage).catch((error) => {
        this.log(`Failed to send DM to ${message.author.username}: ${error.message}`);
      });
    } else {
      await message.reactions.removeAll();
      await message.react('❌');
      await message.reply(`Failed to whitelist: ${apiResponse.getMessage()}`);
      this.log(`Whitelist API error for ${message.author.id} (${username}): ${apiResponse.getMessage()}`);
    }
  }

  private async fetchUuidFromMojang(username: string): Promise<{ name: string; uuid: string } | null> {
    try {
      const response = await fetch(`https://api.mojang.com/users/profiles/minecraft/${encodeURIComponent(username)}`, {
        headers: { 'Content-Type': 'application/json' },
      });

      if (response.status === 204) return null;
      if (response.status !== 200) return null;

      const json = await response.json();
      if (!json || !json.id || !json.name) return null;

      return { name: json.name, uuid: json.id };
    } catch (error: any) {
      this.log(`Failed to fetch UUID from Mojang for ${username}: ${error.message}`);
      return null;
    }
  }

  private log(message: string): void {
    console.log(`[DiscordBot] ${message}`);
  }
}

const env: Env = {
  DISCORD_TOKEN: process.env.DISCORD_TOKEN || '',
  GUILD_ID: process.env.GUILD_ID || '',
  WHITELIST_CHANNEL_ID: process.env.WHITELIST_CHANNEL_ID || '',
  WHITELIST_ROLE_ID: process.env.WHITELIST_ROLE_ID || '',
  LOG_CHANNEL_ID: process.env.LOG_CHANNEL_ID || '',
  PLUGIN_API_URL: process.env.PLUGIN_API_URL || 'http://127.0.0.1:8080',
  PLUGIN_API_KEY: process.env.PLUGIN_API_KEY || '',
};

const bot = new DiscordWhitelistBot(env);
bot.start().catch((error) => {
  console.error(`[DiscordBot] Fatal error: ${error.message}`);
  process.exit(1);
});

process.on('SIGINT', () => {
  bot.stop();
  process.exit(0);
});

process.on('SIGTERM', () => {
  bot.stop();
  process.exit(0);
});

# Seasonal SMP

A production-ready Minecraft Paper plugin for Minecraft 1.21.11 featuring seasonal world transformations, season-bound gameplay, legendary swords, and immersive visual effects.

## Features

- Dynamic 7-day seasonal cycle (Spring, Summer, Autumn, Winter)
- Season Bounds with unique passive bonuses and active abilities
- Four legendary swords with custom models and feel-good abilities
- Full world transformation per season:
  - Spring: Flower petals, faster crops, bees stay passive
  - Summer: Heat haze, ember particles, longer days, stronger mobs
  - Autumn: Falling leaves, leaf decay, harvest bonuses, fog
  - Winter: Snowstorm particles, water freezes, ice spreads near players, longer nights
- Modern UI using Adventure API (BossBars, Titles, ActionBars)
- Fully configurable gameplay values
- Commands with tab-completion
- Persistent player data
- Optional forced resource pack

## Requirements

- Paper 1.21.11+
- Java 21
- Maven 3.8+

## Build

```bash
mvn clean package
```

The compiled JAR will be in `target/SeasonalSMP.jar`.

## Installation

1. Build the plugin with Maven
2. Place `SeasonalSMP.jar` in your server `plugins` folder
3. Configure `config.yml`
4. Restart the server

## Commands

| Command | Description |
|---------|-------------|
| `/season info` | View current season info |
| `/season set <season>` | Set the current season |
| `/season next` | Advance to the next season |
| `/season time` | Show time remaining |
| `/bound view [player]` | View a bound |
| `/bound assign <player> <season>` | Assign a bound |
| `/bound list` | List assigned bounds |
| `/givesword <bound> [player]` | Give a bound sword |
| `/seasonreload` | Reload config |
| `/seasondebug` | Debug info |

## Configuration

The plugin ships with several configuration files:

- `config.yml` — Core plugin settings
- `messages.yml` — All player-facing messages
- `bounds.yml` — Bound ability definitions
- `swords.yml` — Sword stats and ability config
- `worlds.yml` — Per-world season effects

All values are configurable. Do not hardcode values in gameplay logic.

## Resource Pack

The plugin includes a companion resource pack under `src/main/resourcepack/`. It provides placeholder model and texture references for the seasonal swords. Replace the placeholder assets with custom textures and models before distributing.

Resource pack settings:

- `resource-pack.url`
- `resource-pack.sha1`
- `resource-pack.force-pack`

## Discord Whitelist Bot Setup

The plugin includes an integrated Discord whitelist system with a companion bot.

### 1. Copy `.env.example` to `.env`

From the `discord-bot/` directory:

```bash
cp .env.example .env
```

### 2. Where to obtain each Discord ID

- **DISCORD_TOKEN**: Create a bot application at https://discord.com/developers/applications, go to the **Bot** page, and click **Reset Token** or **Copy**.
- **GUILD_ID**: Enable Developer Mode in Discord, right-click your server, and click **Copy ID**.
- **WHITELIST_CHANNEL_ID**: Enable Developer Mode, right-click the whitelist text channel, and click **Copy ID**.
- **WHITELIST_ROLE_ID**: Enable Developer Mode, right-click the whitelisted role, and click **Copy ID**.
- **LOG_CHANNEL_ID**: Enable Developer Mode, right-click the log text channel, and click **Copy ID**.

### 3. How to create a Discord bot application

1. Go to https://discord.com/developers/applications
2. Click **New Application**, enter a name, and accept the terms.
3. In the application page, go to the **Bot** tab.
4. Click **Add Bot** and confirm.
5. Under **Privileged Gateway Intents**, enable:
   - **Message Content Intent**
   - **Server Members Intent** (optional but recommended for role assignment)

### 4. How to obtain the bot token

1. In the Discord Developer Portal, select your application.
2. Go to the **Bot** tab.
3. Under **Token**, click **Reset Token** or **Copy**.
4. Paste the token into your `.env` file as `DISCORD_TOKEN`.

### 5. How to generate a secure API key for the Seasonal SMP plugin

Generate a strong random string and store it in both:

- `plugins/SeasonalSMP/config.yml` under `whitelist.api-key`
- `discord-bot/.env` under `PLUGIN_API_KEY`

Example:

```bash
openssl rand -hex 32
```

### 6. How to configure `PLUGIN_API_URL`

Set `PLUGIN_API_URL` to the public or LAN address where the Paper server is reachable on the API port.

Examples:

- Same machine: `http://127.0.0.1:8080`
- LAN: `http://192.168.1.100:8080`
- Public: `https://your-domain.com:8080`

Ensure the port configured in `plugins/SeasonalSMP/config.yml` under `whitelist.api-port` matches the port exposed by the server.

### 7. How to start the bot

From the `discord-bot/` directory:

```bash
mvn clean package
java -jar target/SeasonalDiscordBot.jar
```

The bot loads `.env` from the current working directory. If any required environment variable is missing, it prints a clear error and exits before starting.

## Developer Guide

This project follows clean architecture and SOLID principles:

- `core/` — Plugin lifecycle and utilities
- `season/` — Season definitions and manager
- `bound/` — Bound types, manager, and per-bound handlers
- `sword/` — Sword management and combat listeners
- `effect/` — Particles and sound services
- `gui/` — UI, BossBars, and messages
- `command/` — Commands and tab completers
- `listener/` — Event listeners
- `data/` — Persistence layer
- `config/` — Configuration management

## License

MIT

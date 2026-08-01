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

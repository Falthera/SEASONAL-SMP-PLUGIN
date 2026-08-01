# Installation Guide

## Server Requirements

- Paper 1.21.11 or newer
- Java 21
- At least 512MB of RAM allocated to the server

## Step 1: Build

```bash
mvn clean package
```

Output JAR: `target/SeasonalSMP.jar`

## Step 2: Install

1. Place `SeasonalSMP.jar` into your server `plugins/` directory.
2. Restart the server.
3. The plugin generates default config files in `plugins/SeasonalSMP/`.

## Step 3: Configure

Edit the generated config files:

- `config.yml` — Core plugin settings
- `messages.yml` — Messages and placeholders
- `bounds.yml` — Bound ability definitions
- `swords.yml` — Sword settings
- `worlds.yml` — Per-world effects

## Step 4: Resource Pack

1. Build or download the companion resource pack.
2. Upload it to a static file host.
3. Set `resource-pack.url` in `config.yml`.
4. Set `resource-pack.sha1` to the SHA-1 hash of the pack.
5. Set `resource-pack.force-pack: true` if you require clients to use it.

## Step 5: Verify

Run `/seasondebug` to verify the plugin loaded correctly.
Run `/season info` to confirm the season system is active.

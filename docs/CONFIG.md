# Configuration Guide

## Core Config: `config.yml`

Key settings:

- `season.cycle-duration-days` — Days per season (default: 7)
- `season.start-season` — Starting season
- `season.auto-cycle` — Enable automatic season advancement
- `world.apply-effects-to` — List of worlds to affect
- `bound.assign-on-first-join` — Auto-assign on first join
- `swords.give-on-bound-assign` — Give sword on assignment
- `resource-pack.url` — Pack URL
- `resource-pack.force-pack` — Force pack on clients
- `effects.particles-per-second-max` — Particle cap per second

## Bound Config: `bounds.yml`

Each bound has:

- `display-name` — Display name
- `color-code` — Chat color code
- `peak-season` — Which season is peak
- `ability-name` — Active ability name
- `passive.enabled` — Enable passive effects
- `passive.effects` — List of passive potion effects
- `peak-bonus.effects` — Peak season effects
- `penalty-seasons.<SEASON>.effects` — Off-season penalties

## Sword Config: `swords.yml`

Each sword has:

- `material` — Item material
- `display-name` — Item name
- `lore` — Item lore lines
- `custom-model-data` — Custom model data for resource pack
- `ability.cooldown-seconds` — Cooldown
- `ability.radius` — Effect radius
- `ability.particles` — Particle settings

## Messages: `messages.yml`

Use MiniMessage formatting. Supported placeholders:

- `{season}` — Current season color code
- `{season_name}` — Season display name
- `{bound}` — Bound color code
- `{bound_name}` — Bound display name
- `{player}` — Player name
- `{days}` — Days remaining
- `{seconds}` — Seconds

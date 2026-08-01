# Developer Guide

## Architecture

The project follows clean architecture and SOLID principles.

### Package Structure

- `core` — Plugin lifecycle, scheduler wrappers
- `season` — Season enum, manager
- `bound` — Bound types, manager, per-bound handlers
- `sword` — Sword manager, build logic, combat listener
- `effect` — Particle and sound services
- `gui` — UI manager and message service
- `command` — Commands and tab completers
- `listener` — Event listeners
- `data` — Persistence (DataStorage, services)
- `config` — YAML config management
- `util` — Shared utilities

### Adding a New Bound

1. Add enum value in `BoundType`
2. Create handler in `bound/<name>/<Name>BoundHandler.java`
3. Register handler in `BoundManager`
4. Add config entries in `bounds.yml` and `swords.yml`

### Adding a New Command

1. Create executor class in `command/`
2. Create tab completer if needed
3. Register in `SeasonalSMP.registerCommands()`

### Adding a New Listener

1. Create listener class in `listener/`
2. Register in `SeasonalSMP.registerListeners()`

## Conventions

- No static state abuse
- Small focused classes
- Dependency injection via constructor
- Avoid God classes
- Use services, not static helpers
- Prefer Paper API over NMS

## Build

```bash
mvn clean package
```

## Testing

Currently manual testing on a Paper 1.21.11 server.

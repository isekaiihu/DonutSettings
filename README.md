# DonutSettings

A fully configurable player settings menu plugin for Paper Minecraft servers. Players open a GUI to toggle personal preferences (chat, TPA, anti-pickup, and more), with every button, sound, particle, and message defined in `config.yml`.

**Author:** ISekai

## Features
- GUI-based settings menu (`/settings`) with a fully configurable layout, materials, names, lore, and slots
- Toggleable player settings: chat visibility, TPA requests, TPAuto, anti-pickup mode, hide players, glow, fast crystals, and do-not-disturb
- Support for arbitrary custom on/off settings defined entirely in config, with no code changes required
- Per-button sound effects and particle effects for both the "on" and "off" states
- Customizable message destinations per toggle (chat, action bar, title/subtitle, or disabled)
- Toggle cooldown to prevent spam-clicking settings
- Per-player settings persisted to disk and loaded/saved automatically on join, quit, and toggle
- Anti-pickup enforcement that cancels item pickups for players who enable it
- Chat visibility toggle that hides chat messages from players who disabled it
- Admin config reload command with success/failure feedback

## Commands
| Command | Description |
|---|---|
| `/settings` (aliases: `setting`, `preferences`, `prefs`) | Open the settings menu |
| `/settingsreload` (aliases: `sreload`, `settingsrl`) | Reload the plugin configuration |
| `/antipickup [on\|off]` (aliases: `ap`, `nopickup`) | Toggle or set anti-pickup mode |

## Permissions
| Permission | Default | Description |
|---|---|---|
| `donutsettings.use` | true | Access to settings menu and commands |
| `donutsettings.admin` | op | Admin permissions for reload |

## Dependencies
- [Paper API](https://papermc.io/) 1.21.4-R0.1-SNAPSHOT (provided)
- Java 21

## Installation
1. Download the jar from the Releases page of this repository.
2. Drop it into your server's `plugins/` folder.
3. Restart or reload the server.

## Building from source
For Maven (DonutSettings):
```bash
mvn clean package
```
Compiled jar lands in `build/libs/` (Gradle) or `target/` (Maven).

## License
See [LICENSE](LICENSE). All rights reserved — see terms above.

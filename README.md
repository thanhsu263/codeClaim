<div align="center">

# CodeClaim

A server-side Fabric mod for Minecraft 1.21.1 that allows players to redeem codes for rewards.
No client mod required. No database required.

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-62b47a?style=flat-square)](https://minecraft.net)
[![Fabric](https://img.shields.io/badge/Fabric-Loader-dbb37d?style=flat-square)](https://fabricmc.net)
[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square)](https://adoptium.net)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)
[![Server Side](https://img.shields.io/badge/Side-Server%20Only-informational?style=flat-square)](https://fabricmc.net)

</div>

---

## Overview

CodeClaim lets server administrators create redeemable codes that reward players with any server command — give items, Pokemon, broadcast titles, and more. Each player can only redeem each code once, and each code can have a global usage limit. All data is stored in simple JSON files with no external database setup required.
<img width="1020" height="603" alt="image" src="https://github.com/user-attachments/assets/6a7e9cdf-f1b8-49c1-ace5-a569e64d0853" />


---

## Features

- Any player can use `/claim` without operator permissions
- Each player can only redeem each code once, enforced by UUID
- Per-code global usage limit via `max_uses`
- Set `max_uses: -1` for unlimited redemptions
- Execute any server command as a reward using `%player%` as a placeholder
- All data auto-saves to a local JSON file
- Hot-reload config and data without restarting the server
- Server-side only — players do not need to install anything

---

## Requirements

- Minecraft 1.21.1
- Fabric Loader 0.15.0 or later
- Fabric API 0.102.0+1.21.1
- Java 21

---

## Installation

1. Download `codeclaim-1.0.0.jar` from the [Releases](../../releases) page
2. Download all required dependencies:
Fabric Language Kotlin
Fabric Permissions API
3. Place the file in your server's `mods/` folder alongside Fabric API
4. Start the server — the config file will be automatically generated at `config/codeclaim.json`
5. Edit the config to define your codes and rewards
6. Use `/claimadmin reload` to apply any changes without restarting

---

## Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/claim <code>` | All players | Redeem a code and receive its reward |
| `/claimadmin reload` | OP level 2 | Reload config and player data from disk |
| `/claimadmin info <code>` | OP level 2 | View usage statistics for a code |

---

## Configuration

Config file location: `config/codeclaim.json`

```json
{
  "messages": {
    "invalid_code":    "&cCode '&4{code}&c' does not exist!",
    "already_claimed": "&cYou have already redeemed '&4{code}&c'!",
    "max_uses":        "&cCode '&4{code}&c' has reached its usage limit!",
    "success":         "&aSuccessfully redeemed code '&2{code}&a'!",
    "reloaded":        "&aCodeClaim config reloaded!"
  },
  "codes": {
    "WELCOME2024": {
      "max_uses": -1,
      "commands": [
        "give %player% minecraft:bread 16"
      ]
    },
    "SHINY_EVENT": {
      "max_uses": 100,
      "commands": [
        "givepokemonother %player% moltres form=galarian",
        "title @a title {\"text\":\"A player claimed SHINY_EVENT!\",\"color\":\"gold\"}"
      ]
    },
    "GRAND_PRIZE": {
      "max_uses": 1,
      "commands": [
        "give %player% minecraft:netherite_ingot 3",
        "say %player% claimed the Grand Prize!"
      ]
    }
  }
}
```

### Field Reference

| Field | Description |
|-------|-------------|
| `max_uses` | Maximum total redemptions across all players. `-1` means unlimited. |
| `commands` | List of server commands to execute when the code is successfully redeemed. |
| `%player%` | Placeholder replaced with the redeeming player's username at runtime. |
| `{code}` | Placeholder replaced with the entered code in message strings. |
| `&a`, `&c` | Standard Minecraft color codes supported in all message fields. |

---

## Data Storage

Player redemption history is automatically saved to:

```
data/codeclaim_data.json
```

This file tracks which players have redeemed which codes, and the total usage count per code. Back up this file regularly to preserve redemption history across server migrations.

---

## Building from Source

Requirements: JDK 21, Gradle 8.8 or later

```bash
git clone https://github.com/YOUR_USERNAME/codeclaim
cd codeclaim

# Linux / macOS
./gradlew build

# Windows
gradlew.bat build
```

The compiled jar will be output to `build/libs/codeclaim-1.0.0.jar`.

---

## Compatibility

| Mod | Status |
|-----|--------|
| Cobblemon | Compatible — use `givepokemonother %player%` |
| LuckPerms | Compatible |
| Any server-side command mod | Compatible |

---

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

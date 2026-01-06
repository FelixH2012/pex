# Permissions Plugin - Cheatsheet

## Commands & Permissions

| Command | Permission | Description |
|---------|------------|-------------|
| `/pex user <player> group set <group> [duration]` | `pex.user.setgroup` | Set player's group (optional: temporary) |
| `/pex group create <name> <prefix> [priority]` | `pex.group.create` | Create a new group |
| `/pex group delete <name>` | `pex.group.delete` | Delete a group |
| `/pex group setpriority <name> <priority>` | `pex.group.setpriority` | Set group priority |
| `/pex group addperm <name> <permission>` | `pex.group.addperm` | Add permission to group |
| `/pex group removeperm <name> <permission>` | `pex.group.removeperm` | Remove permission from group |
| `/pex group info <name>` | `pex.group.info` | Show group details |
| `/pex group list` | `pex.group.list` | List all groups |

## Wildcards

| Permission | Grants |
|------------|--------|
| `pex.group.*` | All group commands |
| `pex.user.*` | All user commands |
| `pex.*` | All pex commands |
| `*` | Everything |

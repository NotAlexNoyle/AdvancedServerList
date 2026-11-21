---
title: /asl cache remove
---

# `/asl cache remove <uuid>`

## Permissions

Any of the following permissions will work:

- `advancedserverlist.admin`
- `advancedserverlist.command.cache`
- `advancedserverlist.command.cache.remove`

## Arguments

| Argument | Description                          |
|----------|--------------------------------------|
| `<uuid>` | UUID of the cached player to remove. |

## Description

Removes a cached player from the Cache based on the provided UUID.  
For database-based caches will this remove the player from the database too, where file-based storage only updates on plugin disabling.
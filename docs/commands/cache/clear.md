---
title: /asl cache clear
---

# `/asl cache clear [type]`

## Permissions

Any of the following permissions will work:

- `advancedserverlist.admin`
- `advancedserverlist.command.cache`
- `advancedserverlist.command.cache.clear`

/// warning | Deprecated permission
`advancedserverlist.command.clearcache` has been deprecated.  
It is still supported for backwards compatability, but may be removed in a future release.
///

## Arguments

| Argument | Description                                                        |
|----------|--------------------------------------------------------------------|
| `[type]` | Optional cache type to clear. Can be `ALL`, `FAVICON` or `PLAYER`. |

## Description

Clears the Favicon cache, Player cache or both, depending on the specified `[type]`.  
When no type is specified will `ALL` be used.

/// note
Clearing the Favicon cache will have the plugin refresh all local favicon files.
///
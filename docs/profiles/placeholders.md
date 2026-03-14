---
icon: material/percent
---

# Placeholders

AdvancedServerList provides a set of pre-made placeholders using the `${<identifier> <placeholder>}` format adobted from BungeeTabListPlus.  
It also has built-in [PlaceholderAPI support](#placeholderapi) for Spigot and Paper Servers and since v2 can even allow you to [add your own placeholders](#adding-your-own-placeholders) by using its [API](../api/index.md).

## Built-in Placeholders

The following placeholders are available in AdvancedServerList itself. Please note that not all placeholders are available on all platforms.

### Player

These placeholders use the player who pinged the server, to return values. They may require the player to be cached in order to work.

| Placeholder                 | Description                                                     | Platforms              | Cached Player required?^[1](#player-n1)^ |
|-----------------------------|-----------------------------------------------------------------|------------------------|------------------------------------------|
| `${player name}`            | The name of the player.                                         | <!-- icon:all -->      | Yes^[2](#player-n2)^                     |
| `${player protocol}`        | The protocol version of the player.                             | <!-- icon:all -->      | No                                       |
| `${player uuid}`            | The UUID of the player.                                         | <!-- icon:all -->      | Yes^[3](#player-n3)^                     |
| `${player version}`         | The protocol version of the player as readable MC version.      | <!-- icon:velocity --> | No                                       |
| `${player hasPlayedBefore}` | Boolean for whether the player has played on the server before. | <!-- icon:paper -->    | Yes                                      |
| `${player isBanned}`        | Boolean for whether the player has been banned from the server. | <!-- icon:paper -->    | Yes                                      |
| `${player isWhitelisted}`   | Boolean for whether the player is whitelisted on the server.    | <!-- icon:paper -->    | Yes                                      |

<small>^1^{ #player-n1 } When `Yes` is set, requires the player to have joined before while AdvancedServerList was running. Placeholder will not work when `disableCache` is true.</small>

<small>^2^{ #player-n2 } Will default to the value provided in `unknownPlayer -> name` of the config.yml, should the player not be cached yet.</small>

<small>^3^{ #player-n3 } Will default to the value provided in `unknownPlayer -> uuid` of the config.yml, should the player not be cached yet.</small>

### Server

These placeholders use values given by the server/proxy AdvancedServerList runs on.

| Placeholder                  | Description                                                             | Platforms           |
|------------------------------|-------------------------------------------------------------------------|---------------------|
| `${server playersOnline}`    | The number of players online on this proxy/server.^[1](#server-n1)^     | <!-- icon:all -->   |
| `${server playersMax}`       | The total number of players that can join this server.^[2](#server-n2)^ | <!-- icon:all -->   |
| `${server host}`             | The domain/IP the player pinged.^[3](#server-n3)^                       | <!-- icon:all -->   |
| `${server whitelistEnabled}` | Whether the whitelist is enabled or not.                                | <!-- icon:paper --> |

<small>^1^{ #server-n1 } You can provide a comma-separated list of world or server names (based on platform) to return the collective count of players on these worlds/servers.  
Additionally, when using [`playerCount -> onlinePlayers`](options/playercount/onlineplayers.md) will this placeholder return the modified online players count, except when used within the [`condition`](options/condition.md) option.</small>

<small>^2^{ #server-n2 } When using either [`playerCount -> extraPlayers`](options/playercount/extraplayers.md) or [`playerCount -> maxPlayers`](options/playercount/maxplayers.md) will this placeholder return the modified max players count, except when used within the [`condition`](options/condition.md) option.</small>

<small>^3^{ #server-n3 } An optional server name may be provided to display the IP/Domain associated with said server. This only works for the BungeeCord and Velocity version of the plugin.</small>

### Proxy

These placeholders are only available on the BungeeCord and Velocity versions of AdvancedServerList.

/// note
Servers are pinged every 10 seconds by the plugin and cached for that time period.
///

| Placeholders                 | Description                                                       | Default^[1](#proxy-n1)^ |
|------------------------------|-------------------------------------------------------------------|-------------------------|
| `${proxy status <name>}`     | Returns `online` or `offline` based on the Server's availability. | `offline`               |
| `${proxy motd <name>}`       | Returns the Server's MOTD as MiniMessage String.                  | `none`                  |
| `${proxy players <name>}`    | Returns the number of online Players on the Server.               | `0`                     |
| `${proxy maxPlayers <name>}` | Returns the max number of Players allowed to join the Server.     | `0`                     |

<small>^1^{ #proxy-n1 } The default value returned, should the Server or parts of it not be available.</small>

### Maintenance

These placeholders require the [Maintenance](https://hangar.papermc.io/kennytv/Maintenance){ target="_blank" rel="nofollow" } plugin to work.

| Placeholder                         | Description                                                    |
|-------------------------------------|----------------------------------------------------------------|
| `${maintenance maintenanceEnabled}` | Returns whether the Global maintenance mode is enabled or not. |

### Date

These placeholders can be used to get the current date or compare it with another.

| Placeholder               | Description                                                                                                               |
|---------------------------|---------------------------------------------------------------------------------------------------------------------------|
| `${date current}`         | Returns the current date.                                                                                                 |
| `${date isBefore <date>}` | Returns, whether the current date is before the specified one. The specified date needs to match the date pattern used.   |
| `${date isExact <date>}`  | Returns, whether the current date is equal to the specified one. The specified date needs to match the date pattern used. |
| `${date isAfter <date>}`  | Returns, whether the current date is after the specified one. The specified date needs to match the date pattern used.    |

/// note
Each placeholder accepts an optional date pattern that follows Java's SimpleDateFormat structure.  
If none is provided does each default to `dd/MM/yyyy`
///

## Adding your own Placeholders

Thanks to the API of AdvancedServerList are you able to add your own Placeholders that can be used using the `${identifier value}` pattern.  
To learn how to do this, head over to the [API page](../api/index.md) to learn more.


## PlaceholderAPI

### Own Placeholders

The Paper version of AdvancedServerList provides the following placeholders to use through PlaceholderAPI.

/// info
AdvancedServerList uses the same logic to determine the value to return for a player as it does when selecting a Server List Profile.
///

| Placeholder                          | Description                                                                       | Default^[1](#papi-n1)^ |
|--------------------------------------|-----------------------------------------------------------------------------------|------------------------|
| `%asl_favicon%`                      | Value of `favicon` when present.                                                  | `(empty string)`       |
| `%asl_motd%`                         | Value of `motd` when present.                                                     | `(empty string)`       |
| `%asl_playercount_extraplayers%`     | Value of `playerCount -> extraPlayers -> amount`.                                 | `(empty string)`       |
| `%asl_playercount_hideplayers%`      | Returns wether `playerCount -> hidePlayers` is enabled or not.                    | `false`                |
| `%asl_playercount_hideplayershover%` | Returns wether `playerCount -> hidePlayersHover` is enabled or not.               | `false`                |
| `%asl_playercount_hover%`            | Value of `playerCount -> hover`.                                                  | `(empty string)`       |
| `%asl_playercount_maxplayers%`       | Value of `playerCount -> maxPlayers -> amount`                                    | `(empty string)`       |
| `%asl_playercount_onlineplayers%`    | Value of `playerCount -> onlinePlayers -> amount`                                 | `(empty string)`       |
| `%asl_playercount_text%`             | Value of `playerCount -> text`                                                    | `(empty string)`       |
| `%asl_playersmax%`                   | Returns the max players based on currently set maxPlayers or extraPlayers values. | `(empty string)`       |

<small>^1^{ #papi-n1 } The default value is returned when the option in the chosen profile is either not set, or `null`.</small>

### Other Placeholders

Placeholders from PlaceholderAPI Expansions can be used in AdvancedServerList using the default `%identifier_value%` format.

PlaceholderAPI placeholders can be used both on the Server (Paper) and on the proxy (BungeeCord, Velocity) version of AdvancedServerList.  
However, the Proxy version requires the usage of [PAPIProxyBridge] to function properly.

Placeholders can also not be used within the [`condition`](options/condition.md) option of a Server List Profile.

## MiniPlaceholders

/// info | New since `v5.7.0`
///

Placeholders provided through [MiniPlaceholders](https://modrinth.com/plugin/miniplaceholders) are supported for all text options except [`condition`](options/condition.md).

Note that due to various limitations, only the `global` placeholders/tags - meaning ones that do not require a player - will work.  
Support also only works for Paper and Velocity.
---
icon: octicons/law-24
---

# Disclaimer

This plugin may collect and store certain player-related information, depending on the configuration chosen by the server owner using this plugin.

## Stored Information

The plugin may store a player's IP, UUID and name upon them joining the server or network.  
The name and UUID may be updated if a player with the same IP but different UUID and/or name joins.

## Storage location

The location of where the information is stored depends on the configuration set by the server owner, and may be one of the following:

- A local JSON file within the plugin's data folder (Default).
- A local or remote SQL-based Database.

## Why this info is stored

Due to limitations in Minecraft is it impossible to obtain a player's UUID and/or name during the time when they ping the server/network on the multiplayer screen.  
This limits how AdvancedServerList can provide customization for server owners.

The only information available, which can be used to recognize a player, is their IP, which the plugin uses to retrieve the previously stored UUID and Name, to then use.

## How this info is used

The stored IP is only used to retrieve the last associated name and UUID for a player.  
Said Name and UUID are then used further for resolving plugins (Own and ones from PlaceholderAPI/other plugins).

/// note
Neither PlaceholderAPI nor any other plugin hooking into AdvancedServerList receive the IP.  
It is only used by this plugin for resolving UUID and name of a player.
///

## Disabling storage

A server owner may decide to disable the caching mechanic, by enabling `disableCache` inside the `config.yml`.  
Doing so will disable any attempts of storing the previously mentioned data.  
However, already stored data prior to disabling the caching will not be deleted and is the responsibility of the server owner.

## Limitations/Issues

Due to the plugin using the IP to identify a player, can it happen that players sharing the same IP - i.e. as part of a Carrier-grade NAT (CGNAT) - may see name and UUID of another player who joined previously.  
This is an unfortunate side-effect that cannot be avoided, due to the lack of alternative ways to identify a player.

## Important Notice

By using this plugin, Server owners are responsible for storing the data, if enabled, in a proper way that follows applicable privacy laws and regulations.  
They are also responsible for properly informing players regarding the storage of the aforementioned data. The plugin adds a Link to this page to the Server/Network's "Server Links" for convenience.
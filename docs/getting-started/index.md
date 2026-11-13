---
icon: octicons/milestone-24
---

# Getting Started

This page will explain how to set up AdvancedServerList and make it work for your server.  
The guide is designed to be as easy to understand as possible. If you still have problems or questions, join the [M.O.S.S Discord Server][discord]{ target="_blank" rel="nofollow" } for more help.

## 1. Download the plugin { #download-the-plugin }

The first step would be to download the plugin from a download page of your choice.  
AdvancedServerList is available on the following places (Please note the current status!):

/// html | div.grid.cards
-   [:simple-modrinth: **Modrinth**][modrinth]{ target="_blank" rel="nofollow" }
    
    ----
    
    :octicons-check-24:{ style="color: var(--md-badge-fg--success);" } **Actively updated!**

-   [:simple-codeberg: **Codeberg**][codeberg]{ target="_blank" rel="nofollow" }
    
    ----
    
    :octicons-check-24:{ style="color: var(--md-badge-fg--success);" } **Actively updated!**

-   [:fontawesome-solid-paper-plane: **HangarMC**][hangar]{ target="_blank" rel="nofollow" }
    
    ----
    
    :octicons-check-24:{ style="color: var(--md-badge-fg--success);" } **Actively updated!**


-   [:simple-spigotmc: **SpigotMC**][spigot]{ target="_blank" rel="nofollow" }
    
    ----
    
    :octicons-alert-24:{ style="color: var(--md-badge-fg--warning);" } **Discontinued!**
///

[spigot]: https://www.spigotmc.org/resources/102910/

The plugin is available for Paper, BungeeCord, Waterfall and Velocity, and has been tested on these platforms. It may support additional forks, but this is not guaranteed.

The plugin also supports these additional plugins. They are all optional.

/// html | div.grid.cards
-   [**PlaceholderAPI**][placeholderapi]{ target="_blank" rel="nofollow" }
    
    ----
    
    AdvancedServerList supports the use of placeholder from PlaceholderAPI in all its text options, except for the `condition` option.
    
    ----
    
    **Supported on:**  
    <!-- icon:paper -->

-   [**ViaVersion**][viaversion]{ target="_blank" rel="nofollow" }
    
    ----
    
    When installed will AdvancedServerList use ViaVersion to resolve the protocol version of a player for its PlaceholderAPI placeholders.
    
    ----

    **Supported on:**  
    <!-- icon:paper -->

-   [**PAPIProxyBridge**][papiproxybridge]{ target="_blank" rel="nofollow" }
    
    ----
    
    Allows AdvancedServerList to parse placeholders from PlaceholderAPI while being used on a proxy.
    
    ----

    **Supported on:**  
    <!-- icon:proxies -->

-   [**Maintenance**][maintenance]{ target="_blank" rel="nofollow" }
    
    ----

    AdvancedServerList will disable any modification of the server list entry should Maintenance mode be enabled, unless you change this in the config.yml file.
    
    ----

    **Supported on:**  
    <!-- icon:all -->

-   [**BanPlugins Addon**][banplugins-addon]
    
    ----
    
    Official Add-on for AdvancedServerList that adds placeholders for different Ban/Punishment plugins such as AdvancedBan and LibertyBans.
    
    ----

    **Supported on:**  
    <!-- icon:all -->

-   [**LuckPerms Addon**][luckperms-addon]
    
    ----

    Official Add-on for AdvancedServerList that adds placeholders for LuckPerms.

    ----

    **Supported on:**  
    <!-- icon:all -->
///

[banplugins-addon]: ../addons/first-party/banplugins.md
[luckperms-addon]: ../addons/first-party/luckperms.md

## 2. Installation { #installation }

Installing the plugin is as simple as moving the jar file into the `plugins` folder. Just make sure to download the right version of AdvancedServerList for the right platform.  
Here is a quick table showing the jar file name with the platforms it is made for:

| Jar file name                                                                       | Platforms             |
|-------------------------------------------------------------------------------------|-----------------------|
| `AdvancedServerList-Paper-{version}.jar`{ data-md-component="plugin-version" }      | Paper                 |
| `AdvancedServerList-Velocity-{version}.jar`{ data-md-component="plugin-version" }   | Velocity              |

After you added it to your plugins folder - alongside any other dependency you may want - (re)start your server or proxy to enable the plugin.  
If everything goes well should AdvancedServerList create a folder named `AdvancedServerList` (`advancedserverlist` on Velocity) with additional files and folders inside of it.

### Folder structure

When you install AdvancedServerList for the first time should it have created the following file structure inside the plugins folder of your Server or Proxy:
```
AdvancedServerList/ # advancedserverlist/ on Velocity
├── favicons/
├── profiles/
│   └── default.yml
└── config.yml
```
Let's cover the files and folders...

#### Favicons folder

This folder allows you to store PNG images that should be used as favicons by the plugin. A favicon from this folder can be used by referencing the name (Including the `.png` file extension) in the [`favicon` option of the server list profile](../profiles/options/favicon.md).

#### Profiles folder and default.yml { #profiles-folder-and-default-yml }

The profiles folder is home of the server list profile files which allow you to modify the server list entry of your server in a player's multiplayer screen.  
On first creation will the folder have a `default.yml` file. This file contains all the options available to use alongside comments to try and help you in using it properly.

/// details | default.yml content
    type: file

```yaml
#
# Set the priority of this profile.
# If there are multiple profiles (files) will they be sorted by priority, starting with highest.
#
# Read more: https://asl.andre601.ch/profiles/#priority
#
priority: 0

#
# Set condition(s) that should return true to display this profile.
# This allows you to create profiles to only show for specific scenarios (i.e. player is banned).
#
# 'AND'/'&&' and 'OR'/'||' can be used to chain together multiple conditions.
#
# Removing the condition option or setting it to an empty String will be treated as a true condition being returned.
#
# Read more: https://asl.andre601.ch/profiles/#condition
#
condition: ''

#
# Set one or multiple profiles.
#
# A profile can contain the same options as used in the file itself, except for priority and condition.
# If multiple entries are present, a random one will be selected. Any options set in the selected profile will override
# any existing option in the file.
#
# Example: The below profile configuration will randomly change the MOTD between two types, but keep the Hover text
#          the same.
#
# When not present or empty (profiles: []), no profiles will be used and global options from the file will be used instead.
#
# Read more: https://asl.andre601.ch/profiles/#profiles
#
profiles:
  - motd: ['<aqua>Line A', '<gold>Line B']
  - motd:
      - '<aqua>Line 1'
      - '<gold>Line 2'

#
# Set the "Message of the day" to display.
# This option supports RGB colors for 1.16+ Servers and clients. More advanced MiniMessage features such as <gradient>
# can be used for further styling the text.
#
# <center> can be put at the start of a line to have AdvancedServerList automatically center the text as good as
# possible.
#
# Only the first two lines will be considered and any additional one is ignored.
#
# When not present or empty (motd: []), no MOTD will be set.
#
# Read more: https://asl.andre601.ch/profiles/#motd
#
motd:
  - '<grey>First Line'
  - '<grey>Second Line'

#
# Set the Favicon (image on the left of a server entry) to display.
# This option supports three types of input:
#
#   1. Filename, including the .png extension, matching a filename in the favicons folder of the plugin.
#      - 'random' can be used to have AdvancedServerList select a random image from the favicons folder every time
#        the server is pinged.
#   3. URL pointing to an image.
#   4. Player name, UUID or a placeholder that returns either.
#      - https://mc-heads.net will be used to retrieve an image of the provided name/UUID.
#
# Any images above or below 64x64 pixels will be automatically resized to these dimensions.
#
# If not present or empty (favicon: ''), defaults to not modifying the favicon.
#
# Read more: https://asl.andre601.ch/profiles/#favicon
#
# "The favicon isn't showing!"
#   This is a side effect of using either URLs or player names/UUIDs for the favicon.
#   These types require AdvancedServerList to create a new favicon object from scratch. This action takes a significant
#   amount of time, which can cause the server to show with a huge ping.
#   To avoid this is this action performed asynchronously. This means that the plugin doesn't wait for the favicon to
#   be created before continuing, which can result in no favicon being available when it is done handling the ping.
#   This should only happen for the first ping of a player and subsequent pings should display the favicon, if the
#   pings are done within the configured cache time (See 'faviconCacheTime' in the config.yml).
#
favicon: ''

#
# Contains options for modifying the player count (Text displaying <online>/<max>).
#
# Read more: https://asl.andre601.ch/profiles/#playercount
#
playerCount:
  #
  # Set whether the Player count should be hidden.
  # When set to true, the player count will be displayed as '???' instead of the usual '<online>/<max>' and all other
  # playerCount options, except for extraPlayers and maxPlayers, will be ignored.
  #
  # If not present, defaults to false.
  #
  # Read more: https://asl.andre601.ch/profiles/#hideplayers
  #
  hidePlayers: false
  #
  # Set whether the List of players is shown when hovering over the player count.
  # Setting this to true will also make the plugin ignore the hover option below.
  #
  # If not present, defaults to false.
  #
  # Read more: https://asl.andre601.ch/profiles/#hideplayershover
  #
  hidePlayersHover: false
  #
  # Set the lines shown when the player hovers over the player count.
  # Unlike the MOTD option does this one only support basic color and formatting codes. Any RGB colors will be
  # down-sampled to the nearest matching color.
  #
  # If not present or empty (hover: []), defaults to not modifying the hover.
  #
  # Read more: https://asl.andre601.ch/profiles/#hover
  #
  hover:
    - '<aqua>Line 1'
    - '<yellow>Line 2'
    - '<green>Line 3'
  #
  # Set the text that should be displayed instead of the default '<online>/<max>' text.
  #
  # Using this option will show the server as "outdated" to the client (Have the ping icon crossed out).
  # This cannot be fixed and would require Mojang to change stuff.
  #
  # If not present or empty (text: ''), defaults to not modifying the text.
  #
  # Read more: https://asl.andre601.ch/profiles/#text
  #
  text: ''
  #
  # The extraPlayers feature allows to set a number that should be added to the current online players and then be
  # used as the "max" player count.
  #
  # Using this feature will modify the ${server playersMax} placeholder, except for usage in the condition option.
  #
  # Read more: https://asl.andre601.ch/profiles/#extraplayers
  #
  extraPlayers:
    #
    # Set whether the extraPlayers feature should be enabled or not.
    # When set to true, the max players count will be modified to be the number of online players with the 'amount'
    # value added to it.
    #
    # If not present, defaults to false.
    #
    # Read more: https://asl.andre601.ch/profiles/#extraplayers-enabled
    #
    enabled: false
    #
    # Set the number that should be added to the online player count to then use as the new max player count.
    # Placeholders resolving to numbers can be used here.
    #
    # Example: Setting this to 1 will result in ${server playersMax} displaying 11 while 10 players are online,
    #          9 when 8 are online and so on.
    #
    # If not present, defaults to 0.
    #
    # Read more: https://asl.andre601.ch/profiles/#extraplayers-amount
    #
    amount: 0
  #
  # The maxPlayers feature allows to set a number to use as the max player count to display.
  #
  # Using this feature will modify the ${server playersMax} placeholder, except for usage in the condition option.
  #
  # Read more: https://asl.andre601.ch/profiles/#maxplayers
  #
  maxPlayers:
    #
    # Set whether the maxPlayers feature should be enabled or not.
    # When set to true, the max player count will be set to the 'amount' value.
    #
    # If not present, defaults to false.
    #
    # Read more: https://asl.andre601.ch/profiles/#maxplayers-enabled
    #
    enabled: false
    #
    # Set the number that should be used as the new max players count.
    # Placeholders resolving to numbers can be used here.
    #
    # If not present, defaults to 0.
    #
    # Read more: https://asl.andre601.ch/profiles/#maxplayers-amount
    #
    amount: 0
  #
  # The onlinePlayers feature allows to set a number to use as the online player count to display.
  #
  # Read more: https://asl.andre601.ch/profiles/#onlineplayers
  #
  onlinePlayers:
    #
    # Set whether the onlinePlayers feature should be enabled or not.
    # When set to true, the max player count will be set to the 'amount' value.
    #
    # If not present, defaults to false.
    #
    # Read more: https://asl.andre601.ch/profiles/#onlineplayers-enabled
    #
    enabled: false
    #
    # Set the number that should be used as the new online players count.
    # Placeholders resolving to numbers can be used here.
    #
    # If not present, defaults to current online player count.
    #
    # Read more: https://asl.andre601.ch/profiles/#onlineplayers-amount
    #
    amount: 0
```
///

#### Config.yml

The config.yml file contains settings related to the plugin itself and some of its functionality.  
These functions include, but aren't limited to:

- Setting the default name and UUID used for an unknown (Not cached) player.
- Disabling/Enabling caching of players.
- Disabling/Enabling checking for updates.
- Disabling/Enabling debug mode.
- Setting how long favicons should be cached for.
- Disabling/Enabling features of the plugin while the plugin Maintenance is active.
- The config version used for migration. Don't touch this, or it may reset your config.

Similar to the default.yml is this file containing comments to try and explain the settings in questions.  
Note that the config can get migrated to newer versions, adding missing options in the process. However, comments will get lost during that process.

/// details | default config.yml content
    type: file

```yaml
#
# Used for when AdvancedServerList doesn't have a cached player to use for player placeholders.
#
unknownPlayer:
  #
  # The name to use.
  #
  # Default: Anonymous
  #
  name: "Anonymous"
  #
  # The UUID to use.
  #
  # Default: 606e2ff0-ed77-4842-9d6c-e1d3321c7838 (UUID of MHF_Question).
  #
  uuid: "606e2ff0-ed77-4842-9d6c-e1d3321c7838"

#
# Should the Caching of players be disabled?
#
# When set to true will no player joining the server be cached and saved to the playercache.json file.
# This will also cause placeholders using a cached player such as ${player name} to no longer work.
#
# Default: false
#
disableCache: false

#
# Should AdvancedServerList check for new versions?
#
# When set to true will AdvancedServerList look for a new update on startup and repeat that check
# every 12 hours after that.
# It uses Modrinth to check for updates.
#
# Default: true
#
checkUpdates: true

#
# Should debug mode be enabled?
#
# Additional messages prefixed with [DEBUG] will be printed in the console when this option is set
# to true.
# Enabling this option is only recommended for when you have problems, or have been told to enable
# it by the plugin developer, as it can otherwise spam your console.
#
# Default: false
#
debug: false

#
# Should the "ASL" ASCI art be displayed on plugin startup?
# Setting this to false will hide the banner.
#
# Default: true
#
printBanner: true

#
# Sets how long Favicons should be cached. Value is in minutes with the lowest possible value being 1.
#
# This option only affects dynamic favicons created from a placeholder or URL. Local favicons will
# not be affected.
#
# Default: 1
#
faviconCacheTime: 1

#
# Sets how AdvancedServerList should handle the size and positioning of images for favicons.
#
# Available options:
#
#   - resize: Resizes the image to 64x64 pixels if it isn't these dimensions already.
#   - center: Doesn't resize the image, but instead tries to center it as good as possible in a 64x64 area.
#   - none:   Doesn't resize the image and leaves it positioned at x 0 and y 0 (top-left) of the 64x64 area.
#
# Setting any not listed option or leaving this blank/removing it sets the option to resize.
#
faviconResizeStrategy: 'resize'

#
# Options to disable while the Maintenance Plugin is present and Maintenance Mode is active.
#
# Should an option be removed from this section will AdvancedServerList continue to set it, as if
# the Maintenance plugin isn't active.
#
disableDuringMaintenance:
  #
  # Whether MOTD changing should be disabled while Maintenance mode is active.
  #
  # Default: true
  #
  motd: true
  #
  # Whether favicon changing should be disabled while Maintenance mode is active.
  #
  # Default: true
  #
  favicon: true
  #
  # Whether player hiding should be disabled while Maintenance mode is active.
  #
  # Default: true
  #
  hidePlayers: true
  #
  # Whether hiding player list on hover should be disabled while Maintenance mode is active.
  #
  # Default: true
  #
  hidePlayersHover: true
  #
  # Whether player count text changing should be disabled while Maintenance mode is active.
  #
  # Default: true
  #
  playerCountText: true
  #
  # Whether player count hover changing should be disabled while Maintenance mode is active.
  #
  # Default: true
  #
  playerCountHover: true
  #
  # Whether the extra players feature should be disabled while Maintenance mode is active.
  #
  # Default: true
  #
  extraPlayers: true
  #
  # Whether the max players feature should be disabled while Maintenance mode is active.
  #
  # Default: true
  #
  maxPlayers: true
  #
  # Whether the online players feature should be disabled while Maintenance mode is active.
  #
  # Default: true
  #
  onlinePlayers: true

#
# DO NOT EDIT!
#
# This is used internally to determine if the config needs to be migrated.
# Changing or even removing this option could result in your config being broken.
#
configVersion: 10
```
///

#### Additional created files and folders

There are certain files and folders that get created when certain events happen:

- A `playercache.json` file will be created on plugin shutdown containing a collection of IPs, UUIDs and Player names for every player that joined the server while AdvancedServerList was running.  
    This file is used to identify a player through their last used IP, to replace placeholders such as `${player name}` or `${player uuid}` with their respective values using the cached data.  
    This file is not created, used, nor updated when `disableCache` is set to `true` in the config.yml
- A `backups` folder containing old config.yml files.  
    This folder is created when AdvancedServerList migrates your old config to a new version. This allows you to transfer settings over in case the migration didn't work properly.  
    Each backup will have the name pattern `config-<date>.yml` with `<date>` being the date and time of when this file has been created.

## 3. Creating your first profile { #creating-your-first-profile }

Creating your first profile is relatively simple.

It's best to open the `default.yml` file located inside `profiles` with the file editor of your choice ([:simple-vscodium: VSCodeium](https://vscodium.com/){ target="_blank" rel="nofollow" } or [:simple-notepadplusplus: Notepad++](https://notepad-plus-plus.org/){ target="_blank" rel="nofollow" } are recommended).  
This file should contain all available options that you can set and alter.

Edit the options to whatever you like. All text-based options, except [`condition`](../profiles/options/condition.md), support [MiniMessage formatting](../profiles/formatting.md).  
Hexadecimal (RGB) colors are supported, however, only the [`motd` option](../profiles/options/motd.md) may display them and only for MC 1.16+ clients. Any other option will have the color down-sampled to the nearest supported color code.

If you're unsure how a specific option should look like, head over to the [Profiles page](../profiles/index.md) for more information about the general structure.  
All you need to know is, that a bare-bones server list profile requires a valid [priority](../profiles/options/priority.md) and at least one of the other options (except condition) to be present.

To give an example is here a basic profile that only modifies the MOTD and nothing else:
```yaml title="default.yml"
priority: 0

motd:
  - 'Hello'
  - 'World!'
```

### Additional profiles

Additional profiles can be created to display different content under specific situations. To create a new file, either manually create a YAML file in the `profiles` folder, or use [`/asl profiles add {name}`](../commands/profiles/add.md) to have one created for you. In either case is it recommended to only use alphanummeric (`a-z` abd `0-9`) characters, dashes and underscores for the file name. You should also choose a name that fits the purpose of the file (i.e. for a profile that displays stuff when someone is banned, name it `banned.yml`).

In case you manually created a YAML file, make sure it contains the [`priority`](../profiles/options/priority.md) setting and one of the other settings, to have a valid profile.  
You should also add a [`condition`](../profiles/options/condition.md) to ensure that the profile is only loaded when needed. The [Expressions page](../profiles/expressions.md) covers what counts as valid expressions and conditions.  
Do note that the plugin goes through the profiles in order of priority, starting with the highest number first, picking the first one that returns a `condition` returning true.

/// info | Note about Condition
The Condition option, if not set or empty, will always return true.
///

Here is another example profile using conditions to show it when the player was banned on the server:
```yaml title="other_profile.yml"
priority: 1

condition: '${player isBanned}'

motd:
  - '<red>You got <bold>banned</bold> from this server.'
```

### Further examples

The [Examples page](../examples/index.md) contains more examples of different server list profiles for all kinds of situations.

## 4. Loading profiles { #loading-profiles }

Once you've set up your server list profile(s) is it time to load it/them.  
To do this, simply run [`/asl reload`](../commands/reload.md).

The plugin should then load any valid YAML file inside the `profiles` folder to then use.

And that's already it! You now have a working server list profile for your server.
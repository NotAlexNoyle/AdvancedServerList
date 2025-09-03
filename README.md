# AdvancedServerList

AdvancedServerList is a plugin designed to allow the largest possible customization for your server's appearance in a Player's multiplayer screen.  
The plugin offers extensive customization including changing the MOTD, player count text, player count hover and favicon of your server. All while also allowing you to create multiple profiles with priorities and conditions to set when a specific profile should be displayed.

## Downloads

<a href="https://modrinth.com/plugin/advancedserverlist" target="_blank">
  <img src="https://cdn.jsdelivr.net/gh/Andre601/devins-badges@13e0142/assets/compact/available/modrinth_vector.svg" height="48" alt="modrinth" title="Available on Modrinth">
</a>
<br>
<a href="https://hangar.papermc.io/Andre_601/AdvancedServerList" target="_blank">
  <img src="https://cdn.jsdelivr.net/gh/Andre601/devins-badges@13e0142/assets/compact/available/hangar_vector.svg" height="48" alt="hangar" title="Available on Hangar">
</a>

## Supported Platforms

<a href="https://papermc.io" target="_blank">
  <img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/supported/paper_vector.svg" height="48" alt="platform-paper" title="Tested on Paper">
</a>
<br>
<a href="https://www.spigotmc.org" target="_blank">
  <img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/supported/bungeecord_vector.svg" height="48" alt="platform-bungeecord" title="Tested on BungeeCord">
</a>
<br>
<a href="https://www.papermc.io" target="_blank">
  <img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/supported/waterfall_vector.svg" height="48" alt="platform-waterfall" title="Tested on Waterfall">
</a>
<br>
<a href="https://velocitypowered.com" target="_blank">
  <img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/supported/velocity_vector.svg" height="48" alt="platform-velocity" title="Tested on Velocity">
</a>

## Server List Profiles

A Server List Profile is a single YAML file located in the plugin's `profiles` folder. It contains settings that allow you to modify the server's appearance within a Player's multiplayer screen.

### Priority and Conditions

Each file has a priority and optional condition. The priority is used to determine what file AdvancedServerList should use first - starting from highest number to lowest - with conditions being used to set when the profile should be used.  
Once a profile has been found with a condition returning true - which is the default should there be no condition - will AdvancedServerList use said profile.

> *Read more about [Priority](https://asl.andre601.ch/profiles/options/priority) and [Conditions](https://asl.andre601.ch/profiles/options/condition)*

### Placeholders

AdvancedServerList supports two sets of placeholders. The first one being its own placeholders using the `${<type> <values>}` format adobted from BungeeTabListPlus and the second one being placeholders from PlaceholderAPI using its `%<identifier>_<values>%` placeholder format.

Note that internal placeholders can be used in all text-based options of a profile, while PlaceholderAPI placeholders can be used in all text-based options except the `condition` one.  
As a final note can plugins provide their own placeholders to use within AdvancedServerList by using its API.

> *Read more about [Placeholders](https://asl.andre601.ch/profiles/placeholders/)*

### Formatting

AdvancedServerList utilizes the MiniMessage formatting to handle colors and formatting of text. This option was chosen over legacy formatting - using `&` character - for being more clear and readable in its design, not to mention having an easier way for more complex features such as RGB gradients.

> *Read more about [Formatting](https://asl.andre601.ch/profiles/formatting/)*

## Dependencies

AdvancedServerList soft-depends on the following plugins, meaning their inclusion is not required.

| Plugin                                                                  | Supported Platforms                    |
|-------------------------------------------------------------------------|----------------------------------------|
| [PlaceholderAPI](https://modrinth.com/plugin/PlaceholderAPI)            | Paper                                  |
| [ViaVersion](https://hangar.papermc.io/ViaVersion/ViaVersion)           | Paper                                  |
| [PAPIProxyBridge](https://hangar.papermc.io/William278/PAPIProxyBridge) | BungeeCord, Waterfall, Velocity        |
| [Maintenance](https://hangar.papermc.io/kennytv/Maintenance)            | Paper, BungeeCord, Waterfall, Velocity |

## Statistics

The plugin sends statistics to [bStats](https://bstats.org) to show.  
You can disable the sending of statistics in the global bStats config located inside `/plugins/bstats/`.

You can find the bStats pages of the plugin for the following platforms:

- [Paper](https://bstats.org/plugin/bungeecord/AdvancedServerList/15584)
- [BungeeCord, Waterfall](https://bstats.org/plugin/bungeecord/AdvancedServerList/15585)
- [Velocity](https://bstats.org/plugin/velocity/AdvancedServerList/15587)

## Videos

Below can you find a collection of videos made for my plugin. If you made a video and it isn't shown here, let me know and I'll add it.

<details><summary>Click to show/hide Videos</summary>
<p>

English Video by KasaiSora  
[![video thumbnail](https://img.youtube.com/vi/9cU0uUWHOO0/0.jpg)](https://youtube.com/watch?v=9cU0uUWHOO0)

Spanish Video by Ajneb97  
[![video thumbnail](https://img.youtube.com/vi/rIbljm_4HVI/0.jpg)](https://youtube.com/watch?v=rIbljm_4HVI)

Indonesian Video by faajrikha  
[![video thumbnail](https://img.youtube.com/vi/-p938rOmqy4/0.jpg)](https://www.youtube.com/watch?v=-p938rOmqy4)

German Video by Finni Gamer  
[![video thumbnail](https://img.youtube.com/vi/1Kh3cPMDd0c/0.jpg)](https://www.youtube.com/watch?v=1Kh3cPMDd0c)

</p>
</details>

## Screenshots

<a href="https://modrinth.com/plugin/advancedserverlist/gallery" target="_blank">
  <img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/documentation/modrinth-gallery_vector.svg" height="48" alt="gallery" title="Check out the Gallery">
</a>

## Support

<a href="https://discord.gg/MyPTjpsgGH" target="_blank">
  <img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/social/discord-singular_vector.svg" height="48" alt="discord" title="Join the M.O.S.S. Discord Server">
</a>
<br>
<a href="https://app.revolt.chat/invite/74TpERXA" target="_blank">
  <img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/social/revolt-singular_vector.svg" height="48" alt="revolt" title="Join my Revolt Server">
</a>
<br>
<a href="https://swiss.social/@andre_601" target="_blank">
  <img src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/social/mastodon-singular_vector.svg" height="48" alt="revolt" title="Chat with me on Mastodon">
</a>
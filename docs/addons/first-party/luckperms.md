---
icon: octicons/checklist-24
---
# LuckPerms Addon

The LuckPerms addon provides placeholders to return values from LuckPerms for a specific player.  
This can be a players primary group, whether they have a specific group or whether they have a specific permission.

/// note
The Userdata retrieval is done asynchronously by LuckPerms.  
However, this addon forces the thread for this to be blocked, which can cause lag, if done on the main thread.

This was done due to possible downsides and issues with trying to keep it async and block-free.  
To mitigate possible lag is the retrieved user data cached for 1 minute before being discarded.
///

## Placeholders

**Supported Platforms:**

<!-- icon:all -->

| Placeholder                               | Description                                                                                                     |
|-------------------------------------------|-----------------------------------------------------------------------------------------------------------------|
| `${luckperms hasGroup <group>}`           | Returns whether the player has the specified group or not.                                                      |
| `${luckperms hasPermission <permission>}` | Returns whether the player has the specified permission.<br/>No permission or one set to `false` returns false. |
| `${luckperms primaryGroup}`               | Gives the name of the primary group the player has.                                                             |

/// details | Example
    type: example

```yaml title="is_admin.yml
priority: 1
condition: "${luckperms hasGroup admin}"

motd:
  - "<yellow>Welcome Admin!"
```
///
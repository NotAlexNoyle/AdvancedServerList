---
title: /asl profiles set
---

# `/asl profiles set <profile> <option> [value]`

## Permissions

Any of the following permissions will work:

- `advancedserverlist.admin`
- `advancedserverlist.command.profiles`
- `advancedserverlist.command.profiles.set`

## Arguments

| Argument    | Description                  |
|-------------|------------------------------|
| `<profile>` | Name of the profile to edit. |
| `<option>`  | The option to edit.          |
| `[value]`   | Optional value to set.       |

Updates the specified option in the profile to the provided value.  
Not providing any value will instead set it to an unset/empty state, depending on the kind of option.  
This requires [`/asl reload`](../reload.md) to be executed, to load the new profile.
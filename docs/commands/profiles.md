---
title: /asl profiles
---

# `/asl profiles add <profile>`<br>`/asl profiles copy <profile> <name>`<br>`/asl profiles edit <profile>`<br>`/asl profiles info <profile>`<br>`/asl profiles list`<br>`/asl profiles set <profile> <option> [value]`

/// info |
**Permissions:**

- `advancedserverlist.admin`
- `advancedserverlist.command.profiles`

**Subcommands:**

- [`add <profile>`](#add)
- [`copy <profile> <name>`](#copy)
- [`info <profile>`](#info)
- [`list`](#list)
- [`set <profile> <option> [value]`](#set)
///

Creates, copies, lists (info of) or sets values for profiles.

## `/asl profiles add <profile>` { #add }

/// info |
**Permissions:**

- `advancedserverlist.admin`
- `advancedserverlist.command.profiles`
- `advancedserverlist.command.profiles.add`

**Arguments:**

- `<profile>` - Name of the profile to create.
///

Creates a new profile using default values.  
[`/asl reload`](reload.md) needs to be executed to load the new profile.

## `/asl profiles copy <profile> <name>` { #copy }

/// info |
**Permissions:**

- `advancedserverlist.admin`
- `advancedserverlist.command.profiles`
- `advancedserverlist.command.profiles.copy`

**Arguments:**

- `<profile>` - Name of the profile to copy.
- `<name>` - Name of the copy to make.
///

Creates a copy from the provided profile.  
[`/asl reload`](reload.md) needs to be executed to load the new profile.

## `/asl profiles edit <profile>` { #edit }

/// info |
**Permissions:**

- `advancedserverlist.admin`
- `advancedserverlist.command.profiles`
- `advancedserverlist.command.profiles.edit`

**Arguments:**

- `<profile>` - Name of the profile to edit.
///

Opens a Dialog to edit the specified Profile.  
The first Dialog allows you to select either the primary settings or the individual `profiles` with each option allowing you to edit their values according to what they support.

/// warning
This Feature only works for `v5.7.0-b1` and newer of AdvancedServerList and only for Paper 1.21.7 Builds 17 and newer due to lack of API in older versions.
///

## `/asl profiles info <profile>` { #info }

/// info |
**Permissions:**

- `advancedserverlist.admin`
- `advancedserverlist.command.profiles`
- `advancedserverlist.command.profiles.info`

**Arguments:**

- `<profile>` - The profile to list info from.
///

<!-- admo:info Only executable by players. -->

Lists currently set values for the provided profile.  
Each option has hover text to show the value and a click action to receive the matching [`/asl profiles set ...`](#set) command.

## `/asl profiles list` { #list }

/// info |
**Permissions:**

- `advancedserverlist.admin`
- `advancedserverlist.command.profiles`
- `advancedserverlist.command.profiles.list`
///

Lists all currently loaded profiles.  
Each profile has a hover text displaying their priority, condition and whether they are considered valid.

## `/asl profiles set <profile> <option> [value]` { #set }

/// info |
**Permissions:**

- `advancedserverlist.admin`
- `advancedserverlist.command.profiles`
- `advancedserverlist.command.profiles.set`

**Arguments:**

- `<profile>` - The profile to modify.
- `<option>` - The option to modify.
- `[value]` - Optional value to set. Leave empty to reset the option.
///

Sets the config option `<option>` to the provided `[value]`. Leaving value empty results in the option being reset to an empty/unset state.  
[`/asl reload`](reload.md) needs to be executed to apply the changes.
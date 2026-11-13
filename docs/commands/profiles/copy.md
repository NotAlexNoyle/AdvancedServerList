---
title: /asl profiles copy
---

# `/asl profiles copy <profile> <name>`

## Permissions

Any of the following permissions will work:

- `advancedserverlist.admin`
- `advancedserverlist.command.profiles`
- `advancedserverlist.command.profiles.copy`

## Arguments

| Arguments   | Description                  |
|-------------|------------------------------|
| `<profile>` | Name of the profile to copy. |
| `<name>`    | Name of the copy to make.    |

## Description

Creates a copy of an already existing profile.  
This requires [`/asl reload`](../reload.md) to be executed, to load the new profile.
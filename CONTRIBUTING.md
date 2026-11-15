# Contributing to AdvancedServerList

*Any contributions made to this project fall under the [MIT](LICENSE) unless stated otherwise.*

## Code Changes

The project is accepting any code changes for bug fixes, feature implementations, etc. as long as the following rules are followed.

### No AI-generated/assisted code

This project has a strict "No AI Code" policy!  
Any additions and modifications done, or assisted by AI will not be accepted.

### New Features

Before attempting to contribute any new feature, please discuss this first on the M.O.S.S. Discord in the `asl-support` channel to see, if there is a general need for this.  
Features that are only added to serve your own needs, but have no use for other people, are generally not accepted and if possible should be made into their own addons for this plugin.

### Bug Fixes

Contributions to fix minor and major bugs found in the plugin are generally accepted.  
Please make sure to create an Issue first to properly report the bug, before making a Pull request, so that it can be properly tracked.

### New Platforms

If you want to add support for a new platform, please discuss this first on the M.O.S.S. Discord in the `#asl-support` channel.  
Please note that any Hybrid platform support (Mod + Plugin environments) won't be accepted.

## Project Structure

The project is split up into multiple modules.

### Core

The `core` module contains all the Code that is used across the individual platforms supported by the plugin.  
This includes classes for logging, Event handling and file handling.

### Addons

The `addons/` directory contains the different Addon modules of the plugin.  
If you want to add a new addon, you have to place it here.

Note that in order for Maven to properly recognize and read the module, you'll have to add `<relativePath>../../pom.xml</relativePath>` to its `pom.xml` file's parent settings.

### Platform

Each supported platform has its own module in the root of the project.  
These modules should only contain code that cannot be run by the core module and requires a translation from or to the platform-specific API.
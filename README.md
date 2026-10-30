# Singleplayer Lens

A Fabric client mod prototype for Minecraft Java 1.21.11. It uses the supplied images as visual inspiration for a translucent dark menu with pink accents.

## Features

- Right Shift opens the menu.
- **Storage ESP** outlines loaded chests, trapped chests, barrels, ender chests, and shulker boxes.
- **Suspicious Chunks** outlines loaded chunks containing at least three supported containers, or at least two containers including a shulker box.
- Both overlays run only when Minecraft has an integrated single-player server. The scan reads block entities already loaded by the local client world.

## Build

Install a JDK and Gradle 9.8.2, then run `gradle build` from this directory. Gradle must be able to reach Fabric Maven and Maven Central for Loom and Fabric API. The mod jar is written to `build/libs/`.

Install Fabric Loader and Fabric API for Minecraft 1.21.11, then place the built jar in the instance's `mods` folder.

This source package has not been compiled in the current workspace because its managed execution environment blocks Gradle's local daemon connection and cannot resolve dependencies from Fabric Maven.

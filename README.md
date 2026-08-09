# Buildup Menu Button

一个Minecraft 26.2客户端模组，将标题和暂停菜单恢复到老版本的经典风格按钮布局。

支持Fabric和NeoForge，同时适配Mod菜单布局和第三方紧凑按钮。

Minecraft 26.2 client mod that restores a classic-style button layout for the title and pause menus.

Supports Fabric and NeoForge. It also adapts Mod Menu layouts and the third-party compact buttons.

## Build

Use JDK 25, then run:

```powershell
.\gradlew.bat clean build
```

Release jars are written to `fabric/build/libs/` and `neoforge/build/libs/`.

## Configuration

On the first client launch, the mod creates `config/buildup_menu_button.properties`.
Both switches default to `true`; set either switch to `false` to keep that screen's vanilla layout.
With Mod Menu installed on Fabric, use this mod's **Configure** button to change both switches live; the change is saved immediately and the current target screen is updated without a restart.

```properties
title_screen_layout_optimization=true
pause_screen_layout_optimization=true
```

## License

MIT

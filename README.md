![Requires Fabric API](https://cdn.imb11.dev/requires_fabric_api.png) [![IMB11 Discord server](https://cdn.imb11.dev/mineblock%20badge_64h.png)](https://discord.imb11.dev/) [![Steve's Underwater Paradise - Steveplays' Discord server](https://cdn.imb11.dev/steve.png)](https://discord.gg/KbWxgGg)

# Fog

As it says on the tin - Fog is a mod that completely revamps how Minecraft handles fog, including its color, start, and end points. It creates a greater sense of depth and atmosphere in Minecraft by shifting the fog start forward, maintaining the same view distance while greatly improving the visuals of the game.

## Features

It should be noted that the majority of these features can be fully customized via the configuration screen or resource packs - if you're using Fabric, you will need Mod Menu to access the configuration screen.

### Cave Fog

As you go deeper underground, the fog gets thicker. This adds to the spooky and mysterious feel of caves, making exploration more intense and immersive. It helps create a sense of depth, making the underground environment feel more alive and engaging.

![Cave Fog comparison (left: Mod Disabled; right: Mod Enabled)](https://cdn.modrinth.com/data/WuGVWUF2/images/4c298cc1a03e59f9b9c1a5d587a4204cae504a39.png)

### Weather Fog

The fog adjusts with the weather, so during rain or snow, the fog will change accordingly. This adds to the overall atmosphere, making each weather condition feel distinct and visually appealing.

![Weather Fog comparison (left: Mod Disabled; right: Mod Enabled)](https://cdn.modrinth.com/data/WuGVWUF2/images/f1ccdd8276b7412e343c70bedec36fb7bd255c0d.png)

### Time-Based Colors

Fog becomes more colorful and vibrant during sunsets, sunrises, and nighttime. This feature enhances the beauty of these moments, providing stunning visuals and a more dynamic environment that changes with the time of day.

![Time-Based Haze comparison (left: Mod Disabled; right: Mod Enabled)](https://cdn.modrinth.com/data/WuGVWUF2/images/f52d639bef3213914c80284fa8b19c0bc03c342e.png)

### Biome Fog Colors

Fog changes color based on the biome you're in, giving each area a unique look and feel. This helps create a coherent visual experience as you move through different biomes. It supports modded biomes and is easy to customize using resource packs, allowing for a highly personalized game experience.

![Biome Fog Colors comparison (left: Mod Disabled; right: Mod Enabled)](https://cdn.modrinth.com/data/WuGVWUF2/images/8cd00399374a9495f8f7ee3188cc76767db61a0b.png)

### Sky Fixes

When flying with an elytra above the clouds, the fog gradually fades away, offering a clear view of the sky without any strange horizon lines.

Additionally, clouds maintain their natural look and are not affected by fog color, ensuring they always appear consistent and visually appealing.

![Sky Fixes comparison (left: Mod Disabled; right: Mod Enabled)](https://cdn.modrinth.com/data/WuGVWUF2/images/5eb2c55f853792271abb509ba853dbbad6e4fdf4.png)

## Dependencies

### Required

- [Fabric API](https://modrinth.com/mod/fabric-api)
- [YetAnotherConfigLib](https://modrinth.com/mod/yacl)

## Compatibility info

### Compatible mods

- [Mod Menu](https://modrinth.com/mod/modmenu): allows access to the configuration screen
- [Sodium](https://modrinth.com/mod/sodium): fully compatible, ensuring that your visuals remain consistent even with performance-enhancing mods
- Shaders that use Minecraft’s default fog settings: fully compatible
- [Distant Horizons](https://modrinth.com/mod/distanthorizons): planned for the future

### Incompatibilities

See the [issue tracker](https://github.com/IMB11/Fog/issues?q=is%3Aissue+is%3Aopen+sort%3Aupdated-desc+label%3Acompat)
for a list of incompatibilities.

## Download

Fog is available for Forge on 1.20.1, and Fabric and NeoForge for 1.20.1, 1.20.4, 1.20.6 and 1.21.  
You can download Fog from the following locations:

- [GitHub Releases](https://github.com/IMB11/Fog/releases)
- [Modrinth](https://modrinth.com/mod/fog)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fog)

## FAQ

- Q: Will you be backporting this mod to lower Minecraft versions?  
  > No - it takes too much time and effort to backport to older versions, and it's not worth the time investment as very little of the community is on those versions.


- Q: Will you be supporting Forge for 1.20.4+?
  > No, you should consider moving to the [NeoForge](https://neoforged.net) mod loader.


- Q: Do you support Quilt?
  > Unfortunately, we are unable to provide support for issues arising from attempting to use this mod with Quilt - you are welcome to try to use it **at your own risk.**


- Q: Does this mod work in multiplayer?  
  > Yes! This mod is fully multiplayer compatible - only clients need to have the mod installed, but if your friends have the mod installed, you should have similar visuals.


- Q: Does only the client need this mod or does the server need it too?  
  > This is a completely client side mod, so only the client needs to have it installed. The server does not need to have it installed.

### For Developers and Server Admins

If you want to add support for your modded biomes, you can utilize Fog's custom JSON fog definition format to create unique fog effects. The mod is fully customizable via resource packs and a dedicated configuration file.

Furthermore, if you're developing a Fabric mod, you can utilize Fog's custom datagen providers to speed your development workflow up when adding compatability with the mod.

Server owners can adjust fog start/end points and biome colors to match their server's aesthetic.

**For more information, check out Fog's [documentation](https://docs.imb11.dev/fog/).**

## License

This project is licensed under All Rights Reserved, see [LICENSE](https://github.com/IMB11/Fog/blob/master/LICENSE).

## Attribution

- [birsy](https://modrinth.com/user/birsy), the original author of [Fog Looks Good Now](https://modrinth.com/mod/fog-looks-good-now) - many of the systems and ideas were expanded from this mod.
- [Steveplays](https://modrinth.com/user/Steveplays), the author of the [Biome Fog](https://modrinth.com/mod/biomefog) mod - who collaborated with me to bring biome fog colors to Fog.

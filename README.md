# CoreTweaks

A Minecraft 1.7.10 coremod that contains various bug fixes, tweaks and optimizations (mainly to startup time).

## Features
* [VanillaFix](https://www.curseforge.com/minecraft/mc-mods/vanillafix)-like crash handling
* A class transformer cache that speeds up startup
* Many small fixes - check the [Config](https://github.com/makamys/CoreTweaks/wiki/Config) page on the wiki for the full list.

## Incompatibilities

* Since other crash handling mods (e.g. [BetterCrashes](https://github.com/vfyjxf/BetterCrashes), [CrashGuard](https://github.com/FalsePattern/CrashGuard)) overlap in functionality with CoreTweaks's `crashHandler`, it will be disabled if one of them is detected.
* Various coremods will cause a crash on startup due to an incompatibility with Mixin. Use [Mixingasm](https://github.com/makamys/Mixingasm) to fix this.

### About `nomixin` builds

The mod comes in two flavors:
* The regular version embeds Mixin 0.7.11, allowing the mod to run standalone. However, this makes the jar a bit larger, and can cause problems in certain use cases.
* The version marked with `+nomixin` doesn't embed Mixin, which lets it avoid these problems. But it requires a separate [Mixin bootstrap mod](https://gist.github.com/makamys/7cb74cd71d93a4332d2891db2624e17c#mixin-bootstrap-mods) to be installed in order to run. If you have one installed already, getting this version is recommended.

## Suggested mods
For more 1.7.10 bugfix/performance/debug mods, refer to [this list](https://gist.github.com/makamys/7cb74cd71d93a4332d2891db2624e17c).

## License

This mod is licensed under the [MIT License](https://github.com/makamys/CoreTweaks/blob/master/LICENSE).

It contains code based on minecraft-backport5160, a mod by Itaros which in turn was based on code from Forge and Paper contributors. See [CREDITS](CREDITS) for details.

## Contributing

When running in an IDE, add these program arguments
```
--tweakClass org.spongepowered.asm.launch.MixinTweaker --mixin coretweaks.mixin.json --mixin coretweaks-init.mixin.json --mixin coretweaks-preinit.mixin.json
```
and these VM arguments
```
-Dfml.coreMods.load=makamys.coretweaks.CoreTweaksPlugin
```

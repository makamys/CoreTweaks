# CoreTweaks

A Minecraft 1.7.10 coremod that contains various bug fixes, tweaks, optimizations (mainly to startup time) and performance diagnostics.

Most of the mod's features target the **client** side. It does not work on servers at all at the moment.

## Features
* [VanillaFix](https://www.curseforge.com/minecraft/mc-mods/vanillafix)-like crash handling
* A class transformer cache that speeds up startup (one that's safer than [FastStart](https://github.com/makamys/FastStart)'s - FastStart's version is also included as an alternative)
* A startup profiler that logs how long each part of startup took
* Many small fixes - check the [Config](https://github.com/makamys/CoreTweaks/wiki/Config) page on the wiki for the full list.

# Incompatibilities

* [FoamFix](https://github.com/asiekierka/FoamFix17): `jarDiscovererMemoryLeakFix` has to be disabled when CoreTweaks's `jarDiscovererCache` is enabled.
* Other crash handling mods (e.g. [BetterCrashes](https://github.com/vfyjxf/BetterCrashes), [CrashGuard](https://github.com/FalsePattern/CrashGuard)) will only work if CoreTweaks's `crashHandler` is disabled. Using them may be preferable in some cases, as they can catch some crashes CoreTweaks cannot. The opposite is also true, however.
* Various coremods will cause a crash on startup due to an incompatibility with Mixin. Use [Mixingasm](https://github.com/makamys/Mixingasm) to fix this.

# License

This mod is licensed under the [MIT License](https://github.com/makamys/CoreTweaks/blob/master/LICENSE).

# Contributing

When running in an IDE, add these program arguments
```
--tweakClass org.spongepowered.asm.launch.MixinTweaker --mixin coretweaks.mixin.json --mixin coretweaks-init.mixin.json --mixin coretweaks-preinit.mixin.json
```
and these VM arguments
```
-Dfml.coreMods.load=makamys.coretweaks.CoreTweaksPlugin
```

package makamys.coretweaks.optimization;

import static makamys.coretweaks.CoreTweaks.LOGGER;

import makamys.coretweaks.Config;
import makamys.coretweaks.util.PluralUtil;
import net.minecraft.launchwrapper.Launch;

public class LibraryTransformationExcluder {
    public static void run() {
        final int n = Config.excludeLibraryTransformationPackages.length;
        LOGGER.info("Adding transformer exclusions for " + n + " library package" + PluralUtil.pluralSuffix(n));
        for(String s : Config.excludeLibraryTransformationPackages) {
            LOGGER.debug("  Excluding " + s);
            Launch.classLoader.addTransformerExclusion(s);
        }
    }
}

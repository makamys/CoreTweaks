package makamys.toomanycrashes.mixin;

import java.io.File;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import cpw.mods.fml.common.discovery.ModDiscoverer;
import net.minecraft.network.play.INetHandlerPlayClient;

@Mixin(ModDiscoverer.class)
abstract class MixinModDiscoverer implements INetHandlerPlayClient {
    
	private File lastFile;
	
	@Redirect(method = "findClasspathMods", at = @At(value = "INVOKE", target = "Ljava/io/File;isFile()Z"), remap = false)
    public boolean redirectIsFile(File file) {
        lastFile = file;
        return file.isFile();
    }
	
    @Redirect(method = "findClasspathMods", at = @At(value = "INVOKE", target = "Ljava/util/List;contains(Ljava/lang/Object;)Z"), remap = false)
    public boolean redirectKnownLibrariesContains(List list, Object obj) {
    	assert lastFile.getName().equals(obj);
        return list.contains(obj) || isDefaultLibrary(lastFile);
    }
    
    /* From Forge 1.12.2-14.23.5.2847 */
    private static boolean isDefaultLibrary(File file)
    {
        String home = System.getProperty("java.home"); // Nullcheck just in case some JVM decides to be stupid
        if (home != null && file.getAbsolutePath().startsWith(home)) return true;
        // Should really pull this from the json somehow, but we dont have that at runtime.
        String name = file.getName();
        if (!name.endsWith(".jar")) return false;
        String[] prefixes =
        {
            "launchwrapper-",
            "asm-all-",
            "akka-actor_2.11-",
            "config-",
            "scala-",
            "jopt-simple-",
            "lzma-",
            "realms-",
            "httpclient-",
            "httpcore-",
            "vecmath-",
            "trove4j-",
            "icu4j-core-mojang-",
            "codecjorbis-",
            "codecwav-",
            "libraryjavawound-",
            "librarylwjglopenal-",
            "soundsystem-",
            "netty-all-",
            "guava-",
            "commons-lang3-",
            "commons-compress-",
            "commons-logging-",
            "commons-io-",
            "commons-codec-",
            "jinput-",
            "jutils-",
            "gson-",
            "authlib-",
            "log4j-api-",
            "log4j-core-",
            "lwjgl-",
            "lwjgl_util-",
            "twitch-",
            "jline-",
            "jna-",
            "platform-",
            "oshi-core-",
            "netty-",
            "libraryjavasound-",
            "fastutil-"
        };
        for (String s : prefixes)
        {
            if (name.startsWith(s)) return true;
        }
        return false;
    }
}
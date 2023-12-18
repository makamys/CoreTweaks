package makamys.coretweaks.util;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class DefaultLibraries {
    /* From Forge 1.12.2-14.23.5.2847 */
    public static boolean isDefaultLibrary(File file)
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
            "fastutil-",
            "lombok-"
        };
        for (String s : prefixes)
        {
            if (name.startsWith(s)) return true;
        }
        return false;
    }

    public static boolean isDefaultLibrary(URL url) {
        try {
            return isDefaultLibrary(new File(url.toURI()));
        } catch(URISyntaxException e) {
            return false;
        }
    }
}

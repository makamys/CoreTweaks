package makamys.coretweaks;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import makamys.coretweaks.optimization.FastDeobfuscationRemapper;
import makamys.coretweaks.optimization.ForgeFastWildcardTransformers;
import makamys.coretweaks.optimization.JarDiscovererCache;
import makamys.coretweaks.optimization.LibraryTransformationExcluder;
import makamys.coretweaks.optimization.ThreadedTextureLoader;
import makamys.coretweaks.optimization.transformercache.full.CachingTransformer;
import makamys.coretweaks.optimization.transformercache.lite.TransformerCache;
import net.minecraft.launchwrapper.Launch;

public class CoreTweaks {
    
    public static final String MODID = "coretweaks";
    public static final String VERSION = "@VERSION@";
    
    public static ThreadedTextureLoader textureLoader;
    
    public static CachingTransformer cachingTransformer;
    
    public static int boundTexture;
    public static boolean isStitching;
    
    public static final Logger LOGGER = LogManager.getLogger("coretweaks");
    
    public static final File MY_DIR = new File(Launch.minecraftHome, "coretweaks");
    public static final File OUT_DIR = new File(MY_DIR, "out");
    public static final File CACHE_DIR = new File(MY_DIR, "cache");
    
    public static void init(){
        excludeRepackagedLibraries();
        
        if(Config.excludeLibraryTransformation.isActive()) {
            LibraryTransformationExcluder.run();
        }
        if(Config.threadedTextureLoader.isActive()) {
                textureLoader = new ThreadedTextureLoader(
                    Config.threadedTextureLoaderThreadCount != 0 ? Config.threadedTextureLoaderThreadCount
                            : Runtime.getRuntime().availableProcessors());
        }
        
        if(Config.transformerCache.isActive() && Config.transformerCacheMode == Config.TransformerCache.FULL) {
            Persistence.loadIfNotLoadedAlready();
            cachingTransformer = CachingTransformer.register();
            Persistence.lastVersion = CoreTweaks.VERSION;
            Persistence.save();
        }
        if(Config.jarDiscovererCache.isActive()) {
            JarDiscovererCache.instance = new JarDiscovererCache();
        }
        if(FastDeobfuscationRemapper.isActive()) {
            FastDeobfuscationRemapper.init();
        }
        if(Config.forgeFastWildcardTransformers.isActive()) {
            ForgeFastWildcardTransformers.instance = new ForgeFastWildcardTransformers();
        }
        if(Config.transformerCache.isActive() && Config.transformerCacheMode == Config.TransformerCache.LITE) {
            TransformerCache.instance.init();
        }
    }

    private static void excludeRepackagedLibraries() {
        // Exclude transformation to reduce class load time
        // Note: the shadow plugin automatically relocates these strings in the built jar!
        Launch.classLoader.addTransformerExclusion("com.esotericsoftware.kryo.kryo5.");
        Launch.classLoader.addTransformerExclusion("net.sf.cglib.");
    }
}

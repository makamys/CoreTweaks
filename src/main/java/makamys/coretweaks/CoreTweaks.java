package makamys.coretweaks;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import makamys.coretweaks.diagnostics.FMLBarProfiler;
import makamys.coretweaks.optimization.FastDeobfuscationRemapper;
import makamys.coretweaks.optimization.ThreadedTextureLoader;
import makamys.coretweaks.optimization.transformercache.full.CachingTransformer;
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
        Persistence.loadIfNotLoadedAlready();
        
        if(Config.threadedTextureLoader) {
                textureLoader = new ThreadedTextureLoader(
                    Config.threadedTextureLoaderThreadCount != 0 ? Config.threadedTextureLoaderThreadCount
                            : Runtime.getRuntime().availableProcessors());
        }
        
        if(Config.transformerCache == Config.TransformerCache.FULL) {
            cachingTransformer = CachingTransformer.register();
        }
        
        if(FMLBarProfiler.isActive()) {
            FMLBarProfiler.instance().init();
        }
        
        if(FastDeobfuscationRemapper.isActive()) {
            FastDeobfuscationRemapper.init();
        }
        
        Persistence.lastVersion = CoreTweaks.VERSION;
        Persistence.save();
    }
}

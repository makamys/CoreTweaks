package makamys.coretweaks.optimization;

import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.util.ResourceLocation;

public class DefaultResourcePackHelper {

    private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("coretweaks.debugDefaultResourcePackMixin", "false"));
    
    private static List<URL> assetsDirUrls;
    
    public static InputStream getResourceStream(ResourceLocation p_110605_1_) {
        if(assetsDirUrls == null || DEBUG) {
            try {
                List<URL> urls = Collections.list(DefaultResourcePack.class.getClassLoader().getResources("assets"));
                if(assetsDirUrls == null) {
                    assetsDirUrls = urls;
                } else {
                    if(!assetsDirUrls.equals(urls)) {
                        LOGGER.error("Mismatch detected in DefaultResourcePack optimization! (old assetsDirUrls=" + assetsDirUrls + ", new assetsDirUrls=" + urls + ") Please report the issue and disable the option (`fastDefaultTexturePack`) for now.");
                        assetsDirUrls = urls;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        
        // Only look for asset in jars which have an assets directory...
        
        InputStream vanillaResult = null;
        if(DEBUG) {
            vanillaResult = DefaultResourcePack.class.getResourceAsStream("/assets/" + p_110605_1_.getResourceDomain() + "/" + p_110605_1_.getResourcePath());
        }
        
        InputStream result = getRelativeResourceAsStream(assetsDirUrls, "/" + p_110605_1_.getResourceDomain() + "/" + p_110605_1_.getResourcePath());
        
        if(DEBUG) {
            if((vanillaResult == null) != (result == null)) {
                LOGGER.error("Mismatch detected in DefaultResourcePack optimization! (resource=" + p_110605_1_ + ", vanillaResult=" + vanillaResult + ", result=" + result + ") Please report the issue and disable the option (`fastDefaultTexturePack`) for now.");
            }
        }
        
        return result;
    }

    private static InputStream getRelativeResourceAsStream(List<URL> baseUrls, String relPath) {
        for(URL url : baseUrls) {
            try {
                URL candidateUrl = new URL(url.toString() + relPath);
                try {
                    return candidateUrl.openStream();
                } catch(IOException e) {
                    // Exception handling is hella slow, but it's how OpenJDK does it, so...
                }
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

}

package makamys.coretweaks.optimization;

import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.resources.DefaultResourcePack;

/**
 * <p>If you frequently call {@link Class#getResourceAsStream(String)} with the same directory prefix, and only a small portion of the jars on the classpath contain this directory, using this class will speed up the operation.
 * <p>It will only search for the resource in the jars that contain the directory.
 * <p>However, if the classpath is modified after this class is instantiated, incorrect results may be returned. I sure hope that never happens.
 */

public class PrefixedClasspathResourceAccelerator {

    private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("coretweaks.debugPrefixedClasspathResourceAccelerator", "false"));
    
    private final String basePath;
    private List<URL> assetsDirUrls;
    
    public PrefixedClasspathResourceAccelerator(String basePath) {
        this.basePath = basePath;

        if(!basePath.startsWith("/")) {
            throw new IllegalArgumentException("basePath must start with /");
        }
        initAssetsDirUrls();
    }

    private void initAssetsDirUrls() {
        if(assetsDirUrls == null || DEBUG) {
            try {
                List<URL> urls = Collections.list(DefaultResourcePack.class.getClassLoader().getResources(basePath.substring(1)));
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
    }

    private InputStream getRelativeResourceAsStream(String relPath) {
        for(URL url : assetsDirUrls) {
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

    public InputStream getResourceAsStream(String path) {
        initAssetsDirUrls();
        
        // Only look for resource in jars which have the directory...
        
        if(!path.startsWith("/")) {
            throw new IllegalArgumentException("Argument must start with /");
        }
        
        InputStream vanillaResult = null;
        if(DEBUG) {
            vanillaResult = DefaultResourcePack.class.getResourceAsStream(path);
        }
        
        InputStream result = getRelativeResourceAsStream(path.substring(basePath.length()));
        
        if(DEBUG) {
            if((vanillaResult == null) != (result == null)) {
                LOGGER.error("Mismatch detected in DefaultResourcePack optimization! (path=" + path + ", vanillaResult=" + vanillaResult + ", result=" + result + ") Please report the issue and disable the option (`fastDefaultTexturePack`) for now.");
            }
        }
        
        return result;
    }

}

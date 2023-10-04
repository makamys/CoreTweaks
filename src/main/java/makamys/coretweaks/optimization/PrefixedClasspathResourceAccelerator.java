package makamys.coretweaks.optimization;

import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.resources.DefaultResourcePack;

/**
 * <p>If you frequently call {@link Class#getResourceAsStream(String)} with the same directory prefix, using this class will speed up the operation.
 * <p>It will only search for the resource in the jars that contain the directory.
 * <p>However, if the classpath is modified after this class is instantiated, incorrect results may be returned. I sure hope that never happens.
 */

public class PrefixedClasspathResourceAccelerator {

    private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("coretweaks.debugPrefixedClasspathResourceAccelerator", "false"));
    
    private Map<String, List<URL>> directoryOwners = new HashMap<>();

    private List<URL> findDirectoryOwners(String path) {
        List<URL> cached = directoryOwners.get(path);
        if(cached != null) {
            return cached;
        }
        
        List<URL> owners = new ArrayList<>();
        
        if(path.lastIndexOf('/') == 0) {
            try {
                owners = Collections.list(DefaultResourcePack.class.getClassLoader().getResources(path.substring(1)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            String parentPath = path.substring(0, path.lastIndexOf('/'));
            String basePath = path.substring(path.lastIndexOf('/'));
            List<URL> parentOwners = findDirectoryOwners(parentPath);
            for(URL url : parentOwners) {
                try {
                    URL candidateUrl = new URL(url.toString() + basePath);
                    try {
                        candidateUrl.openStream();
                        owners.add(candidateUrl);
                    } catch(IOException e) {
                        // Exception handling is hella slow, but it's how OpenJDK does it, so...
                    }
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        directoryOwners.put(path, owners);
        
        return owners;
    }

    private InputStream findResourceAsStream(String path) {
        String parentPath = path.substring(0, path.lastIndexOf('/'));
        String basePath = path.substring(path.lastIndexOf('/'));
        List<URL> directoryOwners = findDirectoryOwners(parentPath);
        for(URL url : directoryOwners) {
            try {
                URL candidateUrl = new URL(url.toString() + basePath);
                try {
                    InputStream is = candidateUrl.openStream();
                    return is;
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
        // Only look for resource in jars which have the directory...
        
        if(!path.startsWith("/")) {
            throw new IllegalArgumentException("Argument must start with /");
        }
        
        InputStream vanillaResult = null;
        if(DEBUG) {
            vanillaResult = DefaultResourcePack.class.getResourceAsStream(path);
        }
        
        InputStream result = findResourceAsStream(path);
        
        if(DEBUG) {
            if((vanillaResult == null) != (result == null)) {
                LOGGER.error("Mismatch detected in DefaultResourcePack optimization! (path=" + path + ", vanillaResult=" + vanillaResult + ", result=" + result + ") Please report the issue and disable the option (`fastDefaultTexturePack`) for now.");
            }
        }
        
        return result;
    }

}

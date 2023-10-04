package makamys.coretweaks.optimization;

import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * It's like {@link PrefixedClasspathResourceAccelerator} but for folder resource packs.
 */

public class PrefixedFolderResourceAccelerator {

    private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("coretweaks.debugPrefixedFolderResourceAccelerator", "false"));
    
    private Map<String, Boolean> directoryExists = new HashMap<>();

    private File home;
    
    public PrefixedFolderResourceAccelerator(File homeFolder) {
        this.home = homeFolder;
        
        directoryExists.put(homeFolder.getPath(), true);
    }
    
    private boolean directoryExists(File directory) {
        String path = directory.getPath();
        Boolean cached = directoryExists.get(path);
        if(cached != null) {
            return cached;
        }
        
        boolean exists = false;
        
        boolean parentExists = directoryExists(directory.getParentFile());
        if(parentExists) {
            exists = directory.isDirectory();
        }
        
        directoryExists.put(path, exists);
        
        return exists;
    }

    private boolean computeIsFile(File file) {
        if(directoryExists(file.getParentFile())) {
            return file.isFile();
        } else {
            return false;
        }
    }

    public boolean isFile(File file) {
        // Only look for resource if directory exists...
        
        if(!file.getPath().startsWith(home.getPath())) {
            throw new IllegalArgumentException("Argument must start with " + home.getPath());
        }
        
        boolean vanillaResult = false;
        if(DEBUG) {
            vanillaResult = file.isFile();
        }
        
        boolean result = computeIsFile(file);
        
        if(DEBUG) {
            if(vanillaResult != result) {
                LOGGER.error("Mismatch detected in FolderResourcePack optimization! (path=" + file.getPath() + ", vanillaResult=" + vanillaResult + ", result=" + result + ") Please report the issue and disable the option (`fast_folder_texture_pack`) for now.");
            }
        }
        
        return result;
    }

}

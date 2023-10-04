package makamys.coretweaks.optimization;

import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.io.File;
import java.util.Set;

public class FastFolderResourcePack {
    
    private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("coretweaks.debugFolderResourcePackMixin", "false"));

    public static void afterConstructor(File folder, Set<String> filePaths) {
        if(DEBUG) LOGGER.info("running after constructor, folder=" + folder);
        
        explore(folder, folder.getPath(), filePaths);
    }

    public static void explore(File folder, String path, Set<String> filePaths) {
        if(DEBUG) LOGGER.info("exploring folder=" + folder + " path=" + path);
        
        for(File f: folder.listFiles()) {
            String myPath = (path.isEmpty() ? "" : path + File.separator) + f.getName();
            filePaths.add(myPath);
            if(f.isDirectory()) {
                explore(f, myPath, filePaths);
            }
        }
    }

    public static boolean redirectIsFile(File file, Set<String> filePaths) {
        boolean result = filePaths.contains(file.getPath());
        
        if(DEBUG) LOGGER.info("isFile " + file + " ? " + result);
        
        return result;
    }

}

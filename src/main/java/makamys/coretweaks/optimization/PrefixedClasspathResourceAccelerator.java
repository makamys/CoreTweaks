package makamys.coretweaks.optimization;

import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import lombok.SneakyThrows;
import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.launchwrapper.Launch;

/**
 * <p>If you frequently call {@link Class#getResourceAsStream(String)} with the same directory prefix, using this class will speed up the operation.
 * <p>It will only search for the resource in the jars that contain the directory.
 * <p>However, if the classpath is modified after this class is instantiated, incorrect results may be returned. I sure hope that never happens.
 */

public class PrefixedClasspathResourceAccelerator {

    private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("coretweaks.debugPrefixedClasspathResourceAccelerator", "false"));
    
    private List<Index> classSources;
    
    private Map<String, List<Index>> directoryOwners = new HashMap<>();

    private void init() {
        long t0 = System.nanoTime();
        classSources = new ArrayList<>();
        for(URL url : Launch.classLoader.getSources()) {
            classSources.add(Index.fromURL(url));
        }
        long t1 = System.nanoTime();
        LOGGER.debug("Indexed classpath resources in " + ((t1 - t0)/1_000_000_000.0) + "s");
    }
    
    private List<Index> findDirectoryOwners(String path) {
        List<Index> cached = directoryOwners.get(path);
        if(cached != null) {
            return cached;
        }
        
        List<Index> owners = new ArrayList<>();
        
        if(!path.contains("/")) {
            for(Index source : classSources) {
                if(source.hasDirectory(path)) {
                    owners.add(source);
                }
            }
        } else {
            String parentPath = path.substring(0, path.lastIndexOf('/'));
            List<Index> parentOwners = findDirectoryOwners(parentPath);
            for(Index s : parentOwners) {
                if(s.hasDirectory(path)) {
                    owners.add(s);
                }
            }
        }
        directoryOwners.put(path, owners);
        
        return owners;
    }

    @SneakyThrows
    private InputStream findResourceAsStream(String path) {
        if(classSources == null) {
            init();
        }
        
        String parentPath = path.substring(0, path.lastIndexOf('/'));
        List<Index> directoryOwners = findDirectoryOwners(parentPath);
        for(Index source : directoryOwners) {
            InputStream is = source.openStream(path);
            if(is != null) {
                return is;
            }
        }
        return null;
    }

    public InputStream getResourceAsStream(String path) {
        // Only look for resource in jars which have the directory...
        InputStream vanillaResult = null;
        if(DEBUG) {
            vanillaResult = DefaultResourcePack.class.getResourceAsStream("/" + path);
        }
        
        InputStream result = null;
        try {
            result = findResourceAsStream(path);
        } catch(Exception e) {
            LOGGER.error("DefaultResourcePack accelerator failed to find resource " + path);
            e.printStackTrace();
        }
        
        if(DEBUG) {
            if((vanillaResult == null) != (result == null)) {
                LOGGER.error("Mismatch detected in DefaultResourcePack optimization! (path=" + path + ", vanillaResult=" + vanillaResult + ", result=" + result + ") Please report the issue and disable the option (`fastDefaultTexturePack`) for now.");
            }
        }
        return result;
    }
    
    public static interface Index {
        public boolean hasDirectory(String directory);
        public InputStream openStream(String path) throws IOException;
        
        public static Index fromURL(URL url) {
            if(url.getProtocol().equals("file")) {
                if(url.getPath().endsWith(".jar")) {
                    return new JarIndex(url);
                } else {
                    return new DirectoryIndex(url);
                }
            } else {
                throw new IllegalArgumentException("Unknown protocol: " + url.getProtocol());
            }
        }
    }
    
    public static class JarIndex implements Index {
        Set<String> directories = new HashSet<>();
        URL url;
        @SneakyThrows
        public JarIndex(URL url) {
            this.url = url;
            File f = Paths.get(url.toURI()).toFile();
            try (ZipFile zf = new ZipFile(f)) {
                Enumeration<? extends ZipEntry> entries = zf.entries();
                while(entries.hasMoreElements()) {
                    ZipEntry ze = entries.nextElement();
                    String dirName = null;
                    if(ze.isDirectory()) {
                        String name = ze.getName();
                        name = name.substring(0, name.length() - 1);
                        dirName = name;
                    } else {
                        String s = ze.getName();
                        if(s.contains("/")) {
                            dirName = s.substring(0, s.lastIndexOf("/"));
                        }
                    }
                    if(dirName != null) {
                        while(directories.add(dirName) && dirName.contains("/")) {
                            dirName = dirName.substring(0, dirName.lastIndexOf("/"));
                        }
                    }
                }
            }
        }

        @Override
        public boolean hasDirectory(String directory) {
            return directories.contains(directory);
        }

        @Override
        public InputStream openStream(String path) throws IOException {
            try {
                return new URL("jar:" + url.toString() + "!/" + path).openStream();
            } catch(Exception e) {
                return null;
            }
        }
    }
    
    public static class DirectoryIndex implements Index {
        Map<String, Boolean> directoryExists = new HashMap<>();
        URL url;
        File file;
        
        @SneakyThrows
        public DirectoryIndex(URL url) {
            this.url = url;
            this.file = Paths.get(url.toURI()).toFile();
        }

        @Override
        public boolean hasDirectory(String directory) {
            return directoryExists(directory);
        }
        
        private boolean directoryExists(String directory) {
            Boolean cached = directoryExists.get(directory);
            if(cached != null) {
                return cached;
            }
            
            boolean exists = false;
            boolean isTopLevel = !directory.contains("/");
            String parent = isTopLevel ? null : directory.substring(0, directory.lastIndexOf('/'));
            
            if(isTopLevel || directoryExists(parent)) {
                exists = new File(file, directory).exists();
            }
            directoryExists.put(directory, exists);
            return exists;
        }

        @Override
        public InputStream openStream(String path) throws IOException {
            try {
                return new URL(url.toString() + path).openStream();
            } catch(Exception e) {
                return null;
            }
        }
    }
}

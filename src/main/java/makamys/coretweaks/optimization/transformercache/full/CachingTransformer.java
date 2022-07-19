package makamys.coretweaks.optimization.transformercache.full;

import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.Files;

import makamys.coretweaks.Config;
import makamys.coretweaks.CoreTweaks;
import makamys.coretweaks.Persistence;
import makamys.coretweaks.util.Util;
import makamys.coretweaks.util.WrappedAddListenableMap;
import makamys.coretweaks.util.WrappedAddListenableMap.MapAddListener;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

/**
 * This transformer takes the place of the entire transformer chain. The first time a class is loaded,
 * it runs it through the other transformers, and saves the result. On following occasions, it simply
 * returns the already cached class, with the goal of speeding up load times.<br><br>
 * 
 * It works by replacing the LaunchClassLoader's transformer list with a {@link WrappedTransformerList}. 
 */
public class CachingTransformer implements IClassTransformer, MapAddListener<String, Class<?>> {
	
	// TODO can't this be compressed into a lambda?
	
	static class SaveThread extends Thread {
		
		private CachingTransformer cacheTransformer;
		
		private int saveInterval = 10000;
		
		public SaveThread(CachingTransformer ct) {
			this.cacheTransformer = ct;
			setName("CacheTransformer save thread");
			setDaemon(false);
		}
		
		@Override
		public void run() {
			while(true) {
				try {
					Thread.sleep(saveInterval);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				cacheTransformer.doSave();
			}
		}
	}
	
	private WrappedTransformerList<IClassTransformer> wrappedTransformers;
	private WrappedAddListenableMap<String, Class<?>> wrappedCachedClasses;
	
	private Map<String, Optional<byte[]>> cache = new ConcurrentHashMap<>();
	private final static int QUEUE_SIZE = Config.recentCacheSize;
	Optional<Cache<String, byte[]>> recentCache = QUEUE_SIZE < 0 ? Optional.empty() :
		Optional.of(CacheBuilder.newBuilder().maximumSize(QUEUE_SIZE).build());
	
	private SaveThread saveThread = new SaveThread(this);
	
	private Set<String> badTransformers = 
			new HashSet<>(Arrays.stream(Config.badTransformers.split(",")).collect(Collectors.toList()));
	private Set<String> badClasses = 
			new HashSet<>(Arrays.stream(Config.badClasses.split(",")).collect(Collectors.toList()));
	
	public static final boolean DEBUG_PRINT = Config.verbosity == 2;
	private static boolean printSave = Config.verbosity >= 1;
	
	private int lastSaveSize = 0;
	private BlockingQueue<String> dirtyClasses = new LinkedBlockingQueue<String>();
	
	private static final File CLASS_CACHE_DAT = Util.childFile(CoreTweaks.CACHE_DIR, "classCache.dat");
	private static final File CLASS_CACHE_DAT_TMP = Util.childFile(CoreTweaks.CACHE_DIR, "classCache.dat~");
	
	public CachingTransformer(List<IClassTransformer> transformers, WrappedTransformerList<IClassTransformer> wrappedTransformers, WrappedAddListenableMap<String, Class<?>> wrappedCachedClasses) {
		LOGGER.info("Initializing cache transformer");
		
		this.wrappedTransformers = wrappedTransformers;
		this.wrappedCachedClasses = wrappedCachedClasses;
		wrappedCachedClasses.addListener(this);
		
		if(isDevEnvironment() || Persistence.modsChanged()) {
			clearCache(isDevEnvironment() ? "this is a dev environment." : "mods have changed.");
		} else {
			loadCache();
		}
		saveThread.start();
	}
	
	private static boolean isDevEnvironment() {
		try {
			return Launch.classLoader.getClassBytes("net.minecraft.world.World") != null;
		} catch (IOException e) {
			return false;
		}
	}
	
	private void clearCache(String reason) {
		LOGGER.info("Rebuilding class cache, because " + reason);
		CLASS_CACHE_DAT.delete();
		Persistence.erroredClassesLog.clear();
	}
	
	public void doSave() {
		saveCache();
		Persistence.erroredClassesLog.flush();
		Persistence.debugLog.flush();
	}
	
	private void loadCache() {
		File inFile = CLASS_CACHE_DAT;
		
		if(inFile.exists()) {
			LOGGER.info("Loading class cache.");
			cache.clear();
			
			boolean foundCorruption = false;
			
			try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(inFile)))){
				try {
					while(true) { // EOFException should break the loop
						String className = in.readUTF();
						int classLength = in.readInt();
						byte[] classData = new byte[classLength];
						int bytesRead = in.read(classData, 0, classLength);
						
						if(bytesRead == classLength) {
							cache.put(className, Optional.of(classData));
							
							superDebug("Loaded " + className);
						} else {
							LOGGER.warn("Length of " + className + " doesn't match advertised length. Skipping.");
							foundCorruption = true;
						}
					}
				} catch(EOFException eof) {}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			LOGGER.info("Loaded " + cache.size() + " cached classes.");
			
			lastSaveSize = cache.size();
			
			if(foundCorruption) {
				LOGGER.warn("There was data corruption present in the cache file. Doing full save to restore integrity.");
				saveCacheFully();
			}
		} else {
			LOGGER.info("Couldn't find class cache file");
		}
	}
	
	private void saveCacheFully() {
		File outFile = CLASS_CACHE_DAT;
		File outFileTmp = CLASS_CACHE_DAT_TMP;
		
		LOGGER.info("Performing full save of class cache (size: " + cache.size() + ")");
		saveCacheChunk(cache.keySet(), outFileTmp, false);
		
		try {
			Files.move(outFileTmp, outFile);
		} catch (IOException e) {
			LOGGER.error("Failed to finish saving class cache");
			e.printStackTrace();
		}
	}
	
	private void saveCache() {
		if(dirtyClasses.isEmpty()) {
			return; // don't save if the cache hasn't changed
		}
		
		File outFile = CLASS_CACHE_DAT;
		try {
            outFile.createNewFile();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
		
		List<String> classesToSave = new ArrayList<String>();
		dirtyClasses.drainTo(classesToSave);
		
		if(printSave) {
			LOGGER.info("Saving class cache (size: " + lastSaveSize + " -> " + cache.size() + " | +" + classesToSave.size() + ")");
		}
		saveCacheChunk(classesToSave, outFile, true);
		
		lastSaveSize += classesToSave.size();
	}
	
	private void saveCacheChunk(Collection<String> classesToSave, File outFile, boolean append) {
		try(DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outFile, append)))){
			for(String name : classesToSave) {
				Optional<byte[]> data = cache.get(name);
		    	if(data != null && data.isPresent()) {
		    		out.writeUTF(name);
					out.writeInt(data.get().length);
					out.write(data.get());
		    	}
			}
			if(printSave) {
				LOGGER.info("Saved class cache");
			}
		} catch (IOException e) {
			LOGGER.info("Exception saving class cache");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String describeBytecode(byte[] basicClass) {
	    return basicClass == null ? "null" : String.format("length: %d, hash: %x", basicClass.length, basicClass.hashCode());
	}
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		byte[] result = null;
		
	    superDebug(String.format("Starting loading class %s (%s) (%s)", name, transformedName, describeBytecode(basicClass)));
		
		try {
			boolean dontCache = false;
			for(String badPrefix : badClasses) {
				if(transformedName.startsWith(badPrefix)){
					dontCache = true;
					break;
				}
			}
			
			if(cache.containsKey(transformedName) && !dontCache) {
				superDebug("Yay, we have it cached!");
				
				if(cache.get(transformedName).isPresent()) { // we still remember it
					result = cache.get(transformedName).get();
					
					if(recentCache.isPresent()) {
						// classes are only loaded once, so no need to keep it around in RAM
						cache.put(transformedName, Optional.empty());
						
						// but keep it around in case it's needed again by another transformer in the chain
						recentCache.get().put(transformedName, result);
					}
				} else if(recentCache.isPresent()){ // we have forgotten it, hopefully it's still around in the recent queue
					result = recentCache.get().getIfPresent(transformedName);
					if(result == null) {
						LOGGER.warn("Couldn't find " + transformedName + " in cache. Is recent queue too small? (" + QUEUE_SIZE + ")");
					}
				}
			}
			if(result == null){
				// fall back to normal behavior..
			    for (final IClassTransformer transformer : wrappedTransformers.original) {
				    if(transformer == this) {
				        LOGGER.info("oops,");
				    }
				    

				    if(isBadTransformer(transformer)) {
				        wrappedTransformers.alt = null; // Hide from the view of conflicting transformers
				    }
				    
				    superDebug(String.format("Before transformer: %s (%s)", transformer.getClass().getName(), describeBytecode(basicClass)));
	                basicClass = transformer.transform(name, transformedName, basicClass);
	                superDebug(String.format("After transformer: %s (%s)", transformer.getClass().getName(), describeBytecode(basicClass)));
	                
	                if(wrappedTransformers.alt == null) {
	                    wrappedTransformers.alt = this; // reappear
	                }
	            }
			    if(basicClass != null && !dontCache) {
			        cache.put(transformedName, Optional.of(basicClass)); // then cache it
			        dirtyClasses.add(transformedName);
			    }
				result = basicClass;
			}
			if(result != null && recentCache.isPresent() && !dontCache) {
				recentCache.get().put(transformedName, result);
			}
		} catch(Exception e) {
			if(DEBUG_PRINT) {
				Persistence.erroredClassesLog.write(transformedName + " / " + e.getClass().getName() + " / " + e.getMessage());
			}
			throw e; // pass it to LaunchClassLoader, who will handle it
		} finally {
			wrappedTransformers.alt = this;
		}

	    superDebug(String.format("Finished loading class %s (%s) (%s)", name, transformedName, describeBytecode(basicClass)));
		return result;
	}
	
	private boolean isBadTransformer(IClassTransformer transformer) {
	    for(String badPattern : badTransformers) {
	    	if(badPattern.endsWith("+")) {
    			Class<?> baseClass = wrappedCachedClasses
    					.get(badPattern.substring(0, badPattern.length() - 1));
				if(baseClass != null && baseClass.isInstance(transformer)) {
					return true;
				}
	    	} else {
	    		if(badPattern.contentEquals(transformer.getClass().getName())) {
	    			return true;
	    		}
	    	}
	    }
	    return false;
	}
	    
	private void superDebug(String msg) {
	    if(DEBUG_PRINT) {
	        LOGGER.trace(msg);
	        Persistence.debugLog.write(msg);
	    }
	}
	
	public static CachingTransformer register() {
		CachingTransformer cacheTransformer = null;
    	try {
            LaunchClassLoader lcl = (LaunchClassLoader)Launch.classLoader;
            
            
            Field transformersField = LaunchClassLoader.class.getDeclaredField("transformers");
            transformersField.setAccessible(true);
            List<IClassTransformer> transformers = (List<IClassTransformer>)transformersField.get(lcl);
            
            WrappedTransformerList<IClassTransformer> wrappedTransformers = 
            		new WrappedTransformerList<IClassTransformer>(transformers);
            
            transformersField.set(lcl, wrappedTransformers);

            
            Field cachedClassesField = LaunchClassLoader.class.getDeclaredField("cachedClasses");
            cachedClassesField.setAccessible(true);
            Map<String, Class<?>> cachedClasses = (Map<String, Class<?>>)cachedClassesField.get(lcl);
            //cachedClasses.clear(); // gotta do this to make Mixin happy
            
            WrappedAddListenableMap<String, Class<?>> wrappedCachedClasses = new WrappedAddListenableMap<String, Class<?>>(cachedClasses);
            cachedClassesField.set(lcl, wrappedCachedClasses);
            
            
            cacheTransformer = new CachingTransformer(transformers, wrappedTransformers, wrappedCachedClasses);
            
            wrappedTransformers.alt = cacheTransformer;
            
            LOGGER.info("Finished initializing cache transformer");
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            LOGGER.info("Exception registering cache transformer.");
            e.printStackTrace();
        }
    	return cacheTransformer;
	}
	
	@Override
	public boolean onPut(Map<String, Class<?>> delegateMap, String key, Class<?> value) {
		/* Explanation: When we're loading cached classes, mixin gives an error because it thinks it's going to
        have to run on already transformed classes. This doesn't actually happen, since CacheTransformer
        steals the transformation right from all other classes, including Mixin. But we need to bypass
        this error somehow, since it results in a crash. This is how we do it. (see MixinInfo.readTargets
        to see why it works)*/ 
     if(cache.keySet().contains(key)) {
         return false;
     } else {
         // For some reason mixin gives a different error if we always refuse to put, so we should only
         // do it when necessary.
         return true;
     }
	}

}

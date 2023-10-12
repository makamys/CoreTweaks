package makamys.coretweaks.optimization.transformercache.lite;

import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.unsafe.UnsafeInput;
import com.esotericsoftware.kryo.kryo5.unsafe.UnsafeOutput;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;

import cpw.mods.fml.repackage.com.nothome.delta.Delta;
import cpw.mods.fml.repackage.com.nothome.delta.GDiffWriter;
import makamys.coretweaks.Config;
import makamys.coretweaks.CoreTweaks;
import makamys.coretweaks.IModEventListener;
import makamys.coretweaks.optimization.NonFunctionAlteringWrapper;
import makamys.coretweaks.optimization.transformercache.lite.TransformerCache.TransformerData.CachedTransformation;
import makamys.coretweaks.util.FastByteBufferSeekableSource;
import makamys.coretweaks.util.InMemoryGDiffPatcher;
import makamys.coretweaks.util.Util;
import makamys.coretweaks.util.WrappedAddListenableList;
import makamys.coretweaks.util.WrappedAddListenableList.AdditionEvent;
import makamys.coretweaks.util.WrappedAddListenableList.AdditionEventListener;
import net.minecraft.launchwrapper.IClassNameTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

/* Format:
 * int8 0
 * int8 version
 * Map<String, TransformerData> map
 */
public class TransformerCache implements IModEventListener, AdditionEventListener<IClassTransformer> {
    
    public static TransformerCache instance = new TransformerCache();
    
    private List<IClassTransformer> myTransformers = new ArrayList<>();
    private Map<String, TransformerData> transformerMap = new HashMap<>();
    
    private static byte[] lastClassData;
    private static int lastClassDataLength;
    
    private static final byte MAGIC_0 = 0;
    private static final byte VERSION = 2;
    
    private static final File DAT_OLD = Util.childFile(CoreTweaks.CACHE_DIR, "transformerCache.dat");
    private static final File DAT = Util.childFile(CoreTweaks.CACHE_DIR, "classTransformerLite.cache");
    private static final File DAT_ERRORED = Util.childFile(CoreTweaks.CACHE_DIR, "classTransformerLite.cache.errored");
    private static final File TRANSFORMERCACHE_PROFILER_CSV = Util.childFile(CoreTweaks.OUT_DIR, "transformercache_profiler.csv");
    private Kryo kryo;
    
    private static final Delta delta = new Delta();
    
    private static final byte[] NULL_BYTE_ARRAY = new byte[0];
    
    private Set<String> transformersToCache = new HashSet<>();
    
    private boolean inited = false;
    
    private static byte[] memoizedHashData;
    private static int memoizedHashValue;
    
    public void init(boolean late) {
        if(inited) return;
        
        transformersToCache = Sets.newHashSet(Config.transformersToCache);
        
        // We get a ClassCircularityError if we don't add these
        Launch.classLoader.addTransformerExclusion("makamys.coretweaks.optimization.transformercache.lite.TransformerCache");
        Launch.classLoader.addTransformerExclusion("makamys.coretweaks.util.InMemoryGDiffPatcher");
        Launch.classLoader.addTransformerExclusion("makamys.coretweaks.util.FastByteBufferSeekableSource");
        
        loadData();
        
        hookClassLoader(late);
    }

    private void hookClassLoader(boolean late) {
        try {
            LaunchClassLoader lcl = (LaunchClassLoader)Launch.classLoader;
            
            Field transformersField = LaunchClassLoader.class.getDeclaredField("transformers");
            transformersField.setAccessible(true);
            List<IClassTransformer> transformers = (List<IClassTransformer>)transformersField.get(lcl);
            if(!late) {
                WrappedAddListenableList<IClassTransformer> wrappedTransformers = 
                        new WrappedAddListenableList<IClassTransformer>(transformers);
                wrappedTransformers.addListener(this);
                
                transformersField.set(lcl, wrappedTransformers);
            } else {
                for(int i = 0; i < transformers.size(); i++) {
                    IClassTransformer proxy = createCachedProxy(transformers.get(i));
                    if(proxy != null) {
                        transformers.set(i, proxy);
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void onAdd(AdditionEvent<IClassTransformer> event) {
        IClassTransformer proxy = createCachedProxy(event.element);
        if(proxy != null) {
            event.element = proxy;
        }
    }
    
    private IClassTransformer createCachedProxy(IClassTransformer transformer) {
        IClassTransformer realTransformer = transformer;
        while(realTransformer instanceof NonFunctionAlteringWrapper<?>) {
            realTransformer = ((NonFunctionAlteringWrapper<IClassTransformer>)realTransformer).getOriginal();
        }
        if(transformersToCache.contains(realTransformer.getClass().getCanonicalName())) {
            LOGGER.info("Replacing " + realTransformer.getClass().getCanonicalName() + " with cached proxy");
            
            try {
                IClassTransformer newTransformer = transformer instanceof IClassNameTransformer
                        ? CachedNameTransformerProxy.of(transformer) : CachedTransformerProxy.of(transformer);
    
                myTransformers.add(newTransformer);
                return newTransformer;
            } catch(Exception e) {
                LOGGER.error("Failed to create proxy class for " + realTransformer.getClass().getCanonicalName());
                e.printStackTrace();
            }
        }
        return null;
    }

    private void loadData() {
        long t0 = System.nanoTime();
        
        kryo = new Kryo();
        kryo.register(HashMap.class);
        kryo.register(TransformerCache.TransformerData.class);
        kryo.register(TransformerCache.TransformerData.CachedTransformation.class);
        kryo.register(byte[].class);
        
        if(DAT_OLD.exists() && !DAT.exists()) {
            LOGGER.info("Migrating class cache: " + DAT_OLD + " -> " + DAT);
            DAT_OLD.renameTo(DAT);
        }
        
        if(DAT.exists()) {
            try(Input is = new UnsafeInput(new BufferedInputStream(new FileInputStream(DAT)))) {
                byte magic0 = kryo.readObject(is, byte.class);
                byte version = kryo.readObject(is, byte.class);
                
                if(magic0 != MAGIC_0 || version != VERSION) {
                    CoreTweaks.LOGGER.warn("Transformer cache is either a different version or corrupted, discarding.");
                } else {
                    transformerMap = returnVerifiedTransformerMap(kryo.readObject(is, HashMap.class));
                }
                
                for(TransformerData data : transformerMap.values()) {
                    if(!Arrays.asList(Config.transformersToCache).contains(data.transformerClassName)) {
                        CoreTweaks.LOGGER.info("Dropping " + data.transformerClassName + " from cache because we don't care about it anymore.");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch(Exception e) {
                CoreTweaks.LOGGER.error("There was an error reading the transformer cache. A new one will be created. The previous one has been saved as " + DAT_ERRORED.getName() + " for inspection.");
                DAT.renameTo(DAT_ERRORED);
                e.printStackTrace();
            }
        }
        
        long t1 = System.nanoTime();
        LOGGER.debug("Loaded lite transformer cache in " + ((t1-t0) / 1_000_000_000.0) + "s");
    }
    
    private static Map<String, TransformerData> returnVerifiedTransformerMap(Map<String, TransformerData> map) {
        if(map.containsKey(null)) {
            throw new RuntimeException("Map contains null key");
        }
        if(map.containsValue(null)) {
            throw new RuntimeException("Map contains null value");
        }
        return map;
    }
    
    @Override
    public void onShutdown() {
        try {
            saveTransformerCache();
            saveProfilingResults();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void saveTransformerCache() throws IOException {
        if(!DAT.exists()) {
            DAT.getParentFile().mkdirs();
            DAT.createNewFile();
        }
        LOGGER.info("Saving transformer cache");
        trimCache((long)Config.liteTransformerCacheMaxSizeMB * 1024l * 1024l);
        try(Output output = new UnsafeOutput(new BufferedOutputStream(new FileOutputStream(DAT)))) {
            kryo.writeObject(output, MAGIC_0);
            kryo.writeObject(output, VERSION);
            kryo.writeObject(output, transformerMap);
        }
    }
    
    private void trimCache(long maxSize) {
        if(maxSize == -1) return;
        
        List<CachedTransformation> data = new ArrayList<>();
        
        for(TransformerData transData : transformerMap.values()) {
            data.addAll(transData.transformationMap.values());
        }
        
        data.sort(this::sortByAge);
        
        long usedSpace = 0;
        int cutoff = -1;
        for(int i = data.size() - 1; i >= 0; i--) {
            usedSpace += data.get(i).getEstimatedSize();
            if(usedSpace > maxSize) {
                cutoff = data.get(i).lastAccessed;
                break;
            }
        }
        
        if(cutoff != -1) {
            final int cutoffCopy = cutoff;
            for(TransformerData transData : transformerMap.values()) {
                transData.transformationMap.entrySet().removeIf(e -> e.getValue().lastAccessed <= cutoffCopy);
            }
            transformerMap.entrySet().removeIf(e -> e.getValue().transformationMap.isEmpty());
        }
    }
    
    private int sortByAge(CachedTransformation a, CachedTransformation b) {
        return a.lastAccessed < b.lastAccessed ? -1 : a.lastAccessed > b.lastAccessed ? 1 : 0;
    }
    
    private void saveProfilingResults() throws IOException {
        try(FileWriter fw = new FileWriter(TRANSFORMERCACHE_PROFILER_CSV)){
            fw.write("class,name,runs,misses\n");
            for(IClassTransformer transformer : myTransformers) {
                String className = transformer.getClass().getCanonicalName();
                String name = transformer.toString();
                int runs = 0;
                int misses = 0;
                if(transformer instanceof CachedTransformerProxy) {
                    CachedTransformerProxy proxy = (CachedTransformerProxy)transformer;
                    runs = proxy.runs;
                    misses = proxy.misses;
                }
                fw.write(className + "," + name + "," + runs + "," + misses + "\n");
            }
        }
    }

    public byte[] getCached(String transName, String name, String transformedName, byte[] basicClass) {
        TransformerData transData = transformerMap.get(transName);
        if(transData != null) {
            CachedTransformation trans = transData.transformationMap.get(transformedName);
            if(trans != null) {
                if(nullSafeLength(basicClass) == trans.preLength && calculateHash(basicClass) == trans.preHash) {
                    trans.lastAccessed = now();
                    return trans.postHash == trans.preHash ? toNullableByteArray(basicClass) : trans.getNewClass(basicClass);
                }
            }
        }
        return null;
    }
    
    public static byte[] toNullableByteArray(byte[] array) {
        return array == null ? NULL_BYTE_ARRAY : array;
    }
    
    public static byte[] fromNullableByteArray(byte[] array) {
        return array == NULL_BYTE_ARRAY ? null : array;
    }
    
    private static int nullSafeLength(byte[] array) {
        return array == null ? -1 : array.length;
    }

    public void prePutCached(String transName, String name, String transformedName, byte[] basicClass) {
        putLastClassData(basicClass);
    }
    
    private void putLastClassData(byte[] data) {
        if(data != null) {
            if(lastClassData == null || lastClassData.length < data.length) {
                int newSize = 1;
                while(newSize < data.length) {
                    newSize *= 2;
                }
                lastClassData = new byte[newSize];
            }
            System.arraycopy(data, 0, lastClassData, 0, data.length);
            lastClassDataLength = data.length;
        } else {
            lastClassData = null;
            lastClassDataLength = 0;
        }
    }

    /** MUST be preceded with a call to prePutCached. */
    public void putCached(String transName, String name, String transformedName, byte[] result) {
        TransformerData data = transformerMap.get(transName);
        if(data == null) {
            transformerMap.put(transName, data = new TransformerData(transName));
        }
        data.transformationMap.put(transformedName, new CachedTransformation(transformedName, lastClassData, lastClassDataLength, result));
    }
    
    public static int calculateHash(byte[] data) {
        return calculateHash(data, data.length);
    }
    
    public static int calculateHash(byte[] data, int len) {
        if(data == memoizedHashData) {
            return memoizedHashValue;
        }
        memoizedHashData = data;
        memoizedHashValue = data == null ? -1 : Hashing.adler32().hashBytes(data, 0, len).asInt();
        return memoizedHashValue;
    }
    
    private static int now() {
        // TODO update the format in 6055
        return (int)(System.currentTimeMillis() / 1000 / 60);
    }
    
    public static class TransformerData {
        String transformerClassName;
        Map<String, CachedTransformation> transformationMap = new HashMap<>();
        
        public TransformerData(String transformerClassName) {
            this.transformerClassName = transformerClassName;
        }
        
        public TransformerData() {}
        
        public static class CachedTransformation {
            String targetClassName;
            int preLength;
            int preHash;
            int postLength;
            int postHash;
            byte[] diff;
            int lastAccessed;
            
            public CachedTransformation() {}
            
            public CachedTransformation(String targetClassName, byte[] source, int sourceLen, byte[] target) {
                this.targetClassName = targetClassName;
                this.preHash = calculateHash(source, sourceLen);
                this.preLength = sourceLen;
                this.postLength = nullSafeLength(target);
                this.postHash = calculateHash(target);
                if(preHash != postHash) {
                    diff = generateDiff(source, sourceLen, target);
                }
                this.lastAccessed = now();
            }
            
            private static byte[] generateDiff(byte[] source, int sourceLen, byte[] target) {
                if(source == null) {
                    return target;
                }
                
                try {
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    delta.compute(new FastByteBufferSeekableSource(ByteBuffer.wrap(source, 0, sourceLen)), new ByteArrayInputStream(target), new GDiffWriter(os));
                    return os.toByteArray();
                } catch(Exception e) {
                    LOGGER.error("Failed to generate diff");
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
            
            public byte[] getNewClass(byte[] source) {
                if(source == null) {
                    return diff;
                }
                byte[] newClass = new byte[postLength];
                InMemoryGDiffPatcher.patch(source, diff, newClass);
                return newClass;
            }

            public int getEstimatedSize() {
                return targetClassName.length() + 4 + 4 + 4 + (diff != null ? diff.length : 0) + 4;
            }
        }
    }
}

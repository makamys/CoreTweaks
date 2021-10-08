package makamys.coretweaks.optimization.transformercache.lite;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.unsafe.UnsafeInput;
import com.esotericsoftware.kryo.unsafe.UnsafeOutput;
import com.google.common.hash.Hashing;

import cpw.mods.fml.relauncher.ReflectionHelper;
import makamys.coretweaks.Config;
import makamys.coretweaks.CoreTweaks;
import makamys.coretweaks.optimization.transformercache.lite.TransformerCache.TransformerData.CachedTransformation;
import makamys.coretweaks.util.Util;
import net.minecraft.launchwrapper.IClassNameTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

public class TransformerCache {
    
    public static TransformerCache instance = new TransformerCache();
    
    private List<IClassTransformer> myTransformers = new ArrayList<>();
    private Map<String, TransformerData> transformerMap = new HashMap<>();
    
    private static final File DAT = Util.childFile(CoreTweaks.CACHE_DIR, "transformerCache.dat");
    private static final File TRANSFORMERCACHE_PROFILER_CSV = Util.childFile(CoreTweaks.OUT_DIR, "transformercache_profiler.csv");
    private final Kryo kryo = new Kryo();
    
    private Set<String> transformersToCache = new HashSet<>();
    
    private boolean inited = false;
    
    private static byte[] memoizedHashData;
    private static int memoizedHashValue;
    
    public void init() {
        if(inited) return;
        
        transformersToCache = Arrays.stream(Config.transformersToCache).collect(Collectors.toSet());
        
        // We get a ClassCircularityError if we don't add this
        Launch.classLoader.addTransformerExclusion("makamys.coretweaks.optimization.transformercache.lite");
        
        loadData();
        
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                shutdown();
            }}, "CoreTweaks transformer cache shutdown thread"));
        
        hookClassLoader();
    }

    private void hookClassLoader() {
        LaunchClassLoader lcl = (LaunchClassLoader)Launch.classLoader;
        List<IClassTransformer> transformers = (List<IClassTransformer>)ReflectionHelper.getPrivateValue(LaunchClassLoader.class, lcl, "transformers");
        for(int i = 0; i < transformers.size(); i++) {
            IClassTransformer transformer = transformers.get(i);
            if(transformersToCache.contains(transformer.getClass().getCanonicalName())) {
                System.out.println("Replacing " + transformer.getClass().getCanonicalName() + " with cached proxy");
                
                IClassTransformer newTransformer = transformer instanceof IClassNameTransformer
                        ? new CachedNameTransformerProxy(transformer) : new CachedTransformerProxy(transformer);

                myTransformers.add(newTransformer);
                transformers.set(i, newTransformer);
            }
        }
    }
    
    private void loadData() {
        kryo.setRegistrationRequired(false);
        
        if(DAT.exists()) {
            try(Input is = new UnsafeInput(new BufferedInputStream(new FileInputStream(DAT)))) {
                transformerMap = kryo.readObject(is, HashMap.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void shutdown() {
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
        System.out.println("Saving transformer cache");
        try(Output output = new UnsafeOutput(new BufferedOutputStream(new FileOutputStream(DAT)))) {
            kryo.writeObject(output, transformerMap);
        }
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
                int preHash = calculateHash(basicClass);
                if(preHash == trans.preHash) {
                    return trans.postHash == trans.preHash ? basicClass : trans.newClass;
                }
            }
        }
        return null;
    }

    public void prePutCached(String transName, String name, String transformedName, byte[] basicClass) {
        TransformerData data = transformerMap.get(transName);
        if(data == null) {
            transformerMap.put(transName, data = new TransformerData(transName));
        }
        data.transformationMap.put(transformedName, new CachedTransformation(transformedName, calculateHash(basicClass)));
    }
    
    /** MUST be preceded with a call to prePutCached. */
    public void putCached(String transName, String name, String transformedName, byte[] result) {
        transformerMap.get(transName).transformationMap.get(transformedName).putClass(result);
    }
    
    public static int calculateHash(byte[] data) {
        if(data == memoizedHashData) {
            return memoizedHashValue;
        }
        memoizedHashData = data;
        memoizedHashValue = Hashing.adler32().hashBytes(data).asInt();
        return memoizedHashValue;
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
            int preHash;
            int postHash;
            byte[] newClass;
            
            public CachedTransformation() {}
            
            public CachedTransformation(String targetClassName, int preHash) {
                this.targetClassName = targetClassName;
                this.preHash = preHash;
            }
            
            public void putClass(byte[] result) {
                postHash = calculateHash(result);
                if(preHash != postHash) {
                    newClass = result;
                }
            }
        }
    }
}

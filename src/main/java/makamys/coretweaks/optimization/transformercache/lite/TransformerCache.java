package makamys.coretweaks.optimization.transformercache.lite;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
import net.minecraft.launchwrapper.IClassNameTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

public class TransformerCache {
    
    public static TransformerCache instance = new TransformerCache();
    
    private Map<String, TransformerData> transformerMap = new HashMap<>();
    
    private final File file = CoreTweaks.getDataFile("transformerCache.dat", false);
    private final Kryo kryo = new Kryo();
    
    private Set<String> transformersToCache = new HashSet<>();
    
    private boolean inited = false;
    
    public void init() {
        if(inited) return;
        
        transformersToCache = Arrays.stream(Config.transformersToCache).collect(Collectors.toSet());
        
        // We get a ClassCircularityError if we don't add this
        Launch.classLoader.addTransformerExclusion("makamys.coretweaks.optimization.transformercache.lite");
        
        loadData();
        
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(!file.exists()) {
                        file.getParentFile().mkdirs();
                        file.createNewFile();
                    }
                    System.out.println("Saving transformer cache");
                    try(Output output = new UnsafeOutput(new BufferedOutputStream(new FileOutputStream(file, true)))) {
                        kryo.writeObject(output, transformerMap);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }}, "CoreTweaks transformer cache save thread"));
        
        hookClassLoader();
    }

    private void hookClassLoader() {
        LaunchClassLoader lcl = (LaunchClassLoader)Launch.classLoader;
        List<IClassTransformer> transformers = (List<IClassTransformer>)ReflectionHelper.getPrivateValue(LaunchClassLoader.class, lcl, "transformers");
        for(int i = 0; i < transformers.size(); i++) {
            IClassTransformer transformer = transformers.get(i);
            if(transformersToCache.contains(transformer.getClass().getCanonicalName())) {
                System.out.println("Replacing " + transformer.getClass().getCanonicalName() + " with cached one");
                transformers.set(i, transformer instanceof IClassNameTransformer
                        ? new CachedNameTransformerProxy(transformer) : new CachedTransformerProxy(transformer));
            }
        }
    }
    
    private void loadData() {
        kryo.setRegistrationRequired(false);
        
        if(file.exists()) {
            try(Input is = new UnsafeInput(new BufferedInputStream(new FileInputStream(file)))) {
                transformerMap = kryo.readObject(is, HashMap.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public byte[] getCached(IClassTransformer transformer, String name, String transformedName, byte[] basicClass) {
        String transName = transformer.getClass().getCanonicalName();
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

    public void prePutCached(IClassTransformer transformer, String name, String transformedName, byte[] basicClass) {
        String transName = transformer.getClass().getCanonicalName();
        TransformerData data = transformerMap.get(transName);
        if(data == null) {
            transformerMap.put(transName, data = new TransformerData(transName));
        }
        CachedTransformation trans = data.transformationMap.get(transformedName);
        if(trans == null) {
            data.transformationMap.put(transformedName, trans = new CachedTransformation(transformedName, calculateHash(basicClass)));
        }
    }
    
    /** MUST be preceded with a call to prePutCached. */
    public void putCached(IClassTransformer transformer, String name, String transformedName, byte[] result) {
        transformerMap.get(transformer.getClass().getCanonicalName()).transformationMap.get(transformedName).putClass(result);
    }
    
    public static int calculateHash(byte[] data) {
        return Hashing.adler32().hashBytes(data).asInt();
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

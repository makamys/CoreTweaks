package makamys.coretweaks.optimization.transformercache.light;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.unsafe.UnsafeInput;
import com.esotericsoftware.kryo.unsafe.UnsafeOutput;

import cpw.mods.fml.common.asm.transformers.DeobfuscationTransformer;
import cpw.mods.fml.relauncher.ReflectionHelper;
import makamys.coretweaks.CoreTweaks;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

public class TransformerCache {
    
    public static TransformerCache instance;
    
    private Map<String, TransformerData> data = new HashMap<>();
    
    private final File file = CoreTweaks.getDataFile("transformerCache.dat");
    private final Kryo kryo = new Kryo();
    
    public static void init() {
        instance = new TransformerCache();
    }
    
    public TransformerCache() {
        hookClassLoader();
        loadData();
        
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(!file.exists()) {
                        file.getParentFile().mkdirs();
                        file.createNewFile();
                    }
                    try(Output output = new UnsafeOutput(new BufferedOutputStream(new FileOutputStream(file, true)))) {
                        kryo.writeObject(output, data);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }}, "CoreTweaks transformer cache save thread"));
    }

    private void hookClassLoader() {
        LaunchClassLoader lcl = (LaunchClassLoader)Launch.classLoader;
        List<IClassTransformer> transformers = (List<IClassTransformer>)ReflectionHelper.getPrivateValue(LaunchClassLoader.class, lcl, "transformers");
        for(int i = 0; i < transformers.size(); i++) {
            if(transformers.get(i) instanceof DeobfuscationTransformer) {
                System.out.println("Found deobfuscation transformer, hooking it with cached one");
                transformers.set(i, new CachedNameTransformer((DeobfuscationTransformer)transformers.get(i)));
            }
        }
    }
    
    private void loadData() {
        kryo.setRegistrationRequired(false);
        
        if(file.exists()) {
            try(Input is = new UnsafeInput(new BufferedInputStream(new FileInputStream(file)))) {
                data = kryo.readObject(is, Map.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public byte[] getCached(IClassTransformer transformer, String name, String transformedName, byte[] basicClass) {
        // TODO Auto-generated method stub
        return null;
    }

    public void prePutCached(IClassTransformer transformer, String name, String transformedName, byte[] basicClass) {
        // TODO Auto-generated method stub
        
    }

    public void putCached(IClassTransformer transformer, String name, String transformedName, byte[] result) {
        // TODO Auto-generated method stub
        
    }
    
    public class TransformerData {
        
    }
}

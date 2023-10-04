package makamys.coretweaks.optimization;

import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;

import org.objectweb.asm.Type;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.unsafe.UnsafeInput;
import com.esotericsoftware.kryo.unsafe.UnsafeOutput;

import cpw.mods.fml.common.discovery.asm.ASMModParser;
import cpw.mods.fml.common.discovery.asm.ModAnnotation;
import cpw.mods.fml.common.discovery.asm.ModAnnotation.EnumHolder;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import makamys.coretweaks.Config;
import makamys.coretweaks.CoreTweaks;
import makamys.coretweaks.IModEventListener;
import makamys.coretweaks.util.Util;

/*
 * Format (v1):
 * int8 0
 * int8 version
 * int32 epoch
 * Map<String, CachedModInfo> cache
 */
public class JarDiscovererCache implements IModEventListener {
    
    public static JarDiscovererCache instance;
    
    private Map<String, CachedModInfo> cache = new HashMap<>();
    private int epoch;
    
    private byte MAGIC_0 = 0;
    private byte VERSION = 1;
    
    private final File DAT_OLD = Util.childFile(CoreTweaks.CACHE_DIR, "jarDiscovererCache.dat");
    private final File DAT = Util.childFile(CoreTweaks.CACHE_DIR, "jarDiscoverer.cache");
    private final File DAT_ERRORED = Util.childFile(CoreTweaks.CACHE_DIR, "jarDiscoverer.cache.errored");
    
    private final Kryo kryo = new Kryo();
    
    public void load() {
        LOGGER.info("Loading JarDiscovererCache");
        kryo.register(Type.class, new TypeSerializer());
        kryo.register(ModAnnotation.class, new ModAnnotationSerializer());
        kryo.register(EnumHolder.class, new EnumHolderSerializer());
        kryo.setRegistrationRequired(false);
        
        if(DAT_OLD.exists() && !DAT.exists()) {
            LOGGER.info("Migrating jar discoverer cache: " + DAT_OLD + " -> " + DAT);
            DAT_OLD.renameTo(DAT);
        }
        
        if(DAT.exists()) {
            try(Input is = new UnsafeInput(new BufferedInputStream(new FileInputStream(DAT)))) {
                byte magic0 = kryo.readObject(is, byte.class);
                byte version = kryo.readObject(is, byte.class);
                epoch = kryo.readObject(is, int.class);
                epoch++;
                
                if(magic0 != MAGIC_0 || version != VERSION) {
                    CoreTweaks.LOGGER.warn("Jar discoverer cache is either a different version or corrupted, discarding.");
                } else {
                    cache = returnVerifiedMap(kryo.readObject(is, HashMap.class));
                }
            } catch (Exception e) {
                CoreTweaks.LOGGER.error("There was an error reading the jar discoverer cache. A new one will be created. The previous one has been saved as " + DAT_ERRORED.getName() + " for inspection.");
                DAT.renameTo(DAT_ERRORED);
                e.printStackTrace();
                cache.clear();
                epoch = 0;
            }
        }
    }
    
    private Map<String, CachedModInfo> returnVerifiedMap(Map<String, CachedModInfo> map) {
        if(map.containsKey(null)) {
            throw new RuntimeException("Map contains null key");
        }
        if(map.containsValue(null)) {
            throw new RuntimeException("Map contains null value");
        }
        return map;
    }
    
    @Override
    public void onPostInit(FMLPostInitializationEvent event) {
        finish();
    }
    
    public void finish() {
        if(!cache.isEmpty()) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        if(!DAT.exists()) {
                            DAT.getParentFile().mkdirs();
                            DAT.createNewFile();
                        }
                        cache.entrySet().removeIf(e -> (epoch - e.getValue().lastAccessed) > Config.jarDiscovererCacheMaxAge);
                        try(Output output = new UnsafeOutput(new BufferedOutputStream(new FileOutputStream(DAT)))) {
                            kryo.writeObject(output, MAGIC_0);
                            kryo.writeObject(output, VERSION);
                            kryo.writeObject(output, epoch);
                            kryo.writeObject(output, cache);
                        }
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    cache = null;
                }
                
            }, "CoreTweaks JarDiscovererCache save thread").start();
        }
    }
    
    public CachedModInfo getCachedModInfo(String hash) {
        CachedModInfo cmi = cache.get(hash);
        if(cmi == null) {
            cmi = new CachedModInfo(true);
            cache.put(hash, cmi);
        }
        cmi.lastAccessed = epoch;
        return cmi;
    }
    
    public static class CachedModInfo {
        
        Map<String, ASMModParser> parserMap = new HashMap<>();
        Set<String> modClasses = new HashSet<>();
        int lastAccessed;
        transient boolean dirty;
        
        public CachedModInfo(boolean dirty) {
            this.dirty = dirty;
        }
        
        public CachedModInfo() {
            this(false);
        }
        
        public ASMModParser getCachedParser(ZipEntry ze) {
            return parserMap.get(ze.getName());
        }
        
        public void putParser(ZipEntry ze, ASMModParser parser) {
            parserMap.put(ze.getName(), parser);
        }
        
        public int getCachedIsModClass(ZipEntry ze) {
            return dirty ? -1 : modClasses.contains(ze.getName()) ? 1 : 0; 
        }
        
        public void putIsModClass(ZipEntry ze, boolean value) {
            if(!dirty) {
                throw new IllegalStateException();
            }
            
            if(value) {
                modClasses.add(ze.getName());
            }
        }
    }
    
    public static class TypeSerializer extends Serializer<Type> {

        @Override
        public void write(Kryo kryo, Output output, Type type) {
            output.writeByte(type.getSort());
            if(type.getSort() >= Type.ARRAY) {
                output.writeString(type.getInternalName());
            }
        }

        @Override
        public Type read(Kryo kryo, Input input, Class<? extends Type> type) {
            int sort = input.readByte();
            String buf = sort >= Type.ARRAY ? input.readString() : null;
            switch(sort) {
                case Type.VOID:
                    return Type.VOID_TYPE;
                case Type.BOOLEAN:
                    return Type.BOOLEAN_TYPE;
                case Type.CHAR:
                    return Type.CHAR_TYPE;
                case Type.BYTE:
                    return Type.BYTE_TYPE;
                case Type.SHORT:
                    return Type.SHORT_TYPE;
                case Type.INT:
                    return Type.INT_TYPE;
                case Type.FLOAT:
                    return Type.FLOAT_TYPE;
                case Type.LONG:
                    return Type.LONG_TYPE;
                case Type.DOUBLE:
                    return Type.DOUBLE_TYPE;
                case Type.ARRAY:
                case Type.OBJECT:
                    return Type.getObjectType(buf);
                case Type.METHOD:
                    return Type.getMethodType(buf);
                default:
                    return null;
            }
        }
        
    }
    
    public static class ModAnnotationSerializer extends Serializer<ModAnnotation> {
        
        private static ModAnnotation lastMa;
        
        @Override
        public void write(Kryo kryo, Output output, ModAnnotation ma) {
            kryo.writeObject(output, ma.getType());
            kryo.writeObject(output, ma.getASMType());
            output.writeString(ma.getMember());
            Map<String, Object> serializableValues = new HashMap<>();
            
            kryo.writeObject(output, ma.getValues());
        }

        @Override
        public ModAnnotation read(Kryo kryo, Input input, Class<? extends ModAnnotation> ma) {
            try {
                Field type = ma.getDeclaredField("type");
                Object at = kryo.readObject(input, type.getType());
                ModAnnotation maa = new ModAnnotation(null, kryo.readObject(input, Type.class), input.readString());
                type.setAccessible(true);
                type.set(maa, at);
                
                lastMa = maa;
                try {
                Map<String, Object> values = kryo.readObject(input, HashMap.class);
                values.forEach((k, v) -> {
                    maa.addProperty(k, v);
                    
                });
                } catch(Exception e) {
                    return null;
                }
                return maa;
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            return null;
        }
        
    }
    
    public static class EnumHolderSerializer extends Serializer<EnumHolder> {

        @Override
        public void write(Kryo kryo, Output output, EnumHolder eh) {
            try {
                Field descF = eh.getClass().getDeclaredField("desc");
                descF.setAccessible(true);
                Field valueF = eh.getClass().getDeclaredField("value");
                valueF.setAccessible(true);
                
                String desc = (String) descF.get(eh);
                String value = (String) valueF.get(eh);
                
                output.writeString(desc);
                output.writeString(value);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public EnumHolder read(Kryo kryo, Input input, Class<? extends EnumHolder> type) {
            return ModAnnotationSerializer.lastMa.new EnumHolder(input.readString(), input.readString());
        }
        
    }
}

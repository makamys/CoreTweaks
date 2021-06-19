package makamys.toomanycrashes;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;

import org.objectweb.asm.Type;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.unsafe.UnsafeInput;
import com.esotericsoftware.kryo.unsafe.UnsafeOutput;

import cpw.mods.fml.common.discovery.asm.ASMModParser;
import cpw.mods.fml.common.discovery.asm.ModAnnotation;
import cpw.mods.fml.common.discovery.asm.ModAnnotation.EnumHolder;
import net.minecraft.launchwrapper.Launch;

public class JarDiscovererCache {
	
	private static Map<String, CachedModInfo> cache = new HashMap<>();
	private static Map<String, CachedModInfo> dirtyCache = new HashMap<>();
	
	private static final File file = new File(new File(Launch.minecraftHome, "toomanycrashes"), "jarDiscovererCache.dat");
	
	private static final Kryo kryo = new Kryo();
	
	public static void load() {
		System.out.println("Loading JarDiscovererCache");
		kryo.register(Type.class, new TypeSerializer());
		kryo.register(ModAnnotation.class, new ModAnnotationSerializer());
		kryo.register(EnumHolder.class, new EnumHolderSerializer());
		kryo.setRegistrationRequired(false);
		
		
		if(file.exists()) {
			try(Input is = new UnsafeInput(new BufferedInputStream(new FileInputStream(file)))) {
				try {
					while(true) {
						String k = kryo.readObject(is, String.class);
						CachedModInfo v = kryo.readObject(is, CachedModInfo.class);
						System.out.println("Read CachedModInfo " + k);
						cache.put(k, v);
					}
					
				} catch(KryoException e) {
					// this feels dirty but I don't know a better way to check for EOF..
					if(!e.getMessage().equals("Buffer underflow.")) {
						throw e;
					}
				}
				
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	public static void finish() {
		if(!dirtyCache.isEmpty()) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						if(!file.exists()) {
							file.getParentFile().mkdirs();
							file.createNewFile();
						}
						try(Output output = new UnsafeOutput(new BufferedOutputStream(new FileOutputStream(file, true)))) {
							for(Entry<String, CachedModInfo> e : dirtyCache.entrySet()) {
								System.out.println("Writing CachedModInfo " + e.getKey());
								kryo.writeObject(output, e.getKey());
								kryo.writeObject(output, e.getValue());
							}
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					cache = null;
					dirtyCache = null;
				}
				
			}, "TooManyCrashes JarDiscovererCache save thread").start();
		}
	}
	
	public static CachedModInfo getCachedModInfo(String hash) {
		CachedModInfo cmi = cache.get(hash);
		if(cmi == null) {
			cmi = new CachedModInfo(true);
			dirtyCache.put(hash, cmi);
		}
		return cmi;
	}
	
	public static class CachedModInfo {
		
		Map<String, ASMModParser> parserMap = new HashMap<>();
		Set<String> modClasses = new HashSet<>();
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

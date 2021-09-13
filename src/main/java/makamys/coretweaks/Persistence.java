package makamys.coretweaks;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.ModListHelper;
import net.minecraft.launchwrapper.Launch;

import static makamys.coretweaks.CoreTweaks.LOGGER;

public class Persistence {
	
	public static class Log {
		
		private File file;
		private OutputStream out;
		
		boolean failed = false;
		
		public Log(String path) {
			file = CoreTweaks.getDataFile(path, false);
		}
		
		public void write(String msg) {
			if(failed) return;
			
			if(out == null) {
				try {
					file.createNewFile();
					out = new BufferedOutputStream(new FileOutputStream(file));
				} catch (IOException e) {
					LOGGER.warn("Failed to open log file: " + file);
					e.getStackTrace();
					failed = true;
				}
			}
			if(out != null) {
				try {
					out.write((msg + "\n").getBytes(Charset.forName("UTF-8")));
				} catch (IOException e) {
					LOGGER.warn("Failed to write to log file " + file);
					e.printStackTrace();
					failed = true;
				}
			}
		}
		
		public void clear() {
			file.delete();
		}
		
		public void flush() {
			if(failed) return;
			
			if(out != null) {
				try {
					out.flush();
				} catch (IOException e) {
					LOGGER.warn("Failed to flush log file " + file);
					e.printStackTrace();
					failed = true;
				}
			}
		}
	}
	
	private static Properties props;
	
	public static String lastMods;
	public static String lastVersion;
	
	public static Log erroredClassesLog = new Log("errored-classes.txt");
	public static Log debugLog = new Log("debug.txt");
	
	public static void loadIfNotLoadedAlready() {
		if(props != null) return;
		
		props = new Properties();
		try {
			props.load(new BufferedInputStream(new FileInputStream(CoreTweaks.getDataFile("persistence.txt"))));
		} catch (IOException e) {
			LOGGER.warn("Failed to load persistence file");
			e.printStackTrace();
		}
		lastMods = props.getProperty("lastMods", "");
		lastVersion = props.getProperty("lastVersion", "");
	}
	
	public static void save() {
		if(props == null) {
			// If we loaded here, we'd need to implement selective loading to make sure we don't
			// overwrite values already set by loading.. it's easier to just make sure we
			// load at the start.
			throw new IllegalStateException("Tried to save persistence without loading first");
		}
		try {
			props.setProperty("lastMods", lastMods);
			props.setProperty("lastVersion", lastVersion);
			
			props.store(new BufferedOutputStream(new FileOutputStream(CoreTweaks.getDataFile("persistence.txt"))),
					"This file is used by CoreTweaks to store data. You probably shouldn't edit it.");
		} catch (IOException e) {
			LOGGER.warn("Failed to save persistence file");
			e.printStackTrace();
		}
	}
	
	static class ModInfo implements Comparable<ModInfo> {
		File file;
		long modificationDate;
		String hash;
		
		public ModInfo(File file, long modDate, String hash) {
			this.file = file;
			this.modificationDate = modDate;
			this.hash = hash;
		}
		
		public ModInfo(File file, long modDate) {
			this(file, modDate, "");
		}
		
		public String getValidHash() {
			if(hash.isEmpty()) {
				hash = Hex.encodeHexString(calculateHash(file));
			}
			return hash;
		}

		@Override
		public int compareTo(ModInfo o) {
			return file.compareTo(o.file);
		}
	}
	
	public static boolean modsChanged() {
		boolean changed = false;
		
		List<ModInfo> modFiles = findMods();
		
		List<ModInfo> previousModFiles = new ArrayList<>();
		
		List<String> lines = Arrays.asList(Persistence.lastMods.split("\n"));
		File lastFile = null;
		long lastModDate = -1;
		for(String line : lines) {
			if(lastFile == null) {
				lastFile = new File(line);
			} else if(lastModDate == -1){
				lastModDate = Long.parseLong(line);
			} else {
				previousModFiles.add(new ModInfo(lastFile, lastModDate, line));
				lastFile = null;
				lastModDate = -1;
			}
		}
		
		changed = previousModFiles.size() != modFiles.size() ||
				!filesMatch(previousModFiles.stream().sorted().iterator(), modFiles.stream().sorted().iterator());
		
		modFiles.parallelStream().forEach(mf -> mf.getValidHash());
		
		Persistence.lastMods = String.join("\n", modFiles.stream()
				.map(p -> p.file.getPath() + "\n" + p.modificationDate + "\n" + p.hash)
				.collect(Collectors.toList()));
		Persistence.save();
	
		
		return changed;
	}
	
	private static boolean filesMatch(Iterator<ModInfo> aIt, Iterator<ModInfo> bIt) {
		Set<String> modFilesToIgnore = new HashSet<>(
				Arrays.stream(Config.modFilesToIgnore.split(",")).collect(Collectors.toList()));
		
		while(aIt.hasNext()) {
			ModInfo a = aIt.next(), b = bIt.next();
			
			if(!a.file.equals(b.file) || 
					(!modFilesToIgnore.contains(a.file.getName()) 
							&& a.modificationDate != b.modificationDate 
							&& !a.getValidHash().equals(b.getValidHash()))) {
				return false;
			}
		}
		return true;
	}
	
	private static byte[] calculateHash(File f) {
		try(InputStream is = new BufferedInputStream(new FileInputStream(f))){
			return DigestUtils.md5(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new byte[] {};
	}
	
	private static List<ModInfo> findMods(){
		File modsDir = new File(Launch.minecraftHome, "mods");
		File versionedModsDir = new File(modsDir, Loader.MC_VERSION);
		
		List<File> modFiles = new ArrayList<>();
		
		for(File dir : Arrays.asList(modsDir, versionedModsDir)) {
			if(dir.isDirectory()) {
				modFiles.addAll(Arrays.asList(
						modsDir.listFiles(x -> x.getName().endsWith(".jar") 
										|| x.getName().endsWith(".litemod"))));
			}
		}
		
		modFiles.addAll(ModListHelper.additionalMods.values());
		return modFiles.parallelStream()
				.map(f -> new ModInfo(f, f.lastModified()))
				.collect(Collectors.toList());
	}
	
}

package makamys.toomanycrashes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;

import cpw.mods.fml.common.discovery.asm.ASMModParser;

public class JarDiscovererCache {
	
	private static Map<String, CachedModInfo> cache = new HashMap<>();
	private static Map<String, CachedModInfo> dirtyCache = new HashMap<>();
	
	public static CachedModInfo getCachedModInfo(String hash) {
		CachedModInfo cmi = cache.get(hash);
		if(cmi == null) {
			cmi = new CachedModInfo();
			dirtyCache.put(hash, cmi);
		}
		return cmi;
	}
	
	public static class CachedModInfo {
		
		Map<String, ASMModParser> parserMap;
		Set<String> modClasses;
		Set<String> dirtyModClasses;
		
		public CachedModInfo() {
			parserMap = new HashMap<>();
			dirtyModClasses = new HashSet<>();
		}
		
		public ASMModParser getCachedParser(ZipEntry ze) {
			return parserMap.get(ze.getName());
		}
		
		public void putParser(ZipEntry ze, ASMModParser parser) {
			parserMap.put(ze.getName(), parser);
		}
		
		public int getCachedIsModClass(ZipEntry ze) {
			return modClasses == null ? -1 : modClasses.contains(ze.getName()) ? 1 : 0; 
		}
		
		public void putIsModClass(ZipEntry ze, boolean value) {
			if(value) {
				dirtyModClasses.add(ze.getName());
			}
		}
	}
}

package makamys.coretweaks.mixin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.commons.codec.digest.DigestUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.ModContainerFactory;
import cpw.mods.fml.common.discovery.ASMDataTable;
import cpw.mods.fml.common.discovery.JarDiscoverer;
import cpw.mods.fml.common.discovery.ModCandidate;
import cpw.mods.fml.common.discovery.asm.ASMModParser;
import makamys.coretweaks.optimization.JarDiscovererCache;
import makamys.coretweaks.optimization.JarDiscovererCache.CachedModInfo;
import net.minecraft.network.play.INetHandlerPlayClient;

import static makamys.coretweaks.CoreTweaks.LOGGER;

@Mixin(JarDiscoverer.class)
abstract class MixinJarDiscoverer implements INetHandlerPlayClient {
    
	private ZipEntry lastZipEntry;
	
	String lastHash;
	CachedModInfo lastCMI;
	
    @Inject(method = "discover", at = @At("HEAD"), remap = false)
    public void preDiscover(ModCandidate candidate, ASMDataTable table, CallbackInfoReturnable cir) {
		String hash = null;
	    File file = candidate.getModContainer();
		hash = file.getPath() + "@" + file.lastModified();
    	
		lastHash = hash;
    	lastCMI = JarDiscovererCache.getCachedModInfo(lastHash);
    	
    	LOGGER.debug("preDiscover " + candidate.getModContainer() + "(hash " + lastHash + ")");
    }
	
	@Redirect(method = "discover", at = @At(value = "INVOKE", target = "Ljava/util/jar/JarFile;getInputStream(Ljava/util/zip/ZipEntry;)Ljava/io/InputStream;"), remap = false)
    public InputStream redirectGetInputStream(JarFile jf, ZipEntry ze) throws IOException {
        lastZipEntry = ze;
        return jf.getInputStream(ze);
    }
	
	@Redirect(method = "discover", at = @At(value = "NEW", target = "cpw/mods/fml/common/discovery/asm/ASMModParser"), remap = false)
    public ASMModParser redirectNewASMModParser(InputStream stream, ModCandidate candidate, ASMDataTable table) throws IOException {
		ASMModParser parser = lastCMI.getCachedParser(lastZipEntry);
		if(parser == null) {
			parser = new ASMModParser(stream);
			lastCMI.putParser(lastZipEntry, parser);
		}
		return parser;
    }
	
	@Redirect(method = "discover", at = @At(value = "INVOKE", target = "Lcpw/mods/fml/common/ModContainerFactory;build(Lcpw/mods/fml/common/discovery/asm/ASMModParser;Ljava/io/File;Lcpw/mods/fml/common/discovery/ModCandidate;)Lcpw/mods/fml/common/ModContainer;"), remap = false)
    public ModContainer redirectBuild(ModContainerFactory factory, ASMModParser modParser, File modSource, ModCandidate container, ModCandidate candidate, ASMDataTable table) {
		int isModClass = lastCMI.getCachedIsModClass(lastZipEntry);
		ModContainer mc = null;
		if(isModClass != 0) {
			mc = factory.build(modParser, modSource, container);
			if(isModClass == -1) {
				lastCMI.putIsModClass(lastZipEntry, mc != null);
			}
		}
		return mc;
    }
}
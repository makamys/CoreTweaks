package makamys.toomanycrashes.mixin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.ModContainerFactory;
import cpw.mods.fml.common.discovery.ASMDataTable;
import cpw.mods.fml.common.discovery.JarDiscoverer;
import cpw.mods.fml.common.discovery.ModCandidate;
import cpw.mods.fml.common.discovery.asm.ASMModParser;
import net.minecraft.network.play.INetHandlerPlayClient;

@Mixin(JarDiscoverer.class)
abstract class MixinJarDiscoverer implements INetHandlerPlayClient {
    
	private ZipEntry lastZipEntry;
	
	// InputStream java.util.jar.JarFile.getInputStream(ZipEntry ze)
	@Redirect(method = "discover", at = @At(value = "INVOKE", target = "Ljava/util/jar/JarFile;getInputStream(Ljava/util/zip/ZipEntry;)Ljava/io/InputStream;"), remap = false)
    public InputStream redirectGetInputStream(JarFile jf, ZipEntry ze) throws IOException {
        lastZipEntry = ze;
        return jf.getInputStream(ze);
    }
	
	@Redirect(method = "discover", at = @At(value = "NEW", target = "cpw/mods/fml/common/discovery/asm/ASMModParser"), remap = false)
    public ASMModParser redirectNewASMModParser(InputStream stream, ModCandidate candidate, ASMDataTable table) throws IOException {
		System.out.println("creating parser for " + String.valueOf(lastZipEntry));
        return new ASMModParser(stream);
    }
	
	// ModContainer cpw.mods.fml.common.ModContainerFactory.build(ASMModParser modParser, File modSource, ModCandidate container)
	
	@Redirect(method = "discover", at = @At(value = "INVOKE", target = "Lcpw/mods/fml/common/ModContainerFactory;build(Lcpw/mods/fml/common/discovery/asm/ASMModParser;Ljava/io/File;Lcpw/mods/fml/common/discovery/ModCandidate;)Lcpw/mods/fml/common/ModContainer;", args = {"log=true"}), remap = false)
    public ModContainer redirectBuild(ModContainerFactory factory, ASMModParser modParser, File modSource, ModCandidate container, ModCandidate candidate, ASMDataTable table) {
		System.out.println("redirect build");
		return factory.build(modParser, modSource, container);
    }
}
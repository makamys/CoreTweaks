package makamys.coretweaks.mixin.optimization.foldertexturepack;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.client.resources.FolderResourcePack;
import net.minecraft.util.ResourceLocation;

@Mixin(DefaultResourcePack.class)
public class MixinDefaultResourcePack {
	//public InputStream func_152780_c(ResourceLocation p_152780_1_) throws IOException
    @Redirect(method = "func_152780_c(Lnet/minecraft/util/ResourceLocation;)Ljava/io/InputStream;", 
            at = @At(value = "INVOKE", target = "Ljava/io/File;isFile()Z"))
    public boolean redirectIsFile(File file) throws IOException {
        //System.out.println("Running isFile redirector (DefaultResourcePack). file=" + file);
        return file.isFile();
    }
	
}

package makamys.coretweaks.mixin.optimization.foldertexturepack;

import java.io.File;
import java.io.IOException;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.resources.DefaultResourcePack;

@Mixin(DefaultResourcePack.class)
public class MixinDefaultResourcePack {
    
    @Redirect(method = "func_152780_c(Lnet/minecraft/util/ResourceLocation;)Ljava/io/InputStream;", 
            at = @At(value = "INVOKE", target = "Ljava/io/File;isFile()Z", remap = false))
    public boolean redirectIsFile(File file) throws IOException {
        return file.isFile();
    }
	
}

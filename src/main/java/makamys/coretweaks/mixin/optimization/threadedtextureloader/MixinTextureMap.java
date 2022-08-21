package makamys.coretweaks.mixin.optimization.threadedtextureloader;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.imageio.ImageIO;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import makamys.coretweaks.CoreTweaks;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

@Mixin(TextureMap.class)
public abstract class MixinTextureMap {
    @Shadow
    Map mapRegisteredSprites;
	
	@Redirect(method = "loadTextureAtlas(Lnet/minecraft/client/resources/IResourceManager;)V", 
            at = @At(value = "INVOKE", target = "Ljavax/imageio/ImageIO;read(Ljava/io/InputStream;)Ljava/awt/image/BufferedImage;", remap = false))
    public BufferedImage redirectImageIORead(InputStream is) throws IOException {
		if(CoreTweaks.textureLoader.isHooked()) {
	        BufferedImage result = CoreTweaks.textureLoader.fetchLastStreamedResource();
	        return result;
		} else {
			return ImageIO.read(is);
		}
    } 
	
   @Redirect(method = "loadTextureAtlas(Lnet/minecraft/client/resources/IResourceManager;)V", 
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/IResource;getInputStream()Ljava/io/InputStream;"))
    public InputStream redirectGetInputStream(IResource res) throws IOException {
	   
	   if(CoreTweaks.textureLoader.isHooked()) {
	       CoreTweaks.textureLoader.setLastStreamedResource(res);
	        return res.getInputStream();
	   } else {
		   return res.getInputStream();
	   }
    }
	
	@Redirect(method = "loadTextureAtlas(Lnet/minecraft/client/resources/IResourceManager;)V", 
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/IResourceManager;getResource(Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/client/resources/IResource;"))
    public IResource redirectGetResource(IResourceManager resMan, ResourceLocation loc) throws IOException {
		
		if(CoreTweaks.textureLoader.isHooked()) {
			return CoreTweaks.textureLoader.fetchResource(loc);
		} else {
			return resMan.getResource(loc);
		}
    }
}

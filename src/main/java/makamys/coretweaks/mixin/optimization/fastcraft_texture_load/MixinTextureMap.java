package makamys.coretweaks.mixin.optimization.fastcraft_texture_load;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import makamys.coretweaks.CoreTweaks;
import net.minecraft.client.renderer.texture.TextureMap;

@Mixin(TextureMap.class)
abstract class MixinTextureMap {
    
    @Inject(method = "loadTextureAtlas", at = @At(value = "INVOKE", target= "Lnet/minecraft/client/renderer/texture/TextureUtil;allocateTextureImpl(IIIIF)V", shift = At.Shift.AFTER))
    public void preUploadTextures(CallbackInfo ci) {
        System.out.println("preUploadTextures");
        CoreTweaks.boundTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
    }
}
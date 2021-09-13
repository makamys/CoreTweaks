package makamys.coretweaks.mixin.optimization.fastcraft_texture_load;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import makamys.coretweaks.CoreTweaks;
import net.minecraft.client.renderer.texture.TextureMap;

@Mixin(TextureMap.class)
abstract class MixinTextureMap {
    
    @Inject(method = "loadTextureAtlas", at = @At(value = "HEAD"))
    private void preLoadTextureAtlas(CallbackInfo ci) {
        CoreTweaks.isStitching = true;
    }
    
    @Inject(method = "loadTextureAtlas", at = @At(value = "RETURN"))
    private void postLoadTextureAtlas(CallbackInfo ci) {
        CoreTweaks.isStitching = false;
    }
}
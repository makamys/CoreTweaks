package makamys.coretweaks.mixin.optimization.fastcrafttextureload;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import makamys.coretweaks.CoreTweaks;
import net.minecraft.client.renderer.texture.TextureUtil;

@Mixin(TextureUtil.class)
abstract class MixinTextureUtil {
    
    @Inject(method = "bindTexture", at = @At(value = "HEAD"))
    private static void onBindTexture(int texture, CallbackInfo ci) {
        CoreTweaks.boundTexture = texture;
    }
}
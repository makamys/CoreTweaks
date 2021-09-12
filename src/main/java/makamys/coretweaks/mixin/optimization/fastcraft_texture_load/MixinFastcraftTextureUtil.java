package makamys.coretweaks.mixin.optimization.fastcraft_texture_load;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import makamys.coretweaks.CoreTweaks;

@Mixin(value = fastcraft.ab.class, remap = false)
abstract class MixinFastcraftTextureUtil {
    
    @Inject(method = "a([[IIIIIZZ)Z", at = @At(value = "HEAD", args = {"log=true"}))
    private static void preUploadTextures(CallbackInfoReturnable<Boolean> ci) {
        CoreTweaks.preFastcraftA();
    }
}
package makamys.coretweaks.mixin.diagnostics.fmlbarprofiler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import cpw.mods.fml.common.ProgressManager;
import cpw.mods.fml.common.ProgressManager.ProgressBar;
import makamys.coretweaks.diagnostics.FMLBarProfiler;

@Mixin(value = ProgressManager.class, remap = false)
abstract class MixinProgressManager {
    
    @Inject(method = "push(Ljava/lang/String;IZ)Lcpw/mods/fml/common/ProgressManager$ProgressBar;", at = @At(value = "RETURN"))
    private static void onPush(CallbackInfoReturnable<ProgressBar> cir) {
        FMLBarProfiler.instance().onPush(cir.getReturnValue());
    }
    
    @Inject(method = "pop", at = @At(value = "HEAD"))
    private static void onPop(ProgressBar bar, CallbackInfo ci) {
        FMLBarProfiler.instance().onPop(bar);
    }
}
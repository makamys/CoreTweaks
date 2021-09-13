package makamys.coretweaks.mixin.diagnostics.fmlbarprofiler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cpw.mods.fml.common.ProgressManager;
import cpw.mods.fml.common.ProgressManager.ProgressBar;
import makamys.coretweaks.diagnostics.FMLBarProfiler;

@Mixin(ProgressBar.class)
abstract class MixinProgressBar {
    
    @Inject(method = "Lcpw/mods/fml/common/ProgressManager$ProgressBar;step(Ljava/lang/String;)V", at = @At(value = "RETURN"))
    private void onStep(CallbackInfo ci) {
        FMLBarProfiler.instance().onStep((ProgressBar)(Object)this);
    }
}
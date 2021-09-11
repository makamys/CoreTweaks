package makamys.coretweaks.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ProgressManager;

@Mixin(ProgressManager.ProgressBar.class)
abstract class MixinProgressBar {


    @Redirect(method = "Lcpw/mods/fml/common/ProgressManager$ProgressBar;step(Ljava/lang/String;)V", at = @At(value = "INVOKE", target = "Lcpw/mods/fml/common/FMLCommonHandler;processWindowMessages()V"), remap = false)
    public void redirectPWM(FMLCommonHandler cmh) {
        // Do nothing
    }
}
package makamys.coretweaks.mixin.tweak.synctweak;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
abstract class MixinMinecraft {

    @Redirect(method = "Lnet/minecraft/client/Minecraft;runGameLoop()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isFramerateLimitBelowMax()Z"))
    public boolean redirectIsFramelimitBelowMax(Minecraft minecraft) {
    	return false;
    }
    
}

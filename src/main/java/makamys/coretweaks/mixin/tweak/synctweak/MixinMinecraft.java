package makamys.coretweaks.mixin.tweak.synctweak;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
abstract class MixinMinecraft {

    @Redirect(method = "Lnet/minecraft/client/Minecraft;runGameLoop()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isFramerateLimitBelowMax()Z", remap = false))
    public boolean redirectIsFramelimitBelowMax(Minecraft minecraft) {
        return false;
    }
    
}

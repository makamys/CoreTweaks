package makamys.coretweaks.mixin.bugfix.guiclicksound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.util.ResourceLocation;

@Mixin(GuiListExtended.class)
public abstract class MixinGuiListExtended {
    
    @Inject(method = "func_148179_a", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiListExtended;func_148143_b(Z)V"))
    public void playClickSound(CallbackInfoReturnable<Boolean> cir) {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
    }
    
}


package makamys.coretweaks.mixin.tweak.newworldguimaxlength;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiTextField;

@Mixin(GuiCreateWorld.class)
public class MixinGuiCreateWorld {
    
    @Shadow
    private GuiTextField field_146333_g; // world name
    @Shadow
    private GuiTextField field_146335_h; // world seed
    
    @Inject(method = "initGui", at = @At(value = "RETURN"))
    private void postInitGui(CallbackInfo ci) {
        field_146333_g.setMaxStringLength(Integer.MAX_VALUE);
        field_146335_h.setMaxStringLength(Integer.MAX_VALUE);
    }
    
}
package makamys.toomanycrashes.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiMainMenu;


@Mixin(GuiMainMenu.class)
abstract class MixinMainMenu {
    
    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onConstructed(CallbackInfo ci) {
        System.out.println("(MIXIN EXAMPLE) Running injector mixin for main menu!");
    }
    
    
}

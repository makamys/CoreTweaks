package makamys.toomanycrashes.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import makamys.toomanycrashes.GuiFatalErrorScreen;
import makamys.toomanycrashes.TooManyCrashes;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMemoryErrorScreen;
import net.minecraft.world.IBlockAccess;


@Mixin(Minecraft.class)
abstract class MixinMinecraft {
    
    @Shadow
    public abstract void runGameLoop();
    
    @Shadow
    private boolean hasCrashed;
    
    private Throwable theError;
    
    // void net.minecraft.client.Minecraft.runGameLoop()
    @Redirect(method = "Lnet/minecraft/client/Minecraft;run()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;runGameLoop()V"))
    public void run(Minecraft minecraft) {
        try {
            runGameLoop();
        } catch(Throwable e) {
            theError = e;
            TooManyCrashes.handleCrash(e);
        }
        if(hasCrashed) {
            hasCrashed = false;
            TooManyCrashes.handleCrash(null);
        }
    }
    // void net.minecraft.client.Minecraft.displayGuiScreen(GuiScreen p_147108_1_)
    // net.minecraft.client.gui.GuiScreen
    
    @Redirect(method = "Lnet/minecraft/client/Minecraft;run()V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;displayGuiScreen(Lnet/minecraft/client/gui/GuiScreen;)V", args = {"log=true"}))
    public void redirectDisplayGuiScreen(Minecraft minecraft) {
        minecraft.displayGuiScreen(new GuiFatalErrorScreen(theError));
    }
    
    
}

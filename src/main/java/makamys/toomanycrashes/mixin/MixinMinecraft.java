package makamys.toomanycrashes.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.world.IBlockAccess;


@Mixin(Minecraft.class)
abstract class MixinMinecraft {
    
    @Shadow
    public abstract void runGameLoop();
    
    @Shadow
    private boolean hasCrashed;
    
    // void net.minecraft.client.Minecraft.runGameLoop()
    @Redirect(method = "Lnet/minecraft/client/Minecraft;run()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;runGameLoop()V"))
    public void run(Minecraft minecraft) {
        try {
            runGameLoop();
        } catch(Throwable e) {
            throw new OutOfMemoryError("client crashed");
        }
        if(hasCrashed) {
            hasCrashed = false;
            throw new OutOfMemoryError("server crashed");
        }
    }
    
    
}

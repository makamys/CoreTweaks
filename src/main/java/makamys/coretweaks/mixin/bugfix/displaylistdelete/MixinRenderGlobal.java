package makamys.coretweaks.mixin.bugfix.displaylistdelete;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.RenderGlobal;

@Mixin(RenderGlobal.class)
public abstract class MixinRenderGlobal {
    
    /* Backport from 1.12. Fixes display lists getting deleted in Minecraft#freeMemory and never reallocated,
     * which causes graphical artifacts to eventually appear.
     */
    @Inject(method = "deleteAllDisplayLists", at = @At("HEAD"), cancellable = true)
    private void preDeleteAllDisplayLists(CallbackInfo ci) {
        ci.cancel();
    }
}
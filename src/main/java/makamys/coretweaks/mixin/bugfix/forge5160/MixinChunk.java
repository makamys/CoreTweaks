package makamys.coretweaks.mixin.bugfix.forge5160;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.chunk.Chunk;

// TODO: Event bus calls are ignored. Should probably fix. Or not?

@Mixin(Chunk.class)
public class MixinChunk {
    
    // Here we mark chunks dirty when entity is entering or leaving one
    // Yes, there is asymmetry in call convention. removeEntity is actually a delegator to naked implementation.
    @Inject(method = {"addEntity", "removeEntityAtIndex"}, at = @At("RETURN"))
    private void postEntityModification(CallbackInfo ci) {
        ((Chunk)(Object)this).setChunkModified();
    }
    
}

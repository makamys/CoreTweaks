package makamys.coretweaks.mixin.tweak.extendsprint;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.client.entity.EntityPlayerSP;

@Mixin(EntityPlayerSP.class)
abstract class MixinEntityPlayerSP {
    
    @ModifyConstant(method = "setSprinting", constant = @Constant(intValue = 600))
    private int modifySprintTime(int original) {
        // Let's set it to be in the middle in case someone decides to increment it.
        // This value corresponds to 1.7 years.
        return Integer.MAX_VALUE / 2;
    }
    
}

package makamys.coretweaks.mixin.tweak.extendsprint;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import makamys.coretweaks.Config;
import net.minecraft.client.entity.EntityPlayerSP;

@Mixin(EntityPlayerSP.class)
abstract class MixinEntityPlayerSP {
    
    @ModifyConstant(method = "setSprinting", constant = @Constant(intValue = 600))
    private int modifySprintTime(int original) {
        return Config.sprintTimeLimit;
    }
    
}

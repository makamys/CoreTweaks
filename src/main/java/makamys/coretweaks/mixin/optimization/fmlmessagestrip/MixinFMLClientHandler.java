package makamys.coretweaks.mixin.optimization.fmlmessagestrip;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import cpw.mods.fml.client.FMLClientHandler;

@Mixin(FMLClientHandler.class)
abstract class MixinFMLClientHandler {
    /**
     * @author makamys
     * @reason Make startup faster.
     * */
    @Overwrite(remap = false)
    public String stripSpecialChars(String message) {
        return message;
    }
}

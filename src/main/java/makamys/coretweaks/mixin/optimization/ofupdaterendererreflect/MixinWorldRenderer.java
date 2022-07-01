package makamys.coretweaks.mixin.optimization.ofupdaterendererreflect;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

import makamys.coretweaks.util.MCUtil;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.WorldRenderer;

@Mixin(WorldRenderer.class)
abstract class MixinWorldRenderer {

    @Redirect(method = "updateRenderer", at = @At(value = "INVOKE", target = "LReflector;callBoolean(Ljava/lang/Object;LReflectorMethod;[Ljava/lang/Object;)Z"), expect = 0)
    public boolean redirectCallBoolean(Object o, @Coerce Object rm, Object... objects) {
		if(MCUtil.Reflector_ForgeBlock_hasTileEntity.equals(rm)) {
			return ((Block)o).hasTileEntity((int)objects[0]);
		} else if(MCUtil.Reflector_ForgeBlock_canRenderInPass.equals(rm)){
			return ((Block)o).canRenderInPass((int)objects[0]);
		} else {
			throw new IllegalStateException("Invalid callBoolean() call in updateRenderer. Unsupported OptiFine version?");
		}
    }
}
package makamys.coretweaks.mixin.optimization.threadedtextureloader;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

@Mixin(TextureMap.class)
public interface ITextureMap {
    @Accessor("mapRegisteredSprites")
    Map mapRegisteredSprites();
    
    @Invoker
    public ResourceLocation callCompleteResourceLocation(ResourceLocation p_147634_1_, int p_147634_2_);
}
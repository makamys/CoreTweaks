package makamys.coretweaks.optimization;

import java.util.ArrayList;
import java.util.List;

import com.google.common.primitives.Bytes;

import cpw.mods.fml.common.asm.transformers.SideTransformer;
import makamys.coretweaks.optimization.transformerproxy.ITransformerWrapper;
import makamys.coretweaks.optimization.transformerproxy.TransformerProxy;
import makamys.coretweaks.optimization.transformerproxy.TransformerProxyManager;
import makamys.coretweaks.optimization.transformerproxy.TransformerProxyManager.ITransformerWrapperProvider;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.classloading.FluidIdTransformer;

/** Speeds up wildcard transformers (transformers that try to transform every class unconditionally) by scanning
 * classes for a string pattern and skipping the transformation if they don't contain the pattern the transformer
 * would look for.
 */
public class ForgeFastWildcardTransformers implements ITransformerWrapperProvider {
    public static ForgeFastWildcardTransformers instance;
    
    public static final String FLUID_ID_TRANSFORMER_PATTERN = "fluidID";
    public static final String SIDE_TRANSFORMER_PATTERN = "cpw/mods/fml/relauncher/SideOnly";
    
    public ForgeFastWildcardTransformers(){
        TransformerProxyManager.instance.addAdditionListener(this, true);
    }
    
    @Override
    public ITransformerWrapper wrap(IClassTransformer transformer) {
        if(transformer instanceof FluidIdTransformer) {
            return new PatternConditionalTransformerWrapper(FLUID_ID_TRANSFORMER_PATTERN);
        } else if(transformer instanceof SideTransformer) {
            return new PatternConditionalTransformerWrapper(SIDE_TRANSFORMER_PATTERN);
        } else {
            return null;
        }
    }
    
    public static class PatternConditionalTransformerWrapper implements ITransformerWrapper {
        private final List<byte[]> patterns = new ArrayList<>();
        
        public PatternConditionalTransformerWrapper(String... patterns) {
            for(String pattern : patterns) {
                this.patterns.add(pattern.getBytes());
            }
        }
        
        @Override
        public byte[] wrapTransform(String name, String transformedName, byte[] basicClass, TransformerProxy proxy) {
            if(containsAnyPattern(basicClass)) {
                return proxy.invokeNextHandler(name, transformedName, basicClass);
            } else {
                return basicClass;
            }
        }

        private boolean containsAnyPattern(byte[] array) {
            if(array == null) {
                return false;
            }
            for(byte[] pattern : patterns) {
                if(Bytes.indexOf(array, pattern) != -1) {
                    return true;
                }
            }
            return false;
        }
    }
}

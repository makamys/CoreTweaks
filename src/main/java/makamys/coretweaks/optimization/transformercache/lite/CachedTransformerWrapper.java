package makamys.coretweaks.optimization.transformercache.lite;

import makamys.coretweaks.optimization.transformerproxy.TransformerProxy;
import makamys.coretweaks.optimization.transformerproxy.ITransformerWrapper;
import net.minecraft.launchwrapper.IClassTransformer;

public class CachedTransformerWrapper implements ITransformerWrapper {
    public int runs = 0;
    public int misses = 0;
    
    public CachedTransformerWrapper(IClassTransformer original) {
        this.transformerName = original.getClass().getCanonicalName();
    }
    
    private String transformerName;
    
    @Override
    public byte[] wrapTransform(String name, String transformedName, byte[] basicClass, TransformerProxy proxy) {
        runs++;
        byte[] result = TransformerCache.instance.getCached(transformerName, name, transformedName, basicClass);
        if(result == null) {
            misses++;
            TransformerCache.instance.prePutCached(transformerName, name, transformedName, basicClass);
            result = proxy.invokeNextHandler(name, transformedName, basicClass);
            if(!TransformerCache.instance.putCached(transformerName, name, transformedName, result)) {
                return basicClass;
            }
        }
        return TransformerCache.fromNullableByteArray(result);
    }
}

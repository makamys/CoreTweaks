package makamys.coretweaks.optimization.transformerproxy;

public interface ITransformerWrapper {
    byte[] wrapTransform(String name, String transformedName, byte[] basicClass, TransformerProxy proxy);
}

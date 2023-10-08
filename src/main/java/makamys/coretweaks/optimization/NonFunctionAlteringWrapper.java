package makamys.coretweaks.optimization;

/** A wrapper object that pretends to be the wrapped object without altering its functionality. */
public interface NonFunctionAlteringWrapper<T> {
    public T getOriginal();
}

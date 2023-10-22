package makamys.coretweaks.api;

/** An object that wraps another object, and is the same type as it. */
public interface IWrapper<T> {
    public T getOriginal();
}

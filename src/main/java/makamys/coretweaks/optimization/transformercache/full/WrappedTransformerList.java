package makamys.coretweaks.optimization.transformercache.full;

import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import net.minecraft.launchwrapper.LaunchClassLoader;

/**
 * The purpose of this class is to be wrapped around the LaunchClassLoader's transformer list.
 * It delegates all operations to the original list, save for one: iterator() (which
 * LaunchClassLoader uses to iterate over the transformer chain):
 * <ul>
 * 	<li> returns an iterator to a list which only contains {@link CachingTransformer} normally</li>
 * 	<li> returns an iterator to the original list inside exceptional transformers that require 
 *       iteration over the real transformer chain</li>
 * </ul>
 */	

public class WrappedTransformerList<T> implements List<T> {
    public List<T> original;
    
    public T alt;
    
    public WrappedTransformerList(List<T> original){
        this.original = original;
    }
    @Override
    public boolean add(T e) {
        return original.add(e);
    }

    @Override
    public void add(int index, T element) {
        original.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return addAll(size(), c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
    	return original.addAll(c);
    }

    @Override
    public void clear() {
        original.clear();
    }

    @Override
    public boolean contains(Object o) {
    	return original.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return original.containsAll(c);
    }

    @Override
    public T get(int index) throws IndexOutOfBoundsException {
		return original.get(index);
    }

    @Override
    public int indexOf(Object o) {
		return original.indexOf(o);
    }

    @Override
    public boolean isEmpty() {
		return original.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
    	return listIterator();
    }

    @Override
    public int lastIndexOf(Object o) {
        return original.lastIndexOf(o); 
    }

    @Override
    public ListIterator<T> listIterator() {
    	return listIterator(0);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        boolean first = true;
        
        if(CachingTransformer.DEBUG_PRINT) {
	        for(StackTraceElement e : Thread.currentThread().getStackTrace()) {
	            if(!first && !e.getClassName().equals(getClass().getName())) {
	                if(!e.getClassName().equals(LaunchClassLoader.class.getName())) {
	                    
	                    LOGGER.info("iterator called by " + String.join(" > ", Arrays.stream(Thread.currentThread().getStackTrace()).map(x -> x.getClassName()).collect(Collectors.toList())));
	                }
	                break;
	            }
	            first = false;
	        }
        }
        
    	if(alt == null) {
    		return original.listIterator(index);
    	} else {
    		List<T> list = new ArrayList<T>(1);
    		list.add(alt);
    		return list.listIterator(index);
    	}
    }

    @Override
    public boolean remove(Object o) {
		return original.remove(o);
    }

    @Override
    public T remove(int index) {
		return original.remove(index);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
		return original.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
		return original.retainAll(c);
    }

    @Override
    public T set(int index, T element) {
		return original.set(index, element);
    }

    @Override
    public int size() {
    	return original.size();
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return original.subList(fromIndex, toIndex);
    }

    @Override
    public Object[] toArray() {
		return original.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
		return original.toArray(a);
    }
}

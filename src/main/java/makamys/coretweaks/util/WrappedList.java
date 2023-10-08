package makamys.coretweaks.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class WrappedList<T> implements List<T> {
    protected final List<T> o;
    public WrappedList(List<T> original) {
        this.o = original;
    }
    
    @Override
    public int size() {
        return o.size();
    }

    @Override
    public boolean isEmpty() {
        return o.isEmpty();
    }

    @Override
    public boolean contains(Object e) {
        return o.contains(e);
    }

    @Override
    public Iterator<T> iterator() {
        return o.iterator();
    }

    @Override
    public Object[] toArray() {
        return o.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return o.toArray(a);
    }

    @Override
    public boolean add(T e) {
        return o.add(e);
    }

    @Override
    public boolean remove(Object e) {
        return o.remove(e);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return o.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return o.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return o.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return o.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return o.retainAll(c);
    }

    @Override
    public void clear() {
        o.clear();
    }

    @Override
    public T get(int index) {
        return o.get(index);
    }

    @Override
    public T set(int index, T element) {
        return o.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        o.add(index, element);
    }

    @Override
    public T remove(int index) {
        return o.remove(index);
    }

    @Override
    public int indexOf(Object e) {
        return o.indexOf(e);
    }

    @Override
    public int lastIndexOf(Object e) {
        return o.lastIndexOf(e);
    }

    @Override
    public ListIterator<T> listIterator() {
        return o.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return o.listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return o.subList(fromIndex, toIndex);
    }
}

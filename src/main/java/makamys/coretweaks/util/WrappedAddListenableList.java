package makamys.coretweaks.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WrappedAddListenableList<T> extends WrappedList<T> {
    public static class AdditionEvent<T> {
        public T element;
        public List<T> list;
        public int index;
        boolean isCanceled;
    }
    
    public static interface AdditionEventListener<T> {
        public void onAdd(AdditionEvent<T> event);
    }
    
    private final List<AdditionEventListener<T>> listeners = new ArrayList<>();
    
    public WrappedAddListenableList(List<T> original) {
        super(original);
    }
    
    public void addListener(AdditionEventListener<T> l) {
        listeners.add(l);
    }
    
    public void removeListener(AdditionEventListener<T> l) {
        listeners.remove(l);
    }
    
    @Override
    public void add(int index, T element) {
        AdditionEvent<T> event = new AdditionEvent<>();
        event.list = o;
        event.element = element;
        event.index = index;
        for(AdditionEventListener l : listeners) {
            l.onAdd(event);
        }
        if(!event.isCanceled) {
            super.add(event.index, event.element);
        }
    }
    
    @Override
    public boolean add(T element) {
        AdditionEvent<T> event = new AdditionEvent<>();
        event.list = o;
        event.element = element;
        event.index = -1;
        for(AdditionEventListener l : listeners) {
            l.onAdd(event);
        }
        if(!event.isCanceled) {
            return super.add(event.element);
        }
        return false;
    }
    
    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }
}

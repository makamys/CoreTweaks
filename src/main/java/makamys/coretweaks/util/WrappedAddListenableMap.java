package makamys.coretweaks.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WrappedAddListenableMap<K, V> implements Map<K, V> {
    
	public static interface MapAddListener<K, V> {
		boolean onPut(Map<K, V> delegateMap, K key, V value);
	}
	
    private Map<K, V> o;
    
    private List<MapAddListener<K, V>> listeners = new ArrayList<>();
    
    public WrappedAddListenableMap(Map<K, V> original) {
        this.o = original;
    }
    
    @Override
    public void clear() {
        o.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return o.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return o.containsValue(value);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return o.entrySet();
    }

    @Override
    public V get(Object key) {
        return o.get(key);
    }

    @Override
    public boolean isEmpty() {
        return o.isEmpty();
    }

    @Override
    public Set<K> keySet() {
        return o.keySet();
    }

    @Override
    public V put(K key, V value) {
    	boolean blocked = false;
    	for(MapAddListener<K, V> l : listeners) {
    		if(!l.onPut(o, key, value)) {
    			blocked = true;
    			break;
    		}
    	}
    	
		return blocked ? null : o.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        o.putAll(m);
    }

    @Override
    public V remove(Object key) {
        return o.remove(key);
    }

    @Override
    public int size() {
        return o.size();
    }

    @Override
    public Collection<V> values() {
        return o.values();
    }
    
    public void addListener(MapAddListener<K, V> l) {
    	listeners.add(l);
    }
    
    public void removeListener(MapAddListener<K, V> l) {
    	listeners.remove(l);
    }
    
}

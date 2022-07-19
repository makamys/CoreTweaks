package makamys.coretweaks.optimization;

import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import makamys.coretweaks.mixin.optimization.threadedtextureloader.ITextureMap;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;

/**
 * Class used to parallelize the I/O operations of loadTextureAtlas. May be useless. 
 */
public class ThreadedTextureLoader {
	
	static class ResourceLoadJob {
		Optional<IResource> resource = Optional.empty();
		Optional<ResourceLocation> resourceLocation = Optional.empty();
		
		public ResourceLoadJob(IResource res) {
			this.resource = Optional.of(res);
		}
		
		public ResourceLoadJob(ResourceLocation resLoc) {
			this.resourceLocation = Optional.of(resLoc);
		}
		
		public static ResourceLoadJob of(Object object){
			if(object instanceof IResource) {
				return new ResourceLoadJob((IResource)object);
			} else if(object instanceof ResourceLocation) {
				return new ResourceLoadJob((ResourceLocation)object);
			} else {
				return null; // uh oh
			}
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof ResourceLoadJob) {
				ResourceLoadJob o = (ResourceLoadJob)obj;
				return Objects.equals(resource, o.resource) && Objects.equals(resourceLocation, o.resourceLocation);
			} else {
				return false;
			}
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(resource, resourceLocation);
		}
	}
	
	/**
	 * Contains an object, or an exception that was encountered when trying to
	 * construct the object.
	 */
	static class Failable<T, E extends Exception> {
		private Optional<T> thing = null;
		private E exception = null;
		
		public Failable(T thing) {
			this.thing = Optional.ofNullable(thing);
		}
		
		public Failable(E e) {
			this.exception = e;
		}
		
		public T get() {
			if(thing == null) {
				throw new NoSuchElementException();
			} else {
				return thing.orElse(null);
			}
		}
		
		public T getOrThrow() throws E {
			if(exception != null) {
				throw exception;
			} else {
				return get();
			}
		}
		
		public boolean present() {
			return thing != null;
		}
		
		public boolean failed() {
			return exception != null;
		}
		
		public Exception getException() {
			return exception;
		}
		
		public static <T, E extends Exception> Failable<T, E> of(T thing){
			return new Failable<T, E>(thing);
		}
		
		public static <T, E extends Exception> Failable<T, E> failed(E e){
			return new Failable<T, E>(e);
		}
	}
	
	List<TextureLoaderThread> threads = new ArrayList<>();
    protected LinkedBlockingQueue<ResourceLoadJob> queue = new LinkedBlockingQueue<>();
    protected ConcurrentHashMap<ResourceLocation, Failable<IResource, IOException>> resMap = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<IResource, Failable<BufferedImage, IOException>> map = new ConcurrentHashMap<>();
    
    protected IResource lastStreamedResource;
    public static final List<ResourceLoadJob> waitingOn = new ArrayList<>(1);
    
    private boolean hooked; // you'll be hooked on the brothers!
    
    public ThreadedTextureLoader(int numThreads) {
    	initThreads(numThreads);
    	LOGGER.info("Initialized threaded texture loader with " + numThreads + " threads.");
    }
    
    private void initThreads(int numThreads) {
        for(int i = 0; i < numThreads; i++) {
            threads.add(new TextureLoaderThread(this, i));
        }
        
        for(TextureLoaderThread t: threads) t.start();
    }
	
    public void setLastStreamedResource(IResource res) {
        lastStreamedResource = res;
    }
    
    public void addSpriteLoadJobs(Map mapRegisteredSprites, ITextureMap itx) {
        Iterator<Entry> iterator = itx.mapRegisteredSprites().entrySet().iterator();
        
        while(iterator.hasNext()) {
            try {
                Entry entry = iterator.next();
                
                ResourceLocation resLoc = new ResourceLocation((String)entry.getKey());
                resLoc = itx.callCompleteResourceLocation(resLoc, 0);
                
                // if we reuse resources, this happens:
                // Unexpected end of ZLIB input stream
                resMap.clear();
                
                if(!map.containsKey(resLoc)) {
                	queue.add(new ResourceLoadJob(resLoc));
                }
            } catch(Exception e) {}
        }
    }
    
    public BufferedImage fetchLastStreamedResource() throws IOException {
        return fetchFromMap(map, lastStreamedResource).getOrThrow();
    }
    
    public IResource fetchResource(ResourceLocation loc) throws IOException {
    	return fetchFromMap(resMap, loc).getOrThrow();
    }
    
    public <K, V> V fetchFromMap(Map<K, V> map, K key){
    	while(true) {
    		synchronized(waitingOn) {
    			if(map.containsKey(key)) {
    				break;
    			} else {
    				//LOGGER.info(lastStreamedResource + " hasn't been loaded yet, waiting...");
    	            waitingOn.add(ResourceLoadJob.of(key));
    	            if(waitingOn.size() > 1) throw new IllegalStateException();
    	            
	                queue.add(ResourceLoadJob.of(key));
	                try {
	                    waitingOn.wait();
	                } catch (InterruptedException e) {
	                    
	                }
    	            //LOGGER.info("Woke up on " + lastStreamedResource);
    			}
    		}
            
        }
        waitingOn.clear();
        //LOGGER.info("Returning " + lastStreamedResource + " fetched by thread");
        return map.get(key);
    }
    
    @SubscribeEvent
    public void onTextureStitchPre(TextureStitchEvent.Pre pre) {
        hooked = !skipFirst();
        if(hooked) {
            addSpriteLoadJobs(((ITextureMap)pre.map).mapRegisteredSprites(), ((ITextureMap)pre.map));
        }
    }
    
    @SubscribeEvent
    public void onTextureStitchPost(TextureStitchEvent.Post post) {
        hooked = false;
        resMap.clear();
        map.clear();
        queue.clear();
        lastStreamedResource = null;
        waitingOn.clear();
    }
    
    // Forge adds this, I think
    private boolean skipFirst() {
        try {
            Field skipFirstField = TextureMap.class.getDeclaredField("skipFirst");
            
            skipFirstField.setAccessible(true);
            
            return (boolean)skipFirstField.get(this);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean isHooked() {
    	return hooked;
    }
}

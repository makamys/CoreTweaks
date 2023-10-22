package makamys.coretweaks.optimization.transformerproxy;

import static makamys.coretweaks.CoreTweaks.LOGGER;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import makamys.coretweaks.api.IWrapper;
import makamys.coretweaks.util.PluralUtil;
import makamys.coretweaks.util.WrappedAddListenableList;
import makamys.coretweaks.util.WrappedAddListenableList.AdditionEvent;
import makamys.coretweaks.util.WrappedAddListenableList.AdditionEventListener;
import net.minecraft.launchwrapper.IClassNameTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

/** Replaces {@link LaunchClassLoader#transformers} with a {@link WrappedAddListenableList} that intercepts element additions and may insert {@link TransformerProxy} instances wrapping the transformer instead. */
public class TransformerProxyManager implements AdditionEventListener<IClassTransformer> {
    public static TransformerProxyManager instance = new TransformerProxyManager();
    
    private boolean installedListener;
    private List<ITransformerWrapperProvider> listeners = new ArrayList<>();
    
    public void addAdditionListener(ITransformerWrapperProvider listener, boolean installListener) {
        listeners.add(0, listener);
        hookClassLoader(installListener);
    }
    
    public void hookClassLoader(boolean installListener) {
        try {
            LaunchClassLoader lcl = (LaunchClassLoader)Launch.classLoader;
            
            Field transformersField = LaunchClassLoader.class.getDeclaredField("transformers");
            transformersField.setAccessible(true);
            List<IClassTransformer> transformers = (List<IClassTransformer>)transformersField.get(lcl);
            
            // replace existing elements
            for(int i = 0; i < transformers.size(); i++) {
                IClassTransformer proxy = createCachedProxy(transformers.get(i), true);
                if(proxy != null) {
                    transformers.set(i, proxy);
                }
            }
            
            // install listener for new elements
            if(installListener && !installedListener) {
                WrappedAddListenableList<IClassTransformer> wrappedTransformers = 
                        new WrappedAddListenableList<IClassTransformer>(transformers);
                wrappedTransformers.addListener(this);
                
                transformersField.set(lcl, wrappedTransformers);
                installedListener = true;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private IClassTransformer createCachedProxy(IClassTransformer transformer, boolean onlyFirst) {
        IClassTransformer realTransformer = transformer;
        while(realTransformer instanceof IWrapper<?>) {
            realTransformer = ((IWrapper<IClassTransformer>)realTransformer).getOriginal();
        }
        List<ITransformerWrapper> wrappers = new ArrayList<>();
        for(ITransformerWrapperProvider l : (onlyFirst ? Collections.singletonList(listeners.get(0)) : listeners)) {
            ITransformerWrapper wrapper = l.wrap(realTransformer);
            if(wrapper != null) {
                wrappers.add(wrapper);
            }
        }
        if(!wrappers.isEmpty()) {
            try {
                LOGGER.info("Replacing " + transformer.getClass().getCanonicalName() + " with " + PluralUtil.pluralizeCount(wrappers.size(), "wrapper") + ": " + wrappers.stream().map(x -> x.getClass().getSimpleName()).collect(Collectors.toList()));
                TransformerProxy newTransformer = transformer instanceof TransformerProxy ? (TransformerProxy)transformer
                        : transformer instanceof IClassNameTransformer
                        ? NameTransformerProxy.of(transformer) : TransformerProxy.of(transformer);
                for(ITransformerWrapper wrapper : Lists.reverse(wrappers)) {
                    newTransformer.addWrapper(wrapper);
                }
                return transformer instanceof TransformerProxy ? null : newTransformer;
            } catch(Exception e) {
                LOGGER.error("Failed to create proxy class for " + realTransformer.getClass().getCanonicalName());
                e.printStackTrace();
            }
        }
        return null;
    }
    
    @Override
    public void onAdd(AdditionEvent<IClassTransformer> event) {
        IClassTransformer proxy = createCachedProxy(event.element, false);
        if(proxy != null) {
            event.element = proxy;
        }
    }
    
    public static interface ITransformerWrapperProvider {
        public ITransformerWrapper wrap(IClassTransformer transformer);
    }
}

package makamys.coretweaks.asm.itaros.asmutils;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Arrays;
import java.util.List;

public class SimpleNameResolver {

    public SimpleNameResolver(ImmutablePair<String, String>[] pairs) {
        _pairs = Arrays.asList(pairs);
    }

    public enum NamingPolicy {
        PKG,
        SRG
    }

    public void setNamingPolicy(NamingPolicy policy) {
        _policy = policy;
    }

    public String getName(String pkgName) throws NameResolutionException {
        return getName(pkgName, _policy);
    }

    public String getName(String pkgName, NamingPolicy policy) throws NameResolutionException {
        ImmutablePair<String, String> found = null;
        for (ImmutablePair<String, String> pair :
                _pairs) {
            if (pair.getLeft().equals(pkgName)) {
                found = pair;
                break;
            }
        }
        if (found == null)//TODO: Introduce special exception type
            throw new NameResolutionException(pkgName);
        return policy == NamingPolicy.PKG ? found.getLeft() : found.getRight();
    }

    private final List<ImmutablePair<String, String>> _pairs;
    private NamingPolicy _policy = NamingPolicy.PKG;

}

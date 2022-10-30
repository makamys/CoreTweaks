package makamys.coretweaks.asm.itaros.asmutils;

public class NameResolutionException extends RuntimeException {

    public NameResolutionException(String originalName){
        super(getMessage(originalName));
    }

    private static String getMessage(String originalName) {
        return "Name resolution for ASM has failed for: "+originalName;
    }

    public NameResolutionException(String originalName, Throwable cause){
        super(getMessage(originalName), cause);
    }

}

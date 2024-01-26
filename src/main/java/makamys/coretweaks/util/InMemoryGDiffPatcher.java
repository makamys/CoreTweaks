package makamys.coretweaks.util;

/** nothome's GDiffPatcher is implemented in a horribly inefficient way where it
copies bytes one by one. This can be mitigated by providing a custom
SeekableSource implementation (~53% faster), but reimplementing the algorithm
from scratch using arrays is even better (~150% faster), which is what this
class does. */
public class InMemoryGDiffPatcher {
    private static final int BYTE = 1, UBYTE = 2, SHORT = 3, USHORT = 4, INT = 5, UINT = 6, LONG = 7, ULONG = 8;
    
    private static final int[] TYPE_LENGTHS = new int[] {0, 1, 1, 2, 2, 4, 4, 8, 8};
    private static final int[] COPY_POSITION_TYPES = new int[] {USHORT, USHORT, USHORT, INT, INT, INT, LONG};
    private static final int[] COPY_LENGTH_TYPES = new int[] {UBYTE, USHORT, INT, UBYTE, USHORT, INT, INT};
    
    public static void patch(byte[] source, byte[] patch, byte[] dest) {
        if(!(patch[0] == (byte)0xD1 && patch[1] == (byte)0xFF && patch[2] == (byte)0xD1 && patch[3] == (byte)0xFF)) {
            throw new IllegalArgumentException("wrong header");
        }
        if(patch[4] != 4) {
            throw new IllegalArgumentException("unsupported gdiff version: " + patch[5]);
        }
        
        int patchPos = 5;
        int destPos = 0;
        while(patchPos < patch.length) {
            int cmd = Byte.toUnsignedInt(patch[patchPos]);
            if(cmd == 0) { // eof
                break;
            } else if(cmd >= 1 && cmd <= 248) { // data
                int lengthType = cmd < 247 ? 0 : cmd == 247 ? USHORT : INT;
                int lengthTypeLength = TYPE_LENGTHS[lengthType];
                int length = lengthType == 0 ? cmd : readValue(patch, patchPos + 1, lengthType);
                
                System.arraycopy(patch, patchPos + 1 + lengthTypeLength, dest, destPos, length);
                patchPos += 1 + lengthTypeLength + length;
                destPos += length;
            } else if(cmd >= 249 && cmd <= 254) { // copy
                int positionType = COPY_POSITION_TYPES[cmd - 249];
                int positionTypeLength = TYPE_LENGTHS[positionType];
                int lengthType = COPY_LENGTH_TYPES[cmd - 249];
                int lengthTypeLength = TYPE_LENGTHS[lengthType];
                int position = readValue(patch, patchPos + 1, positionType);
                int length = readValue(patch, patchPos + 1 + positionTypeLength, lengthType);
                System.arraycopy(source, position, dest, destPos, length);
                patchPos += 1 + positionTypeLength + lengthTypeLength;
                destPos += length;
            } else {
                throw new UnsupportedOperationException("long operand is not supported");
            }
        }
    }

    private static int readValue(byte[] array, int offset, int type) {
        switch(type) {
        case UBYTE:
            return Byte.toUnsignedInt(array[offset]);
        case USHORT:
            return (Byte.toUnsignedInt(array[offset]) << 8) | (Byte.toUnsignedInt(array[offset + 1]));
        case INT:
            return (Byte.toUnsignedInt(array[offset]) << 24) | (Byte.toUnsignedInt(array[offset + 1]) << 16) | (Byte.toUnsignedInt(array[offset + 2]) << 8) | (Byte.toUnsignedInt(array[offset + 3]) << 0);
        default:
            throw new UnsupportedOperationException("unimplemented type: " + type);
        }
    }
}

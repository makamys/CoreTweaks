/*
 * ByteArraySeekableSource.java, modified to read more efficiently
 * 
 * The original license is reproduced below:
 *
 * Created on May 17, 2006, 12:41 PM
 * Copyright (c) 2006 Heiko Klein
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 *
 */

package makamys.coretweaks.util;

import java.io.IOException;
import java.nio.ByteBuffer;

import cpw.mods.fml.repackage.com.nothome.delta.SeekableSource;

/**
 * Wraps a byte buffer as a source
 */
public class FastByteBufferSeekableSource implements SeekableSource {
    
    private ByteBuffer bb;
    private ByteBuffer cur;
    
    /**
     * Constructs a new ByteArraySeekableSource.
     */
    public FastByteBufferSeekableSource(byte[] source) {
        this(ByteBuffer.wrap(source));
    }
    
    /**
     * Constructs a new ByteArraySeekableSource.
     */
    public FastByteBufferSeekableSource(ByteBuffer bb) {
        if (bb == null)
            throw new NullPointerException("bb");
        this.bb = bb;
        bb.rewind();
        try {
            seek(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void seek(long pos) throws IOException {
        cur = bb.slice();
        if (pos > cur.limit())
            throw new IOException("pos " + pos + " cannot seek " + cur.limit());
        cur.position((int) pos);
    }
    
    @Override
    public int read(ByteBuffer dest) throws IOException {
        if (!cur.hasRemaining())
            return -1;
        int c = Math.min(cur.remaining(), dest.remaining());
        int oldLim = cur.limit();
        cur.limit(cur.position() + c);
        dest.put(cur);
        cur.limit(oldLim);
        return c;
    }
    
    @Override
    public void close() throws IOException {
        bb = null;
        cur = null;
    }

    /**
     * Returns a debug <code>String</code>.
     */
    @Override
    public String toString()
    {
        return "BBSeekable" +
            " bb=" + this.bb.position() + "-" + bb.limit() +
            " cur=" + this.cur.position() + "-" + cur.limit() +
            "";
    }
    
}
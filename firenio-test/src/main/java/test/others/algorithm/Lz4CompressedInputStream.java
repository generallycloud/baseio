package test.others.algorithm;

import java.io.IOException;
import java.io.InputStream;

import com.firenio.buffer.ByteBuf;
import com.firenio.common.ByteUtil;

/**
 * @author wangkai
 */
public class Lz4CompressedInputStream extends InputStream {

    private ByteBuf buf;

    private boolean hasRemaining = true;

    private InputStream inputStream;

    public Lz4CompressedInputStream(InputStream inputStream, int bufSize) {
        this.inputStream = inputStream;
        this.buf = ByteBuf.heap(bufSize);
        this.buf.writeIndex(0);
    }

    public static int read(ByteBuf dst, InputStream src) throws IOException {
        return read(dst, src, dst.capacity());
    }

    public static int read(ByteBuf dst, InputStream src, long limit) throws IOException {
        byte[] array = dst.array();
        if (!dst.hasReadableBytes()) {
            int read = (int) Math.min(limit, dst.capacity());
            int len  = src.read(array, 0, read);
            if (len > 0) {
                dst.readIndex(0);
                dst.writeIndex(len);
            }
            return len;
        }
        int remaining = dst.readableBytes();
        System.arraycopy(array, dst.readIndex(), array, 0, remaining);
        dst.readIndex(0);
        dst.writeIndex(remaining);
        int read = (int) Math.min(limit, dst.capacity() - remaining);
        int len  = src.read(array, remaining, read);
        if (len > 0) {
            dst.writeIndex(remaining + len);
        }
        return len;
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    @Override
    public int read() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        ByteBuf buf        = this.buf;
        byte[]  readBuffer = buf.array();
        int     limit      = buf.writeIndex();
        int     offset     = buf.readIndex();
        if (buf.readableBytes() <= 4) {
            if (!hasRemaining) {
                return -1;
            }
            read(buf);
            return read(b, off, len);
        }
        int _len = ByteUtil.getInt(readBuffer, offset);
        offset += 4;
        if (limit - offset < _len) {
            if (!hasRemaining) {
                return -1;
            }
            read(buf);
            return read(b, off, len);
        }
        buf.readIndex(offset + _len);
        return Lz4RawDecompressor.decompress(readBuffer, offset, _len, b, off, len);
    }

    private int read(ByteBuf buf) throws IOException {
        int len = read(buf, inputStream);
        if (len == -1) {
            hasRemaining = false;
        }
        return len;
    }

}

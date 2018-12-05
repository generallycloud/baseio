/*
 * Copyright 2015 The Baseio Project
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.generallycloud.baseio.buffer;

import java.nio.ByteBuffer;

import com.generallycloud.baseio.common.ByteUtil;

public abstract class AbstractHeapByteBuf extends AbstractByteBuf {

    protected int        limit;
    protected int        markPos;
    protected byte[]     memory;
    protected ByteBuffer nioBuffer;
    protected int        position;

    public AbstractHeapByteBuf(ByteBufAllocator allocator, byte[] memory) {
        super(allocator);
        this.memory = memory;
    }

    @Override
    public int absLimit() {
        return limit;
    }

    @Override
    public int absPos() {
        return position;
    }

    @Override
    public byte[] array() {
        return memory;
    }

    @Override
    public ByteBuf clear() {
        this.position = offset;
        this.limit = ix(capacity);
        return this;
    }

    @Override
    public ByteBuf flip() {
        this.limit = position;
        this.position = offset;
        return this;
    }

    @Override
    public int forEachByte(int index, int length, ByteProcessor processor) {
        byte[] array = memory;
        int start = ix(index);
        int end = start + length;
        try {
            for (int i = start; i < end; i++) {
                if (!processor.process(array[i])) {
                    return i - start;
                }
            }
        } catch (Exception e) {}
        return -1;
    }

    @Override
    public int forEachByteDesc(int index, int length, ByteProcessor processor) {
        byte[] array = memory;
        int start = ix(index);
        int end = start + length;
        try {
            for (int i = end; i >= start; i--) {
                if (!processor.process(array[i])) {
                    return i - start;
                }
            }
        } catch (Exception e) {}
        return -1;
    }

    @Override
    public void get(byte[] dst, int offset, int length) {
        System.arraycopy(memory, position, dst, offset, length);
        this.position += length;
    }

    @Override
    public byte getByte() {
        return memory[position++];
    }

    @Override
    public byte getByte(int index) {
        return memory[ix(index)];
    }

    @Override
    public int getInt() {
        int v = ByteUtil.byte2Int(memory, position);
        this.position += 4;
        return v;
    }

    @Override
    public int getInt(int index) {
        return ByteUtil.byte2Int(memory, ix(index));
    }

    @Override
    public int getIntLE() {
        int v = ByteUtil.byte2IntLE(memory, position);
        this.position += 4;
        return v;
    }

    @Override
    public int getIntLE(int offset) {
        return ByteUtil.byte2IntLE(memory, ix(offset));
    }

    @Override
    public long getLong() {
        long v = ByteUtil.byte2Long(memory, position);
        this.position += 8;
        return v;
    }

    @Override
    public long getLong(int index) {
        return ByteUtil.byte2Long(memory, ix(index));
    }

    @Override
    public long getLongLE() {
        long v = ByteUtil.byte2LongLE(memory, position);
        this.position += 8;
        return v;
    }

    @Override
    public long getLongLE(int index) {
        return ByteUtil.byte2LongLE(memory, ix(index));
    }

    @Override
    public ByteBuffer getNioBuffer() {
        if (nioBuffer == null) {
            nioBuffer = ByteBuffer.wrap(memory);
        }
        return nioBuffer;
    }

    @Override
    public short getShort() {
        short v = ByteUtil.byte2Short(memory, position);
        this.position += 2;
        return v;
    }

    @Override
    public short getShort(int index) {
        return ByteUtil.byte2Short(memory, ix(index));
    }

    @Override
    public short getShortLE() {
        short v = ByteUtil.byte2ShortLE(memory, position);
        this.position += 2;
        return v;
    }

    @Override
    public short getShortLE(int index) {
        return ByteUtil.byte2ShortLE(memory, ix(index));
    }

    @Override
    public short getUnsignedByte() {
        return (short) (getByte() & 0xff);
    }

    @Override
    public short getUnsignedByte(int index) {
        return (short) (getByte(index) & 0xff);
    }

    @Override
    public long getUnsignedInt() {
        long v = ByteUtil.byte2UnsignedInt(memory, position);
        this.position += 4;
        return v;
    }

    @Override
    public long getUnsignedInt(int index) {
        return ByteUtil.byte2UnsignedInt(memory, ix(index));
    }

    @Override
    public long getUnsignedIntLE() {
        long v = ByteUtil.byte2UnsignedIntLE(memory, position);
        this.position += 4;
        return v;
    }

    @Override
    public long getUnsignedIntLE(int index) {
        return ByteUtil.byte2UnsignedIntLE(memory, ix(index));
    }

    @Override
    public int getUnsignedShort() {
        int v = ByteUtil.byte2UnsignedShort(memory, position);
        this.position += 2;
        return v;
    }

    @Override
    public int getUnsignedShort(int index) {
        return ByteUtil.byte2UnsignedShort(memory, ix(index));
    }

    @Override
    public int getUnsignedShortLE() {
        int v = ByteUtil.byte2UnsignedShortLE(memory, position);
        this.position += 2;
        return v;
    }

    @Override
    public int getUnsignedShortLE(int index) {
        return ByteUtil.byte2UnsignedShortLE(memory, ix(index));
    }

    @Override
    public boolean hasArray() {
        return true;
    }

    @Override
    public boolean hasRemaining() {
        return position < limit;
    }

    @Override
    public int indexOf(int pos, byte b) {
        int p = ix(pos);
        int l = limit;
        byte[] m = memory;
        for (; p < l; p++) {
            if (m[p] == b) {
                return p;
            }
        }
        return -1;
    }
    
    @Override
    public int lastIndexOf(byte b) {
        int p = limit;
        int l = position - 1;
        byte[] m = memory;
        for (; p > l; p--) {
            if (m[p] == b) {
                return p;
            }
        }
        return -1;
    }

    @Override
    public int limit() {
        return limit - offset;
    }

    @Override
    public ByteBuf limit(int limit) {
        this.limit = ix(limit);
        return this;
    }

    @Override
    public ByteBuf markL() {
        markLimit = limit;
        return this;
    }

    @Override
    public ByteBuf markP() {
        markPos = position;
        return this;
    }

    @Override
    public ByteBuffer nioBuffer() {
        ByteBuffer buffer = getNioBuffer();
        return (ByteBuffer) buffer.limit(limit).position(position);
    }

    @Override
    public int position() {
        return position - offset;
    }

    @Override
    public ByteBuf position(int position) {
        this.position = ix(position);
        return this;
    }

    @Override
    public void put(byte[] src, int offset, int length) {
        System.arraycopy(src, offset, memory, position, length);
        this.position += length;
    }

    @Override
    public void putByte(byte b) {
        memory[position++] = b;
    }

    @Override
    public void putInt(int value) {
        ByteUtil.int2Byte(memory, value, position);
        position += 4;
    }

    @Override
    public void putIntLE(int value) {
        ByteUtil.int2ByteLE(memory, value, position);
        position += 4;
    }

    @Override
    public void putLong(long value) {
        ByteUtil.long2Byte(memory, value, position);
        position += 8;
    }

    @Override
    public void putLongLE(long value) {
        ByteUtil.long2ByteLE(memory, value, position);
        position += 8;
    }

    @Override
    public void putShort(short value) {
        ByteUtil.short2Byte(memory, value, position);
        position += 2;
    }

    @Override
    public void putShortLE(short value) {
        ByteUtil.short2ByteLE(memory, value, position);
        position += 2;
    }

    @Override
    public void putUnsignedInt(long value) {
        ByteUtil.unsignedInt2Byte(memory, value, position);
        position += 4;
    }

    @Override
    public void putUnsignedIntLE(long value) {
        ByteUtil.unsignedInt2ByteLE(memory, value, position);
        position += 4;
    }

    @Override
    public void putUnsignedShort(int value) {
        ByteUtil.unsignedShort2Byte(memory, value, position);
        position += 2;
    }

    @Override
    public void putUnsignedShortLE(int value) {
        ByteUtil.unsignedShort2ByteLE(memory, value, position);
        position += 2;
    }

    @Override
    public int read0(ByteBuf src, int read) {
        src.get(memory, position, read);
        skip(read);
        return read;
    }

    @Override
    public int read0(ByteBuffer src, int read) {
        src.get(memory, position, read);
        skip(read);
        return read;
    }

    @Override
    public int remaining() {
        return limit - position;
    }

    @Override
    public ByteBuf resetL() {
        limit = markLimit;
        return this;
    }

    @Override
    public ByteBuf resetP() {
        position = markPos;
        return this;
    }

    @Override
    public ByteBuf reverse() {
        position = nioBuffer.position();
        return this;
    }

    @Override
    public ByteBuf skip(int length) {
        position += length;
        return this;
    }

}

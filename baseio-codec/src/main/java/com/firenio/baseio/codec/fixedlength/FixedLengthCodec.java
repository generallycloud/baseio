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
package com.firenio.baseio.codec.fixedlength;

import java.io.IOException;

import com.firenio.baseio.buffer.ByteBuf;
import com.firenio.baseio.component.Frame;
import com.firenio.baseio.component.Channel;
import com.firenio.baseio.component.ProtocolCodec;

/**
 * <pre>
 *  B0 - B3：
 *  +-----------------+-----------------+-----------------+-----------------+
 *  |        B0                B1                B2               B3        |
 *  + - - - - - - - - + - - - - - - - - + - - - - - - - - + - - - - - - - - +
 *  | 0 1 2 3 4 5 6 7   0 1 2 3 4 5 6 7   0 1 2 3 4 5 6 7   0 1 2 3 4 5 6 7 |
 *  | - - - - - - - - + - - - - - - - - + - - - - - - - - + - - - - - - - - +
 *  |                                                                       |
 *  |                          Data-length(P0-P31)                          |
 *  |                                                                       |
 *  |                                                                       |
 *  +-----------------+-----------------+-----------------+-----------------+
 *  
 *  Data-length:-1表示心跳PING,-2表示心跳PONG,正数为报文长度
 *  注意: 无论是否是心跳报文，报文头长度固定为4个字节
 * 
 * </pre>
 */
public class FixedLengthCodec extends ProtocolCodec {

    public static final IOException ILLEGAL_PROTOCOL = EXCEPTION("illegal protocol");
    public static final IOException OVER_LIMIT       = EXCEPTION("over limit");
    private static final ByteBuf    PING;
    private static final ByteBuf    PONG;
    public static final int         PROTOCOL_HEADER  = 4;
    public static final int         PROTOCOL_PING    = -1;
    public static final int         PROTOCOL_PONG    = -2;

    static {
        PING = ByteBuf.heap(4);
        PONG = ByteBuf.heap(4);
        PING.putInt(PROTOCOL_PING);
        PONG.putInt(PROTOCOL_PONG);
        PING.flip();
        PONG.flip();
    }
    private int limit;

    public FixedLengthCodec() {
        this(1024 * 8);
    }

    public FixedLengthCodec(int limit) {
        this.limit = limit;
    }

    private Frame decodePing(int len) throws IOException {
        if (len == FixedLengthCodec.PROTOCOL_PING) {
            return new FixedLengthFrame().setPing();
        } else if (len == FixedLengthCodec.PROTOCOL_PONG) {
            return new FixedLengthFrame().setPong();
        } else {
            throw ILLEGAL_PROTOCOL;
        }
    }

    @Override
    public Frame decode(Channel ch, ByteBuf src) throws IOException {
        if (src.remaining() < PROTOCOL_HEADER) {
            return null;
        }
        int len = src.getInt();
        if (len < 0) {
            return decodePing(len);
        }
        if (len > limit) {
            throw OVER_LIMIT;
        }
        if (len > src.remaining()) {
            src.skip(-PROTOCOL_HEADER);
            return null;
        }
        byte[] data = new byte[len];
        src.get(data);
        return new FixedLengthFrame(new String(data, ch.getCharset()));
    }

    @Override
    public ByteBuf encode(Channel ch, Frame frame) throws IOException {
        if (frame.isTyped()) {
            return frame.isPing() ? PING.duplicate() : PONG.duplicate();
        }
        FixedLengthFrame f = (FixedLengthFrame) frame;
        ByteBuf buf = f.getBufContent().flip();
        if (buf.limit() == PROTOCOL_HEADER) {
            throw new IOException("null write buffer");
        }
        buf.putInt(0, buf.limit() - PROTOCOL_HEADER);
        return buf;
    }

    @Override
    public String getProtocolId() {
        return "FixedLength";
    }

    @Override
    public int headerLength() {
        return PROTOCOL_HEADER;
    }

    @Override
    public Frame ping(Channel ch) {
        return new FixedLengthFrame().setPing();
    }

}

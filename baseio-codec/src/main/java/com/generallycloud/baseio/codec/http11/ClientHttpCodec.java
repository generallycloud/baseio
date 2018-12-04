/*
 * Copyright 2015-2017 GenerallyCloud.com
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
package com.generallycloud.baseio.codec.http11;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.ByteBufUtil;
import com.generallycloud.baseio.collection.IntEntry;
import com.generallycloud.baseio.collection.IntMap;
import com.generallycloud.baseio.common.Util;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.FastThreadLocal;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.Frame;

/**
 * @author wangkai
 *
 */
public class ClientHttpCodec extends HttpCodec {

    private static final byte[] COOKIE                  = "Cookie:".getBytes();
    private static final byte[] PROTOCOL                = " HTTP/1.1\r\nContent-Length: "
            .getBytes();
    private static final byte   SEMICOLON               = ';';
    private int                 websocketLimit          = 1024 * 128;
    private int                 websocketFrameStackSize = 0;

    @Override
    ClientHttpFrame allocHttpFrame(NioSocketChannel ch) {
        ClientHttpFrame f = (ClientHttpFrame) ch.getAttribute(FRAME_DECODE_KEY);
        if (f == null) {
            return new ClientHttpFrame();
        }
        return f;
    }

    @Override
    public ByteBuf encode(NioSocketChannel ch, Frame frame) throws IOException {
        ClientHttpFrame f = (ClientHttpFrame) frame;
        int write_size = f.getWriteSize();
        byte[] byte32 = FastThreadLocal.get().getBytes32();
        byte[] url_bytes = getRequestURI(f).getBytes();
        byte[] method_bytes = f.getMethod().getBytes();
        int len_idx = Util.valueOf(write_size, byte32);
        int len_len = 32 - len_idx;
        int len = method_bytes.length + 1 + url_bytes.length + PROTOCOL.length + len_len + 2;
        List<byte[]> encode_bytes_array = getEncodeBytesArray();
        int header_size = 0;
        int cookie_size = 0;
        IntMap<String> headers = f.getRequestHeaders();
        if (headers != null) {
            headers.remove(HttpHeader.Content_Length.getId());
            for (IntEntry<String> header : headers.entries()) {
                byte[] k = HttpHeader.get(header.key()).getBytes();
                byte[] v = header.value().getBytes();
                if (v == null) {
                    continue;
                }
                header_size++;
                encode_bytes_array.add(k);
                encode_bytes_array.add(v);
                len += 4;
                len += k.length;
                len += v.length;
            }
        }
        List<Cookie> cookieList = f.getCookieList();
        if (cookieList != null && !cookieList.isEmpty()) {
            len += COOKIE.length;
            for (Cookie c : cookieList) {
                byte[] k = c.getName().getBytes();
                byte[] v = c.getValue().getBytes();
                cookie_size++;
                encode_bytes_array.add(k);
                encode_bytes_array.add(v);
                len += 2;
                len += k.length;
                len += v.length;
            }
        }
        len += 2;
        len += write_size;
        ByteBuf buf = ch.alloc().allocate(len);
        buf.put(method_bytes);
        buf.putByte(SPACE);
        buf.put(url_bytes);
        buf.put(PROTOCOL);
        buf.put(byte32, len_idx, len_len);
        buf.putByte(R);
        buf.putByte(N);
        int j = 0;
        for (int i = 0; i < header_size; i++) {
            buf.put(encode_bytes_array.get(j++));
            buf.putByte((byte) ':');
            buf.putByte(SPACE);
            buf.put(encode_bytes_array.get(j++));
            buf.putByte(R);
            buf.putByte(N);
        }
        for (int i = 0; i < cookie_size; i++) {
            buf.put(encode_bytes_array.get(j++));
            buf.putByte((byte) ':');
            buf.put(encode_bytes_array.get(j++));
            buf.putByte(SEMICOLON);
        }
        buf.putByte(R);
        buf.putByte(N);
        if (write_size != 0) {
            buf.put(f.getWriteBuffer(), 0, write_size);
        }
        return buf.flip();
    }

    @Override
    int decodeRemainBody(NioSocketChannel ch, ByteBuf src, HttpFrame frame) {
        ClientHttpFrame f = (ClientHttpFrame) frame;
        if (f.bodyArray == null) {
            f.bodyArray = new byte[f.contentLength];
            f.bodyBuf = ByteBufUtil.wrap(f.bodyArray);
        }
        f.bodyBuf.read(src);
        if (f.bodyBuf.hasRemaining()) {
            return decode_state_body;
        }
        if (HttpStatic.application_urlencoded.equals(f.contentType)) {
            // FIXME encoding
            String paramString = new String(f.bodyArray, ch.getCharset());
            parse_kv(f.params, paramString, '=', '&');
        } else {
            // FIXME 解析BODY中的内容
        }
        return decode_state_complate;
    }

    protected void parseFirstLine(HttpFrame f, StringBuilder line) {
        int index = Util.indexOf(line, ' ');
        int status = Integer.parseInt(line.substring(index + 1, index + 4));
        f.setVersion(HttpVersion.HTTP1_1.getId());
        f.setStatus(HttpStatus.get(status));
    }

    @Override
    public String getProtocolId() {
        return "HTTP11";
    }

    public int getWebsocketLimit() {
        return websocketLimit;
    }

    public void setWebsocketLimit(int websocketLimit) {
        this.websocketLimit = websocketLimit;
    }

    public int getWebsocketFrameStackSize() {
        return websocketFrameStackSize;
    }

    public void setWebsocketFrameStackSize(int websocketFrameStackSize) {
        this.websocketFrameStackSize = websocketFrameStackSize;
    }

    private String getRequestURI(HttpFrame frame) {
        Map<String, String> params = frame.getRequestParams();
        if (params == null || params.isEmpty()) {
            return frame.getRequestURL();
        }
        String url = frame.getRequestURL();
        StringBuilder u = new StringBuilder(url);
        u.append("?");
        Set<Entry<String, String>> ps = params.entrySet();
        for (Entry<String, String> p : ps) {
            u.append(p.getKey());
            u.append("=");
            u.append(p.getValue());
            u.append("&");
        }
        return u.toString();
    }

    @Override
    public void initialize(ChannelContext context) throws Exception {
        WebSocketCodec.init(context, websocketLimit, websocketFrameStackSize);
    }

}

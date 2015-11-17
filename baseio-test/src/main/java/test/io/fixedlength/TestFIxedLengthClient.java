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
package test.io.fixedlength;

import com.firenio.baseio.codec.fixedlength.FixedLengthCodec;
import com.firenio.baseio.codec.fixedlength.FixedLengthFrame;
import com.firenio.baseio.component.ChannelConnector;
import com.firenio.baseio.component.Frame;
import com.firenio.baseio.component.IoEventHandle;
import com.firenio.baseio.component.LoggerChannelOpenListener;
import com.firenio.baseio.component.Channel;

public class TestFIxedLengthClient {

    public static void main(String[] args) throws Exception {
        ChannelConnector context = new ChannelConnector("192.168.1.115", 8300);
        IoEventHandle eventHandle = new IoEventHandle() {
            @Override
            public void accept(Channel ch, Frame frame) throws Exception {
                FixedLengthFrame f = (FixedLengthFrame) frame;
                System.out.println();
                System.out.println("____________________" + f.getStringContent());
                System.out.println();
                context.close();
            }
        };

        context.setIoEventHandle(eventHandle);
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.addProtocolCodec(new FixedLengthCodec());
        Channel ch = context.connect(999999);
        FixedLengthFrame frame = new FixedLengthFrame();
        frame.setContent(ch.allocate());
        frame.write("hello server!", ch);
        ch.writeAndFlush(frame);

    }

}

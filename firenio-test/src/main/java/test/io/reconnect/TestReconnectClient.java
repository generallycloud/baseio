/*
 * Copyright 2015 The FireNio Project
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
package test.io.reconnect;

import com.firenio.codec.lengthvalue.LengthValueCodec;
import com.firenio.common.Util;
import com.firenio.component.ChannelConnector;
import com.firenio.component.Frame;
import com.firenio.component.IoEventHandle;
import com.firenio.component.LoggerChannelOpenListener;
import com.firenio.component.Channel;
import com.firenio.component.ReConnector;

public class TestReconnectClient {

    public static void main(String[] args) throws Exception {

        IoEventHandle eventHandleAdaptor = new IoEventHandle() {

            @Override
            public void accept(Channel ch, Frame frame) throws Exception {

            }
        };

        ChannelConnector context = new ChannelConnector(8300);

        ReConnector connector = new ReConnector(context);

        connector.setRetryTime(5000);

        context.setIoEventHandle(eventHandleAdaptor);

        context.addChannelEventListener(new LoggerChannelOpenListener());

        context.addProtocolCodec(new LengthValueCodec());

        //		context.addChannelEventListener(new CloseConnectorSEListener(connector.getRealConnector()));

        connector.connect();

        int count = 99999;
        for (int i = 0; ; i++) {
            Util.sleep(1000);
            if (i > count) {
                break;
            }
        }

        Util.close(connector);
    }
}

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
package test.test;

import java.math.BigDecimal;

import com.firenio.common.Util;
import com.firenio.log.Logger;
import com.firenio.log.LoggerFactory;

public class ITestHandle {

    private static Logger logger = LoggerFactory.getLogger(ITestHandle.class);

    public static void doTest(ITest test, long time, String testName) {
        logger.info("################## Test start ####################");
        logger.info("## Test Name:" + testName);
        long old = Util.now_f();
        for (int i = 0; i < time; i++) {
            try {
                test.test(i);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        long spend = Util.past(old);
        logger.info("## Expend  Time:" + spend);
        logger.info("## Execute Time:" + time);
        logger.info("## OP(W)/S:" + new BigDecimal(time).divide(new BigDecimal(spend), 2, BigDecimal.ROUND_HALF_UP).doubleValue() / 10);

    }
}

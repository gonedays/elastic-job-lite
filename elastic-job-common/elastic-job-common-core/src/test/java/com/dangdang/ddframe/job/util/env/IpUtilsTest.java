/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.util.env;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public final class IpUtilsTest {

    @Before
    public void clearCache() {
        IpUtils.evictCache();
    }

    @Test
    public void assertGetIp() {
        String ip = IpUtils.getIp();
        System.out.println("Got ip:" + ip);
        assertNotNull(ip);
    }

    @Test
    public void assertGetHostName() {
        assertNotNull(IpUtils.getHostName());
    }

    @Test
    public void assertGetIpWithPrefix() {
        System.setProperty(IpUtils.ENV_PARAM_IP_PREFIX, "192.168");
        String ip = IpUtils.getIp();
        System.out.println("assertGetIpWithPrefix, got ip:" + ip);
        assertTrue(ip.startsWith("192.168"));
    }

    @Test
    public void assertGetIpWithPrefix_2() {
        System.setProperty(IpUtils.ENV_PARAM_IP_PREFIX, "10.255");
        String ip = IpUtils.getIp();
        System.out.println("assertGetIpWithPrefix, got ip:" + ip);
        assertTrue(ip.startsWith("10.255"));
    }
}

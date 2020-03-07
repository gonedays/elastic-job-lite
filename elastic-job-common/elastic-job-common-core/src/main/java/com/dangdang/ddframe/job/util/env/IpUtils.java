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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * 获取真实本机网络的服务.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IpUtils {

    /**
     * IP地址的正则表达式.
     */
    public static final String IP_REGEX = "((\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})";

    private static volatile String cachedIpAddress;

    /**
     * 环境变量中注册实例使用的IP前缀
     */
    public static final String ENV_PARAM_IP_PREFIX = "elastic.job.instance.prefer.ip";

    /**
     * 获取本机IP地址.
     *
     * <p>
     * 为了应对服务器有多个IP的情况，优先从环境变量中查询是否设置了想要的IP前缀，若有，则返回第一个匹配的IP，
     * 否则获取其他IP
     * </p>
     *
     * @return 本机IP地址
     */
    public static String getIp() {
        if (null != cachedIpAddress) {
            return cachedIpAddress;
        }
        String ipPrefix = System.getProperty(ENV_PARAM_IP_PREFIX, "");
        return getIp(ipPrefix);
    }

    /**
     * 通过指定的前缀获取本机IP地址.
     *
     * <p>
     * 有限获取IP地址，优先获取指定前缀的ip地址.若取不到，则返回getGeneralIp()取到的地址.
     * </p>
     *
     * @param prefix ip前缀
     * @return 本机IP地址
     */

    public static String getIp(String prefix) {
        if (null != cachedIpAddress) {
            return cachedIpAddress;
        }
        if (StringUtils.isBlank(prefix)) {
            return getGeneralIp();
        }

        Enumeration<NetworkInterface> netInterfaces;
        try {
            netInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (final SocketException ex) {
            throw new HostException(ex);
        }
        while (netInterfaces.hasMoreElements()) {
            NetworkInterface netInterface = netInterfaces.nextElement();
            Enumeration<InetAddress> ipAddresses = netInterface.getInetAddresses();
            while (ipAddresses.hasMoreElements()) {
                InetAddress ipAddress = ipAddresses.nextElement();
                if (isSpecifiedIpAddress(ipAddress, prefix)) {
                    String specifiedIPAddress = ipAddress.getHostAddress();
                    cachedIpAddress = specifiedIPAddress;
                    return specifiedIPAddress;
                }
            }
        }
        return getGeneralIp();
    }

    /**
     * 获取本机IP地址.
     *
     * <p>
     * 有限获取IP地址.
     * 也有可能是链接着路由器的最终IP地址.
     * </p>
     *
     * @return 本机IP地址
     */
    public static String getGeneralIp() {
        if (null != cachedIpAddress) {
            return cachedIpAddress;
        }
        Enumeration<NetworkInterface> netInterfaces;
        try {
            netInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (final SocketException ex) {
            throw new HostException(ex);
        }
        String localIpAddress = null;
        while (netInterfaces.hasMoreElements()) {
            NetworkInterface netInterface = netInterfaces.nextElement();
            Enumeration<InetAddress> ipAddresses = netInterface.getInetAddresses();
            while (ipAddresses.hasMoreElements()) {
                InetAddress ipAddress = ipAddresses.nextElement();
                if (isPublicIpAddress(ipAddress)) {
                    String publicIpAddress = ipAddress.getHostAddress();
                    cachedIpAddress = publicIpAddress;
                    return publicIpAddress;
                }
                if (isLocalIpAddress(ipAddress)) {
                    localIpAddress = ipAddress.getHostAddress();
                }
            }
        }
        cachedIpAddress = localIpAddress;
        return localIpAddress;
    }

    /**
     * 是否指定前缀的IP地址
     *
     * @param ipAddress
     * @param ipPrefix
     * @return
     */
    private static boolean isSpecifiedIpAddress(final InetAddress ipAddress, String ipPrefix) {
        if (StringUtils.isBlank(ipPrefix) || StringUtils.isBlank(ipAddress.getHostAddress())) {
            return false;
        }
        if (ipAddress.getHostAddress().startsWith(ipPrefix)) {
            return true;
        }
        return false;
    }

    private static boolean isPublicIpAddress(final InetAddress ipAddress) {
        return !ipAddress.isSiteLocalAddress() && !ipAddress.isLoopbackAddress() && !isV6IpAddress(ipAddress);
    }

    private static boolean isLocalIpAddress(final InetAddress ipAddress) {
        return ipAddress.isSiteLocalAddress() && !ipAddress.isLoopbackAddress() && !isV6IpAddress(ipAddress);
    }

    private static boolean isV6IpAddress(final InetAddress ipAddress) {
        return ipAddress.getHostAddress().contains(":");
    }

    /**
     * 获取本机Host名称.
     *
     * @return 本机Host名称
     */
    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (final UnknownHostException ex) {
            throw new HostException(ex);
        }
    }

    /**
     * 清空缓存,为了方便单元测试测试IP前缀的情况
     */
    public static void evictCache() {
        cachedIpAddress = null;
    }
}

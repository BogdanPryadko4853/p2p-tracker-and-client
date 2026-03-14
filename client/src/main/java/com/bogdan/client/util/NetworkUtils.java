package com.bogdan.client.util;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

@Slf4j
public class NetworkUtils {

    public static String getLocalIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();

                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    String hostAddress = addr.getHostAddress();
                    if (hostAddress.contains(".") && !addr.isLoopbackAddress()) {
                        log.info("Found local IP: {} on interface {}", hostAddress, iface.getName());
                        return hostAddress;
                    }
                }
            }
        } catch (SocketException e) {
            log.error("Failed to get local IP", e);
        }
        log.warn("No suitable network interface found, using localhost");
        return "127.0.0.1";
    }

    public static boolean isLocalIp(String ip) {
        return ip.startsWith("192.168.") ||
                ip.startsWith("10.") ||
                ip.startsWith("172.16.") ||
                ip.startsWith("127.0.0.");
    }
}
package com.arc.nas.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class GetLocalIPAddress {

    private static final Logger log = LoggerFactory.getLogger(GetLocalIPAddress.class);

  public static String localNetAddress = null;

    static {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && inetAddress.isSiteLocalAddress()) {
                        localNetAddress = inetAddress.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            log.error("GetLocalIPAddress get ip error.", e);
        }
    }

    public static String getLocalIPAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && inetAddress.isSiteLocalAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            log.error("GetLocalIPAddress get ip error.", e);
        }
        return null; // 如果未找到合适的IP地址，则返回null
    }


    public static void main(String[] args) {
        System.out.println(localNetAddress);
        String localIPAddress = getLocalIPAddress();
        if (localIPAddress != null) {
            System.out.println("本机IP地址: " + localIPAddress);
        } else {
            System.out.println("未找到本机IP地址");
        }
    }
}

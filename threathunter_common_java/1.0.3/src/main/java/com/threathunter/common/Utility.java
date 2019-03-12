package com.threathunter.common;

import org.reflections.Reflections;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * created by www.threathunter.cn
 */
public class Utility {
    public static boolean isEmptyStr(String s) {
        return s == null || s.isEmpty();
    }

    public static String generateHashKey(List<String> attrs){
        StringBuilder sb = new StringBuilder("");
        if(attrs!=null && attrs.size()>0){
            sb.append(attrs.get(0));
            if(attrs.get(0) == null){
                sb.append(-1);
            }else{
                sb.append(attrs.get(0).length());
            }
            for(int i=1;i<attrs.size();i++){
                sb.append("-_-");
                sb.append(attrs.get(i));
                if(attrs.get(i) == null){
                    sb.append(-1);
                }else{
                    sb.append(attrs.get(i).length());
                }
            }
        }
        return sb.toString();
    }

    public static boolean isEqual(Object left,Object right){
        if(left == null && right == null){
            return true;
        }
        if(left != null && right != null){
            return left.equals(right);
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    public static boolean isMapEqual(Map left,Map right){
        if(left == null && right == null){
            return true;
        }
        if(left != null && right != null){
            for(Object obj:left.entrySet()){
                Entry entry = (Entry)obj;
                Object key = entry.getKey();
                Object value = entry.getValue();
                if(!right.containsKey(key)){
                    return false;
                }else{
                    if(!isEqual(right.get(key),value)){
                        return false;
                    }
                }
            }
            for(Object obj:right.entrySet()){
                Entry entry = (Entry)obj;
                Object key = entry.getKey();
                Object value = entry.getValue();
                if(!left.containsKey(key)){
                    return false;
                }else{
                    if(!isEqual(left.get(key),value)){
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    public static boolean isListEqual(List left,List right){
        if(left == null && right == null){
            return true;
        }
        if(left != null && right != null){
            for(Object obj:left){
                if(!right.contains(obj)){
                    return false;
                }
            }
            for(Object obj:right){
                if(!left.contains(obj)){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Join a vector of type string with the given separator.
     *
     * @param sep the separator
     * @param parts a vector of type string
     * @return a long string constituted by the multiple string and the separator
     */
    public static String joinStrings(String sep, String[] parts) {
        if (parts == null || parts.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for(String part : parts) {
            sb.append(part);
            sb.append(sep);
        }
        // remove the last sep
        return sb.substring(0, sb.lastIndexOf(sep));
    }

    /**
     * Join a vector of type string with the given separator.
     *
     * @param sep the separator
     * @param parts a vector of type string
     * @return a long string constituted by the multiple string and the separator
     */
    public static String joinStrings(String sep, List<String> parts) {
        return joinStrings(sep, parts.toArray(new String[0]));
    }

    /**
     * Return non-null parts separated by regex
     *
     * @param regex the separator in the form of regular expression
     * @param data the data to be splitted
     * @return a list of string which is from data
     */
    public static List<String> splitStrings(String regex, String data) {
        List<String> result = new ArrayList<String>();
        if (isEmptyStr(data)) {
            return result;
        }
        String[] parts = data.split(regex);
        if (parts == null) {
            return result;
        }
        for(String p : parts) {
            if (p == null) {
                continue;
            }
            p = p.trim();
            if (isEmptyStr(p)) {
                continue;
            }
            result.add(p);
        }
        return result;
    }

    public static <T> Set<Class<? extends T>> scannerSubTypeFromPackage(String packageName, Class<T> subType) {
        Reflections reflections = new Reflections(packageName);
        return reflections.getSubTypesOf(subType);
    }

    public static void argumentNotEmpty(Object arg, String msg) {
        if (arg != null) {
            if (!(arg instanceof String) || (!((String) arg).isEmpty())) {
                if (!(arg instanceof Collection) || !(((Collection) arg).isEmpty())) {
                    // not null && not empty string && not empty collection;
                    return;
                }
            }
        }

        // either null or empty string or empty collection.
        throw new IllegalArgumentException(msg);
    }

    public static String getParentPackageName(Class<?> cls) {
        String packageName = cls.getPackage().getName();
        int index = packageName.lastIndexOf('.');
        return packageName.substring(0, index);
    }

    private static volatile String LOCAL_IP = null;
    public static String getLocalIPAddress() {
        if (LOCAL_IP != null) {
            return LOCAL_IP;
        }

        try {
            Map<String, List<String>> cache = new HashMap();
            List<String> prefixes = Arrays.asList("eth", "em", "wlan", "wifi", "ppp", "vmnet");

            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()){
                NetworkInterface current = interfaces.nextElement();
                if (!current.isUp() || current.isLoopback() || current.isVirtual()) continue;
                Enumeration<InetAddress> addresses = current.getInetAddresses();
                String name = current.getName().toLowerCase();
                while (addresses.hasMoreElements()){
                    InetAddress current_addr = addresses.nextElement();
                    if (current_addr.isLoopbackAddress()) continue;
                    if (current_addr instanceof Inet6Address) continue;

                    String ip = current_addr.getHostAddress();
                    final AtomicBoolean match = new AtomicBoolean(false);
                    prefixes.forEach((String prefix) -> {
                        if (name.startsWith(prefix)) {
                            cache.computeIfAbsent(prefix, (String pre) -> new ArrayList<String>()).add(ip);
                            match.set(true);
                        }
                    });
                    if (!match.get()) {
                        cache.computeIfAbsent("other", (String pre) -> new ArrayList<String>()).add(ip);
                    }
                }
            }
            AtomicBoolean find = new AtomicBoolean(false);
            Arrays.asList("eth", "em", "wlan", "wifi", "ppp", "other", "vmnet").forEach(
                    (String prefix) -> {
                        if (!find.get() && cache.containsKey(prefix) && !cache.get(prefix).isEmpty()) {
                            LOCAL_IP = cache.get(prefix).get(0);
                            find.set(true);
                        }
                    }
            );
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return LOCAL_IP;
    }
}

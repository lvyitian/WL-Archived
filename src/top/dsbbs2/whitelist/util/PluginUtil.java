package top.dsbbs2.whitelist.util;

import top.dsbbs2.whitelist.WhiteListPlugin;

public class PluginUtil {
    public static String getPluginVersion(){
        return "3.7.4";
    }

    public static boolean isDebugMode(){
        return WhiteListPlugin.instance.debugMode;
    }



}

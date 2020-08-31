package top.dsbbs2.whitelist.util;

import org.bukkit.Bukkit;
import top.dsbbs2.whitelist.WhiteListPlugin;

public class ServerUtil {
    public static void updateJson(){
        try {
            WhiteListPlugin.instance.whitelist.saveConfig();
            System.out.println("��awhitelist.json�������°汾!");
        }catch (Throwable e){
            e.printStackTrace();
            System.out.println("��cwhitelist.json����ʧ��!");
            System.out.println("��c����whitelist.json�����Ѿ���!");
        }
    }
    public static boolean getOnlineMode(){
        if(Bukkit.getOnlineMode()){
            MsgUtil.makeDebugMsgAndSend("Bukkit.getOnlineMode() is true");
            return true;
        }else{
            MsgUtil.makeDebugMsgAndSend("Bukkit.getOnlineMode() is false");
            return false;
        }
    }
    public static boolean isOnlineStorageMode()
    {
        return (Bukkit.getOnlineMode() || WhiteListPlugin.instance.whitelist.con.forceOnline.equalsIgnoreCase("Online"))&&!WhiteListPlugin.instance.whitelist.con.forceOnline.equalsIgnoreCase("Offline");
    }
}

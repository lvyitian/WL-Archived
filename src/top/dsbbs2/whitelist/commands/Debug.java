package top.dsbbs2.whitelist.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import top.dsbbs2.whitelist.WhiteListPlugin;
import top.dsbbs2.whitelist.util.VectorUtil;

import java.util.Vector;

public class Debug implements IChildCommand{
    @Override
    public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
        if(arg3.length==1){
            if(arg3[0].equalsIgnoreCase("debug")){
                if(WhiteListPlugin.instance.debugMode){
                    //将改为false
                    WhiteListPlugin.instance.debugMode = false;
                    arg0.sendMessage("§d§l已经为您关闭debug模式!");
                    return true;
                }else{
                    //将改为true
                    WhiteListPlugin.instance.debugMode = true;
                    arg0.sendMessage("§d§l已经为您开启debug模式!");
                    arg0.sendMessage("§d§l注意,debug模式并不能提升插件,仅仅是用来查错的,当插件出现错误,但是没有任何错误信息,请开启此命令,然后大约5分钟后,向插件制作者发送日志,以便查找问题!");
                    return true;
                }
            }
        }
        return false;
    }
    @NotNull
    @Override
    public String getUsage() {
        return "/wl debug";
    }

    @NotNull
    @Override
    public Vector<Class<?>> getArgumentsTypes()
    {
        return VectorUtil.toVector(String.class);
    }

    @NotNull
    @Override
    public Vector<String> getArgumentsDescriptions()
    {
        return VectorUtil.toVector("");
    }

    @NotNull
    @Override
    public String getPermission()
    {
        return "whitelist.debug";
    }

    @NotNull
    @Override
    public String getDescription(){
        return "开启/关闭 debug模式";
    }

}

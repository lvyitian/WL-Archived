package top.dsbbs2.whitelist;

import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import top.dsbbs2.common.file.FileUtils;
import top.dsbbs2.whitelist.com.comphenix.tinyprotocol.TinyProtocol;
import top.dsbbs2.whitelist.commands.*;
import top.dsbbs2.whitelist.config.SimpleConfig;
import top.dsbbs2.whitelist.config.struct.WhiteListConfig;
import top.dsbbs2.whitelist.listeners.PlayerListener;
import top.dsbbs2.whitelist.util.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class WhiteListPlugin extends JavaPlugin {
	public static HashMap<String,Integer> playerInteract = new HashMap<>();
	public static Vector<String> blackUUIDList = new Vector<>();
	public static Vector<Long> isSameList = new Vector<>();
	// /\ blackList
	public static boolean isSameMode = true;
	public static volatile WhiteListPlugin instance=null;
	public volatile SimpleConfig<WhiteListConfig> whitelist=new SimpleConfig<>(this.getDataFolder()+"/whitelist.json","UTF8",WhiteListConfig.class);
	public volatile Vector<IChildCommand> childCmds=new Vector<>();
	public volatile TinyProtocol protocol;
	public void registerListeners()
	{
		registerListener(new PlayerListener());
	}
	public void registerListener(Listener lis)
	{
		Bukkit.getPluginManager().registerEvents(lis, this);
	}
	public static ConcurrentHashMap<Long,String> CNCU = new ConcurrentHashMap<>();//confirm name,to change uuid. UUID��������
	public static ConcurrentHashMap<Long,UUID> CUCN = new ConcurrentHashMap<>();    //confirm uuid,to change name. ��Ҹ���
	public static boolean debugMode = false;

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length>0)
		{
			IChildCommand c=CommandUtil.getCommand(this.childCmds,args[0]);
			if(c==null)
			{

				sender.sendMessage("���� "+args[0]+" ������");
			}else {
				if(!c.getPermission().trim().equals("") && !sender.hasPermission(c.getPermission()))
				{
					sender.sendMessage("��û��Ȩ����ô��,����Ҫ"+c.getPermission()+"Ȩ��!");
					return true;
				}
				if(!c.onCommand(sender, command, label, args))
				{
					String usage=c.getUsage();
					if(!usage.trim().equals(""))
						sender.sendMessage(usage);
				}
			}
			return true;
		}
		return super.onCommand(sender, command, label, args);
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if(args.length>0)
			return TabUtil.betterGetStartsWithList(realOnTabComplete(sender,command,alias,args),args[args.length-1]);
		else
			return realOnTabComplete(sender,command,alias,args);
	}
	public List<String> realOnTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if(args.length<=1)
			return VectorUtil.toArrayList(CommandUtil.commandListToCommandNameList(childCmds));
		if(args.length>1)
		{
			IChildCommand c=CommandUtil.getCommand(childCmds, args[0]);
			if(c!=null)
			{
				Vector<Class<?>> cats=c.getArgumentsTypes();
				if(cats.size()>args.length-2)
				{
					Class<?> argType=cats.get(args.length-2);
					Vector<String> des=c.getArgumentsDescriptions();
					String desc=null;
					if(des.size()>args.length-2)
						desc=des.get(args.length-2);
					if(desc==null)
					{
						return ListUtil.toList(argType.getSimpleName());
					}else if(desc.equals("player"))
					{
						return PlayerUtil.getOfflinePlayersNameList();
					}else if(desc.equals("unwhitelisted_player"))
					{
						return PlayerUtil.getUnwhitelistedOfflinePlayersNameList();
					}else if(desc.equals("whitelisted_player")){
						return PlayerUtil.whiteListPlayerListToNameList(this.whitelist.con.players);
					}else if(desc.equals("qq")){
						return VectorUtil.toArrayList(VectorUtil.toStringVector(PlayerUtil.getQQList()));
					}else if(desc.equals("noname_player")){
						return VectorUtil.toArrayList(PlayerUtil.getNoNameWhiteListPlayerUUIDString());
					}else if(desc.contains("/")){
						return ListUtil.toList(desc.split("/"));
					}else{
						return ListUtil.toList(desc);
					}
				}
			}
		}
		return new ArrayList<>();
	}
	public void initChildCommands()
	{
		addChildCmd(new Add());
		addChildCmd(new Remove());
		addChildCmd(new QRemove());
		addChildCmd(new QBan());
		addChildCmd(new top.dsbbs2.whitelist.commands.List());
		addChildCmd(new NoNameRemove());
		addChildCmd(new Reload());
		addChildCmd(new Import());
		addChildCmd(new Get());
		addChildCmd(new Confirm());
		addChildCmd(new Debug());
		addChildCmd(new Help());
	}
	public void addChildCmd(IChildCommand c)
	{
		this.childCmds.add(c);
	}
	@Override
	public void onLoad()
	{
		instance=this;
		try {
			whitelist.loadConfig();
			if (!FileUtils.readTextFile(new File(this.getDataFolder()+"/whitelist.json"), StandardCharsets.UTF_8).trim().equals(whitelist.g.toJson(whitelist.getConfig()).trim()))
			{
				FileUtils.writeTextFile(new File(this.getDataFolder()+"/whitelist.json.bak"),FileUtils.readTextFile(new File(this.getDataFolder()+"/whitelist.json"), StandardCharsets.UTF_8),StandardCharsets.UTF_8,false);
			    whitelist.saveConfig();
			}
		}catch(Throwable e) {throw new RuntimeException(e);}
		initChildCommands();
//		try{
//			AsyncCatcher.enabled = false;
//		}catch (Throwable e){e.printStackTrace();}

	}
	@Override
	public void onEnable()
	{
		registerListeners();
		if(this.whitelist.con.enableTabIntercept) {
			try {
				protocol = new TinyProtocol(this) {
					@Override
					public Object onPacketInAsync(Player sender, Channel channel, Object packet) {
						if (WhiteListPlugin.instance != null && WhiteListPlugin.instance.isEnabled() && packet.getClass().getSimpleName().equals("PacketPlayInTabComplete")) {
							try {
								if (!PlayerUtil.isInWhiteList(sender)) {
									PlayerUtil.setInv(sender, true);
									return null;
								}
							} catch (Throwable e2) {
								e2.printStackTrace();
								return null;
							}
						}

						return super.onPacketInAsync(sender, channel, packet);
					}

					@Override
					public Object onPacketOutAsync(Player reciever, Channel channel, Object packet) {
						if (WhiteListPlugin.instance != null && WhiteListPlugin.instance.isEnabled() && packet.getClass().getSimpleName().equals("PacketPlayOutTabComplete")) {
							try {
								if (!PlayerUtil.isInWhiteList(reciever)) {
									PlayerUtil.setInv(reciever, true);
									return null;
								}
							} catch (Throwable e2) {
								e2.printStackTrace();
								return null;
							}
						}

						return super.onPacketOutAsync(reciever, channel, packet);
					}
				};
			} catch (Throwable e) {
				getLogger().warning("����,�������ķ���˹���,TinyProtocol��ʱ��֧���°�,���������޷������ް�������ҵ�tab���ع���!");
				getLogger().warning("����������û���κ�Ӱ��,��Ӱ������ʹ��(���㲻����tab û�����������Ҳ�޷����͸�����,���˼��ݵ�¼����Ĺ���[�ɹر�])");
			}
		}
		MsgUtil.makeDebugMsgAndSend("��ʼ�Ƚ�ģʽ...");
		try {
			compareOnlineMode();
		}catch (Throwable e){
			MsgUtil.makeDebugMsgAndSend("�޷���������� ���������Ѵ�ӡ ");
			e.printStackTrace();
		}
		ServerUtil.updateJson();

		updateOnlineModeinWhiteListJson();
		if(ServerUtil.isOnlineStorageMode()) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				PlayerUtil.checkUUIDAndName(player);
			}
			System.out.println("��a[whitelist]�Ѽ��ȫ����ҵ�UUID��Name!");
		}
		if(MsgUtil.getDebugMode()){
			MsgUtil.makeDebugMsgAndSend("[BukkitWhitelist]�ѿ���debugģʽ!");
		}
		PlayerUtil.updateSameList();
//		PlayerUtil.updateBlackUUID();
		System.out.println("WLĿǰ�汾Ϊ: "+PluginUtil.getPluginVersion());
	}


	public static void updateOnlineModeinWhiteListJson(){

		if(!isSameMode){
			MsgUtil.makeDebugMsgAndSend("isSameMode Ϊ false");
			if (ServerUtil.isOnlineStorageMode()) {
				System.out.println("[whitelist]��⵽���������ӵ�������ĵ������!");
				System.out.println("[whitelist]��ʼת��whitelist.json����!");
				new Thread(()->{

					convertOnlineOrOfflineJsonMode(true);
				}).start();

			} else {
				MsgUtil.makeDebugMsgAndSend("������ת��Ϊ����");
				MsgUtil.makeDebugMsgAndSend("Actual: "+Bukkit.getServer().getOnlineMode()+"");
				MsgUtil.makeDebugMsgAndSend((!CommandUtil.isnull(WhiteListPlugin.instance.whitelist.con.forceOnline)&&WhiteListPlugin.instance.whitelist.con.forceOnline.equalsIgnoreCase("Online"))+"");
				MsgUtil.makeDebugMsgAndSend(!CommandUtil.isnull(WhiteListPlugin.instance.whitelist.con.forceOnline)+"");
				MsgUtil.makeDebugMsgAndSend(WhiteListPlugin.instance.whitelist.con.forceOnline.equalsIgnoreCase("Online")+"");
				System.out.println("[whitelist]��⵽������������������ĵ������!");
				System.out.println("[whitelist]��ʼת��whitelist.json����!");
				new Thread(()->{

					convertOnlineOrOfflineJsonMode(false);
				}).start();
			}

		}
	}

	public static void convertOnlineOrOfflineJsonMode(boolean isOnline){
		//ture is online
		//false is offline
		long s = System.currentTimeMillis();
		int index = WhiteListPlugin.instance.whitelist.con.players.size();
		for(int i=0;i<index;i++){
			WhiteListConfig.WLPlayer wlp = WhiteListPlugin.instance.whitelist.con.players.get(0);
			//||((!CommandUtil.isnull(WhiteListPlugin.instance.whitelist.con.forceOnline)))&&WhiteListPlugin.instance.whitelist.con.forceOnline.equalsIgnoreCase("")
			if(!isOnline ) {
				WhiteListPlugin.instance.whitelist.con.players.add(new WhiteListConfig.WLPlayer(wlp.name, wlp.QQ));
				System.out.println("��a��l[" + wlp.name + "][Online] -> [" + wlp.name + "][Offline]");
			}else{
				OfflinePlayer op = null;
				try {
					op = Bukkit.getOfflinePlayer(MojangUtil.getUUIDFromMojang(wlp.name));
				} catch (Throwable e) {
					System.out.println("ͨ��Mojang��������ȡUUID����ʧ��,����ʼͨ�����ػ�ȡ!");
					op = Bukkit.getOfflinePlayer(wlp.name);
				}
				WhiteListPlugin.instance.whitelist.con.players.add(new WhiteListConfig.WLPlayer(op.getUniqueId(),wlp.name, wlp.QQ));
				System.out.println("��a��l[" + wlp.name + "][Offline] -> [" + wlp.name + "][Online]");
			}
			WhiteListPlugin.instance.whitelist.con.players.remove(0);
		}
		if(!isOnline) {
			WhiteListPlugin.instance.whitelist.con.isOnlineServer = "�����(�Զ����,������Ч)";
		}else{
			WhiteListPlugin.instance.whitelist.con.isOnlineServer = "�����(�Զ����,������Ч)";
		}
		try {
			WhiteListPlugin.instance.whitelist.saveConfig();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("�����쳣����ջ��¼�Ѵ�ӡ������̨");
		}
		long s2 = System.currentTimeMillis();
		System.out.println("ת�����,��ʱ"+(s2-s)+"����!");
	}

	public static void compareOnlineMode(){
		if(!(!(CommandUtil.isnull(WhiteListPlugin.instance.whitelist.con.forceOnline)) || WhiteListPlugin.instance.whitelist.con.forceOnline.equalsIgnoreCase("No"))) {
			MsgUtil.makeDebugMsgAndSend("ǿ��ģʽû������");
			//MsgUtil.makeDebugMsgAndSend("!CommandUtil.isnull(WhiteListPlugin.instance.whitelist.con.forceOnline)=" + !CommandUtil.isnull(WhiteListPlugin.instance.whitelist.con.forceOnline));
			//MsgUtil.makeDebugMsgAndSend("!(CommandUtil.isnull(WhiteListPlugin.instance.whitelist.con.forceOnline)) || WhiteListPlugin.instance.whitelist.con.forceOnline.equalsIgnoreCase(No)= "+(!(CommandUtil.isnull(WhiteListPlugin.instance.whitelist.con.forceOnline)) || WhiteListPlugin.instance.whitelist.con.forceOnline.equalsIgnoreCase("No")));

			if (WhiteListPlugin.instance.whitelist.con.isOnlineServer != null && WhiteListPlugin.instance.whitelist.con.isOnlineServer.equals("�����(�Զ����,������Ч)")) {

				if (Bukkit.getServer().getOnlineMode()) {
					isSameMode = true;
					return;
				} else {
					isSameMode = false;
					return;
				}
			} else if (WhiteListPlugin.instance.whitelist.con.isOnlineServer != null && WhiteListPlugin.instance.whitelist.con.isOnlineServer.equals("�����(�Զ����,������Ч)") ) {

				if (!Bukkit.getServer().getOnlineMode()) {
					isSameMode = true;
					return;
				} else {
					isSameMode = false;
					return;
				}
			}
		}else{
			MsgUtil.makeDebugMsgAndSend("ǿ��ģʽ�Ѿ�����");
			if(WhiteListPlugin.instance.whitelist.con.isOnlineServer != null && WhiteListPlugin.instance.whitelist.con.isOnlineServer.equals("�����(�Զ����,������Ч)")  && (!CommandUtil.isnull(WhiteListPlugin.instance.whitelist.con.forceOnline) &&WhiteListPlugin.instance.whitelist.con.forceOnline.equalsIgnoreCase("Offline"))){
				MsgUtil.makeDebugMsgAndSend("ǿ��ģʽ����Ϊ�˵��� ���Ƿ�����֮ǰ������ isSamemode ����Ϊ false");
				isSameMode = false;
				return;
			}
			if(WhiteListPlugin.instance.whitelist.con.isOnlineServer != null && WhiteListPlugin.instance.whitelist.con.isOnlineServer.equals("�����(�Զ����,������Ч)")  && (!CommandUtil.isnull(WhiteListPlugin.instance.whitelist.con.forceOnline) &&WhiteListPlugin.instance.whitelist.con.forceOnline.equalsIgnoreCase("Online"))){
				MsgUtil.makeDebugMsgAndSend("ǿ��ģʽ����Ϊ������ ���Ƿ�����֮ǰ�ǵ��� isSamemode ����Ϊ false");
				isSameMode = false;
				return;
			}
			MsgUtil.makeDebugMsgAndSend((WhiteListPlugin.instance.whitelist.con.isOnlineServer !=null)+"");
			MsgUtil.makeDebugMsgAndSend(WhiteListPlugin.instance.whitelist.con.isOnlineServer.equals("�����(�Զ����,������Ч)")+"");
			MsgUtil.makeDebugMsgAndSend(!CommandUtil.isnull(WhiteListPlugin.instance.whitelist.con.forceOnline)+"");
			MsgUtil.makeDebugMsgAndSend(WhiteListPlugin.instance.whitelist.con.forceOnline.equalsIgnoreCase("Online")+"");
			MsgUtil.makeDebugMsgAndSend("isSamemode ����Ϊ true");
			isSameMode = true;
			return;
		}
	}



	@Override
	public void onDisable()
	{
		instance=null;
		if(protocol!=null)
			protocol.close();
		try {
			//closeServerSocket();
		} catch (Throwable e) {
			//System.out.println("���Թرշ����Socketʱ,���ִ���:"+e.getStackTrace().toString()+",��ʼ�ٴγ���!");
			e.printStackTrace();
			onDisable();
		}

	}
	/*
	public static void closeServerSocket() throws Throwable{
		String CraftBukkitPackage=Bukkit.getServer().getClass().getPackage().getName();
		String NMSPackage=CraftBukkitPackage.replace("org.bukkit.craftbukkit", "net.minecraft.server");
		Class<?> cls = Class.forName(NMSPackage+".RemoteControlSession");
		Constructor<?> con = cls.getConstructor();
//		Field serverSocket = cls.getDeclaredField("j");
//		serverSocket.setAccessible(true);
//		serverSocket = null;
		Method meth = cls.getDeclaredMethod("g");
		meth.setAccessible(true);
		//meth.invoke(cls.newInstance(new IMinecraftServer(),"",new Socket(127.0.0.1)));
		System.out.println("�ѹر�ServerSocket��ֵ!(BukkitWhitelist���1.14.4��bug�޸�����)");	}
*/
}

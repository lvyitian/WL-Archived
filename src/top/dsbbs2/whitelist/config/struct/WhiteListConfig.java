package top.dsbbs2.whitelist.config.struct;

import java.util.UUID;
import java.util.Vector;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import top.dsbbs2.whitelist.WhiteListPlugin;
import top.dsbbs2.whitelist.util.ServerUtil;

public class WhiteListConfig {
	public static class WLPlayer{
		public UUID uuid;
		public String name;
		public long QQ;
		public WLPlayer(UUID uuid,String name,long QQ)
		{
			this.uuid=uuid;
			this.name = name;
			this.QQ=QQ;
		}
		public WLPlayer(String name,long QQ)
		{
			this.name = name;
			this.QQ=QQ;
		}



		public Player toPlayer()
		{
			if(Bukkit.getServer().getOnlineMode() || WhiteListPlugin.instance.whitelist.getConfig().forceOnline.equalsIgnoreCase("Online") ) {
				return Bukkit.getPlayer(this.uuid);
			}else{
				return Bukkit.getPlayer(this.name);
			}
		}
		@Deprecated
		public OfflinePlayer toOfflinePlayerOld()
		{
			if(this.uuid==null){
				return Bukkit.getOfflinePlayer(this.name);
			}
			return Bukkit.getOfflinePlayer(this.uuid);
		}
		public OfflinePlayer toOfflinePlayer()
		{
			if(ServerUtil.isOnlineStorageMode()) {
				return Bukkit.getOfflinePlayer(this.uuid);
			}else{
				return Bukkit.getOfflinePlayer(this.name);
			}
		}
		public static WLPlayer fromPlayer(Player p,long QQ)
		{
			return new WLPlayer(p.getUniqueId(),p.getName(),QQ);
		}
		public static WLPlayer fromOfflinePlayer(OfflinePlayer p,long QQ)
		{
			return new WLPlayer(p.getUniqueId(),p.getName(),QQ);
		}
	}
	public String mess = "�㻹���ڰ�������,�ڴ�֮ǰ�㽫���ܽ����κζԴ˷�������ʵ���Զ���";
	public String PlayerCantJoinMSG = "��Ŀǰû�а�����,�޷�����,�������������!";
	public String congratulate = "��ϲ����ð�����!";
	public String unCongratulate = "��ʧȥ�˰�����!";
	public String on_UUID_Is_Right_But_Name = "��⵽���İ������е�Name����,����Ⱥ�� '@������+��֤',���������û�жԽ�Ⱥ,���ҹ���Ա����/wl confirm <����QQ��>";
	public String on_Name_Is_Right_But_UUID = "��⵽���İ������е�UUID����,����Ⱥ�� '@������+��֤',���������û�жԽ�Ⱥ,���ҹ���Ա����/wl confirm <����QQ��>";
	public String isOnlineServer = Bukkit.getServer().getOnlineMode()==true?"�����(�Զ����,������Ч)":"�����(�Զ����,������Ч)";
	public String forceOnline = "No";
	public boolean useBlackList = true;
	/*
		No = ������ǿ��
		Online = "ǿ������ģʽΪonline"
		Offline = "ǿ������ģʽΪOffline"
	 */
	public boolean uesLoginPluginOrNot = true;
	public boolean useSkinonWLList = false;
	public boolean debugMode = false;
	public boolean canNoWhitePlayerGetIn = true;
	public boolean enableTabIntercept = false;
	public boolean antiNPCBug = true;
	public Vector<WLPlayer> players=new Vector<>();

}

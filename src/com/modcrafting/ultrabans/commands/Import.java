/* COPYRIGHT (c) 2012 Joshua McCurry
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
 * and use of this software or its code is an agreement to this license.
 * A full copy of this license can be found at
 * http://creativecommons.org/licenses/by-nc-sa/3.0/. 
 */
package com.modcrafting.ultrabans.commands;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import com.modcrafting.ultrabans.Ultrabans;

public class Import implements CommandExecutor{
	Ultrabans plugin;
	String permission = "ultraban.import";
	public Import(Ultrabans ultraBan) {
	this.plugin = ultraBan;
	}
	@SuppressWarnings("deprecation")
	public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED+plugin.perms);
			return true;
		}
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin,new Runnable(){
			@Override
			public void run() {
				String msg = plugin.getConfig().getString("Messages.Import.Loading","Be patient. Loading...");
				msg=plugin.util.formatMessage(msg);
				sender.sendMessage(ChatColor.GRAY + msg);
				try {
					BufferedReader banlist = new BufferedReader(new FileReader("banned-players.txt"));
					String p;
					while ((p = banlist.readLine()) != null){
						if(!p.contains("#")&&p.length()>0){
							g(p);
						}
					}
					banlist.close();
					BufferedReader bannedIP = new BufferedReader(new FileReader("banned-ips.txt"));
					String ip;
					while ((ip = bannedIP.readLine()) != null){
						if(ip!=null&&!ip.contains("#")&&ip.length()>0){
						    String[] args = ip.split("\\|");
						    String name = args[0].trim();
							if(!plugin.bannedIPs.contains(name)) plugin.bannedIPs.add(name);
							String cknullIP = plugin.db.getName(name);
							if (cknullIP != null){
								plugin.db.addPlayer(plugin.db.getName(name), "imported", args[2].trim(), 0, 1);
							}else{
								plugin.db.setAddress("import", name);
								plugin.db.addPlayer("import", "imported", args[2].trim(), 0, 1);
							}
						}
					}
					bannedIP.close();
					msg = plugin.getConfig().getString("Messages.Import.Completed","System imported the banlist to the database.");
					msg=plugin.util.formatMessage(msg);
					sender.sendMessage(ChatColor.GRAY + msg);
					plugin.getLogger().info(msg);
				} catch (IOException e) {
					msg = plugin.getConfig().getString("Messages.Import.Failed","Could not import ban list.");
					msg=plugin.util.formatMessage(msg);
					sender.sendMessage(ChatColor.RED + msg);
					plugin.getLogger().severe(msg);
				}
			}
		});	
		return true;
	}

	public void g(String line) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
	    String[] args = line.split("\\|");
	    if(args.length<4) return;
	    String name = args[0].trim();
	    long temp = 0;
	    int type = 0;
	    try {
		    if(!args[3].trim().equalsIgnoreCase("Forever")){
		    	temp=format.parse(args[3].trim()).getTime();
		    }
		} catch (ParseException e) {
			e.printStackTrace();
		}
	    String admin = args[2].trim();
	    String reason = args[4];
		plugin.db.addPlayer(name, reason, admin, temp, type);
	}
}

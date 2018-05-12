package com.focess.team.listener;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.focess.team.command.CountryChatCommand;
import com.focess.team.command.TeamChatCommand;
import com.focess.team.team.Country;
import com.focess.team.team.Team;

public class AsyncPlayerChatListener implements Listener {

	private static com.focess.team.Team team;

	private final HashMap<String, String> messages = new HashMap<>();

	public AsyncPlayerChatListener(final com.focess.team.Team team) {
		AsyncPlayerChatListener.team = team;
		this.loadConfig();
	}

	private String getMessage(final String key) {
		return this.messages.get(key);
	}

	private void loadConfig() {
		final File message = new File(
				AsyncPlayerChatListener.team.getDataFolder(), "message.yml");
		final YamlConfiguration yml = YamlConfiguration
				.loadConfiguration(message);
		final Set<String> keys = yml.getKeys(false);
		for (final String key : keys)
			this.messages.put(key, yml.getString(key));
	}

	@EventHandler
	public void onAsyncPlayerChat(final AsyncPlayerChatEvent event) {
		event.setCancelled(true);
		boolean isCountry = false;
		Country c = null;
		for (final Country country : Country.listCountries())
			if (country.includePlayer(event.getPlayer())) {
				isCountry = true;
				c = country;
			}
		if (!isCountry) {
			event.getPlayer().sendMessage(this.getMessage("NoTalk"));
			return;
		}
		boolean isTeam = false;
		Team t = null;
		for (final Team team : c.getTeams())
			if (team.getMembers().contains(event.getPlayer().getName())) {
				isTeam = true;
				t = team;
			}
		String tname = "";
		if (isTeam)
			tname = t.getName();
		final String message = AsyncPlayerChatListener.team.getConfig()
				.getString("chatformat").replace("%country%", c.getName())
				.replace("%team%", tname)
				.replace("%player%", event.getPlayer().getName())
				+ " " + event.getMessage().replace("&", "§");
		if (CountryChatCommand.ccs.get(c) != null)
			if (CountryChatCommand.ccs.get(c).contains(event.getPlayer())) {
				for (final Player player : CountryChatCommand.ccs.get(c))
					player.sendMessage(message);
				return;
			}
		if (isTeam)
			if (TeamChatCommand.tcs.get(c.getName() + "." + t.getName()) != null)
				if (TeamChatCommand.tcs.get(c.getName() + "." + t.getName())
						.contains(event.getPlayer()))
					for (final Player player : TeamChatCommand.tcs.get(c
							.getName() + "." + t.getName()))
						player.sendMessage(message);
		for (final Player player : Bukkit.getOnlinePlayers())
			player.sendMessage(message);
		Bukkit.getServer().getConsoleSender().sendMessage(message);
	}

}

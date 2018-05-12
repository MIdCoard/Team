package com.focess.team.listener;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.focess.team.team.Country;
import com.focess.team.team.Team;

public class PlayerMoveListener extends Permission implements Listener {

	private final HashMap<String, String> messages = new HashMap<String, String>();
	private final com.focess.team.Team team;

	public PlayerMoveListener(final com.focess.team.Team team) {
		this.team = team;
		this.loadConfig();
	}

	@Override
	public String getLabel() {
		return "move";
	}

	private String getMessage(final String key) {
		return this.messages.get(key);
	}

	private void loadConfig() {
		final File message = new File(this.team.getDataFolder(), "message.yml");
		final YamlConfiguration yml = YamlConfiguration
				.loadConfiguration(message);
		final Set<String> keys = yml.getKeys(false);
		for (final String key : keys)
			this.messages.put(key, yml.getString(key));
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerMove(final PlayerMoveEvent event) {
		for (final TeamOfCountry tc : this.tcbs) {
			final Country c = tc.country;
			final Team t = tc.team;
			final boolean per = tc.value;
			if (c.includeTeam(t.getName()))
				if (t.inside(event.getTo())) {
					if (per)
						if (!t.getMembers().contains(
								event.getPlayer().getName())) {
							event.setCancelled(true);
							event.getPlayer().sendMessage(
									this.getMessage("MovePerError").replace(
											"%team%", t.getName()));
							for (final String p : t.getMembers()) {
								final File player = new File(this.team
										.getDataFolder().getPath()
										+ "/players/" + p + ".yml");
								if (player.exists()) {
									final YamlConfiguration yml = YamlConfiguration
											.loadConfiguration(player);
									if (yml.contains("isNotice"))
										if (!yml.getBoolean("isNotice"))
											return;
								}
								final Player pp = Bukkit.getPlayerExact(p);
								if (pp != null)
									pp.sendMessage(this
											.getMessage("MoveNError")
											.replace("%player%",
													event.getPlayer().getName()));
							}

						}
					if (!t.inside(event.getFrom()))
						for (final String p : t.getMembers()) {
							final File player = new File(this.team
									.getDataFolder().getPath()
									+ "/players/"
									+ p + ".yml");
							if (player.exists()) {
								final YamlConfiguration yml = YamlConfiguration
										.loadConfiguration(player);
								if (yml.contains("isNotice"))
									if (!yml.getBoolean("isNotice"))
										return;
							}
							final Player pp = Bukkit.getPlayerExact(p);
							if (pp != null)
								pp.sendMessage(this.getMessage("MoveN")
										.replace("%player%",
												event.getPlayer().getName()));
						}
				}
		}
	}
}

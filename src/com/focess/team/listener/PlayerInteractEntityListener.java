package com.focess.team.listener;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import com.focess.team.Team;
import com.focess.team.team.Country;

public class PlayerInteractEntityListener implements Listener {

	private final HashMap<String, String> messages = new HashMap<String, String>();

	private final Team team;

	public PlayerInteractEntityListener(final Team team) {
		this.team = team;
		this.loadConfig();
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

	@EventHandler
	public void onPlayerInteractEntity(final PlayerInteractEntityEvent event) {
		if (event.getRightClicked() instanceof Player) {
			boolean flag = false;
			Country c = null;
			for (final Country country : Country.listCountries())
				if (country.includePlayer((Player) event.getRightClicked())) {
					flag = true;
					c = country;
					break;
				}
			Country c2 = null;
			boolean flag2 = false;
			for (final Country country : Country.listCountries())
				if (country.includePlayer(event.getPlayer())) {
					flag2 = true;
					c2 = country;
					break;
				}
			String relation = "";
			if (flag2)
				if (c.isEnemy(c2))
					relation = "敌国";
				else if (c.isFriend(c2))
					relation = "友国";
			if (flag)
				event.getPlayer().sendMessage(
						this.getMessage("Interact")
								.replace(
										"%player%",
										((Player) event.getRightClicked())
												.getName())
								.replace("%country%", c.getName())
								.replace("%relation%", relation));
			event.setCancelled(true);
		}
	}

}

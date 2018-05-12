package com.focess.team.listener;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.focess.team.Team;
import com.focess.team.team.Country;

public class PlayerDeathListener implements Listener {

	private final HashMap<String, String> messages = new HashMap<>();

	private final Team team;

	public PlayerDeathListener(final Team team) {
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

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerDeath(final PlayerDeathEvent event) {
		if (event.getEntity().getKiller() != null) {
			boolean isCountry = false;
			Country c = null;
			for (final Country country : Country.listCountries())
				if (country.includePlayer(event.getEntity())) {
					isCountry = true;
					c = country;
				}
			if (!isCountry)
				return;
			boolean isCountry2 = false;
			Country c2 = null;
			for (final Country country : Country.listCountries())
				if (country.includePlayer(event.getEntity().getKiller())) {
					isCountry2 = true;
					c2 = country;
				}
			if (!isCountry2)
				return;
			if (Country.haveRelation(c, c2))
				if (Country.getRelation(c, c2)) {
					this.team.economy.withdrawPlayer(event.getEntity()
							.getKiller().getName(), this.team.getConfig()
							.getInt("killfriend"));
					event.getEntity()
							.getKiller()
							.sendMessage(
									this.getMessage("KillFriend")
											.replace("%player%",
													event.getEntity().getName())
											.replace(
													"%money%",
													this.team
															.getConfig()
															.getInt("killfriend")
															+ ""));
				} else {
					this.team.economy.depositPlayer(event.getEntity()
							.getKiller().getName(), this.team.getConfig()
							.getInt("killenemy"));
					event.getEntity()
							.getKiller()
							.sendMessage(
									this.getMessage("KillEnemy")
											.replace("%player%",
													event.getEntity().getName())
											.replace(
													"%money%",
													this.team
															.getConfig()
															.getInt("killenemy")
															+ ""));
				}
		}
	}

}

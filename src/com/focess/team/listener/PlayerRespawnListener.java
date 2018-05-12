package com.focess.team.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.focess.team.team.Country;

public class PlayerRespawnListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerRespawn(final PlayerRespawnEvent event) {
		for (final Country country : Country.listCountries())
			if (country.includePlayer(event.getPlayer()))
				if (country.getSpawnLocation() != null)
					event.setRespawnLocation(country.getSpawnLocation());
	}

}

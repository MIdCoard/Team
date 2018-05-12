package com.focess.team.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import com.focess.team.team.Country;
import com.focess.team.team.Team;

public class InventoryCloseListener implements Listener {

	@EventHandler
	public void onInventoryClose(final InventoryCloseEvent event) {
		if (event.getPlayer() instanceof Player && event.getPlayer().isOp())
			if (event.getInventory().getName().startsWith("§a请放入为队伍")
					&& event.getInventory().getName().endsWith("存放的物资")) {
				final String name = event.getInventory().getName()
						.replace("§a请放入为队伍", "").replace("存放的物资", "");
				final String[] temp = name.split("\\.");
				if (!Country.include(temp[0]))
					return;
				for (final Team team : Country.getCountry(temp[0]).getTeams())
					if (team.getName().equalsIgnoreCase(temp[1])) {
						team.setItemStacks(event.getInventory().getContents());
						break;
					}
			}
	}

}

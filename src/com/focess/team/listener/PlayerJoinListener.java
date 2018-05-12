package com.focess.team.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.focess.team.Team;
import com.focess.team.team.Country;

public class PlayerJoinListener implements Listener {

	private class RefreshJoinPlayer extends BukkitRunnable {
		private final Country country;
		private final Player player;

		public RefreshJoinPlayer(final Country country, final Player player) {
			this.country = country;
			this.player = player;
			this.runTaskTimer(PlayerJoinListener.team, 0, 20);
		}

		@Override
		public void run() {
			if (this.country.includePlayer(this.player)) {
				if (this.country.refreshPlayer(this.player.getName()))
					this.cancel();
			} else
				this.cancel();
		}
	}

	private static Team team;

	public PlayerJoinListener(final Team team) {
		PlayerJoinListener.team = team;
	}

	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event) {
		for (final Country country : Country.listCountries())
			if (country.includePlayer(event.getPlayer()))
				new RefreshJoinPlayer(country, event.getPlayer());
	}

}

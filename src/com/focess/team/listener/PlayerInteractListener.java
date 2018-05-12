package com.focess.team.listener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import com.focess.team.Team;
import com.focess.team.team.Country;

public class PlayerInteractListener extends Permission implements Listener {

	public static class CheckedPlace {

		private static List<CheckedPlace> cps = new ArrayList<>();

		private static void addLocation(final Player player,
				final Location location) {
			boolean flag = false;
			CheckedPlace c = null;
			for (final CheckedPlace cp : CheckedPlace.cps)
				if (cp.player.equals(player)) {
					flag = true;
					c = cp;
					break;
				}
			if (flag)
				c.addLocation(location);
			else
				new CheckedPlace(player).addLocation(location);
		}

		public static CheckedPlace getCheckedPlace(final Player player) {
			for (final CheckedPlace cp : CheckedPlace.cps)
				if (cp.player.equals(player))
					return cp;
			return null;
		}

		private Location l1;

		private Location l2;

		private final Player player;

		public CheckedPlace(final Player player) {
			this.player = player;
			CheckedPlace.cps.add(this);
		}

		private void addLocation(final Location location) {
			if (this.l1 == null) {
				this.l1 = location;
				this.player.sendMessage(PlayerInteractListener
						.getMessage("Location1"));
			} else if (this.l2 == null)
				if (!this.l1.getWorld().getName().equals(location.getWorld())) {
					this.l2 = location;
					this.player.sendMessage(PlayerInteractListener
							.getMessage("Location2"));
				} else
					this.player.sendMessage(PlayerInteractListener
							.getMessage("Location2Error"));
			else {
				this.l1 = location;
				this.player.sendMessage(PlayerInteractListener
						.getMessage("Location1"));
				this.l2 = null;
			}
		}

		public Location getLocation1() {
			return this.l1;
		}

		public Location getLocation2() {
			return this.l2;
		}

	}

	private static HashMap<String, String> messages = new HashMap<>();

	private static String getMessage(final String key) {
		return PlayerInteractListener.messages.get(key);
	}

	private final Team team;

	public PlayerInteractListener(final Team team) {
		this.team = team;
		this.loadConfig();
	}

	@Override
	public String getLabel() {
		return "use";
	}

	private void loadConfig() {
		final File message = new File(this.team.getDataFolder(), "message.yml");
		final YamlConfiguration yml = YamlConfiguration
				.loadConfiguration(message);
		final Set<String> keys = yml.getKeys(false);
		for (final String key : keys)
			PlayerInteractListener.messages.put(key, yml.getString(key));
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerInteract(final PlayerInteractEvent event) {
		if (event.getPlayer().isOp())
			if (event.getItem() != null)
				if (event.getItem().getType().equals(Material.BLAZE_ROD))
					if (event.getClickedBlock() != null) {
						CheckedPlace.addLocation(event.getPlayer(), event
								.getClickedBlock().getLocation());
						event.setCancelled(true);
					}
		for (final TeamOfCountry tc : this.tcbs) {
			final Country c = tc.country;
			final com.focess.team.team.Team t = tc.team;
			final boolean per = tc.value;
			if (c.includeTeam(t.getName()))
				if (event.getClickedBlock() != null)
					if (t.inside(event.getClickedBlock().getLocation()))
						if (per)
							if (!t.getMembers().contains(
									event.getPlayer().getName())) {
								event.setCancelled(true);
								event.getPlayer().sendMessage(
										PlayerInteractListener.getMessage(
												"UsePerError").replace(
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
										pp.sendMessage(PlayerInteractListener
												.getMessage("UseNError")
												.replace(
														"%player%",
														event.getPlayer()
																.getName()));
								}
							}
		}
	}

}

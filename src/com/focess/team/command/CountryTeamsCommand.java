package com.focess.team.command;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.focess.team.Team;
import com.focess.team.listener.PlayerInteractListener.CheckedPlace;
import com.focess.team.team.Country;

public class CountryTeamsCommand extends Command {

	private static class RemoveWaiting extends BukkitRunnable {

		private static final List<RemoveWaiting> rws = new CopyOnWriteArrayList<>();

		private Player player;

		private com.focess.team.team.Team team;

		private int time = 0;

		private RemoveWaiting(final Player player,
				final com.focess.team.team.Team team) {
			for (final RemoveWaiting rw : RemoveWaiting.rws)
				if (rw.player.equals(player)) {
					rw.time = 0;
					return;
				}
			this.player = player;
			this.team = team;
			this.runTaskTimer(CountryTeamsCommand.team, 0, 20);
			RemoveWaiting.rws.add(this);
		}

		private void delete() {
			RemoveWaiting.rws.remove(this);
			this.cancel();
		}

		@Override
		public void run() {
			this.time++;
			if (this.time == CountryTeamsCommand.team.getConfig()
					.getInt("time"))
				this.delete();
		}

	}

	private static Team team;

	private final HashMap<String, String> messages = new HashMap<>();

	public CountryTeamsCommand(final List<String> aliases, final Team team) {
		super("CountryTeams", "", "", aliases);
		CountryTeamsCommand.team = team;
		this.loadConfig();
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean execute(final CommandSender sender, final String cmd,
			final String[] args) {
		if (sender instanceof Player) {
			if (args.length == 1)
				if (args[0].equalsIgnoreCase("list")) {
					boolean isCountry = false;
					Country country = null;
					for (final Country c : Country.listCountries())
						if (c.getPlayers().contains(sender.getName())) {
							sender.sendMessage(this.getMessage("ListCountry")
									+ ": " + c.getName());
							isCountry = true;
							country = c;
							break;
						}
					if (!isCountry) {
						sender.sendMessage(this.getMessage("NoCountry"));
						sender.sendMessage(this.getMessage("NoTeam"));
						return true;
					}
					boolean isTeam = false;
					for (final com.focess.team.team.Team team : country
							.getTeams())
						if (team.getMembers().contains(sender.getName())) {
							sender.sendMessage(this.getMessage("ListTeam")
									+ ": " + team.getName());
							isTeam = true;
							break;
						}
					if (!isTeam)
						sender.sendMessage(this.getMessage("NoTeam"));
				} else if (args[0].equalsIgnoreCase("remove")) {
					boolean isCountry = false;
					boolean isTeam = false;
					for (final Country c : Country.listCountries())
						if (c.includePlayer((Player) sender)) {
							isCountry = true;
							for (final com.focess.team.team.Team team : c
									.getTeams())
								if (team.getMembers()
										.contains(sender.getName())) {
									isTeam = true;
									sender.sendMessage(this
											.getMessage("RemoveNotice")
											.replace(
													"%money%",
													CountryTeamsCommand.team
															.getConfig()
															.getString("money"))
											.replace(
													"%time%",
													CountryTeamsCommand.team
															.getConfig()
															.getString("time")));
									new RemoveWaiting((Player) sender, team);
								}
						}
					if (!isCountry)
						sender.sendMessage(this.getMessage("NoCountry"));
					if (!isTeam)
						sender.sendMessage(this.getMessage("NoTeam"));
				} else if (args[0].equalsIgnoreCase("confirm")) {
					boolean isExists = false;
					if (RemoveWaiting.rws != null)
						for (final RemoveWaiting rw : RemoveWaiting.rws)
							if (rw.player.equals(sender)) {
								isExists = true;
								if (!CountryTeamsCommand.team.economy
										.withdrawPlayer(
												sender.getName(),
												CountryTeamsCommand.team
														.getConfig().getDouble(
																"money"))
										.transactionSuccess()) {
									sender.sendMessage(this
											.getMessage("NoMoney"));
									sender.sendMessage(CountryTeamsCommand.team
											.getConfig().getDouble("money")
											+ "");
									rw.delete();
								} else {
									rw.team.removeMember((Player) sender);

									rw.delete();
									sender.sendMessage(this
											.getMessage("Remove"));
								}
							}
					if (!isExists)
						sender.sendMessage(this.getMessage("NoConfirm"));
				} else if (args[0].equalsIgnoreCase("get")) {
					boolean isCountry = false;
					Country c = null;
					for (final Country country : Country.listCountries())
						if (country.includePlayer((Player) sender)) {
							isCountry = true;
							c = country;
							break;
						}
					if (!isCountry) {
						sender.sendMessage(this.getMessage("NoCountry"));
						return true;
					}
					boolean isTeam = false;
					com.focess.team.team.Team t = null;
					for (final com.focess.team.team.Team team : c.getTeams())
						if (team.getMembers().contains(sender.getName())) {
							isTeam = true;
							t = team;
							break;
						}
					if (!isTeam) {
						sender.sendMessage(this.getMessage("NoTeam"));
						return true;
					}
					boolean flag = false;
					final File players = new File(
							CountryTeamsCommand.team.getDataFolder(), "players");
					File p = null;
					for (final File player : players.listFiles())
						if (player.getName().equalsIgnoreCase(
								sender.getName() + ".yml")) {
							flag = true;
							p = player;
						}
					if (flag) {
						final YamlConfiguration yml = YamlConfiguration
								.loadConfiguration(p);
						if (yml.contains("isGet"))
							if (yml.getBoolean("isGet")) {
								sender.sendMessage(this
										.getMessage("HaveGotten"));
								return true;
							} else {
								((Player) sender).getInventory().addItem(
										t.getItemStacks());
								yml.set("isGet", true);
								try {
									yml.save(p);
								} catch (final IOException e) {
									e.printStackTrace();
								}
							}
						else {
							((Player) sender).getInventory().addItem(
									t.getItemStacks());
							yml.set("isGet", true);
							try {
								yml.save(p);
							} catch (final IOException e) {
								e.printStackTrace();
							}
						}
					} else {
						final File player = new File(players, sender.getName()
								+ ".yml");
						try {
							player.createNewFile();
						} catch (final IOException e) {
							e.printStackTrace();
						}
						final YamlConfiguration yml = YamlConfiguration
								.loadConfiguration(player);
						((Player) sender).getInventory().addItem(
								t.getItemStacks());
						yml.set("isGet", true);
						try {
							yml.save(player);
						} catch (final IOException e) {
							e.printStackTrace();
						}
					}
					sender.sendMessage(this.getMessage("Get"));
				} else if (args[0].equalsIgnoreCase("notice")) {
					final File player = new File(CountryTeamsCommand.team
							.getDataFolder().getPath()
							+ "/players/"
							+ sender.getName() + ".yml");
					if (!player.exists())
						try {
							player.createNewFile();
						} catch (final IOException e) {
							e.printStackTrace();
						}
					final YamlConfiguration yml = YamlConfiguration
							.loadConfiguration(player);
					if (yml.contains("isNotice"))
						if (yml.getBoolean("isNotice"))
							yml.set("isNotice", false);
						else
							yml.set("isNotice", true);
					else
						yml.set("isNotice", false);
					try {
						yml.save(player);
					} catch (final IOException e) {
						e.printStackTrace();
					}
					if (yml.getBoolean("isNotice"))
						sender.sendMessage(this.getMessage("Notice"));
					else
						sender.sendMessage(this.getMessage("NoticeNot"));
				} else
					sender.sendMessage(this.getMessage("CommandError"));
			else if (args.length == 2)
				if (args[0].equalsIgnoreCase("create"))
					if (sender.isOp())
						if (Country.createCountry(args[1].replace("&", "§")))
							sender.sendMessage(this.getMessage("CreateCountry")
									+ ": " + args[1].replace("&", "§"));
						else
							sender.sendMessage(this
									.getMessage("CreateCountryError"));
					else
						sender.sendMessage(this.getMessage("SenderNotOp"));
				else if (args[0].equalsIgnoreCase("add"))
					if (Country.include(args[1].replace("&", "§")))
						if (!Country.getCountry(args[1].replace("&", "§"))
								.includePlayer((Player) sender)) {
							for (final Country country : Country
									.listCountries())
								if (country.includePlayer((Player) sender)) {
									sender.sendMessage(this
											.getMessage("HaveJoined"));
									return true;
								}
							Country.getCountry(args[1].replace("&", "§"))
									.addPlayer((Player) sender);
							sender.sendMessage(this.getMessage("AddCountry")
									+ ": " + args[1].replace("&", "§"));
						} else
							sender.sendMessage(this
									.getMessage("AddCountryError")
									+ ": "
									+ args[1].replace("&", "§"));
					else
						sender.sendMessage(this.getMessage("CountryNotFound"));
				else if (args[0].equalsIgnoreCase("list"))
					if (Country.include(args[1].replace("&", "§")))
						if (Country.getCountry(args[1].replace("&", "§"))
								.includePlayer((Player) sender)) {
							sender.sendMessage(this
									.getMessage("ListCountryPlayer") + ":");
							final StringBuilder sb = new StringBuilder();
							for (final String p : Country.getCountry(
									args[1].replace("&", "§")).getPlayers())
								sb.append(p + "  ");
							sender.sendMessage(sb.toString());
						} else if (sender.isOp()) {
							sender.sendMessage(this
									.getMessage("ListCountryPlayer") + ":");
							final StringBuilder sb = new StringBuilder();
							for (final String p : Country.getCountry(
									args[1].replace("&", "§")).getPlayers())
								sb.append(p + "  ");
							sender.sendMessage(sb.toString());
						} else
							sender.sendMessage(this
									.getMessage("SenderNotPlayer"));
					else
						sender.sendMessage(this.getMessage("CountryNotFound"));
				else if (args[0].equalsIgnoreCase("listplayer")) {
					boolean isCountry = false;
					Country country = null;
					for (final Country c : Country.listCountries())
						if (c.getPlayers().contains(args[1].replace("&", "§"))) {
							sender.sendMessage(this
									.getMessage("ListPlayerCountry")
									+ ": "
									+ c.getName());
							isCountry = true;
							country = c;
							break;
						}
					if (!isCountry) {
						sender.sendMessage(this.getMessage("NoPlayerCountry"));
						sender.sendMessage(this.getMessage("NoPlayerTeam"));
						return true;
					}
					boolean isTeam = false;
					for (final com.focess.team.team.Team team : country
							.getTeams())
						if (team.getMembers().contains(args[1])) {
							sender.sendMessage(this
									.getMessage("ListPlayerTeam")
									+ ": "
									+ team.getName());
							isTeam = true;
							break;
						}
					if (!isTeam)
						sender.sendMessage(this.getMessage("NoPlayerTeam"));
				} else if (args[0].equalsIgnoreCase("remove"))
					if (sender.isOp())
						if (Country.include(args[1].replace("&", "§"))) {
							Country.getCountry(args[1].replace("&", "§"))
									.remove();
							sender.sendMessage(this.getMessage("RemoveCountry"));
						} else
							sender.sendMessage(this
									.getMessage("CountryNotFound"));
					else
						sender.sendMessage(this.getMessage("SenderNotOp"));
				else
					sender.sendMessage(this.getMessage("CommandError"));

			else if (args.length == 3)
				if (args[0].equalsIgnoreCase("create"))
					if (sender.isOp()) {
						final Country country = Country.getCountry(args[2]
								.replace("&", "§"));
						if (country == null) {
							sender.sendMessage(this
									.getMessage("CountryNotFound"));
							return true;
						}
						if (country.createTeam(args[1]))
							sender.sendMessage(this.getMessage("CreateTeam")
									+ ": " + args[1]);
						else
							sender.sendMessage(this
									.getMessage("CreateTeamError"));
					} else
						sender.sendMessage(this.getMessage("SenderNotOp"));
				else if (args[0].equalsIgnoreCase("add")) {
					final Country country = Country.getCountry(args[2].replace(
							"&", "§"));
					if (country == null) {
						sender.sendMessage(this.getMessage("CountryNotFound"));
						return true;
					}
					if (!country.includePlayer((Player) sender)) {
						sender.sendMessage(this.getMessage("NotCountry"));
						return true;
					}
					for (final com.focess.team.team.Team team : country
							.getTeams())
						if (team.getMembers().contains(sender.getName())) {
							sender.sendMessage(this
									.getMessage("HaveJoinedTeam"));
							return true;
						}
					if (country.includeTeam(args[1]))
						if (country.addTeamPlayer(args[1], (Player) sender))
							sender.sendMessage(this.getMessage("AddTeam")
									+ ": " + args[1]);
						else
							sender.sendMessage(this.getMessage("AddTeamError"));
					else
						sender.sendMessage(this.getMessage("TeamNotFound"));
				} else if (args[0].equalsIgnoreCase("list")) {
					final Country country = Country.getCountry(args[2].replace(
							"&", "§"));
					if (country == null) {
						sender.sendMessage(this.getMessage("CountryNotFound"));
						return true;
					}
					if (country.includePlayer((Player) sender))
						if (country.includeTeam(args[1]))
							if (country.getTeam(args[1]).getMembers()
									.contains(sender.getName())) {
								sender.sendMessage(this
										.getMessage("ListTeamPlayer") + ":");
								final StringBuilder sb = new StringBuilder();
								for (final String p : country.getTeam(args[1])
										.getMembers())
									sb.append(p + "  ");
								sender.sendMessage(sb.toString());
							} else if (sender.isOp()) {
								sender.sendMessage(this
										.getMessage("ListTeamPlayer") + ":");
								final StringBuilder sb = new StringBuilder();
								for (final String p : country.getTeam(args[1])
										.getMembers())
									sb.append(p + "  ");
								sender.sendMessage(sb.toString());
							} else
								sender.sendMessage(this
										.getMessage("SenderNotOp"));
						else
							sender.sendMessage(this.getMessage("TeamNotFound"));
					else if (sender.isOp())
						if (country.includeTeam(args[1])) {
							sender.sendMessage(this
									.getMessage("ListTeamPlayer") + ":");
							final StringBuilder sb = new StringBuilder();
							for (final String p : country.getTeam(args[1])
									.getMembers())
								sb.append(p + "  ");
							sender.sendMessage(sb.toString());
						} else
							sender.sendMessage(this.getMessage("TeamNotFound"));
					else
						sender.sendMessage(this.getMessage("SenderNotOp"));
				} else if (args[0].equalsIgnoreCase("friend"))
					if (Country.include(args[1].replace("&", "§"))
							&& Country.include(args[2].replace("&", "§")))
						if (Country.getCountry(args[1].replace("&", "§"))
								.addFriend(
										Country.getCountry(args[2].replace("&",
												"§"))))
							sender.sendMessage(this.getMessage("AddFriend"));
						else
							sender.sendMessage(this
									.getMessage("AddFriendError"));
					else
						sender.sendMessage(this.getMessage("CountryNotFound"));
				else if (args[0].equalsIgnoreCase("enemy"))
					if (Country.include(args[1].replace("&", "§"))
							&& Country.include(args[2].replace("&", "§")))
						if (Country.getCountry(args[1].replace("&", "§"))
								.addEnemy(
										Country.getCountry(args[2].replace("&",
												"§"))))
							sender.sendMessage(this.getMessage("AddEnemy"));
						else
							sender.sendMessage(this.getMessage("AddEnemyError"));
					else
						sender.sendMessage(this.getMessage("CountryNotFound"));
				else if (args[0].equalsIgnoreCase("remove"))
					if (Country.include(args[1].replace("&", "§"))
							&& Country.include(args[2].replace("&", "§")))
						if (Country.getCountry(args[1].replace("&", "§"))
								.remove(Country.getCountry(args[2].replace("&",
										"§"))))
							sender.sendMessage(this.getMessage("RemoveR"));
						else
							sender.sendMessage(this.getMessage("RemoveError"));
					else
						sender.sendMessage(this.getMessage("CountryNotFound"));
				else if (args[0].equalsIgnoreCase("open"))
					if (sender.isOp())
						if (Country.include(args[2].replace("&", "§"))) {
							boolean flag = false;
							com.focess.team.team.Team t = null;
							for (final com.focess.team.team.Team team : Country
									.getCountry(args[2].replace("&", "§"))
									.getTeams())
								if (team.getName().equalsIgnoreCase(args[1])) {
									t = team;
									flag = true;
									break;
								}
							if (!flag)
								sender.sendMessage(this
										.getMessage("TeamNotFound"));
							t.openInventory((Player) sender, Country
									.getCountry(args[2].replace("&", "§")));
						} else
							sender.sendMessage(this
									.getMessage("CountryNotFound"));
					else
						sender.sendMessage(this.getMessage("SenderNotOp"));
				else if (args[0].equalsIgnoreCase("realm"))
					if (sender.isOp())
						if (Country.include(args[2].replace("&", "§"))) {
							final CheckedPlace cp = CheckedPlace
									.getCheckedPlace((Player) sender);
							if (cp == null)
								sender.sendMessage(this
										.getMessage("RealmError"));
							boolean flag = false;
							com.focess.team.team.Team t = null;
							for (final com.focess.team.team.Team team : Country
									.getCountry(args[2].replace("&", "§"))
									.getTeams())
								if (team.getName().equalsIgnoreCase(args[1])) {
									flag = true;
									t = team;
									break;
								}
							if (!flag) {
								sender.sendMessage(this
										.getMessage("TeamNotFound"));
								return true;
							}
							if (t.setRealm(cp))
								sender.sendMessage(this.getMessage("Realm"));
							else
								sender.sendMessage(this
										.getMessage("RealmError"));
						} else
							sender.sendMessage(this
									.getMessage("CountryNotFound"));
					else
						sender.sendMessage(this.getMessage("SenderNotOp"));
				else if (args[0].equalsIgnoreCase("removeteam"))
					if (sender.isOp())
						if (Country.include(args[2].replace("&", "§")))
							if (Country.getCountry(args[2].replace("&", "§"))
									.getTeam(args[1]) != null) {
								Country.getCountry(args[2].replace("&", "§"))
										.removeTeam(args[1]);
								sender.sendMessage(this
										.getMessage("RemoveTeam"));
							} else
								sender.sendMessage(this
										.getMessage("TeamNotFound"));
						else
							sender.sendMessage(this
									.getMessage("CountryNotFound"));
					else
						sender.sendMessage(this.getMessage("SenderNotOp"));
				else if (args[0].equalsIgnoreCase("removeplayer"))
					if (sender.isOp())
						if (Country.include(args[2].replace("&", "§")))
							if (Country.getCountry(args[2].replace("&", "§"))
									.includePlayer(args[1])) {
								Country.getCountry(args[2].replace("&", "§"))
										.removePlayer(args[1]);
								sender.sendMessage(this
										.getMessage("RemovePlayer"));
							} else
								sender.sendMessage(this
										.getMessage("TeamNotFound"));
						else
							sender.sendMessage(this
									.getMessage("CountryNotFound"));
					else
						sender.sendMessage(this.getMessage("SenderNotOp"));
				else
					sender.sendMessage(this.getMessage("CommandError"));
			else if (args.length == 4)
				if (args[0].equalsIgnoreCase("set"))
					if (sender.isOp())
						if (Country.include(args[3].replace("&", "§")))
							if (Country.getCountry(args[3].replace("&", "§"))
									.getTeam(args[2]) != null) {
								final Player p = Bukkit.getPlayerExact(args[1]);
								if (p != null)
									p.sendMessage(this.getMessage("Team"));
								Country.getCountry(args[3].replace("&", "§"))
										.getTeam(args[2]).setCaptain(args[1]);
								sender.sendMessage(this.getMessage("Set"));
							} else
								sender.sendMessage(this
										.getMessage("TeamNotFound"));
						else
							sender.sendMessage(this
									.getMessage("CountryNotFound"));
					else
						sender.sendMessage(this.getMessage("SenderNotOp"));
				else
					sender.sendMessage(this.getMessage("CommandError"));
			else if (args.length == 5)
				if (args[0].equalsIgnoreCase("per"))
					if (sender.isOp())
						if (Country.include(args[2].replace("&", "§")))
							if (Country.getCountry(args[2].replace("&", "§"))
									.getTeam(args[1]) != null)
								if (Country
										.getCountry(args[2].replace("&", "§"))
										.getTeam(args[1])
										.addPer(args[3],
												args[4],
												Country.getCountry(args[2]
														.replace("&", "§"))))
									sender.sendMessage(this
											.getMessage("AddPer"));
								else
									sender.sendMessage(this
											.getMessage("AddPerError"));
							else
								sender.sendMessage(this
										.getMessage("CountryNotFound"));
						else if (Country.include(args[2].replace("&", "§")))
							if (Country.getCountry(args[2].replace("&", "§"))
									.getTeam(args[1]) != null)
								if (Country
										.getCountry(args[2].replace("&", "§"))
										.getTeam(args[1]).getCaptain()
										.equals(sender.getName()))
									if (Country
											.getCountry(
													args[2].replace("&", "§"))
											.getTeam(args[1])
											.addPer(args[3],
													args[4],
													Country.getCountry(args[2]
															.replace("&", "§"))))
										sender.sendMessage(this
												.getMessage("AddPer"));
									else
										sender.sendMessage(this
												.getMessage("AddPerError"));
								else
									sender.sendMessage(this
											.getMessage("SenderNotOp"));
							else
								sender.sendMessage(this
										.getMessage("TeamNotFound"));
						else
							sender.sendMessage(this.getMessage("SenderNotOp"));
					else
						sender.sendMessage(this.getMessage("CommandError"));
				else if (args.length == 6)
					if (args[0].equalsIgnoreCase("spawn"))
						if (sender.isOp())
							if (Country.include(args[1].replace("&", "§"))) {
								final World world = Bukkit.getWorld(args[2]);
								if (world == null) {
									sender.sendMessage(this
											.getMessage("WorldNotFound"));
									return true;
								}
								int x = 0;
								int y = 0;
								int z = 0;
								try {
									x = Integer.parseInt(args[3]);
									y = Integer.parseInt(args[4]);
									z = Integer.parseInt(args[5]);
								} catch (final Exception e) {
									sender.sendMessage(this
											.getMessage("ArgsNotInt"));
									return true;
								}
								final Location location = new Location(world,
										x, y, z);
								Country.getCountry(args[1].replace("&", "§"))
										.setSpawnLocation(location);
								sender.sendMessage(this.getMessage("SetSpawn"));
							} else
								sender.sendMessage(this
										.getMessage("CountryNotFound"));
						else
							sender.sendMessage(this.getMessage("SenderNotOp"));
					else
						sender.sendMessage(this.getMessage("CommandError"));
				else
					sender.sendMessage(this.getMessage("CommandError"));
		} else
			sender.sendMessage(this.getMessage("SenderNotPlayer"));
		return true;
	}

	private String getMessage(final String key) {
		return this.messages.get(key);
	}

	private void loadConfig() {
		final File message = new File(CountryTeamsCommand.team.getDataFolder(),
				"message.yml");
		final YamlConfiguration yml = YamlConfiguration
				.loadConfiguration(message);
		final Set<String> keys = yml.getKeys(false);
		for (final String key : keys)
			this.messages.put(key, yml.getString(key));
	}

}

package com.focess.team.team;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class Country {

	private static List<Country> countries = new ArrayList<>();

	public static com.focess.team.Team team;

	public static boolean createCountry(final String name) {
		if (!Country.include(name)) {
			new Country(name);
			return true;
		}
		return false;
	}

	public static Country getCountry(final String name) {
		for (final Country country : Country.countries)
			if (country.getName().equalsIgnoreCase(name))
				return country;
		return null;
	}

	public static boolean getRelation(final Country country,
			final Country country2) {
		if (country.friends.contains(country2.getName()))
			return true;
		if (country.enemies.contains(country2.getName()))
			return false;
		if (country.getName().equalsIgnoreCase(country2.getName()))
			return true;
		return false;
	}

	public static boolean haveRelation(final Country country,
			final Country country2) {
		if (country.friends.contains(country2.getName())
				|| country.enemies.contains(country2.getName())
				|| country.getName().equalsIgnoreCase(country2.getName()))
			return true;
		return false;
	}

	public static boolean include(final String name) {
		boolean flag = false;
		for (final Country country : Country.countries)
			if (country.getName().equalsIgnoreCase(name))
				flag = true;
		return flag;
	}

	public static List<Country> listCountries() {
		return Country.countries;
	}

	public static void loadCountries(final com.focess.team.Team team) {
		Country.team = team;
		final File countries = new File(team.getDataFolder(), "countries");
		if (countries.listFiles() != null)
			for (final File country : countries.listFiles())
				if (country.isDirectory())
					new Country(country.getName());

	}

	public static void SerializeAll() {
		for (final Country country : Country.countries)
			country.Serialize();
	}

	private List<String> enemies = new CopyOnWriteArrayList<>();

	private List<String> friends = new CopyOnWriteArrayList<>();

	private Location loc;

	private String name;

	private List<String> players = new ArrayList<>();

	private final List<Team> teams = new ArrayList<>();

	public Country(final String name) {
		this.name = name;
		Country.countries.add(this);
		final File file = new File(Country.team.getDataFolder().getPath()
				+ "/countries/" + name);
		if (!file.exists())
			file.mkdir();
		final File config = new File(file.getPath() + "/config.yml");
		if (config.exists()) {
			final YamlConfiguration yml = YamlConfiguration
					.loadConfiguration(config);
			if (yml.contains("friends"))
				this.friends = yml.getStringList("friends");
			if (yml.contains("enemies"))
				this.enemies = yml.getStringList("enemies");
			if (yml.contains("world")) {
				final String w = yml.getString("world");
				final World world = Bukkit.getWorld(w);
				if (world == null)
					return;
				final int x = yml.getInt("x");
				final int y = yml.getInt("y");
				final int z = yml.getInt("z");
				this.loc = new Location(world, x, y, z);
			}
		} else
			try {
				config.createNewFile();
			} catch (final IOException e1) {
				e1.printStackTrace();
			}
		final File players = new File(Country.team.getDataFolder().getPath()
				+ "/countries/" + name + "/players.ct");
		if (players.exists())
			try {
				this.players = SLAPI.load(players.getPath());
			} catch (final Exception e) {
				e.printStackTrace();
			}
		for (final File team : file.listFiles())
			if (team.isDirectory())
				this.teams.add(new Team(team.getName(), this));
		this.refreshPlayer(this.players.toArray(new String[this.players.size()]));
	}

	public boolean addEnemy(final Country country) {
		boolean flag = false;
		for (final String enemy : this.enemies)
			if (country.getName().equalsIgnoreCase(enemy))
				flag = true;
		if (flag)
			return false;
		this.enemies.add(country.getName());
		country.enemies.add(this.getName());
		return true;
	}

	public boolean addFriend(final Country country) {
		boolean flag = false;
		for (final String friend : this.friends)
			if (country.getName().equalsIgnoreCase(friend))
				flag = true;
		if (flag)
			return false;
		this.friends.add(country.getName());
		country.friends.add(this.getName());
		return true;
	}

	public void addPlayer(final Player player) {
		this.players.add(player.getName());
		this.refreshPlayer(player.getName());
	}

	public boolean addTeamPlayer(final String name, final Player player) {
		if (!this.players.contains(player.getName()))
			return false;
		if (this.includeTeam(name))
			for (final Team team : this.teams)
				if (team.getName().equalsIgnoreCase(name))
					if (!team.getMembers().contains(player.getName()))
						return team.addMember(player);
					else
						return false;
		return true;
	}

	public boolean createTeam(final String name) {
		for (final Team team : this.teams)
			if (team.getName().equalsIgnoreCase(name))
				return false;
		this.teams.add(new Team(name, this));
		return true;
	}

	public List<String> getEnemies() {
		return this.enemies;
	}

	public List<String> getFriends() {
		return this.friends;
	}

	public String getName() {
		return this.name;
	}

	public List<String> getPlayers() {
		return this.players;
	}

	public Location getSpawnLocation() {
		return this.loc;
	}

	public Team getTeam(final String name) {
		for (final Team team : this.teams)
			if (team.getName().equalsIgnoreCase(name))
				return team;
		return null;
	}

	public List<Team> getTeams() {
		return this.teams;
	}

	public boolean includePlayer(final Player player) {
		boolean flag = false;
		for (final String p : this.players)
			if (p.equals(player.getName()))
				flag = true;
		return flag;
	}

	public boolean includePlayer(final String player) {
		boolean flag = false;
		for (final String p : this.players)
			if (p.equals(player))
				flag = true;
		return flag;
	}

	public boolean includeTeam(final String name) {
		boolean flag = false;
		for (final Team team : this.teams)
			if (team.getName().equalsIgnoreCase(name))
				flag = true;
		return flag;
	}

	public boolean isEnemy(final Country country) {
		for (final String enemy : this.enemies)
			if (enemy.equals(country.getName()))
				return true;
		return false;
	}

	public boolean isFriend(final Country country) {
		for (final String friend : this.friends)
			if (friend.equals(country.getName()))
				return true;
		return false;
	}

	@SuppressWarnings("deprecation")
	public boolean refreshPlayer(final String... players) {
		boolean flag = false;
		for (final String player : players) {
			final Player p = Bukkit.getPlayerExact(player);
			if (p != null) {
				flag = true;
				p.setDisplayName(this.getName() + ": " + p.getName());
				p.setCustomName(this.getName() + ": " + p.getName());
			}
		}
		return flag;
	}

	public void remove() {
		for (final Team team : this.teams)
			Country.team.removePer(this, team);
		this.players.clear();
		this.teams.clear();
		for (final String friend : this.friends)
			this.removeFriend(Country.getCountry(friend));
		for (final String enemy : this.enemies)
			this.removeEnemy(Country.getCountry(enemy));
		Country.countries.remove(this);
		final File country = new File(Country.team.getDataFolder().getPath()
				+ "/countries/" + this.getName());
		for (final File file : country.listFiles())
			if (!file.isDirectory())
				file.delete();
			else {
				for (final File f : file.listFiles())
					f.delete();
				file.delete();
			}
		country.delete();
		try {
			this.finalize();
		} catch (final Throwable e) {
			e.printStackTrace();
		}
	}

	public boolean remove(final Country country) {
		if (this.removeFriend(country) || this.removeEnemy(country))
			return true;
		return false;
	}

	public boolean removeEnemy(final Country country) {
		boolean flag = false;
		for (final String enemy : this.enemies)
			if (country.getName().equalsIgnoreCase(enemy))
				flag = true;
		if (!flag)
			return false;
		this.enemies.remove(country.getName());
		country.enemies.remove(this.getName());
		return true;
	}

	public boolean removeFriend(final Country country) {
		boolean flag = false;
		for (final String friend : this.friends)
			if (country.getName().equalsIgnoreCase(friend))
				flag = true;
		if (!flag)
			return false;
		this.friends.remove(country.getName());
		country.friends.remove(this.getName());
		return true;
	}

	@SuppressWarnings("deprecation")
	public void removePlayer(final String name) {
		this.players.remove(name);
		final Player p = Bukkit.getPlayerExact(name);
		if (p != null)
			p.setDisplayName(p.getName());

	}

	public void removeTeam(final String name) {
		if (this.includeTeam(name)) {
			Country.team.removePer(this, this.getTeam(name));
			this.teams.remove(this.getTeam(name));
			this.getTeam(name).remove(this);
		}
	}

	public void Serialize() {
		final File players = new File(Country.team.getDataFolder().getPath()
				+ "/countries/" + this.name + "/players.ct");
		try {
			SLAPI.save(this.players, players.getPath());
		} catch (final Exception e) {
			e.printStackTrace();
		}
		final File config = new File(Country.team.getDataFolder().getPath()
				+ "/countries/" + this.name + "/config.yml");
		final YamlConfiguration yml = YamlConfiguration
				.loadConfiguration(config);
		yml.set("friends", this.friends);
		yml.set("enemies", this.enemies);
		if (this.loc != null) {
			yml.set("world", this.loc.getWorld().getName());
			yml.set("x", this.loc.getBlockX());
			yml.set("y", this.loc.getBlockY());
			yml.set("z", this.loc.getBlockZ());
		}
		try {
			yml.save(config);
		} catch (final IOException e) {
			e.printStackTrace();
		}
		for (final Team team : this.teams)
			team.Serialize(this);
	}

	public void setSpawnLocation(final Location location) {
		this.loc = location;
	}

}

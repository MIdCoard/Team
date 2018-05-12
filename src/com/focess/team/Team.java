package com.focess.team;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.focess.team.command.CountryChatCommand;
import com.focess.team.command.CountryTeamsCommand;
import com.focess.team.command.TeamChatCommand;
import com.focess.team.listener.AsyncPlayerChatListener;
import com.focess.team.listener.BlockBreakListener;
import com.focess.team.listener.BlockPlaceListener;
import com.focess.team.listener.InventoryCloseListener;
import com.focess.team.listener.Permission;
import com.focess.team.listener.PlayerDeathListener;
import com.focess.team.listener.PlayerInteractEntityListener;
import com.focess.team.listener.PlayerInteractListener;
import com.focess.team.listener.PlayerJoinListener;
import com.focess.team.listener.PlayerMoveListener;
import com.focess.team.listener.PlayerRespawnListener;
import com.focess.team.runnable.RefreshRunnable;
import com.focess.team.team.Country;

public class Team extends JavaPlugin {

	private Permission blockbreak;

	private Permission build;

	private CommandMap commandMap;

	public Economy economy;

	private Permission move;

	private Permission use;

	{
		try {
			this.getCommandMap();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public boolean addPer(final String key, final boolean value,
			final Country country, final com.focess.team.team.Team team) {
		if (key.equalsIgnoreCase("move"))
			if (value)
				this.move.addPermission(Permission.low, team, country);
			else
				this.move.addPermission(Permission.high, team, country);
		else if (key.equalsIgnoreCase("break"))
			if (value)
				this.blockbreak.addPermission(Permission.low, team, country);
			else
				this.blockbreak.addPermission(Permission.high, team, country);
		else if (key.equalsIgnoreCase("build"))
			if (value)
				this.build.addPermission(Permission.low, team, country);
			else
				this.build.addPermission(Permission.high, team, country);
		else if (key.equalsIgnoreCase("use"))
			if (value)
				this.use.addPermission(Permission.low, team, country);
			else
				this.use.addPermission(Permission.high, team, country);
		else
			return false;
		return true;
	}

	private void getCommandMap() throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		final Class<?> c = Bukkit.getServer().getClass();
		for (final Method method : c.getDeclaredMethods())
			if (method.getName().equals("getCommandMap"))
				this.commandMap = (CommandMap) method.invoke(this.getServer(),
						new Object[0]);
	}

	private void loadConfig() {
		if (!this.getDataFolder().exists())
			this.getDataFolder().mkdir();
		final File file = new File(this.getDataFolder(), "config.yml");
		if (!file.exists())
			this.saveDefaultConfig();
		this.reloadConfig();
		final File countries = new File(this.getDataFolder(), "countries");
		if (!countries.exists())
			countries.mkdir();
		final File teams = new File(this.getDataFolder(), "teams");
		if (!teams.exists())
			teams.mkdir();
		final File players = new File(this.getDataFolder(), "players");
		if (!players.exists())
			players.mkdir();
		this.loadFile(new File(this.getDataFolder(), "message.yml"),
				"message.yml");
	}

	private void loadFile(final File targetFile, final String loadingFile) {
		if (targetFile.exists())
			return;
		String jarFilePath = this.getClass().getProtectionDomain()
				.getCodeSource().getLocation().getFile();
		try {
			jarFilePath = URLDecoder.decode(jarFilePath, "UTF-8");
			final JarFile jar = new JarFile(jarFilePath);
			InputStream is = null;
			final Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				final JarEntry entry = entries.nextElement();
				if (entry.getName().equals(loadingFile)) {
					is = jar.getInputStream(entry);
					break;
				}
			}
			final FileOutputStream out = new FileOutputStream(targetFile);
			int c = 0;
			while ((c = is.read()) != -1)
				out.write(c);
			out.close();
			jar.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDisable() {
		this.getLogger().info("Team载入成功");
		Country.SerializeAll();
		this.move.Serialize();
		this.blockbreak.Serialize();
		this.build.Serialize();
		this.use.Serialize();
	}

	@Override
	public void onEnable() {
		this.getLogger().info("Team载出成功");
		this.loadConfig();
		if (Bukkit.getPluginManager().getPlugin("Vault") != null)
			this.setupEconomy();
		Country.loadCountries(this);
		Bukkit.getScheduler().runTaskTimer(this, new RefreshRunnable(this), 0,
				20);
		Bukkit.getPluginManager().registerEvents(
				new AsyncPlayerChatListener(this), this);
		Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(this),
				this);
		Bukkit.getPluginManager().registerEvents(new PlayerRespawnListener(),
				this);
		Bukkit.getPluginManager().registerEvents(new InventoryCloseListener(),
				this);
		Bukkit.getPluginManager().registerEvents(
				(Listener) (this.use = new PlayerInteractListener(this)), this);
		Bukkit.getPluginManager().registerEvents(
				(Listener) (this.move = new PlayerMoveListener(this)), this);
		Bukkit.getPluginManager().registerEvents(
				(Listener) (this.blockbreak = new BlockBreakListener(this)),
				this);
		Bukkit.getPluginManager().registerEvents(
				(Listener) (this.build = new BlockPlaceListener(this)), this);
		Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this),
				this);
		Bukkit.getPluginManager().registerEvents(new PlayerInteractEntityListener(this),
				this);
		final List<String> ctali = new ArrayList<>();
		ctali.add("ct");
		final List<String> ccali = new ArrayList<>();
		ccali.add("cc");
		final List<String> tcali = new ArrayList<>();
		tcali.add("tc");
		this.commandMap.register(this.getDescription().getName(),
				new CountryTeamsCommand(ctali, this));
		this.commandMap.register(this.getDescription().getName(),
				new CountryChatCommand(ccali, this));
		this.commandMap.register(this.getDescription().getName(),
				new TeamChatCommand(tcali, this));
	}

	public void removePer(final Country country,
			final com.focess.team.team.Team team) {
		this.move.removePermission(team, country);
		this.blockbreak.removePermission(team, country);
		this.build.removePermission(team, country);
		this.use.removePermission(team, country);

	}

	private boolean setupEconomy() {
		final RegisteredServiceProvider<Economy> economyProvider = Bukkit
				.getServer().getServicesManager()
				.getRegistration(Economy.class);
		if (economyProvider != null)
			this.economy = economyProvider.getProvider();
		return this.economy != null;
	}
}

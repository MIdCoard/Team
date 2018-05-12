package com.focess.team.team;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.focess.team.listener.PlayerInteractListener.CheckedPlace;

public class Team {

	private final List<ItemStack> itemStacks = new ArrayList<>();

	private final Location[] ls = new Location[2];

	protected List<String> members = new ArrayList<>();

	protected String name;

	private String team = "";

	public Team(final String name, final Country country) {
		this.name = name;
		if (country == null)
			return;
		final File file = new File(Country.team.getDataFolder().getPath()
				+ "/countries/" + country.getName() + "/" + this.name);
		if (!file.exists())
			file.mkdir();
		final File players = new File(file, "players.ct");
		if (players.exists())
			try {
				this.members = SLAPI.load(players.getPath());
			} catch (final Exception e) {
				e.printStackTrace();
			}
		final File itemStacks = new File(Country.team.getDataFolder().getPath()
				+ "/countries/" + country.getName() + "/" + this.getName()
				+ "/itemstacks.yml");
		if (itemStacks.exists()) {
			final YamlConfiguration yml = YamlConfiguration
					.loadConfiguration(itemStacks);
			for (final String key : yml.getKeys(false))
				this.itemStacks.add(yml.getItemStack(key));
		}
		final File config = new File(Country.team.getDataFolder().getPath()
				+ "/countries/" + country.getName() + "/" + this.getName()
				+ "/config.yml");
		if (config.exists()) {
			final YamlConfiguration yml = YamlConfiguration
					.loadConfiguration(config);
			if (yml.contains("world1")) {
				final String w = yml.getString("world1");
				final World world = Bukkit.getWorld(w);
				if (world == null)
					return;
				final int x = yml.getInt("x1");
				final int y = yml.getInt("y1");
				final int z = yml.getInt("z1");
				this.ls[0] = new Location(world, x, y, z);
			}
			if (yml.contains("world2")) {
				final String w = yml.getString("world2");
				final World world = Bukkit.getWorld(w);
				if (world == null)
					return;
				final int x = yml.getInt("x2");
				final int y = yml.getInt("y2");
				final int z = yml.getInt("z2");
				this.ls[1] = new Location(world, x, y, z);
			}
			if (yml.contains("team"))
				this.team = yml.getString("team");
		}
	}

	public boolean addMember(final Player player) {
		if (this.members.size() > Country.team.getConfig().getInt("teammax") - 1)
			return false;
		this.members.add(player.getName());
		return true;
	}

	public boolean addPer(final String per, final String value,
			final Country country) {
		final boolean flag = Boolean.parseBoolean(value);
		return Country.team.addPer(per, flag, country, this);
	}

	public String getCaptain() {
		return this.team;
	}

	public ItemStack[] getItemStacks() {
		return this.itemStacks.toArray(new ItemStack[this.itemStacks.size()]);
	}

	public List<String> getMembers() {
		return this.members;
	}

	public String getName() {
		return this.name;
	}

	public boolean inside(final Location loc) {
		if (this.ls[0] == null || this.ls[1] == null)
			return false;
		if (loc.getWorld().equals(this.ls[0].getWorld())) {
			if (this.ls[0].getBlockX() > this.ls[1].getBlockX()) {
				if (loc.getBlockX() > this.ls[0].getBlockX()
						|| loc.getBlockX() < this.ls[1].getBlockX())
					return false;
			} else if (loc.getBlockX() < this.ls[0].getBlockX()
					|| loc.getBlockX() > this.ls[1].getBlockX())
				return false;
			if (this.ls[0].getBlockY() > this.ls[1].getBlockY()) {
				if (loc.getBlockY() > this.ls[0].getBlockY()
						|| loc.getBlockY() < this.ls[1].getBlockY())
					return false;
			} else if (loc.getBlockY() < this.ls[0].getBlockY()
					|| loc.getBlockY() > this.ls[1].getBlockY())
				return false;
			if (this.ls[0].getBlockZ() > this.ls[1].getBlockZ()) {
				if (loc.getBlockZ() > this.ls[0].getBlockZ()
						|| loc.getBlockZ() < this.ls[1].getBlockZ())
					return false;
			} else if (loc.getBlockZ() < this.ls[0].getBlockZ()
					|| loc.getBlockZ() > this.ls[1].getBlockZ())
				return false;
		}
		return true;
	}

	public void openInventory(final Player player, final Country country) {
		final Inventory inventory = Bukkit.createInventory(null,
				InventoryType.CHEST, "§a请放入为队伍" + country.getName() + "."
						+ this.getName() + "存放的物资");
		final List<ItemStack> temp = new ArrayList<>();
		for (final ItemStack itemStack : this.itemStacks)
			if (itemStack != null)
				temp.add(itemStack);
		inventory.addItem(temp.toArray(new ItemStack[temp.size()]));
		player.openInventory(inventory);
	}

	protected void remove(final Country country) {
		this.members.clear();
		this.itemStacks.clear();
		final File team = new File(Country.team.getDataFolder().getPath()
				+ "/countries/" + country.getName() + "/" + this.getName());
		for (final File file : team.listFiles())
			file.delete();
		team.delete();
		try {
			this.finalize();
		} catch (final Throwable e) {
			e.printStackTrace();
		}
	}

	public void removeMember(final Player player) {
		this.members.remove(player.getName());
	}

	public void Serialize(final Country country) {
		final File players = new File(Country.team.getDataFolder().getPath()
				+ "/countries/" + country.getName() + "/" + this.name
				+ "/players.ct");
		try {
			SLAPI.save(this.members, players.getPath());
		} catch (final Exception e) {
			e.printStackTrace();
		}
		final File itemStacks = new File(Country.team.getDataFolder().getPath()
				+ "/countries/" + country.getName() + "/" + this.getName()
				+ "/itemstacks.yml");
		if (!itemStacks.exists())
			try {
				itemStacks.createNewFile();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		else {
			itemStacks.delete();
			try {
				itemStacks.createNewFile();
			} catch (final IOException e1) {
				e1.printStackTrace();
			}
		}
		final YamlConfiguration yml = YamlConfiguration
				.loadConfiguration(itemStacks);
		for (int i = 0; i < this.itemStacks.size(); i++)
			if (this.itemStacks.get(i) != null)
				yml.set(i + "", this.itemStacks.get(i));
		try {
			yml.save(itemStacks);
		} catch (final IOException e) {
			e.printStackTrace();
		}
		final File config = new File(Country.team.getDataFolder().getPath()
				+ "/countries/" + country.getName() + "/" + this.name
				+ "/config.yml");
		if (!config.exists())
			try {
				config.createNewFile();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		final YamlConfiguration configyml = YamlConfiguration
				.loadConfiguration(config);
		if (this.ls[0] != null) {
			configyml.set("world1", this.ls[0].getWorld().getName());
			configyml.set("x1", this.ls[0].getBlockX());
			configyml.set("y1", this.ls[0].getBlockY());
			configyml.set("z1", this.ls[0].getBlockZ());
		}
		if (this.ls[1] != null) {
			configyml.set("world2", this.ls[1].getWorld().getName());
			configyml.set("x2", this.ls[1].getBlockX());
			configyml.set("y2", this.ls[1].getBlockY());
			configyml.set("z2", this.ls[1].getBlockZ());
		}
		if (this.team != null)
			configyml.set("team", this.team);
		try {
			configyml.save(config);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public void setCaptain(final String name) {
		this.team = name;
	}

	public void setItemStacks(final ItemStack... itemStacks) {
		this.itemStacks.clear();
		for (final ItemStack itemStack : itemStacks)
			this.itemStacks.add(itemStack);
	}

	public boolean setRealm(final CheckedPlace cp) {
		if (cp.getLocation1() == null || cp.getLocation2() == null)
			return false;
		this.ls[0] = cp.getLocation1();
		this.ls[1] = cp.getLocation2();
		return true;
	}
}

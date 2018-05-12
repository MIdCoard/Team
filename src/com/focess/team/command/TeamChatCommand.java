package com.focess.team.command;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.focess.team.Team;
import com.focess.team.team.Country;

public class TeamChatCommand extends Command {

	public static HashMap<String, List<Player>> tcs = new HashMap<>();

	private final HashMap<String, String> messages = new HashMap<>();

	private final Team team;

	public TeamChatCommand(final List<String> aliases, final Team team) {
		super("TeamChat", "", "", aliases);
		this.team = team;
		this.loadConfig();
	}

	@SuppressWarnings("serial")
	@Override
	public boolean execute(final CommandSender sender, final String cmd,
			final String[] args) {
		if (sender instanceof Player) {
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
			if (CountryChatCommand.ccs.get(c) != null
					&& CountryChatCommand.ccs.get(c).contains(sender)) {
				final List<Player> temp = CountryChatCommand.ccs.get(c);
				temp.remove(sender);
				CountryChatCommand.ccs.put(c, temp);
				sender.sendMessage(this.getMessage("ExitCC"));
			}
			if (TeamChatCommand.tcs.get(c.getName() + "." + t.getName()) == null)
				TeamChatCommand.tcs.put(c.getName() + "." + t.getName(),
						new ArrayList<Player>() {
							{
								this.add((Player) sender);
							}
						});
			else if (!TeamChatCommand.tcs.get(c.getName() + "." + t.getName())
					.contains(sender)) {
				final List<Player> temp = TeamChatCommand.tcs.get(c.getName()
						+ "." + t.getName());
				temp.add((Player) sender);
				TeamChatCommand.tcs.put(c.getName() + "." + t.getName(), temp);
			} else {
				final List<Player> temp = TeamChatCommand.tcs.get(c.getName()
						+ "." + t.getName());
				temp.remove(sender);
				TeamChatCommand.tcs.put(c.getName() + "." + t.getName(), temp);
				sender.sendMessage(this.getMessage("ExitTC"));
				return true;
			}
			sender.sendMessage(this.getMessage("EnterTC"));
		} else
			sender.sendMessage(this.getMessage("SenderNotPlayer"));
		return true;
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

}

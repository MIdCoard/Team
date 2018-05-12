package com.focess.team.listener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

import com.focess.team.team.Country;
import com.focess.team.team.Team;

public abstract class Permission {

	public class TeamOfCountry {

		public Country country;

		public Team team;

		public boolean value;

		public TeamOfCountry(final Team team, final Country country,
				final boolean value) {
			this.team = team;
			this.country = country;
			this.value = value;
		}

		@Override
		public boolean equals(final Object obj) {
			if (!(obj instanceof TeamOfCountry))
				return false;
			if (((TeamOfCountry) obj).country.getName().equals(
					this.country.getName())
					&& ((TeamOfCountry) obj).team.getName().equals(
							this.team.getName()))
				return true;
			return false;
		}
	}

	public static final int high = 0;

	public static final int low = 1;

	protected List<TeamOfCountry> tcbs = new ArrayList<>();

	public Permission() {
		for (final Country country : Country.listCountries())
			for (final Team team : country.getTeams()) {
				final File config = new File(Country.team.getDataFolder()
						.getPath()
						+ "/countries/"
						+ country.getName()
						+ "/"
						+ team.getName() + "/config.yml");
				if (config.exists()) {
					final YamlConfiguration yml = YamlConfiguration
							.loadConfiguration(config);
					if (yml.contains(this.getLabel()))
						if (yml.getBoolean(this.getLabel()))
							this.addPermission(Permission.low, team, country);
						else
							this.addPermission(Permission.high, team, country);
				}
			}
	}

	public void addPermission(final int level, final Team team,
			final Country country) {
		if (!country.includeTeam(team.getName()))
			return;
		TeamOfCountry t = null;
		if (level == 0)
			t = new TeamOfCountry(team, country, false);
		else if (level == 1)
			t = new TeamOfCountry(team, country, true);
		for (final TeamOfCountry tc : this.tcbs)
			if (tc.equals(t)) {
				tc.value = t.value;
				return;
			}
		this.tcbs.add(t);
	}

	public abstract String getLabel();

	public void removePermission(final Team team, final Country country) {
		if (!country.includeTeam(team.getName()))
			return;
		final TeamOfCountry tc = new TeamOfCountry(team, country, false);
		TeamOfCountry temp = null;
		for (final TeamOfCountry t : this.tcbs)
			if (t.equals(tc))
				temp = t;
		if (temp != null)
			this.tcbs.remove(temp);
	}

	public void Serialize() {
		for (final TeamOfCountry tc : this.tcbs) {
			final Country c = tc.country;
			final boolean per = tc.value;
			final File team = new File(Country.team.getDataFolder().getPath()
					+ "/countries/" + c.getName() + "/" + tc.team.getName()
					+ "/config.yml");
			if (!team.exists())
				try {
					team.createNewFile();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			final YamlConfiguration yml = YamlConfiguration
					.loadConfiguration(team);
			yml.set(this.getLabel(), per);
			try {
				yml.save(team);
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

}

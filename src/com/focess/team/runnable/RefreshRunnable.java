package com.focess.team.runnable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import com.focess.team.Team;

public class RefreshRunnable extends BukkitRunnable {

	private int day = 0;
	private final Team team;
	private final String time;

	public RefreshRunnable(final Team team) {
		this.team = team;
		this.day = team.getConfig().getInt("day");
		this.time = team.getConfig().getString("rtime");
	}

	private String getTime() {
		final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		return sdf.format(new Date());
	}

	private int getWeek() {
		final Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		final int week = cal.get(Calendar.DAY_OF_WEEK) - 1;
		if (week == 0)
			return 7;
		else
			return week;
	}

	@Override
	public void run() {
		if (this.getWeek() == this.day)
			if (this.getTime().equals(this.time)) {
				final File players = new File(this.team.getDataFolder(),
						"players");
				for (final File player : players.listFiles()) {
					final YamlConfiguration yml = YamlConfiguration
							.loadConfiguration(player);
					yml.set("isGet", false);
					try {
						yml.save(player);
					} catch (final IOException e) {
						e.printStackTrace();
					}
				}
			}
	}

}

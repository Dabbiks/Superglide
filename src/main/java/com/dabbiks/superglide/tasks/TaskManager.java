package com.dabbiks.superglide.tasks;

import com.dabbiks.superglide.game.tasks.Start;
import com.dabbiks.superglide.game.tasks.Time;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;

import static com.dabbiks.superglide.Superglide.plugin;

public class TaskManager extends BukkitRunnable {

    private final List<Task> tasks;

    public TaskManager() {
        tasks = new ArrayList<>();

        tasks.add(new Start());
        tasks.add(new Time());

        runTaskTimer(plugin, 0, 1);
    }

    @Override
    public void run() {
        for (Task task : tasks) {
            if (Bukkit.getCurrentTick() % task.getPeriod() != 0) continue;
            task.tick();
        }
    }
}

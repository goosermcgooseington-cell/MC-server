package com.gooser.swimboost;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SwimBoost extends JavaPlugin {

    private static final double UPWARD_ACCELERATION = 0.06;
    private static final double MAX_UPWARD_SPEED = 0.12;
    private static final double SWIM_SPRINT_MULTIPLIER = 1.6;
    private static final double MAX_SWIM_SPEED = 0.65;

    @Override
    public void onEnable() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.isFlying() || !isInWater(player)) {
                        continue;
                    }

                    Vector velocity = player.getVelocity();
                    boolean changed = false;

                    if (player.isSneaking()) {
                        velocity.setY(Math.min(
                            velocity.getY() + UPWARD_ACCELERATION,
                            MAX_UPWARD_SPEED
                        ));
                        changed = true;
                    }

                    if (player.isSprinting()) {
                        velocity.setX(velocity.getX() * SWIM_SPRINT_MULTIPLIER);
                        velocity.setZ(velocity.getZ() * SWIM_SPRINT_MULTIPLIER);

                        double horizontalSpeed = Math.sqrt(
                            velocity.getX() * velocity.getX()
                            + velocity.getZ() * velocity.getZ()
                        );

                        if (horizontalSpeed > MAX_SWIM_SPEED) {
                            double scale = MAX_SWIM_SPEED / horizontalSpeed;
                            velocity.setX(velocity.getX() * scale);
                            velocity.setZ(velocity.getZ() * scale);
                        }

                        changed = true;
                    }

                    if (changed) {
                        player.setVelocity(velocity);
                    }
                }
            }
        }.runTaskTimer(this, 1L, 1L);

        getLogger().info("SwimBoost enabled!");
    }

    private boolean isInWater(Player player) {
        for (double y = 0.0; y <= 1.5; y += 0.5) {
            Material m = player.getLocation().clone()
                .add(0, y, 0).getBlock().getType();

            if (m == Material.WATER
                    || m == Material.STATIONARY_WATER) {
                return true;
            }
        }
        return false;
    }
}

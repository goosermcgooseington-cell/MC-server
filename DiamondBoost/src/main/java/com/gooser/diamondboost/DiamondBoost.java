package com.gooser.diamondboost;

import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.plugin.java.JavaPlugin;

public class DiamondBoost extends JavaPlugin implements Listener {

    private final BlockPopulator populator = new DiamondPopulator();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        // Also handle worlds that were loaded before this plugin enabled.
        for (World world : Bukkit.getWorlds()) {
            addPopulator(world);
        }

        getLogger().info("DiamondBoost enabled: two extra diamond veins per new Overworld chunk.");
    }

    @EventHandler
    public void onWorldInit(WorldInitEvent event) {
        addPopulator(event.getWorld());
    }

    private void addPopulator(World world) {
        if (world.getEnvironment() != World.Environment.NORMAL) {
            return;
        }

        List<BlockPopulator> populators = world.getPopulators();
        if (!populators.contains(populator)) {
            populators.add(populator);
        }
    }

    private static class DiamondPopulator extends BlockPopulator {

        @Override
        public void populate(World world, Random random, Chunk source) {
            if (world.getEnvironment() != World.Environment.NORMAL) {
                return;
            }

            int baseX = source.getX() << 4;
            int baseZ = source.getZ() << 4;

            // Vanilla 1.12.2 attempts one diamond vein per chunk.
            // Add two more attempts to target approximately three times
            // the original vein-attempt count.
            for (int vein = 0; vein < 2; vein++) {
                int x = random.nextInt(16);
                int y = random.nextInt(15) + 1; // Y = 1 through 15
                int z = random.nextInt(16);

                int length = 6 + random.nextInt(3);

                for (int i = 0; i < length; i++) {
                    Block block = world.getBlockAt(baseX + x, y, baseZ + z);

                    // Never overwrite caves, existing ores, or other blocks.
                    if (block.getType() == Material.STONE) {
                        block.setType(Material.DIAMOND_ORE, false);
                    }

                    // A short random walk forms a small ore cluster.
                    x = Math.max(0, Math.min(15, x + random.nextInt(3) - 1));
                    z = Math.max(0, Math.min(15, z + random.nextInt(3) - 1));
                    y = Math.max(1, Math.min(15, y + random.nextInt(3) - 1));
                }
            }
        }
    }
}

package com.gooser.chestfreedom;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Chest;
import org.bukkit.plugin.java.JavaPlugin;

public class ChestFreedom extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("ChestFreedom enabled!");
    }

    private BlockFace getPlayerFacing(Player player) {
        float yaw = player.getLocation().getYaw();
        int direction = Math.round(yaw / 90.0F) & 3;

        switch (direction) {
            case 0: return BlockFace.SOUTH;
            case 1: return BlockFace.WEST;
            case 2: return BlockFace.NORTH;
            default: return BlockFace.EAST;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChestClick(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();

        if (item == null || item.getType() != Material.CHEST) return;

        Block clicked = event.getClickedBlock();

        if (clicked == null || clicked.getType() != Material.CHEST) return;

        BlockFace face = event.getBlockFace();
        Block target = clicked.getRelative(face);

        if (target.getType() != Material.AIR) return;

        event.setCancelled(true);

        BlockState oldState = target.getState();

        target.setType(Material.CHEST);

        Chest chestData = new Chest();

        if (face == BlockFace.UP || face == BlockFace.DOWN) {
            chestData.setFacingDirection(getPlayerFacing(player));
        } else {
            chestData.setFacingDirection(face.getOppositeFace());
        }

        target.setData(chestData.getData());

        BlockPlaceEvent placeEvent = new BlockPlaceEvent(
            target,
            oldState,
            clicked,
            item.clone(),
            player,
            true
        );

        getServer().getPluginManager().callEvent(placeEvent);

        if (placeEvent.isCancelled()) {
            oldState.update(true, false);
            return;
        }

        if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
            if (item.getAmount() <= 1) {
                player.setItemInHand(null);
            } else {
                item.setAmount(item.getAmount() - 1);
                player.setItemInHand(item);
            }
        }

        player.updateInventory();
    }
}

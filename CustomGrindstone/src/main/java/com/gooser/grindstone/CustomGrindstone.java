package com.gooser.grindstone;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomGrindstone extends JavaPlugin implements Listener {

    private static final int XP_REWARD = 5;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("CustomGrindstone enabled!");
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        CraftingInventory inventory = event.getInventory();
        ItemStack result = createResult(inventory.getMatrix());

        if (result != null) {
            inventory.setResult(result);
        }
    }

    @EventHandler
    public void onCraftClick(InventoryClickEvent event) {
        if (event.getRawSlot() != 0) {
            return;
        }

        Inventory top = event.getView().getTopInventory();

        if (!(top instanceof CraftingInventory)) {
            return;
        }

        CraftingInventory crafting = (CraftingInventory) top;
        ItemStack[] matrix = crafting.getMatrix();
        ItemStack output = createResult(matrix);

        if (output == null) {
            return;
        }

        event.setCancelled(true);

        ClickType click = event.getClick();
        Player player = (Player) event.getWhoClicked();

        if (click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT) {
            PlayerInventory playerInventory = player.getInventory();

            if (!canFitOne(playerInventory, output)) {
                return;
            }

            playerInventory.addItem(output);
        } else if (click == ClickType.LEFT || click == ClickType.RIGHT) {
            ItemStack cursor = event.getCursor();

            if (cursor == null || cursor.getType() == Material.AIR) {
                event.setCursor(output);
            } else if (cursor.isSimilar(output)
                    && cursor.getAmount() < cursor.getMaxStackSize()) {
                cursor.setAmount(cursor.getAmount() + 1);
                event.setCursor(cursor);
            } else {
                return;
            }
        } else {
            return;
        }

        consumeOne(crafting, matrix);
        player.giveExp(XP_REWARD);
        player.updateInventory();
    }

    private ItemStack createResult(ItemStack[] matrix) {
        ItemStack input = null;
        int occupiedSlots = 0;

        for (ItemStack item : matrix) {
            if (item != null && item.getType() != Material.AIR) {
                occupiedSlots++;
                input = item;
            }
        }

        if (occupiedSlots != 1 || input == null) {
            return null;
        }

        if (input.getType() == Material.ENCHANTED_BOOK) {
            ItemMeta meta = input.getItemMeta();

            if (!(meta instanceof EnchantmentStorageMeta)) {
                return null;
            }

            EnchantmentStorageMeta bookMeta =
                    (EnchantmentStorageMeta) meta;

            if (bookMeta.getStoredEnchants().isEmpty()) {
                return null;
            }

            return new ItemStack(Material.BOOK, 1);
        }

        ItemStack result = input.clone();
        result.setAmount(1);

        ItemMeta meta = result.getItemMeta();

        if (meta == null || meta.getEnchants().isEmpty()) {
            return null;
        }

        List<Enchantment> enchantments =
                new ArrayList<Enchantment>(meta.getEnchants().keySet());

        for (Enchantment enchantment : enchantments) {
            meta.removeEnchant(enchantment);
        }

        result.setItemMeta(meta);
        return result;
    }

    private boolean canFitOne(PlayerInventory inventory, ItemStack output) {
        for (ItemStack item : inventory.getStorageContents()) {
            if (item == null || item.getType() == Material.AIR) {
                return true;
            }

            if (item.isSimilar(output)
                    && item.getAmount() < item.getMaxStackSize()) {
                return true;
            }
        }

        return false;
    }

    private void consumeOne(CraftingInventory crafting, ItemStack[] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            ItemStack item = matrix[i];

            if (item != null && item.getType() != Material.AIR) {
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                    matrix[i] = item;
                } else {
                    matrix[i] = new ItemStack(Material.AIR);
                }

                crafting.setMatrix(matrix);
                return;
            }
        }
    }
}

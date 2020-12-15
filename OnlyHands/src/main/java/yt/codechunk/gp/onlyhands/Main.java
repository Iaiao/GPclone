package yt.codechunk.gp.onlyhands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

public class Main extends JavaPlugin implements Listener {
    private final List<Integer> allowedSLots = Arrays.asList(4, 36, 37, 38, 39, 40);

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            getServer().getOnlinePlayers().forEach(player -> {
                for (int i = 0; i < 46; i++) {
                    if (!allowedSLots.contains(i)) {
                        ItemStack item = player.getInventory().getItem(i);
                        if (item == null || !item.equals(new ItemStack(Material.BARRIER))) {
                            player.getInventory().setItem(i, new ItemStack(Material.BARRIER));
                        }
                    } else {
                        if (player.getInventory().getItem(i) != null && player.getInventory().getItem(i).getType() == Material.BARRIER) {
                            player.getInventory().setItem(i, null);
                        }
                    }
                }
            });
        }, 1, 1);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            getServer().getOnlinePlayers().forEach(player -> {
                player.getInventory().setHeldItemSlot(4);
            });
        }, 20, 20);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().getInventory().setHeldItemSlot(4);
        for (int i = 0; i < 46; i++) {
            if (!allowedSLots.contains(i)) {
                event.getPlayer().getInventory().setItem(i, new ItemStack(Material.BARRIER));
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.BARRIER) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getCursor() != null && event.getCursor().getType() == Material.BARRIER) {
            event.setResult(Event.Result.DENY);
        }
    }

    @EventHandler
    public void onHeldChange(PlayerItemHeldEvent event) {
        event.getPlayer().getInventory().setHeldItemSlot(4);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().getType() == Material.BARRIER) {
            event.setCancelled(true);
        }
    }
}

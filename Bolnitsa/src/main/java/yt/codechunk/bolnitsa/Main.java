package yt.codechunk.bolnitsa;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BoundingBox;

public class Main extends JavaPlugin implements Listener {
    private Location teleportLocation;
    private final BoundingBox box = new BoundingBox(2, 0, 167, 16, 256, 182);

    @Override
    public void onEnable() {
        teleportLocation = new Location(getServer().getWorld("world"), 8.5, 64.125, 175.5);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            Bukkit.getOnlinePlayers().forEach(player -> {
                if(box.contains(player.getLocation().toVector())) {
                    if(player.isSleeping()) {
                            player.setHealth(Math.min(player.getHealth() + 1, player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()));
                            int hearts = (int) player.getHealth() / 2;
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < hearts; i++) {
                                sb.append('\uae0e');
                            }
                            if(player.getHealth() % 2 != 0) {
                                sb.append('\uae0f');
                            }
                            player.sendMessage(sb.toString());
                    }
                }
            });
        }, 20, 5 * 20);
    }

    @EventHandler
    public void onDeath(PlayerRespawnEvent event) {
        event.setRespawnLocation(teleportLocation);
        Bukkit.getScheduler().runTask(this, () -> {
            event.getPlayer().setHealth(1);
        });
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (box.contains(event.getFrom().toVector()) && !box.contains(event.getTo().toVector())) {
            if (event.getPlayer().getHealth() <= event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() - 0.5) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity().getType() == EntityType.PLAYER && box.contains(event.getEntity().getLocation().toVector())) {
            event.setCancelled(true);
        }
    }
}

package yt.codechunk.bolnitsa;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
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
    private BoundingBox region;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        World world = getServer().getWorld(getConfig().getString("world"));
        double teleportX = getConfig().getDouble("teleport to.x");
        double teleportY = getConfig().getDouble("teleport to.y");
        double teleportZ = getConfig().getDouble("teleport to.z");
        teleportLocation = new Location(world, teleportX, teleportY, teleportZ);
        double regionX1 = getConfig().getDouble("region.x1");
        double regionY1 = getConfig().getDouble("region.y1");
        double regionZ1 = getConfig().getDouble("region.z1");
        double regionX2 = getConfig().getDouble("region.x2");
        double regionY2 = getConfig().getDouble("region.y2");
        double regionZ2 = getConfig().getDouble("region.z2");
        region = new BoundingBox(
                Math.min(regionX1, regionX2),
                Math.min(regionY1, regionY2),
                Math.min(regionZ1, regionZ2),
                Math.max(regionX1, regionX2),
                Math.max(regionY1, regionY2),
                Math.max(regionZ1, regionZ2)
        );
        String format = ChatColor.translateAlternateColorCodes('&', getConfig().getString("hearts format"));
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (region.contains(player.getLocation().toVector())) {
                    if (player.isSleeping()) {
                        player.setHealth(Math.min(player.getHealth() + 1, player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()));
                        int hearts = (int) player.getHealth() / 2;
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < hearts; i++) {
                            sb.append('\uae0e');
                        }
                        if (Math.floor(player.getHealth()) % 2 == 1) {
                            sb.append('\uae0f');
                        }
                        player.sendMessage(format.replaceAll("\\{health}", sb.toString()));
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
        if (region.contains(event.getFrom().toVector()) && !region.contains(event.getTo().toVector())) {
            if (event.getPlayer().getHealth() <= event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() - 0.5) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity().getType() == EntityType.PLAYER && region.contains(event.getEntity().getLocation().toVector())) {
            event.setCancelled(true);
        }
    }
}

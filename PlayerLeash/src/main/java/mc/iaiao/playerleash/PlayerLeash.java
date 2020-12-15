package mc.iaiao.playerleash;

import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.EntityZombieHusk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftHusk;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class PlayerLeash extends JavaPlugin implements Listener {
    private double maxMoveDistance;
    private final Map<Player, LivingEntity> handles = new HashMap<>();
    private Scoreboard scoreboard;
    private boolean nms;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        nms = getConfig().getBoolean("nms");
        maxMoveDistance = getConfig().getDouble("max move distance");

        scoreboard = getServer().getScoreboardManager().getNewScoreboard();

        getServer().getPluginManager().registerEvents(this, this);

        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            List<Player> toRemove = new LinkedList<>();
            handles.forEach((player, handle) -> {
                if (!handle.isValid() || !handle.isLeashed()) {
                    handle.remove();
                    toRemove.add(player);
                    return;
                }
                ((Husk) handle).setTarget(null);
                if (player.getLocation().distance(handle.getLeashHolder().getLocation()) <= maxMoveDistance) {
                    handle.teleport(player);
                } else {
                    player.getLocation().setX(handle.getLocation().getX());
                    player.getLocation().setY(handle.getLocation().getY());
                    player.getLocation().setZ(handle.getLocation().getZ());
                    player.setVelocity(handle.getVelocity());
                }
            });
            toRemove.forEach(handles::remove);
        }, 1, 1);

        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            handles.forEach((player, handle) -> {
                Location destination = handle.getLocation();
                destination.setPitch(player.getLocation().getPitch());
                destination.setYaw(player.getLocation().getYaw());
                player.teleport(destination);
            });
        }, 1, getConfig().getLong("sync period"));
    }

    @Override
    public void onDisable() {
        handles.values().forEach(Entity::remove);
        handles.clear();
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (handles.containsKey(event.getEntity())) {
            handles.get(event.getEntity()).remove();
            handles.remove(event.getEntity());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (handles.containsKey(event.getPlayer())) {
            handles.get(event.getPlayer()).remove();
            handles.remove(event.getPlayer());
        }
    }

    @EventHandler
    public void onUnleash(EntityUnleashEvent event) {
        if (handles.containsValue(event.getEntity())) {
            event.getEntity().remove();
            Player leashed = handles.entrySet().stream().filter(e -> e.getValue().equals(event.getEntity())).findFirst().get().getKey();
            handles.remove(leashed);
        }
    }

    @EventHandler
    public void onLeash(PlayerInteractEntityEvent event) {
        if (event.getPlayer().getEquipment().getItem(event.getHand()).getType() == Material.LEAD && event.getRightClicked().getType() == EntityType.PLAYER) {
            if (handles.containsKey(event.getRightClicked())) {
                handles.get(event.getRightClicked()).remove();
                return;
            }
            Husk handle;
            if (nms) {
                EntityZombieHusk husk = new EntityZombieHusk(EntityTypes.HUSK, ((CraftWorld) event.getRightClicked().getWorld()).getHandle());
                husk.setInvisible(true);
                husk.persistentInvisibility = true;
                husk.setLocation(event.getRightClicked().getLocation().getX(), event.getRightClicked().getLocation().getY(), event.getRightClicked().getLocation().getZ(), 0, 0);
                ((CraftWorld) event.getRightClicked().getWorld()).addEntity(husk, CreatureSpawnEvent.SpawnReason.CUSTOM);
                handle = new CraftHusk((CraftServer) getServer(), husk);
            } else {
                handle = event.getPlayer().getWorld().spawn(event.getRightClicked().getLocation().clone().add(0, 1000, 0), Husk.class);
                handle.setInvisible(true);
                handle.teleport(event.getRightClicked());
            }
            handle.setCollidable(false);
            handle.getEquipment().setHelmet(null);
            handle.getEquipment().setChestplate(null);
            handle.getEquipment().setLeggings(null);
            handle.getEquipment().setBoots(null);
            handle.getEquipment().setItemInMainHand(null);
            handle.getEquipment().setItemInOffHand(null);
            handle.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 1000000, 255));
            handle.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 1000000, 255));
            handle.setCanPickupItems(false);
            handle.setInvulnerable(true);
            handle.setSilent(true);
            handle.setLeashHolder(event.getPlayer());
            handles.put((Player) event.getRightClicked(), handle);
            if (scoreboard.getTeam(event.getRightClicked().getName()) != null) {
                scoreboard.getTeam(event.getRightClicked().getName()).unregister();
            }
            Team team = scoreboard.registerNewTeam(event.getRightClicked().getName());
            team.addEntry(event.getRightClicked().getName());
            team.addEntry(handle.getUniqueId().toString());
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OWN_TEAM);
        }
    }
}

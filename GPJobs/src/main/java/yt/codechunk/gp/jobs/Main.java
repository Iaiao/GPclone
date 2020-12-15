package yt.codechunk.gp.jobs;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Main extends JavaPlugin implements Listener {
    private final File saveFile = new File(getDataFolder(), "save.dat");
    public HashMap<String, Jobs> jobs = new HashMap<>();

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) getDataFolder().mkdir();
        load();

        getCommand("job").setExecutor(new JobCommand());
        getCommand("secondaryjob").setExecutor(new SecondaryJobCommand());

        getServer().getPluginManager().registerEvents(this, this);

        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            getServer().getOnlinePlayers().forEach(player -> {
                if (!getJob(player).canRedstone()) {
                    Location location = player.getLocation();
                    for (int x = -3; x <= 3; x++) {
                        for (int y = -3; y <= 3; y++) {
                            for (int z = -3; z <= 3; z++) {
                                if (REDSTONE_BLOCKS.contains(location.clone().add(x, y, z).getBlock().getType())) {
                                    player.damage(1);
                                }
                            }
                        }
                    }
                }
            });
        }, 5, 5);

        getServer().getScheduler().scheduleSyncRepeatingTask(this, this::save, 20, 20 * 60 * 30);
    }

    public Jobs getJob(String name) {
        return jobs.getOrDefault(name.toLowerCase(), Jobs.UNEMPLOYED);
    }

    public Jobs getJob(Player player) {
        return getJob(player.getName());
    }

    @Override
    public void onDisable() {
        save();
    }

    private void save() {
        try {
            if (!saveFile.exists()) saveFile.createNewFile();
            ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(saveFile));
            stream.writeObject(jobs);
            stream.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static Main getInstance() {
        return Main.getPlugin(Main.class);
    }

    private void load() {
        if (!saveFile.exists()) return;
        try {
            ObjectInputStream stream = new ObjectInputStream(new FileInputStream(saveFile));
            jobs = (HashMap<String, Jobs>) stream.readObject();
            stream.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private static final List<Material> CROPS = Arrays.asList(
            Material.BEETROOTS,
            Material.POTATOES,
            Material.WHEAT,
            Material.CARROTS,
            Material.MELON_STEM,
            Material.MELON,
            Material.PUMPKIN,
            Material.PUMPKIN_STEM,
            Material.GRASS,
            Material.TALL_GRASS
    );
    private static final List<Material> REDSTONE_BLOCKS = Arrays.asList(
            Material.REDSTONE_BLOCK,
            Material.REDSTONE_LAMP,
            Material.REDSTONE,
            Material.REDSTONE_WIRE,
            Material.REDSTONE_TORCH,
            Material.REDSTONE_WALL_TORCH,
            Material.REPEATER,
            Material.COMPARATOR
    );

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (
                !(getJob(event.getPlayer()).canFarm() && CROPS.contains(event.getBlock().getType())) &&
                        !(getJob(event.getPlayer()).canRedstone() && REDSTONE_BLOCKS.contains(event.getBlock().getType()))
        ) {
            if (!getJob(event.getPlayer()).canBreak()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (
                !(getJob(event.getPlayer()).canFarm() && CROPS.contains(event.getBlock().getType())) &&
                        !(getJob(event.getPlayer()).canRedstone() && REDSTONE_BLOCKS.contains(event.getBlock().getType())) &&
                        !(event.getBlock().getType() == Material.FIRE)
        ) {
            if (!getJob(event.getPlayer()).canPlace()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager().getType() == EntityType.PLAYER) {
            if (!getJob((Player) event.getDamager()).canDamage()) {
                event.setDamage(event.getDamage() / 10.0);
            }
        }
    }

    @EventHandler
    public void onBrew(InventoryOpenEvent event) {
        if (event.getPlayer().getType() == EntityType.PLAYER) {
            if (event.getInventory().getType() == InventoryType.BREWING) {
                if (!getJob((Player) event.getPlayer()).canBrew()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        if (!getJob(event.getEnchanter()).canEnchant()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPortal(PortalCreateEvent event) {
        if (getServer().getOnlinePlayers().stream().noneMatch(player -> getJob(player).canCreatePortal() && player.getWorld().equals(event.getWorld()) && player.getLocation().distance(event.getBlocks().get(0).getLocation()) < 10)) {
            event.setCancelled(true);
        }
    }

    private static final List<InventoryType> REDSTONE_INVENTORIES = Arrays.asList(
            InventoryType.DISPENSER,
            InventoryType.DROPPER
    );

    @EventHandler
    public void onRedstoneOpen(InventoryOpenEvent event) {
        if (event.getPlayer().getType() == EntityType.PLAYER) {
            if (REDSTONE_INVENTORIES.contains(event.getInventory().getType())) {
                if (!getJob((Player) event.getPlayer()).canRedstone()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onRedstoneInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            assert event.getClickedBlock() != null;
            if (REDSTONE_BLOCKS.contains(event.getClickedBlock().getType())) {
                if (!getJob(event.getPlayer()).canRedstone()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private static final List<InventoryType> TINKER_INVENTORIES = Arrays.asList(
            InventoryType.ANVIL,
            InventoryType.WORKBENCH,
            InventoryType.SMOKER,
            InventoryType.BLAST_FURNACE,
            InventoryType.SMITHING
    );

    @EventHandler
    public void onTinker(InventoryOpenEvent event) {
        if (event.getPlayer().getType() == EntityType.PLAYER) {
            if (TINKER_INVENTORIES.contains(event.getInventory().getType())) {
                if (!getJob((Player) event.getPlayer()).canTinker()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onTinker(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            assert event.getClickedBlock() != null;
            if (event.getClickedBlock().getType() == Material.CAULDRON) {
                if (!getJob(event.getPlayer()).canTinker()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onMap(PlayerInteractEvent event) {
        if (event.getItem() != null && event.getItem().getType() == Material.MAP) {
            if (!getJob(event.getPlayer()).canMakeMaps()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCartography(InventoryOpenEvent event) {
        if (event.getPlayer().getType() == EntityType.PLAYER) {
            if (event.getInventory().getType() == InventoryType.CARTOGRAPHY) {
                if (!getJob((Player) event.getPlayer()).canMakeMaps()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onNether(PlayerPortalEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL && event.getFrom().getWorld().getEnvironment() == World.Environment.NORMAL) {
            if (!getJob(event.getPlayer()).canEnterNether()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFishing(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.FISHING) {
            if (!getJob(event.getPlayer()).canFish()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBookWrite(PlayerEditBookEvent event) {
        if (event.isSigning()) {
            if (getJob(event.getPlayer()).isLawyer()) {
                BookMeta meta = event.getNewBookMeta();
                meta.setAuthor(
                        yt.codechunk.gp.name.Main.formatName(
                                yt.codechunk.gp.name.Main.getInstance().getName(
                                        meta.getAuthor()
                                )
                        )
                );
                if (event.getNewBookMeta().getTitle().startsWith("@")) {
                    meta.setTitle(meta.getTitle().substring(1).trim());
                } else {
                    meta.setTitle(ChatColor.GRAY + meta.getTitle());
                    meta.addPage("Shift+ПКМ по " + meta.getAuthor() + " чтобы подписать");
                }
                event.setNewBookMeta(meta);
            }
        }
    }

    @EventHandler
    public void onSign(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() == EntityType.PLAYER) {
            ItemStack itemStack = event.getPlayer().getEquipment().getItem(event.getHand());
            if (itemStack.getType() == Material.WRITTEN_BOOK) {
                if (event.getPlayer().isSneaking()) {
                    BookMeta meta = (BookMeta) itemStack.getItemMeta();
                    if (meta != null && meta.getTitle() != null) {
                        if (meta.getTitle().startsWith(ChatColor.GRAY.toString())) {
                            if (yt.codechunk.gp.name.Main.getInstance().getName(event.getRightClicked().getName()).equalsIgnoreCase(meta.getAuthor())) {
                                String name = yt.codechunk.gp.name.Main.formatName(
                                        yt.codechunk.gp.name.Main.getInstance().getName(
                                                event.getPlayer().getName()
                                        )
                                );
                                meta.addPage(ChatColor.GRAY + "Подписано: " + name);
                                itemStack.setItemMeta(meta);
                                event.getPlayer().sendMessage(ChatColor.GREEN + "Подписано!");
                                event.getPlayer().closeInventory();
                                event.getRightClicked().sendMessage(name + ChatColor.GREEN + " подписал " + meta.getTitle());
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (
                event.getRecipe().getResult().getType().name().endsWith("_STAIRS") ||
                        event.getRecipe().getResult().getType().name().endsWith("_SLAB") ||
                        event.getRecipe().getResult().getType().name().endsWith("_WALL") ||
                        event.getRecipe().getResult().getType().name().startsWith("POLISHED_") ||
                        event.getRecipe().getResult().getType().name().startsWith("CUT_") ||
                        event.getRecipe().getResult().getType().name().startsWith("CHISELED_") ||
                        event.getRecipe().getResult().getType().name().startsWith("SMOOTH_") ||
                        event.getRecipe().getResult().getType().name().endsWith("_PILLAR")
        ) {
            event.setResult(Event.Result.DENY);
        }
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent event) {
        if (event.getRecipe() == null) return;
        if (
                event.getRecipe().getResult().getType().name().endsWith("_STAIRS") ||
                        event.getRecipe().getResult().getType().name().endsWith("_SLAB") ||
                        event.getRecipe().getResult().getType().name().endsWith("_WALL") ||
                        event.getRecipe().getResult().getType().name().startsWith("POLISHED_") ||
                        event.getRecipe().getResult().getType().name().startsWith("CUT_") ||
                        event.getRecipe().getResult().getType().name().startsWith("CHISELED_") ||
                        event.getRecipe().getResult().getType().name().startsWith("SMOOTH_") ||
                        event.getRecipe().getResult().getType().name().endsWith("_PILLAR")
        ) {
            event.getInventory().setResult(null);
        }
    }

    @EventHandler
    public void onStonecut(InventoryOpenEvent event) {
        if (event.getPlayer().getType() == EntityType.PLAYER) {
            if (event.getInventory().getType() == InventoryType.STONECUTTER) {
                if (!getJob((Player) event.getPlayer()).canStonecut()) {
                    event.setCancelled(true);
                }
            }
        }
    }
}

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
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Main extends JavaPlugin implements Listener {
    private final File saveFile = new File(getDataFolder(), "save.dat");
    public HashMap<String, Jobs> jobs = new HashMap<>();

    int configRedstoneDamageRadius;
    int configNonHunterDamageMultiplier;
    String configJobSet;
    String configHowToSign;
    String configSignedBy;
    String configSigned;
    String configPlayerSigned;
    List<Material> configCropBlocks;
    List<Material> configRedstoneBlocks;
    List<InventoryType> configTinkerInventories;
    List<InventoryType> configRedstoneInventories;

    public static Main getInstance() {
        return Main.getPlugin(Main.class);
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        load();

        getCommand("job").setExecutor(new JobCommand());
        getCommand("secondaryjob").setExecutor(new SecondaryJobCommand());

        getServer().getPluginManager().registerEvents(this, this);

        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            getServer().getOnlinePlayers().forEach(player -> {
                if (!getJob(player).canRedstone()) {
                    Location location = player.getLocation();
                    for (int x = -configRedstoneDamageRadius; x <= configRedstoneDamageRadius; x++) {
                        for (int y = -configRedstoneDamageRadius; y <= configRedstoneDamageRadius; y++) {
                            for (int z = -configRedstoneDamageRadius; z <= configRedstoneDamageRadius; z++) {
                                if (configRedstoneBlocks.contains(location.clone().add(x, y, z).getBlock().getType())) {
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

    private void load() {
        if (!saveFile.exists()) return;
        try {
            ObjectInputStream stream = new ObjectInputStream(new FileInputStream(saveFile));
            jobs = (HashMap<String, Jobs>) stream.readObject();
            stream.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        configRedstoneDamageRadius = getConfig().getInt("redstone damage radius");
        configJobSet = ChatColor.translateAlternateColorCodes('&', getConfig().getString("job set"));
        configHowToSign = ChatColor.translateAlternateColorCodes('&', getConfig().getString("how to sign"));
        configSignedBy = ChatColor.translateAlternateColorCodes('&', getConfig().getString("signed by"));
        configSigned = ChatColor.translateAlternateColorCodes('&', getConfig().getString("signed"));
        configPlayerSigned = ChatColor.translateAlternateColorCodes('&', getConfig().getString("player signed"));
        configCropBlocks = getConfig().getStringList("blocks.crops").stream().map(Material::matchMaterial).collect(Collectors.toList());
        configRedstoneBlocks = getConfig().getStringList("blocks.redstone").stream().map(Material::matchMaterial).collect(Collectors.toList());
        configRedstoneInventories = getConfig().getStringList("inventories.redstone").stream().map(InventoryType::valueOf).collect(Collectors.toList());
        configTinkerInventories = getConfig().getStringList("inventories.tinker").stream().map(InventoryType::valueOf).collect(Collectors.toList());
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (
                !(getJob(event.getPlayer()).canFarm() && configCropBlocks.contains(event.getBlock().getType())) &&
                        !(getJob(event.getPlayer()).canRedstone() && configRedstoneBlocks.contains(event.getBlock().getType()))
        ) {
            if (!getJob(event.getPlayer()).canBreak()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (
                !(getJob(event.getPlayer()).canFarm() && configCropBlocks.contains(event.getBlock().getType())) &&
                        !(getJob(event.getPlayer()).canRedstone() && configRedstoneBlocks.contains(event.getBlock().getType())) &&
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
                event.setDamage(event.getDamage() * configNonHunterDamageMultiplier);
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

    @EventHandler
    public void onRedstoneOpen(InventoryOpenEvent event) {
        if (event.getPlayer().getType() == EntityType.PLAYER) {
            if (configRedstoneInventories.contains(event.getInventory().getType())) {
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
            if (configRedstoneBlocks.contains(event.getClickedBlock().getType())) {
                if (!getJob(event.getPlayer()).canRedstone()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onTinker(InventoryOpenEvent event) {
        if (event.getPlayer().getType() == EntityType.PLAYER) {
            if (configTinkerInventories.contains(event.getInventory().getType())) {
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
                    meta.setTitle(meta.getTitle().substring("@".length()).trim());
                } else {
                    meta.setTitle(ChatColor.GRAY + meta.getTitle());
                    meta.addPage(configHowToSign.replaceAll("\\{player}", meta.getAuthor()));
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
                                meta.addPage(configSignedBy.replaceAll("\\{player}", name));
                                itemStack.setItemMeta(meta);
                                event.getPlayer().sendMessage(configSigned);
                                event.getPlayer().closeInventory();
                                event.getRightClicked().sendMessage(configPlayerSigned.replaceAll("\\{player}", name).replaceAll("\\{document}", meta.getTitle()));
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

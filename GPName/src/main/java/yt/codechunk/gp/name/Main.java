package yt.codechunk.gp.name;

import net.md_5.bungee.api.chat.TranslatableComponent;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_16_R3.PacketPlayOutTitle;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import yt.codechunk.gp.jobs.Jobs;

import java.io.*;
import java.util.*;

public class Main extends JavaPlugin implements Listener {
    private final File saveFile = new File(getDataFolder(), "save.dat");
    private final List<Player> acceptedResourcePack = new LinkedList<>();
    private Team nameTagHideTeam;
    HashMap<String, String> playerNames = new HashMap<>();
    Strangers strangers;
    OOBE oobe;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) getDataFolder().mkdir();
        strangers = new Strangers();
        getServer().getPluginManager().registerEvents(strangers, this);
        getServer().getOnlinePlayers().forEach(strangers::newStranger);
        oobe = new OOBE();
        getServer().getPluginManager().registerEvents(oobe, this);
        load();
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new Chat(), this);

        nameTagHideTeam = getServer().getScoreboardManager().getMainScoreboard().getTeam("hidenametag");
        if (nameTagHideTeam == null) {
            nameTagHideTeam = getServer().getScoreboardManager().getMainScoreboard().registerNewTeam("hidenametag");
        }
        nameTagHideTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);

        // Small check to make sure that PlaceholderAPI is installed
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new NamePlaceholder().register();
        }

        getCommand("ban").setExecutor(new BanCommand());
        getCommand("gtn").setExecutor(strangers);
        getCommand("name").setExecutor((sender, cmd, s, args) -> {
            if (!(sender instanceof Player)) {
                return false;
            }
            if (args.length != 2) {
                return false;
            }
            if(playerNames.containsKey(sender.getName().toLowerCase())) {
                sender.sendMessage("Менять имя нельзя!");
                return true;
            }
            String name = (args[0] + " " + args[1]).toLowerCase();
            if (playerNames.containsValue(name)) {
                sender.sendMessage("Такое имя уже занято");
                return true;
            }
            if (playerNames.containsKey(sender.getName())) {
                sender.sendMessage("Имя менять запрещено");
                return true;
            }
            if(!name.matches("[а-яА-Я]{0,32} [а-яА-Я]{0,32}")) {
                sender.sendMessage("Неверное имя или фамилия: Они должны быть киррилическими");
                return true;
            }
            playerNames.put(sender.getName().toLowerCase(), name);
            sender.sendMessage("Имя установлено");
            yt.codechunk.gp.jobs.Main.getInstance().jobs.put(sender.getName().toLowerCase(), Jobs.UNEMPLOYED);
            reloadPlayer((Player) sender);
            oobe.newbies.remove(sender);
            ((Player) sender).setGameMode(GameMode.SURVIVAL);
            return true;
        });

        getCommand("getname").setExecutor(new GetName());

        getCommand("getrealname").setExecutor(new GetRealName());

        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (Player player : getServer().getOnlinePlayers()) {
                for (Player player1 : getServer().getOnlinePlayers()) {
                    IChatBaseComponent prev = ((CraftPlayer) player1).getHandle().listName;
                    ((CraftPlayer) player1).getHandle().listName = CraftChatMessage.fromStringOrNull(
                            strangers.knows(player.getName(), player1.getName()) ?
                                    (acceptedResourcePack.contains(player) ?
                                            yt.codechunk.gp.jobs.Main.getInstance().getJob(player1.getName()).toString()
                                            : ""
                                    ) + formatName(getName(player1.getName())) :
                                    strangers.getStrangerName(player1.getName())
                    );
                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(
                            new PacketPlayOutPlayerInfo(
                                    PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME,
                                    ((CraftPlayer) player1).getHandle())
                    );
                    ((CraftPlayer) player1).getHandle().listName = prev;
                }
            }
        }, 20, 20);

        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            int i = (int) (Math.random() * 16);
            String color = "\u00A7" + Integer.toHexString(i);
            oobe.newbies.forEach(player -> player.sendTitle(color + "Открой чат", "Там инструкция как начать игру", 5, 25, 5));
        }, 20, 20);

        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            getServer().getOnlinePlayers().forEach(player -> {
                ArrayList<Entity> entities = (ArrayList<Entity>) player.getNearbyEntities(20, 20, 20);
                String title = "";
                Location l = player.getEyeLocation().clone();
                Vector step = player.getLocation().getDirection().multiply(0.1);
                int i = 200;
                while (i != 0) {
                    i--;
                    l.add(step);
                    for (Entity entity : entities) {
                        if (entity.getBoundingBox().contains(l.toVector())) {
                            if (entity.getType() == EntityType.PLAYER) {
                                Player p = (Player) entity;
                                title = ChatColor.YELLOW + "-=[" + ChatColor.WHITE + strangers.nameFor(player, p) + ChatColor.YELLOW + "]=-";
                                break;
                            }
                        }
                    }
                }
                PacketPlayOutTitle packet = new PacketPlayOutTitle(
                        PacketPlayOutTitle.EnumTitleAction.ACTIONBAR,
                        CraftChatMessage.fromStringOrNull(title),
                        1, 2, 1
                );
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
            });
        }, 1, 1);

        getServer().getScheduler().scheduleSyncRepeatingTask(this, this::save, 20, 20 * 60 * 30);
    }

    @Override
    public void onDisable() {
        save();
    }

    private void save() {
        try {
            if (!saveFile.exists()) saveFile.createNewFile();
            ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(saveFile));
            stream.writeObject(playerNames);
            stream.writeObject(strangers.knowing);
            stream.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void load() {
        if (!saveFile.exists()) return;
        try {
            ObjectInputStream stream = new ObjectInputStream(new FileInputStream(saveFile));
            playerNames = (HashMap<String, String>) stream.readObject();
            strangers.knowing = (List<String>) stream.readObject();
            stream.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public String getName(String name) {
        return playerNames.getOrDefault(name.toLowerCase(), name.toLowerCase());
    }

    public static String formatName(String fullName) {
        String name = fullName.split(" ")[0];
        String surname = fullName.contains(" ") ? fullName.split(" ")[1] : " ";
        return name.substring(0, 1).toUpperCase() + name.substring(1) + " " + surname.substring(0, 1).toUpperCase() + surname.substring(1);
    }

    public Optional<String> getRealName(String name) {
        String name1 = name.toLowerCase();
        return playerNames
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(name1))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    public void reloadPlayer(Player player) {
        if (playerNames.containsKey(player.getName().toLowerCase())) {
            player.setDisplayName(formatName(playerNames.get(player.getName().toLowerCase())));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        nameTagHideTeam.addEntry(event.getPlayer().getName());
        getLogger().info(nameTagHideTeam.getEntries().toString());
        reloadPlayer(event.getPlayer());
        event.getPlayer().setResourcePack("https://mrbans.ru/gppack");
        for (Player player : getServer().getOnlinePlayers()) {
            player.spigot().sendMessage(new TranslatableComponent("multiplayer.player.joined", strangers.nameFor(player, event.getPlayer())));
        }
        event.setJoinMessage(null);
    }

    @EventHandler
    public void onResourcePackAccept(PlayerResourcePackStatusEvent event) {
        if (event.getStatus() == PlayerResourcePackStatusEvent.Status.ACCEPTED) {
            acceptedResourcePack.add(event.getPlayer());
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        acceptedResourcePack.remove(event.getPlayer());
        for (Player player : getServer().getOnlinePlayers()) {
            player.spigot().sendMessage(new TranslatableComponent("multiplayer.player.left", strangers.nameFor(player, event.getPlayer())));
        }
        event.setQuitMessage(null);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.setDeathMessage(null);
    }

    public static Main getInstance() {
        return Main.getPlugin(Main.class);
    }
}

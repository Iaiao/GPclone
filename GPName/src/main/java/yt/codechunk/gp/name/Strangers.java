package yt.codechunk.gp.name;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Strangers implements Listener, TabExecutor {
    HashMap<String, Integer> strangerIds = new HashMap<>();
    List<String> knowing = new LinkedList<>(); // format: "player1 player2"

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        newStranger(event.getPlayer());
    }

    public void newStranger(Player player) {
        int i = 1;
        while (true) {
            if (!strangerIds.containsValue(i)) {
                break;
            } else {
                i++;
                continue; // unnecessary continue is here for readability reasons
            }
        }
        strangerIds.put(player.getName().toLowerCase(), i);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        strangerIds.remove(event.getPlayer().getName().toLowerCase());
    }

    @EventHandler
    public void onClick(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() == EntityType.PLAYER) {
            if (!knows(event.getPlayer().getName(), event.getRightClicked().getName())) {
                if (event.getHand() == EquipmentSlot.HAND) {
                    String strangerName = getStrangerName(event.getRightClicked().getName());
                    TextComponent component = new TextComponent("Хочешь познакомиться с " + strangerName + "? ");
                    TextComponent click = new TextComponent("[Жми сюда]");
                    click.setColor(ChatColor.GRAY);
                    click.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Да, сюда")));
                    click.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/gtn " + strangerName));
                    component.addExtra(click);
                    event.getPlayer().spigot().sendMessage(component);
                }
            }
        }
    }

    public String getStrangerName(Player player) {
        return getStrangerName(player.getName());
    }

    public String getStrangerName(String player) {
        return String.format("Незнакомец (%d)", strangerIds.getOrDefault(player.toLowerCase(), 0));
    }

    public Optional<Player> getFromStrangerId(int id) {
        return strangerIds.entrySet().stream().filter(entry -> entry.getValue() == id).map(entry -> Bukkit.getPlayer(entry.getKey())).findFirst();
    }

    public Optional<Player> getFromStrangerName(String name) {
        if (!name.matches("Незнакомец \\(\\d+\\)")) {
            throw new IllegalArgumentException("Неверное имя незнакомца: " + name);
        } else {
            return getFromStrangerId(Integer.parseInt(name.substring("Незнакомец (".length()).substring(0, name.length() - "Незнакомец (".length() - ")".length())));
        }
    }

    public boolean knows(String a, String b) {
        return a.equalsIgnoreCase(b) || knowing.contains((a + " " + b).toLowerCase());
    }

    public String nameFor(Player player, Player player1) {
        return knows(player.getName(), player1.getName()) ? Main.formatName(Main.getInstance().getName(player1.getName())) : getStrangerName(player1.getName());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String puk, @NotNull String[] args) {
        if (args.length != 2) {
            return false;
        }
        String s = String.join(" ", args);
        Player player;
        try {
            player = getFromStrangerName(s).get();
        } catch (Exception exception) {
            Optional<String> p = Main.getInstance().getRealName(s);
            if (p.isPresent()) {
                player = Bukkit.getPlayer(p.get());
                if (player == null) {
                    return false;
                }
            } else {
                return false;
            }
        }
        String entry = (player.getName() + " " + sender.getName()).toLowerCase();
        String ign = Main.formatName(Main.getInstance().getName(player.getName()));
        String senderIgn = Main.formatName(Main.getInstance().getName(sender.getName()));
        if (ign.equalsIgnoreCase(player.getName())) {
            sender.sendMessage("У этого игрока ещё нет имени");
        } else if (!knowing.contains(entry)) {
            knowing.add(entry);
            sender.sendMessage("Ты познакомился с " + s);
            TextComponent component = new TextComponent(senderIgn + " хочут с тобой познакомиться. ");
            TextComponent accept = new TextComponent("[ПОЗНАКОМИТЬСЯ]");
            accept.setColor(ChatColor.GRAY);
            accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Познакомиться с " + senderIgn)));
            accept.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/gtn " + senderIgn));
            component.addExtra(accept);
            player.spigot().sendMessage(component);
        } else {
            sender.sendMessage(s + " уже тебя знает. Он должен написать \"/gtn " + Main.formatName(Main.getInstance().getName(sender.getName())) + "\" чтобы ты знал его");
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String kak, @NotNull String[] args) {
        String now = String.join(" ", args);
        List<String> s = Bukkit.getOnlinePlayers().stream().map(player -> {
            if (knows(sender.getName(), player.getName())) {
                return Main.formatName(Main.getInstance().getName(player.getName()));
            } else {
                return getStrangerName(player);
            }
        }).collect(Collectors.toList());
        return s.stream()
                .filter(a -> a.toLowerCase().startsWith(now.toLowerCase()))
                .map(a -> Arrays.stream(a.split(" ")).skip(args.length - 1).collect(Collectors.joining(" ")))
                .collect(Collectors.toList());
    }
}

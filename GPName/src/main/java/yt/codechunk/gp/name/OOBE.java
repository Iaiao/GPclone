package yt.codechunk.gp.name;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.LinkedList;
import java.util.List;

public class OOBE implements Listener {
    List<Player> newbies = new LinkedList<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if(Main.getInstance().getName(event.getPlayer().getName()).equalsIgnoreCase(event.getPlayer().getName())) {
            event.getPlayer().setGameMode(GameMode.SPECTATOR);
            newbies.add(event.getPlayer());
            event.getPlayer().sendMessage(Main.getInstance().configOobeChat);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if(!newbies.contains(event.getPlayer())) return;

        Location location = event.getFrom().clone();
        location.setYaw(event.getTo().getYaw());
        location.setPitch(event.getTo().getPitch());
        event.setTo(location);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if(!newbies.contains(event.getPlayer())) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage(Main.getInstance().configSetname);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if(!newbies.contains(event.getPlayer())) return;
        if(!event.getMessage().startsWith("/name") && !event.getMessage().startsWith("/имя")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Main.getInstance().configSetname);
        }
    }
}

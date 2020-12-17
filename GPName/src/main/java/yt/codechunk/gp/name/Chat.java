package yt.codechunk.gp.name;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Collection;
import java.util.stream.Collectors;

public class Chat implements Listener {
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            String format, message;
            Collection<Player> players;
            if (event.getMessage().startsWith("!")) {
                format = Main.getInstance().chatGlobal;
                message = event.getMessage().substring(1);
                players = (Collection<Player>) Bukkit.getOnlinePlayers();
            } else {
                format = Main.getInstance().chatLocal;
                message = event.getMessage();
                players = event.getPlayer()
                        .getNearbyEntities(Main.getInstance().chatLocalRadius, Main.getInstance().chatLocalRadius, Main.getInstance().chatLocalRadius)
                        .stream()
                        .filter(entity -> entity.getType() == EntityType.PLAYER)
                        .map(entity -> (Player) entity)
                        .collect(Collectors.toList());
                players.add(event.getPlayer());
            }
            players.forEach(player -> player.sendMessage(
                    String.format(
                            format,
                            Main.getInstance().strangers.nameFor(player, event.getPlayer()),
                            message
                    )
            ));
        });
    }
}

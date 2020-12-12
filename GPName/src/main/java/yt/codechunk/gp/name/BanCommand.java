package yt.codechunk.gp.name;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BanCommand implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(args.length != 2) {
            return false;
        }
        String name = (args[0] + " " + args[1]).toLowerCase();
        Optional<String> pName = Main.getInstance().getRealName(name);
        if(pName.isPresent()) {
            String playerName = pName.get();
            Bukkit.getBanList(BanList.Type.NAME).addBan(playerName, Arrays.stream(args).skip(2).collect(Collectors.joining(" ")), null, null);
            sender.sendMessage("Забанен!");
        } else {
            sender.sendMessage("Игрок не найден");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        return Main.getInstance().playerNames
                .values()
                .stream()
                .filter(name -> name.startsWith(String.join(" ", args).toLowerCase()))
                .map(Main::formatName)
                .map(name -> Arrays.stream(name.split(" ")).skip(args.length - 1).collect(Collectors.joining(" ")))
                .collect(Collectors.toList());
    }
}

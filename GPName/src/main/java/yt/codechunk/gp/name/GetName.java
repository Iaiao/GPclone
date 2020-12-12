package yt.codechunk.gp.name;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class GetName implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(args.length != 1) return false;
        sender.sendMessage("Имя игрока " + args[0] + ": " + Main.formatName(Main.getInstance().getName(args[0])));
        return true;
    }
}

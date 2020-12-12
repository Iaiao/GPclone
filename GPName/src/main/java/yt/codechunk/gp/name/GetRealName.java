package yt.codechunk.gp.name;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GetRealName implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(args.length != 2) return false;
        String name = Main.formatName(args[0] + " " + args[1]);
        String realName = Main.getInstance().getRealName(name).orElse("Игрок не найден");
        sender.sendMessage("Настоящее имя " + name + ": " + realName);
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

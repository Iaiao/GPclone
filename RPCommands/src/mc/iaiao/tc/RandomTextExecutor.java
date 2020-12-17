package mc.iaiao.tc;

import java.util.*;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;

public class RandomTextExecutor implements CommandExecutor {
    private HashMap<String, Integer> formats;
    private int range;
    private int randomDefaultMin;
    private int randomDefaultMax;
    private boolean randomInputRange;
    private int randomPlayerMin;
    private int randomPlayerMax;
    private String randomError;
    private String randomInvalidNumber;

    RandomTextExecutor(final HashMap<String, Integer> formats, final int range, final int randomDefaultMin, final int randomDefaultMax, final boolean randomInputRange, final int randomPlayerMin, final int randomPlayerMax, final String randomError, final String randomInvalidNumber) {
        this.formats = formats;
        this.range = range;
        this.randomDefaultMin = randomDefaultMin;
        this.randomDefaultMax = randomDefaultMax;
        this.randomInputRange = randomInputRange;
        this.randomPlayerMin = randomPlayerMin;
        this.randomPlayerMax = randomPlayerMax;
        this.randomError = randomError;
        this.randomInvalidNumber = randomInvalidNumber;
    }

    public boolean onCommand(final CommandSender sender, @Nonnull final Command cmd, @Nonnull final String s, @Nonnull final String[] args) {
        String message = Arrays.stream(args).skip(this.randomInputRange ? 2L : 0L).collect(Collectors.joining(" "));
        List<CommandSender> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (sender instanceof Player && this.range > 0) {
            players = ((Player) sender).getNearbyEntities(this.range, this.range, this.range).stream().filter(e -> e.getType() == EntityType.PLAYER).collect(Collectors.toList());
            players.add(sender);
        }
        int i = (int) Math.floor(Math.random() * this.formats.values().stream().reduce(Integer::sum).get());
        String format = "";
        for (final Map.Entry<String, Integer> e2 : this.formats.entrySet()) {
            i -= e2.getValue();
            if (i <= 0) {
                format = e2.getKey();
                break;
            }
        }
        String msg = format.replaceAll("\\{player}", sender instanceof Player ? ((Player) sender).getDisplayName() : sender.getName());
        try {
            int rndMin = (this.randomInputRange && args.length >= 2) ? Integer.parseInt(args[0]) : this.randomDefaultMin;
            int rndMax = (this.randomInputRange && args.length >= 2) ? (Integer.parseInt(args[1]) + 1) : this.randomDefaultMax;
            if (rndMin < this.randomPlayerMin || rndMax - 1 > this.randomPlayerMax) {
                sender.sendMessage(this.randomInvalidNumber);
                return true;
            }
            while (msg.contains("{random}")) {
                msg = msg.replaceFirst("\\{random}", String.valueOf((int) Math.floor(rndMin + Math.random() * (rndMax - rndMin))));
            }
            msg = msg.replaceAll("\\{fixedrandom}", String.valueOf((int) Math.floor(rndMin + Math.random() * (rndMax - rndMin))));
        } catch (NumberFormatException exception) {
            msg = this.randomError;
        }
        final String finalMsg = msg.replaceAll("\\{message}", message);
        players.forEach(p -> p.sendMessage(finalMsg));
        return true;
    }
}

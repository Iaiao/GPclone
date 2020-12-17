package mc.iaiao.tc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class SplitExecutor implements CommandExecutor {
    private String format;
    private int range;
    private String splitBy;
    private int randomDefaultMin;
    private int randomDefaultMax;
    private boolean randomInputRange;
    private int randomPlayerMin;
    private int randomPlayerMax;
    private String randomError;
    private String randomInvalidNumber;

    SplitExecutor(String format, int range, String splitBy, int randomDefaultMin, int randomDefaultMax, boolean randomInputRange, int randomPlayerMin, int randomPlayerMax, String randomError, String randomInvalidNumber) {
        this.format = format;
        this.range = range;
        this.splitBy = splitBy;
        this.randomDefaultMin = randomDefaultMin;
        this.randomDefaultMax = randomDefaultMax;
        this.randomInputRange = randomInputRange;
        this.randomPlayerMin = randomPlayerMin;
        this.randomPlayerMax = randomPlayerMax;
        this.randomError = randomError;
        this.randomInvalidNumber = randomInvalidNumber;
    }

    public boolean onCommand(CommandSender sender, @Nonnull Command cmd, @Nonnull String s, @Nonnull String[] args) {
        String message = Arrays.stream(args).skip(this.randomInputRange ? 2L : 0L).collect(Collectors.joining(" "));
        String[] messages = message.split(this.splitBy);
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (sender instanceof Player && this.range > 0) {
            players = ((Player)sender).getNearbyEntities((double)this.range, (double)this.range, (double)this.range).stream().filter(e -> e.getType() == EntityType.PLAYER).map(p -> (Player)p).collect(Collectors.toList());
            players.add((Player) sender);
        }
        String msg = this.format.replaceAll("\\{player}", ((Player) sender).getDisplayName());
        try {
            int rndMin = this.randomInputRange && args.length >= 2 ? Integer.parseInt(args[0]) : this.randomDefaultMin;
            int rndMax = this.randomInputRange && args.length >= 2 ? Integer.parseInt(args[1]) + 1 : this.randomDefaultMax;
            if (rndMin < this.randomPlayerMin || rndMax - 1 > this.randomPlayerMax) {
                sender.sendMessage(this.randomInvalidNumber);
                return true;
            }
            while (msg.contains("{random}")) {
                msg = msg.replaceFirst("\\{random}", String.valueOf((int)Math.floor((double)rndMin + Math.random() * (double)(rndMax - rndMin))));
            }
            msg = msg.replaceAll("\\{fixedrandom}", String.valueOf((int)Math.floor((double)rndMin + Math.random() * (double)(rndMax - rndMin))));
        }
        catch (NumberFormatException ignored) {
            msg = this.randomError;
        }
        String finalMsg = SplitExecutor.replace(msg, Pattern.compile("\\{message \\d}"), m -> {
            String a = m.group();
            int n = Integer.parseInt(a.substring("{message ".length(), a.length() - "}".length()));
            if (messages.length < n) {
                return "";
            }
            return messages[n - 1];
        });
        players.forEach(p -> p.sendMessage(finalMsg));
        return true;
    }

    private static String replace(String input, Pattern regex, Function<Matcher, String> callback) {
        StringBuffer resultString = new StringBuffer();
        Matcher regexMatcher = regex.matcher(input);
        while (regexMatcher.find()) {
            regexMatcher.appendReplacement(resultString, callback.apply(regexMatcher));
        }
        regexMatcher.appendTail(resultString);
        return resultString.toString();
    }
}


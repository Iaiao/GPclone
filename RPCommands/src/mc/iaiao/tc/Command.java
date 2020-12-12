// 
// Decompiled by Procyon v0.5.36
// 

package mc.iaiao.tc;

import org.bukkit.ChatColor;
import java.util.Iterator;
import org.bukkit.configuration.ConfigurationSection;
import java.util.HashMap;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.bukkit.command.CommandSender;
import java.util.List;
import java.util.Collections;
import org.bukkit.command.CommandExecutor;

public class Command extends org.bukkit.command.Command
{
    private CommandType type;
    private CommandExecutor executor;

    protected Command(final String name, final String type, final Main plugin) {
        super(name, "Команда /" + name, "/" + name + " <текст>", Collections.emptyList());
        this.type = CommandType.valueOf(type.toUpperCase());
        if (plugin.getConfig().getBoolean("use permissions")) {
            this.setPermission("rpcommands." + name);
        }
        switch (this.type) {
            case TEXT: {
                this.executor = new TextExecutor(this.getString(plugin, "format"), this.getInt(plugin, "range"), this.getInt(plugin, "random.default-min"), this.getInt(plugin, "random.default-max"), this.getBoolean(plugin, "random.input-range"), this.getInt(plugin, "random.player-min"), this.getInt(plugin, "random.player-max"), this.getString(plugin, "random.error"), this.getString(plugin, "random.invalid-player-range"));
                break;
            }
            case RANDOMTEXT: {
                this.executor = new RandomTextExecutor(this.getStringIntHashmap(plugin, "chances"), this.getInt(plugin, "range"), this.getInt(plugin, "random.default-min"), this.getInt(plugin, "random.default-max"), this.getBoolean(plugin, "random.input-range"), this.getInt(plugin, "random.player-min"), this.getInt(plugin, "random.player-max"), this.getString(plugin, "random.error"), this.getString(plugin, "random.invalid-player-range"));
                break;
            }
            case SPLIT: {
                this.executor = new SplitExecutor(this.getString(plugin, "format"), this.getInt(plugin, "range"), this.getString(plugin, "split-by"), this.getInt(plugin, "random.default-min"), this.getInt(plugin, "random.default-max"), this.getBoolean(plugin, "random.input-range"), this.getInt(plugin, "random.player-min"), this.getInt(plugin, "random.player-max"), this.getString(plugin, "random.error"), this.getString(plugin, "random.invalid-player-range"));
                break;
            }
        }
    }
    
    public boolean execute(@Nonnull final CommandSender sender, @Nonnull final String s, @Nonnull final String[] args) {
        return this.executor.onCommand(sender, this, s, args);
    }
    
    private String getString(final Main plugin, final String path) {
        return color(Objects.requireNonNull(plugin.getConfig().getString(this.getName() + "." + path, "")));
    }
    
    private HashMap<String, Integer> getStringIntHashmap(final Main plugin, final String path) {
        final HashMap<String, Integer> map = new HashMap<>();
        for (final String key : Objects.requireNonNull(plugin.getConfig().getConfigurationSection(this.getName() + "." + path)).getKeys(false)) {
            map.put(key, this.getInt(plugin, path + "." + key));
        }
        return map;
    }
    
    private int getInt(final Main plugin, final String path) {
        return plugin.getConfig().getInt(this.getName() + "." + path, -1);
    }
    
    private boolean getBoolean(final Main plugin, final String path) {
        return plugin.getConfig().getBoolean(this.getName() + "." + path, false);
    }
    
    private static String color(final String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
    
    private enum CommandType
    {
        TEXT, 
        RANDOMTEXT, 
        SPLIT;
    }
}

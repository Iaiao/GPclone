// 
// Decompiled by Procyon v0.5.36
// 

package mc.iaiao.tc;

import org.bukkit.command.CommandMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.logging.LogManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin
{
    public void onEnable() {
        this.saveDefaultConfig();
        for (final String cmd : this.getConfig().getKeys(false)) {
            if (cmd.equals("use permissions")) {
                continue;
            }
            this.getLogger().info("Loading command " + cmd);
            final CommandMap commands = this.getServer().getCommandMap();
            final org.bukkit.command.Command command = new Command(cmd, Objects.requireNonNull(this.getConfig().getString(cmd + ".type")), this);
            commands.register(cmd, command);
        }
    }
}

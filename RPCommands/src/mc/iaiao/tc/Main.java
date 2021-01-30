package mc.iaiao.tc;

import org.bukkit.command.CommandMap;

import java.lang.reflect.Field;
import java.util.Objects;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    public void onEnable() {
        this.saveDefaultConfig();
        for (final String cmd : this.getConfig().getKeys(false)) {
            if (cmd.equals("use permissions")) {
                continue;
            }
            this.getLogger().info("Loading command " + cmd);
            final CommandMap commands = getCommandMap();
            final org.bukkit.command.Command command = new Command(cmd, Objects.requireNonNull(this.getConfig().getString(cmd + ".type")), this);
            commands.register(cmd, command);
        }
    }
    
    public CommandMap getCommandMap() {
        try {
            Field field = getServer().getClass().getDeclaredField("commandMap");
            boolean wasAccessible = field.isAccessible();
            field.setAccessible(true);
            CommandMap map = (CommandMap) field.get(getServer());
            field.setAccessible(wasAccessible);
            return map;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            throw new IllegalStateException("Cannot get CraftServer#commandMap");
        }
    }
}

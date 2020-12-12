package yt.codechunk.gp.name;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class NamePlaceholder extends PlaceholderExpansion {
    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public boolean canRegister(){
        return true;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "gpname";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Chunk";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if(player == null) return "";
        if(identifier.equals("name")) {
            return Main.formatName(Main.getInstance().getName(player.getName()));
        } else {
            return null;
        }
    }
}

package yt.codechunk.gp.jobs;

import java.io.Serializable;

public class Jobs implements Serializable {
    public static Jobs UNEMPLOYED = new Jobs(Job.UNEMPLOYED, Job.UNEMPLOYED);

    public Job primary;
    public Job secondary;

    public Jobs(Job primary, Job secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }

    boolean canBreak() { return this.primary.canBreak() || this.secondary.canBreak(); }
    boolean canPlace() { return primary.canPlace() || secondary.canPlace(); }
    boolean canDamage() { return primary.canDamage() || secondary.canDamage(); }
    boolean canFarm() { return primary.canFarm() || secondary.canFarm(); }
    boolean canBrew() { return primary.canBrew() || secondary.canBrew(); }
    boolean canRedstone() { return primary.canRedstone() || secondary.canRedstone(); }
    boolean canEnchant() { return primary.canEnchant() || secondary.canEnchant(); }
    boolean canCreatePortal() { return primary.canCreatePortal() || secondary.canCreatePortal(); }
    boolean canTinker() { return primary.canTinker() || secondary.canTinker(); }
    boolean canMakeMaps() { return primary.canMakeMaps() || secondary.canMakeMaps(); }
    boolean canFish() { return primary.canFish() || secondary.canFish(); }
    boolean isLawyer() { return primary.isLawyer() || secondary.isLawyer(); }
    boolean canStonecut() { return primary.canStonecut() || secondary.canStonecut(); }

    @Override
    public String toString() {
        return primary.toString() + secondary.toString();
    }
}

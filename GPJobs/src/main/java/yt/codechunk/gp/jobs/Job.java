package yt.codechunk.gp.jobs;

import java.io.Serializable;

public enum Job implements Serializable {
    UNEMPLOYED  ("\uae00"),
    TINKERER    ("\uae01"),
    BUILDER     ("\uae02"),
    MINER       ("\uae03"),
    HUNTER      ("\uae04"),
    FARMER      ("\uae05"),
    FISHERMAN   ("\uae06"),
    BREWER      ("\uae07"),
    ENGINEER    ("\uae08"),
    WIZARD      ("\uae09"),
    CARTOGRAPHER("\uae0a"),
    // NETHER   ("\uae0b"),
    LAWYER      ("\uae0c"),
    MASON       ("\uae0d");

    private final String icon;

    Job(String icon) {
        this.icon = icon;
    }

    boolean canBreak() {
        return this == MINER;
    }

    boolean canPlace() {
        return this == BUILDER;
    }

    boolean canDamage() {
        return this == HUNTER;
    }

    boolean canFarm() {
        return this == FARMER;
    }

    boolean canBrew() {
        return this == BREWER;
    }

    boolean canRedstone() {
        return this == ENGINEER;
    }

    boolean canEnchant() {
        return this == WIZARD;
    }

    boolean canCreatePortal() {
        return this == WIZARD;
    }

    boolean canTinker() {
        return this == TINKERER;
    }

    boolean canMakeMaps() {
        return this == CARTOGRAPHER;
    }

    boolean canFish() {
        return this == FISHERMAN;
    }

    boolean isLawyer() {
        return this == LAWYER;
    }

    boolean canStonecut() {
        return this == MASON;
    }

    @Override
    public String toString() {
        return icon;
    }
}

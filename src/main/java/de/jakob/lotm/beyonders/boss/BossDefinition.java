package de.jakob.lotm.beyonders.boss;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Mob;

public final class BossDefinition {
    public enum Hostility {
        ALWAYS,
        RETALIATE
    }

    public enum IdleBehavior {
        STAY,
        WANDER
    }

    private final Component barTitle;
    private final BossEvent.BossBarColor barColor;
    private final BossEvent.BossBarOverlay barOverlay;
    private final Hostility hostility;
    private final IdleBehavior idleBehavior;
    private final double wanderRadius;
    private final double targetRange;
    private final double barRange;
    private final boolean meleeFallback;
    private final Component alertTitle;
    private final Component alertSubtitle;
    private final int alertFadeIn;
    private final int alertStay;
    private final int alertFadeOut;

    private BossDefinition(Builder builder) {
        this.barTitle = builder.barTitle;
        this.barColor = builder.barColor;
        this.barOverlay = builder.barOverlay;
        this.hostility = builder.hostility;
        this.idleBehavior = builder.idleBehavior;
        this.wanderRadius = Math.max(0.0, builder.wanderRadius);
        this.targetRange = Math.max(1.0, builder.targetRange);
        this.barRange = Math.max(0.0, builder.barRange);
        this.meleeFallback = builder.meleeFallback;
        this.alertTitle = builder.alertTitle;
        this.alertSubtitle = builder.alertSubtitle == null ? Component.empty() : builder.alertSubtitle;
        this.alertFadeIn = Math.max(0, builder.alertFadeIn);
        this.alertStay = Math.max(0, builder.alertStay);
        this.alertFadeOut = Math.max(0, builder.alertFadeOut);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static BossDefinition defaults(Component title) {
        return builder().barTitle(title).build();
    }

    public Builder toBuilder() {
        return new Builder()
            .barTitle(barTitle)
            .barColor(barColor)
            .barOverlay(barOverlay)
            .hostility(hostility)
            .idleBehavior(idleBehavior, wanderRadius)
            .targetRange(targetRange)
            .barRange(barRange)
            .meleeFallback(meleeFallback)
            .alert(alertTitle, alertSubtitle, alertFadeIn, alertStay, alertFadeOut);
    }

    public Component barTitle() {
        return barTitle;
    }

    public BossEvent.BossBarColor barColor() {
        return barColor;
    }

    public BossEvent.BossBarOverlay barOverlay() {
        return barOverlay;
    }

    public Hostility hostility() {
        return hostility;
    }

    public IdleBehavior idleBehavior() {
        return idleBehavior;
    }

    public double wanderRadius() {
        return wanderRadius;
    }

    public double targetRange() {
        return targetRange;
    }

    public double barRange() {
        return barRange;
    }

    public boolean meleeFallback() {
        return meleeFallback;
    }

    public Component alertTitle() {
        return alertTitle;
    }

    public Component alertSubtitle() {
        return alertSubtitle;
    }

    public int alertFadeIn() {
        return alertFadeIn;
    }

    public int alertStay() {
        return alertStay;
    }

    public int alertFadeOut() {
        return alertFadeOut;
    }

    public boolean hasAlert() {
        return alertTitle != null;
    }

    CompoundTag save(Mob mob, boolean enabled) {
        HolderLookup.Provider registries = mob.level().registryAccess();
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("enabled", enabled);
        tag.putString("barTitle", Component.Serializer.toJson(barTitle, registries));
        tag.putString("barColor", barColor.name());
        tag.putString("barOverlay", barOverlay.name());
        tag.putString("hostility", hostility.name());
        tag.putString("idleBehavior", idleBehavior.name());
        tag.putDouble("wanderRadius", wanderRadius);
        tag.putDouble("targetRange", targetRange);
        tag.putDouble("barRange", barRange);
        tag.putBoolean("meleeFallback", meleeFallback);
        tag.putBoolean("hasAlert", hasAlert());
        if (alertTitle != null) {
            tag.putString("alertTitle", Component.Serializer.toJson(alertTitle, registries));
            tag.putString("alertSubtitle", Component.Serializer.toJson(alertSubtitle, registries));
        }
        tag.putInt("alertFadeIn", alertFadeIn);
        tag.putInt("alertStay", alertStay);
        tag.putInt("alertFadeOut", alertFadeOut);
        return tag;
    }

    static BossDefinition load(Mob mob, CompoundTag tag) {
        HolderLookup.Provider registries = mob.level().registryAccess();
        Component title = parseComponent(tag.getString("barTitle"), registries, mob.getDisplayName());
        BossEvent.BossBarColor color = enumValue(BossEvent.BossBarColor.class, tag.getString("barColor"), BossEvent.BossBarColor.RED);
        BossEvent.BossBarOverlay overlay = enumValue(BossEvent.BossBarOverlay.class, tag.getString("barOverlay"), BossEvent.BossBarOverlay.PROGRESS);
        Hostility hostility = enumValue(Hostility.class, tag.getString("hostility"), Hostility.ALWAYS);
        IdleBehavior idleBehavior = enumValue(IdleBehavior.class, tag.getString("idleBehavior"), IdleBehavior.WANDER);
        Component alertTitle = tag.getBoolean("hasAlert") ? parseComponent(tag.getString("alertTitle"), registries, Component.literal("You have been noticed!")) : null;
        Component alertSubtitle = tag.getBoolean("hasAlert") ? parseComponent(tag.getString("alertSubtitle"), registries, Component.empty()) : Component.empty();
        return builder()
            .barTitle(title)
            .barColor(color)
            .barOverlay(overlay)
            .hostility(hostility)
            .idleBehavior(idleBehavior, tag.getDouble("wanderRadius"))
            .targetRange(tag.contains("targetRange") ? tag.getDouble("targetRange") : 64.0)
            .barRange(tag.contains("barRange") ? tag.getDouble("barRange") : 64.0)
            .meleeFallback(!tag.contains("meleeFallback") || tag.getBoolean("meleeFallback"))
            .alert(alertTitle, alertSubtitle, tag.getInt("alertFadeIn"), tag.getInt("alertStay"), tag.getInt("alertFadeOut"))
            .build();
    }

    private static Component parseComponent(String json, HolderLookup.Provider registries, Component fallback) {
        if (json == null || json.isBlank()) {
            return fallback;
        }
        try {
            Component component = Component.Serializer.fromJson(json, registries);
            return component == null ? fallback : component;
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    private static <T extends Enum<T>> T enumValue(Class<T> type, String name, T fallback) {
        try {
            return Enum.valueOf(type, name);
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    public static final class Builder {
        private Component barTitle = Component.literal("Boss");
        private BossEvent.BossBarColor barColor = BossEvent.BossBarColor.RED;
        private BossEvent.BossBarOverlay barOverlay = BossEvent.BossBarOverlay.PROGRESS;
        private Hostility hostility = Hostility.ALWAYS;
        private IdleBehavior idleBehavior = IdleBehavior.WANDER;
        private double wanderRadius = 12.0;
        private double targetRange = 64.0;
        private double barRange = 64.0;
        private boolean meleeFallback = true;
        private Component alertTitle;
        private Component alertSubtitle = Component.empty();
        private int alertFadeIn = 10;
        private int alertStay = 60;
        private int alertFadeOut = 20;

        public Builder barTitle(Component title) {
            this.barTitle = title == null ? Component.literal("Boss") : title;
            return this;
        }

        public Builder barColor(BossEvent.BossBarColor color) {
            this.barColor = color == null ? BossEvent.BossBarColor.RED : color;
            return this;
        }

        public Builder barOverlay(BossEvent.BossBarOverlay overlay) {
            this.barOverlay = overlay == null ? BossEvent.BossBarOverlay.PROGRESS : overlay;
            return this;
        }

        public Builder hostility(Hostility hostility) {
            this.hostility = hostility == null ? Hostility.ALWAYS : hostility;
            return this;
        }

        public Builder idleBehavior(IdleBehavior behavior, double radius) {
            this.idleBehavior = behavior == null ? IdleBehavior.WANDER : behavior;
            this.wanderRadius = radius;
            return this;
        }

        public Builder targetRange(double range) {
            this.targetRange = range;
            return this;
        }

        public Builder barRange(double range) {
            this.barRange = range;
            return this;
        }

        public Builder meleeFallback(boolean enabled) {
            this.meleeFallback = enabled;
            return this;
        }

        public Builder alert(Component title, Component subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
            this.alertTitle = title;
            this.alertSubtitle = subtitle == null ? Component.empty() : subtitle;
            this.alertFadeIn = fadeInTicks;
            this.alertStay = stayTicks;
            this.alertFadeOut = fadeOutTicks;
            return this;
        }

        public Builder alertTitle(Component title) {
            this.alertTitle = title;
            return this;
        }

        public Builder alertSubtitle(Component subtitle) {
            this.alertSubtitle = subtitle == null ? Component.empty() : subtitle;
            return this;
        }

        public Builder alertTiming(int fadeInTicks, int stayTicks, int fadeOutTicks) {
            this.alertFadeIn = fadeInTicks;
            this.alertStay = stayTicks;
            this.alertFadeOut = fadeOutTicks;
            return this;
        }

        public Builder noAlert() {
            this.alertTitle = null;
            this.alertSubtitle = Component.empty();
            return this;
        }

        public BossDefinition build() {
            return new BossDefinition(this);
        }
    }
}

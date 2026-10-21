package de.jakob.lotm.beyonders.boss;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import de.jakob.lotm.entity.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

public final class BossRegistry {
    private static final String INSTANCE_KEY = "neoboss_instance_config";
    private static final Map<EntityType<?>, BossDefinition> DEFINITIONS = new ConcurrentHashMap<>();

    private BossRegistry() {
    }

    public static <T extends Mob> void register(EntityType<T> entityType, BossDefinition definition) {
        DEFINITIONS.put(entityType, definition);
    }

    public static void unregister(EntityType<? extends Mob> entityType) {
        DEFINITIONS.remove(entityType);
    }

    public static Optional<BossDefinition> getDefinition(Mob mob) {
        CompoundTag persistent = mob.getPersistentData();
        if (persistent.contains(INSTANCE_KEY, Tag.TAG_COMPOUND)) {
            CompoundTag instance = persistent.getCompound(INSTANCE_KEY);
            if (!instance.getBoolean("enabled")) {
                return Optional.empty();
            }
            return Optional.of(BossDefinition.load(mob, instance));
        }
        return Optional.ofNullable(DEFINITIONS.get(mob.getType()));
    }

    public static Optional<BossDefinition> getRegisteredDefinition(Mob mob) {
        return Optional.ofNullable(DEFINITIONS.get(mob.getType()));
    }

    public static void setInstanceDefinition(Mob mob, BossDefinition definition) {
        mob.getPersistentData().put(INSTANCE_KEY, definition.save(mob, true));
    }

    public static void disableInstance(Mob mob) {
        CompoundTag persistent = mob.getPersistentData();
        CompoundTag disabled = new CompoundTag();
        disabled.putBoolean("enabled", false);
        persistent.put(INSTANCE_KEY, disabled);
    }

    public static void clearInstanceOverride(Mob mob) {
        mob.getPersistentData().remove(INSTANCE_KEY);
    }

    public static boolean hasInstanceOverride(Mob mob) {
        return mob.getPersistentData().contains(INSTANCE_KEY, Tag.TAG_COMPOUND);
    }

    public static void registerBosses() {
        register(
                ModEntities.FORS_WALL.get(),
                BossDefinition.builder()
                        .hostility(BossDefinition.Hostility.RETALIATE)
                        .alert(Component.literal("Fors Wall").withColor(0x8cdeff), Component.literal("The Angel of Stars"), 6, 15, 5)
                        .barTitle(Component.literal("Fors Wall"))
                        .barColor(BossEvent.BossBarColor.BLUE)
                        .barOverlay(BossEvent.BossBarOverlay.NOTCHED_10)
                        .build()
        );
    }
}

package de.jakob.lotm.beyonders.abilities.common.passives;

import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityItem;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.beyonders.sefirah.SefirahHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ElevatedDivinationAbility extends PassiveAbilityItem {

    /** Players with this passive active — their divination never fails against non-sefirot holders. */
    public static final Set<UUID> ELEVATED_DIVINATION_ACTIVE =
            Collections.newSetFromMap(new ConcurrentHashMap<>());

    /**
     * Players with elevated divination who are currently inside the Sefirah Castle dimension.
     * Only while present here does the "can divine anyone except darkness seq 0" bonus apply.
     */
    public static final Set<UUID> ELEVATED_DIVINATION_IN_CASTLE =
            Collections.newSetFromMap(new ConcurrentHashMap<>());

    private static final String REQUIRED_SEFIROT = "sefirah_castle";

    public ElevatedDivinationAbility(Item.Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of();
    }

    /** Only applies to the Sefirah Castle owner. Checked server-side only. */
    @Override
    public boolean shouldApplyTo(LivingEntity entity) {
        if (entity.level().isClientSide()) return false;
        if (!(entity instanceof ServerPlayer player)) return false;
        return REQUIRED_SEFIROT.equals(SefirahHandler.getClaimedSefirot(player));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide()) return;
        ELEVATED_DIVINATION_ACTIVE.add(entity.getUUID());
        // Track whether the owner is currently inside the sefirah_castle dimension
        if (entity.level().dimension().equals(ModDimensions.SEFIRAH_CASTLE_DIMENSION_KEY)) {
            ELEVATED_DIVINATION_IN_CASTLE.add(entity.getUUID());
        } else {
            ELEVATED_DIVINATION_IN_CASTLE.remove(entity.getUUID());
        }
    }

    @Override
    public void onPassiveAbilityRemoved(LivingEntity entity, net.minecraft.server.level.ServerLevel serverLevel) {
        ELEVATED_DIVINATION_ACTIVE.remove(entity.getUUID());
        ELEVATED_DIVINATION_IN_CASTLE.remove(entity.getUUID());
    }
}

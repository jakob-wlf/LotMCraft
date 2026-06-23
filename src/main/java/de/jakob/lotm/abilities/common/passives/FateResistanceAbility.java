package de.jakob.lotm.beyonders.abilities.common.passives;

import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityItem;
import de.jakob.lotm.beyonders.sefirah.SefirahHandler;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Passive: Fate Resistance.
 *
 * Granted to the Sefirah Castle owner at Sequence 6 or below.
 * While active, the player's luck can only be altered by a Sequence 1 beyonder.
 * All other luck modifications from outside sources are blocked.
 */
public class FateResistanceAbility extends PassiveAbilityItem {

    /** UUIDs of players who currently have Fate Resistance active. */
    public static final Set<UUID> FATE_RESISTANCE_ACTIVE =
            Collections.newSetFromMap(new ConcurrentHashMap<>());

    private static final String REQUIRED_SEFIROT = "sefirah_castle";
    private static final int THRESHOLD_SEQ = 6;

    public FateResistanceAbility(Item.Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of();
    }

    /** Only the Sefirah Castle owner at seq 6 or below. */
    @Override
    public boolean shouldApplyTo(LivingEntity entity) {
        if (entity.level().isClientSide()) return false;
        if (!(entity instanceof ServerPlayer player)) return false;
        if (!REQUIRED_SEFIROT.equals(SefirahHandler.getClaimedSefirot(player))) return false;
        int seq = BeyonderData.getSequence(player);
        return seq < 0 || seq <= THRESHOLD_SEQ;
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide()) return;
        FATE_RESISTANCE_ACTIVE.add(entity.getUUID());
    }

    @Override
    public void onPassiveAbilityRemoved(LivingEntity entity, net.minecraft.server.level.ServerLevel serverLevel) {
        FATE_RESISTANCE_ACTIVE.remove(entity.getUUID());
    }

    /**
     * Returns true if a luck change on {@code targetUUID} should be blocked.
     * Blocked when the target has Fate Resistance AND the changer is not Sequence 0 or 1.
     *
     * @param targetUUID  UUID of the player whose luck is being changed
     * @param changerSeq  sequence of the player/ability causing the change, or -1 for system/internal
     */
    public static boolean blocksLuckChange(UUID targetUUID, int changerSeq) {
        if (!FATE_RESISTANCE_ACTIVE.contains(targetUUID)) return false;
        return changerSeq > 1;
    }
}

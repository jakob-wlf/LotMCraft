package de.jakob.lotm.abilities.common.passives;

import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.sefirah.SefirahHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ElevatedConcealmentAbility extends PassiveAbilityItem {

    /**
     * Players with this passive active — they are concealed from all divination and targeting
     * abilities used by players who do not own a sefirot.
     */
    public static final Set<UUID> ELEVATED_CONCEALMENT_ACTIVE =
            Collections.newSetFromMap(new ConcurrentHashMap<>());

    private static final String REQUIRED_SEFIROT = "river_of_eternal_darkness";

    public ElevatedConcealmentAbility(Item.Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        // No pathway/sequence requirement — granted purely by sefirot ownership.
        return Map.of();
    }

    /** Only applies to the River of Eternal Darkness owner. Checked server-side only. */
    @Override
    public boolean shouldApplyTo(LivingEntity entity) {
        if (entity.level().isClientSide()) return false;
        if (!(entity instanceof ServerPlayer player)) return false;
        return REQUIRED_SEFIROT.equals(SefirahHandler.getClaimedSefirot(player));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide()) return;
        // Refresh presence in the set every 5 ticks (the tick rate for passives)
        ELEVATED_CONCEALMENT_ACTIVE.add(entity.getUUID());
    }

    @Override
    public void onPassiveAbilityRemoved(LivingEntity entity, net.minecraft.server.level.ServerLevel serverLevel) {
        ELEVATED_CONCEALMENT_ACTIVE.remove(entity.getUUID());
    }
}

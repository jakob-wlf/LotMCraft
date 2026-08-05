package de.jakob.lotm.beyonders.abilities.wheel_of_fortune.passives;

import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityItem;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MercuryBodyAbility extends PassiveAbilityItem {
    private static final Set<UUID> mercuryBodyHolders = ConcurrentHashMap.newKeySet();

    public MercuryBodyAbility(Item.Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of("wheel_of_fortune", 4);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        mercuryBodyHolders.add(entity.getUUID());
    }

    @Override
    public void onPassiveAbilityRemoved(LivingEntity entity, ServerLevel serverLevel) {
        mercuryBodyHolders.remove(entity.getUUID());
    }

    public static boolean hasMercuryBody(LivingEntity entity) {
        return mercuryBodyHolders.contains(entity.getUUID());
    }

    public static boolean blocksInquiry(LivingEntity target, LivingEntity caster) {
        return blocksInquiry(target, BeyonderData.getSequence(caster));
    }

    public static boolean blocksInquiry(LivingEntity target, int casterSequence) {
        if (!hasMercuryBody(target)) return false;
        int targetSequence = BeyonderData.getSequence(target);
        return casterSequence < 0 || casterSequence >= targetSequence;
    }

    public static void warn(LivingEntity target, String casterName, int casterSequence, String details) {
        if (!(target instanceof ServerPlayer player) || !hasMercuryBody(target)) return;
        player.sendSystemMessage(Component.literal(
                "\u00A76[Mercury Body] \u00A7e" + casterName + " \u00A77(Sequence " + casterSequence
                        + ") bypassed your resistance: \u00A7f" + details));
    }
}
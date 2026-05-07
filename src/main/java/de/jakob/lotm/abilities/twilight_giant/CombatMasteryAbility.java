package de.jakob.lotm.abilities.twilight_giant;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)


public class CombatMasteryAbility extends Ability {

    // Cooldown duration in seconds
    private static final int Cooldown_Duration = 10;

    

    
    private static final Map<UUID, Integer> Crit_Charges = new ConcurrentHashMap<>();

    public CombatMasteryAbility(String id) {
        super(id, Cooldown_Duration); // cooldown matches active duration
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "twilight_giant", 9
        ));
    }

    @Override
    protected float getSpiritualityCost() {
        return 20;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        UUID uuid = entity.getUUID();
        int sequence = BeyonderData.getSequence(entity);
        int charges = sequence <= 8 ? 2 : 1;
        Crit_Charges.put(uuid, charges);
        AbilityUtil.sendActionBar(entity, Component.literal("Combat Mastery: " + charges + " crits"));
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
            SoundEvents.PLAYER_ATTACK_CRIT, entity.getSoundSource(), 1.0f, 1.0f);
        if (level instanceof ServerLevel serverLevel) {
            ServerScheduler.scheduleDelayed(20 * Cooldown_Duration, () -> Crit_Charges.remove(uuid), serverLevel);
        }
    }

    @SubscribeEvent
    public static void onOutgoingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        UUID uuid = attacker.getUUID();
        Integer charges = Crit_Charges.get(uuid);
        if (charges == null || charges <= 0) return;
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) return;

        event.setAmount(event.getAmount() * 1.75f);
        ParticleUtil.spawnParticles(serverLevel, ParticleTypes.ANGRY_VILLAGER,
            event.getEntity().getEyePosition().subtract(0, 0.25, 0),
            25, 0.6, 0.0);

        int remaining = charges - 1;
        if (remaining <= 0) {
            Crit_Charges.remove(uuid);
        } else {
            Crit_Charges.put(uuid, remaining);
        }
        AbilityUtil.sendActionBar(attacker, Component.literal("Combat Mastery: " + Math.max(remaining, 0) + " crits"));
    }


}
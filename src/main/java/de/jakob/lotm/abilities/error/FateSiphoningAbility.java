package de.jakob.lotm.abilities.error;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.rendering.effectRendering.DirectionalEffectManager;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class FateSiphoningAbility extends Ability {

    private static final HashMap<UUID, UUID> linkedEntities = new HashMap<>();

    public FateSiphoningAbility(String id) {
        super(id, 25);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("error", 2));
    }

    @Override
    public float getSpiritualityCost() {
        return 1000;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 30, 2);
        if(target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.fate_siphoning.no_target").withColor(0x6d32a8));
            return;
        }

        // High-sequence opponents may outright resist the fate link being established
        double failureChance = AbilityUtil.getSequenceFailureChance(entity, target);
        if (ThreadLocalRandom.current().nextDouble() < failureChance) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.fate_siphoning.resisted").withColor(0x6d32a8));
            return;
        }

        DirectionalEffectManager.playEffect(DirectionalEffectManager.DirectionalEffect.FATE_SIPHONING, entity.getEyePosition().x, entity.getEyePosition().y, entity.getEyePosition().z,
                target.getX(), target.getY() + target.getEyeHeight() * 0.5, target.getZ(),
                40,
                serverLevel);

        linkedEntities.put(entity.getUUID(), target.getUUID());
        ServerScheduler.scheduleDelayed(20 * 14, () -> linkedEntities.remove(entity.getUUID()));
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingIncomingDamageEvent event) {
        if(!(event.getEntity().level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if(event.getSource().is(ModDamageTypes.LOOSING_CONTROL)) {
            return;
        }

        LivingEntity entity = event.getEntity();
        if(!linkedEntities.containsKey(entity.getUUID())) {
            return;
        }

        Entity target = serverLevel.getEntity(linkedEntities.get(entity.getUUID()));
        if(target instanceof LivingEntity targetLiving) {
            float damage = event.getAmount();
            DamageSource source = serverLevel.damageSources().generic();

            // The target's sequence may reduce how much fate can be siphoned onto them
            double resistance = AbilityUtil.getSequenceResistanceFactor(entity, targetLiving);
            float redirected = (float)(damage * (1.0 - resistance));
            if (redirected > 0) {
                targetLiving.hurt(source, redirected);
            }
        }

        // Always cancel to protect the caster: the fate link absorbs the incoming damage
        // regardless of how much is actually redirected to the target. When the target's
        // sequence reduces the redirect, that portion is simply lost rather than
        // returning to the caster.
        event.setCanceled(true);
    }
}

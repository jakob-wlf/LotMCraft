package de.jakob.lotm.abilities.hanged.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.abilities.hanged.HangedEffectUtil;
import de.jakob.lotm.abilities.hanged.HangedPathwayConstants;
import de.jakob.lotm.abilities.hanged.HangedRenderEffectUtil;
import de.jakob.lotm.damage.ModDamageTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.Map;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class DarkAngelAbility extends PassiveAbilityItem {
    private static final float PURIFICATION_DAMAGE_MULTIPLIER = 0.4f;

    public DarkAngelAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of(HangedPathwayConstants.PATHWAY_ID, HangedPathwayConstants.SEQUENCE_DARK_ANGEL);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide) {
            return;
        }

        entity.removeEffect(MobEffects.BLINDNESS);
        entity.removeEffect(MobEffects.DARKNESS);
        entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 1, false, false, false));

        if (entity.tickCount % 20 == 0) {
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 1, false, false, false));
        }

        if (level instanceof ServerLevel serverLevel && entity.tickCount % 30 == 0) {
            HangedRenderEffectUtil.playMovable(de.jakob.lotm.rendering.effectRendering.MovableEffectManager.MovableEffect.DEPRAVITY_ARMOR, serverLevel, entity, 28, false);
            HangedEffectUtil.spawnShadowAura(serverLevel, entity);
            HangedEffectUtil.spawnDepravityAura(serverLevel, entity);
            HangedEffectUtil.playShadowPulse(serverLevel, entity.position(), 0.72f);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (!((DarkAngelAbility) PassiveAbilityHandler.DARK_ANGEL.get()).shouldApplyTo(entity)) {
            return;
        }

        if (event.getSource().is(ModDamageTypes.DARKNESS_GENERIC)) {
            event.setCanceled(true);
            if (entity.level() instanceof ServerLevel serverLevel) {
                HangedEffectUtil.spawnShadowBurst(serverLevel, entity.position().add(0, entity.getBbHeight() * 0.5, 0), 0.9, 20);
            }
            return;
        }

        if (event.getSource().is(ModDamageTypes.PURIFICATION) || event.getSource().is(ModDamageTypes.PURIFICATION_INDIRECT)) {
            event.setAmount(event.getAmount() * PURIFICATION_DAMAGE_MULTIPLIER);
        }
    }
}

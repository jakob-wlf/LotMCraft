package de.jakob.lotm.effect;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.DisabledAbilitiesComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class SpiritCalledEffect extends MobEffect {

    protected SpiritCalledEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) return true;

        entity.setDeltaMovement(Vec3.ZERO);
        entity.hurtMarked = true;
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 100, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffects.JUMP, 30, 128, false, false, false));

        if(amplifier >= 1) {
            entity.hurt(ModDamageTypes.source(entity.level(), ModDamageTypes.SPIRIT_CALLED, entity), amplifier * 2);
        }

        if(amplifier >= 2 && BeyonderData.getSequence(entity) >= (10 - amplifier - 1)) {
            DisabledAbilitiesComponent component = entity.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
            component.disableAbilityUsageForTime("spirit_called", 20, entity);
        }

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    // -------------------------------------------------------------------------
    // Block item usage while Spirit Called is active
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity().level() instanceof ServerLevel)) return;
        if (event.getEntity().hasEffect(ModEffects.SPIRIT_CALLED)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity().level() instanceof ServerLevel)) return;
        if (event.getEntity().hasEffect(ModEffects.SPIRIT_CALLED)) {
            event.setCanceled(true);
        }
    }

    // -------------------------------------------------------------------------
    // Armor bypass: re-deal incoming damage through the armor-bypassing source
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (!target.hasEffect(ModEffects.SPIRIT_CALLED)) return;
        if (!(target.level() instanceof ServerLevel serverLevel)) return;

        // Don't re-process damage that is already armor-bypassing (avoids infinite loop)
        var damageTypeKey = event.getSource().typeHolder().unwrapKey();
        if (damageTypeKey.isPresent() && damageTypeKey.get().equals(ModDamageTypes.SPIRIT_CALLED)) return;

        float amount = event.getAmount();
        if (amount <= 0) return;

        // Cancel the armored hit and reschedule as armor-bypassing damage next tick
        // (We can't deal damage inside a damage event safely, so we defer by 1 tick)
        event.setCanceled(true);
        net.minecraft.server.level.ServerPlayer attacker = null;
        if (event.getSource().getEntity() instanceof ServerPlayer sp) {
            attacker = sp;
        }
        final net.minecraft.world.entity.Entity attackerEntity = event.getSource().getEntity();
        final float finalAmount = amount;

        de.jakob.lotm.util.scheduling.ServerScheduler.scheduleDelayed(1, () -> {
            if (!target.isAlive() || !target.hasEffect(ModEffects.SPIRIT_CALLED)) return;
            target.hurt(
                    ModDamageTypes.source(serverLevel, ModDamageTypes.SPIRIT_CALLED, attackerEntity != null ? attackerEntity : target),
                    finalAmount
            );
        }, serverLevel);
    }
}

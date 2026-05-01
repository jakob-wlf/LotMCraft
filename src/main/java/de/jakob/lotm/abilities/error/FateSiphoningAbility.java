package de.jakob.lotm.abilities.error;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.events.ProhibitionHandler;
import de.jakob.lotm.rendering.effectRendering.DirectionalEffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class FateSiphoningAbility extends Ability {

    private static final HashMap<UUID, UUID> linkedEntities = new HashMap<>();

    public FateSiphoningAbility(String id) {
        super(id, 20);
        canBeCopied = false;
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("error", 2));
    }

    @Override
    public float getSpiritualityCost() {
        return 4000;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (ProhibitionHandler.IsInTheftZone(entity.position(), (ServerLevel) level, AbilityUtil.getSeqWithArt(entity, this))) return;
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 30*(int) Math.max(multiplier(entity)/4,1), 2);
        if(target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.fate_siphoning.no_target").withColor(0x6d32a8));
            return;
        }

        if(linkedEntities.containsKey(target.getUUID()) &&
                linkedEntities.get(target.getUUID()).equals(entity.getUUID())){
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.fate_siphoning.resisted").withColor(0x6d32a8));
            return;
        }

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        if(BeyonderData.getPathway(target).equals("error") && BeyonderData.getSequence(target) < entitySeq){
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.fate_siphoning.resisted").withColor(0x6d32a8));
            return;
        }

        // High-sequence opponents may outright resist the fate link being established
        double failureChance = AbilityUtil.getSequenceFailureChance(entitySeq, BeyonderData.getSequence(target));
        if (ThreadLocalRandom.current().nextDouble() < failureChance) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.fate_siphoning.resisted").withColor(0x6d32a8));
            return;
        }

        DirectionalEffectManager.playEffect(DirectionalEffectManager.DirectionalEffect.FATE_SIPHONING, entity.getEyePosition().x, entity.getEyePosition().y, entity.getEyePosition().z,
                target.getX(), target.getY() + target.getEyeHeight() * 0.5, target.getZ(),
                40,
                serverLevel,
                entity);

        linkedEntities.put(entity.getUUID(), target.getUUID());
        ServerScheduler.scheduleDelayed(20 * 7*(int) Math.max(multiplier(entity)/4,1), () -> linkedEntities.remove(entity.getUUID()));
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingIncomingDamageEvent event) {
        if(!(event.getEntity().level() instanceof ServerLevel serverLevel)) {
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

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if(!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if(!(entity instanceof ServerPlayer player))
            return;

        if(!linkedEntities.containsKey(player.getUUID())) {
            return;
        }

        Entity target = serverLevel.getEntity(linkedEntities.get(player.getUUID()));
        if(!(target instanceof LivingEntity targetLiving))
            return;

        var luck = entity.getData(ModAttachments.LUCK_COMPONENT.get());
        var luckTarget = target.getData(ModAttachments.LUCK_COMPONENT.get());

        if(luck.getLuck() < 0) {
            luckTarget.setLuck(luckTarget.getLuck() + luck.getLuck());
            luck.setLuck(0);
        }
    }

    @SubscribeEvent
    public static void onEffectApply(MobEffectEvent.Applicable event) {
        Entity entity = event.getEntity();
        if(!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if(!(entity instanceof ServerPlayer player))
            return;

        if(!linkedEntities.containsKey(player.getUUID())) {
            return;
        }

        MobEffectInstance effect = event.getEffectInstance();

        if (effect.getEffect().value().isBeneficial()) return;

        Entity targetEntity = serverLevel.getEntity(linkedEntities.get(player.getUUID()));

        if (!(targetEntity instanceof LivingEntity target)) return;
        event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        target.addEffect(new MobEffectInstance(effect));
    }
}

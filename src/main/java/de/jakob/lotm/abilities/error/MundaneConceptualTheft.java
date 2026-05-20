package de.jakob.lotm.abilities.error;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.error.handler.TheftHandler;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.events.ProhibitionHandler;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.TeleportationUtil;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class MundaneConceptualTheft extends SelectableAbility {
    public static final HashMap<UUID, Integer> stolenDistanceMap = new HashMap<>(80);

    public MundaneConceptualTheft(String id) {
        super(id, 1);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("error", 5));
    }

    @Override
    protected float getSpiritualityCost() {
        return 500;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.mundane_conceptual_theft.steal_walk",
                "ability.lotmcraft.mundane_conceptual_theft.steal_sight",
                "ability.lotmcraft.mundane_conceptual_theft.steal_health",
                "ability.lotmcraft.mundane_conceptual_theft.steal_distance"
                //"ability.lotmcraft.mundane_conceptual_theft.steal_thoughts"
        };
    }

    private int getTheftDuration(int userSeq, int targetSeq) {
        int baseDurationSeconds = 30;

        if(targetSeq == -1) {
            return baseDurationSeconds * 20;
        }

        if (targetSeq < userSeq) {
            int difference = userSeq - targetSeq;
            int durationSeconds = Math.max(5, baseDurationSeconds - (difference * 5));
            return durationSeconds * 20;
        }

        int difference = targetSeq - userSeq;
        int durationSeconds = Math.min(120, baseDurationSeconds + (difference * 10));
        return durationSeconds * 20;
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(!(level instanceof ServerLevel serverLevel)) {
            if(entity instanceof Player player) {
                player.playSound(SoundEvents.BELL_RESONATE, 1, 1);
            }
            return;
        }
        if (ProhibitionHandler.IsInTheftZone(entity.position(), (ServerLevel) level, AbilityUtil.getSeqWithArt(entity, this))) return;
        if(abilityIndex == 3) {
            stealDistance(serverLevel, entity);
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(entity, (int) (15 * (multiplier(entity) * multiplier(entity))), 1.5f);
        if(target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.mundane_conceptual_theft.no_target").withColor(0x4742c9));
            return;
        }

        EffectManager.playEffect(EffectManager.Effect.CONCEPTUAL_THEFT, target.getX(), target.getEyeY(), target.getZ(), serverLevel, entity);

        if(BeyonderData.isBeyonder(target) && TheftHandler.doesTheftFail(entity, target, random, this)) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.mundane_conceptual_theft.theft_failed").withColor(0x4742c9));
            return;
        }

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        int targetSeq = BeyonderData.getSequence(target);

        switch (abilityIndex) {
            case 0 -> stealWalk(target, getTheftDuration(entitySeq, targetSeq));
            case 1 -> stealSight(target, getTheftDuration(entitySeq, targetSeq));
            case 2 -> stealHealth(entity, target);

        }
    }

    private void stealDistance(ServerLevel level, LivingEntity entity) {
        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        if(entitySeq > 6) return;

        Vec3 targetLoc = AbilityUtil.getTargetBlock(entity, TheftHandler.getDistancePerSeq(entitySeq), true).getCenter().add(0, 1, 0);
        level.playSound(null, targetLoc.x, targetLoc.y, targetLoc.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, .5f, 1);

        var validatedPos = TeleportationUtil.clampToBorder(level, targetLoc);

        if(entity instanceof ServerPlayer player) {
            int distance = (int) player.position().distanceTo(validatedPos);
            int storedDistance = 0;
            if(stolenDistanceMap.containsKey(player.getUUID()))
                storedDistance = stolenDistanceMap.get(player.getUUID());

            stolenDistanceMap.put(player.getUUID(), storedDistance + distance);
        }

        entity.teleportTo(validatedPos.x, validatedPos.y, validatedPos.z);
    }

    private void stealHealth(LivingEntity entity, LivingEntity target) {
        float healthToSteal = (float) (DamageLookup.lookupDamage(5, 1f) *(int) Math.max(multiplier(entity)/2,1));
        target.hurt(ModDamageTypes.source(target.level(), ModDamageTypes.BEYONDER_GENERIC, entity), healthToSteal);
        entity.setHealth(Math.min(entity.getMaxHealth(), entity.getHealth() + healthToSteal));
    }

    private void stealSight(LivingEntity target, int theftDuration) {
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, theftDuration, 4, false, false, false));

        if(target instanceof Mob mob) {
            mob.setTarget(null);
        }
    }

    private void stealWalk(LivingEntity target, int theftDuration) {
        AttributeInstance movementSpeed = target.getAttribute(Attributes.MOVEMENT_SPEED);
        if(movementSpeed == null) {
            return;
        }
        if(movementSpeed.getModifier(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "mundane_conceptual_theft_walk")) != null) {
            return;
        }
        movementSpeed.addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "mundane_conceptual_theft_walk"), -100, AttributeModifier.Operation.ADD_VALUE));

        ServerScheduler.scheduleForDuration(0, 2, theftDuration, () -> {
            target.setDeltaMovement(new Vec3(0, 0, 0));
            target.hurtMarked = true;
        });

        ServerScheduler.scheduleDelayed(theftDuration, () -> {
            AttributeInstance movementSpeedInner = target.getAttribute(Attributes.MOVEMENT_SPEED);

            if(movementSpeedInner != null) {
                movementSpeedInner.removeModifier(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "mundane_conceptual_theft_walk"));
            }
        });
    }
}

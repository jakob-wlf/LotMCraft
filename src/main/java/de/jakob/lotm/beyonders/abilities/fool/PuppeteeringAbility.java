package de.jakob.lotm.beyonders.abilities.fool;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.*;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.gui.custom.mass_puppeteering.MassPuppeteeringMenuProvider;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.AbilitySelectionPacket;
import de.jakob.lotm.rendering.effectRendering.EffectIds;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.rendering.effectRendering.EffectParams;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.beyonders.abilities.fool.marionettes.MarionetteUtils;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class PuppeteeringAbility extends SelectableAbility {

    private final HashMap<UUID, LivingEntity> entitiesBeingManipulated = new HashMap<>();
    private final Map<UUID, Set<UUID>> massEntitiesBeingManipulated = new HashMap<>();

    public PuppeteeringAbility(String id) {
        super(id, 1);

        onHoldTickInverval = 1;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("fool", 5));
    }

    @Override
    public float getSpiritualityCost() {
        return 40;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.puppeteering_ability.puppeteere",
                "ability.lotmcraft.puppeteering_ability.mass_puppeteering"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if(level.isClientSide)
            return;

        switch(selectedAbility){
            case 0 -> puppeteering(level, entity);
            case 1 -> massPuppeteering(level, entity);
        }
    }

    @Override
    public void nextAbility(LivingEntity entity){
        if(getAbilityNames().length == 0)
            return;

        if(!selectedAbilities.containsKey(entity.getUUID())) {
            selectedAbilities.put(entity.getUUID(), 0);
        }

        int selectedAbility = selectedAbilities.get(entity.getUUID());
        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        selectedAbility++;
        if(selectedAbility >= getAbilityNames().length) {
            selectedAbility = 0;
        }

        if((entitySeq > 4 && selectedAbility >= 1)){
            selectedAbility = 0;
        }

        selectedAbilities.put(entity.getUUID(), selectedAbility);
        PacketHandler.sendToServer(new AbilitySelectionPacket(getId(), selectedAbility));
    }

    @Override
    public void previousAbility(LivingEntity entity){
        if(getAbilityNames().length == 0)
            return;

        if(!selectedAbilities.containsKey(entity.getUUID())) {
            selectedAbilities.put(entity.getUUID(), 0);
        }

        int selectedAbility = selectedAbilities.get(entity.getUUID());
        selectedAbility--;
        if(selectedAbility <= -1) {
            selectedAbility = getAbilityNames().length - 1;
        }

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        if((entitySeq > 4 && selectedAbility >= 1)){
            selectedAbility = 0;
        }

        selectedAbilities.put(entity.getUUID(), selectedAbility);
        PacketHandler.sendToServer(new AbilitySelectionPacket(getId(), selectedAbility));
    }

    public void massPuppeteering(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!(entity instanceof ServerPlayer player)) return;
        if(!BeyonderData.isBeyonder(entity)) return;

        Map<LivingEntity, Integer> validTargets = new HashMap<>();

        int sequence = AbilityUtil.getSeqWithArt(entity, this);

        AllyComponent allyComponent = player.getData(ModAttachments.ALLY_COMPONENT);

        for (Entity e : serverLevel.getAllEntities()) {
            if (e instanceof LivingEntity target) {
                if(target != player && target.isAlive() && !(target instanceof Phantom) && !allyComponent.isAlly(target.getUUID())) {
                    MarionetteComponent component = target.getData(ModAttachments.MARIONETTE_COMPONENT.get());
                    if (!component.isMarionette()) {
                        int targetSequence = BeyonderData.getSequence(target);
                        int time = getManipulationTimeBySequenceAndSequenceDifference(sequence, targetSequence) * 4;
                        if (time >= 0) {
                            if (player.distanceTo(target) < getManipulationDistance(sequence) * 0.25) {
                                SanityComponent sanityComponent = target.getData(ModAttachments.SANITY_COMPONENT);
                                if (sanityComponent.getSanity() < 0.8f) {
                                    time = (int) (time * (0.15f + sanityComponent.getSanity()));
                                }

                                validTargets.put(target, time);
                            }
                        }
                    }
                }
            }
        }

        player.openMenu(
                new MassPuppeteeringMenuProvider(validTargets),
                buf -> {
                    buf.writeVarInt(validTargets.size());
                    for (Map.Entry<LivingEntity, Integer> entry : validTargets.entrySet()) {
                        buf.writeVarInt(entry.getKey().getId());
                        buf.writeVarInt(entry.getValue());
                    }
                }
        );
    }

    public void executeMassPuppeteering(Level level, ServerPlayer serverPlayer, Map<LivingEntity, Integer> livingEntitiesMap) {
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel) || serverPlayer == null || livingEntitiesMap == null || livingEntitiesMap.isEmpty()) {
            return;
        }

        int sequence = AbilityUtil.getSeqWithArt(serverPlayer, this);
        Set<UUID> playerMassTargets = massEntitiesBeingManipulated.computeIfAbsent(serverPlayer.getUUID(), k -> new HashSet<>());

        for (Map.Entry<LivingEntity, Integer> entry : livingEntitiesMap.entrySet()) {
            LivingEntity target = entry.getKey();
            int time = entry.getValue();

            if (target == null || !target.isAlive() || target.isRemoved() || target == serverPlayer || target instanceof Phantom) {
                continue;
            }

            playerMassTargets.add(target.getUUID());

            int targetSequence = BeyonderData.getSequence(target);
            if (LOTMCraft.abilityHandler.getById("divination_ability").hasAbility(target) || targetSequence <= 3) {
                if (target instanceof Mob mob) {
                    mob.setTarget(serverPlayer);
                }
            }

            AtomicBoolean stopped = new AtomicBoolean(false);
            AtomicDouble health = new AtomicDouble(target.getHealth());
            AtomicDouble casterHealth = new AtomicDouble(serverPlayer.getHealth());
            AtomicDouble elapsedTicks = new AtomicDouble(0.0);
            int finalTime = time;

            ServerScheduler.scheduleForDuration(0, 1, time, () -> {
                if (stopped.get()) {
                    return;
                }

                Set<UUID> currentTargets = massEntitiesBeingManipulated.get(serverPlayer.getUUID());
                if (currentTargets == null || !currentTargets.contains(target.getUUID())) {
                    stopped.set(true);
                    return;
                }

                double currentTick = elapsedTicks.addAndGet(1.0);
                float progress = (float) currentTick / finalTime;

                if (!target.isAlive() || target.isRemoved() || target.level() != level || !serverPlayer.isAlive() || serverPlayer.isRemoved()) {
                    removeMassTarget(serverPlayer.getUUID(), target.getUUID());
                    stopped.set(true);
                    return;
                }

                if (target.distanceTo(serverPlayer) >= getManipulationDistance(sequence) * 1.75f) {
                    removeMassTarget(serverPlayer.getUUID(), target.getUUID());
                    stopped.set(true);
                    return;
                }

                if (target.getHealth() < health.get() * 0.33) {
                    removeMassTarget(serverPlayer.getUUID(), target.getUUID());
                    stopped.set(true);
                    return;
                }

                if (serverPlayer.getHealth() < casterHealth.get() * 0.5) {
                    removeMassTarget(serverPlayer.getUUID(), target.getUUID());
                    stopped.set(true);
                    return;
                }

                Vec3 start = VectorUtil.getRelativePosition(serverPlayer.getEyePosition(), new Vec3(serverPlayer.getLookAngle().x, 0, serverPlayer.getLookAngle().z), .1, .35, -.5);
                Vec3 end = target.getEyePosition();
                EffectManager.playEffect(EffectIds.MARIONETTE_THREADS, start.x(), start.y(), start.z(), serverPlayer, EffectParams.directionWithParams(2, start.x(), start.y(), start.z(), end.x(), end.y(), end.z(), 0.5f, 0.1f, 0.7f));

                // Progressive debuffs
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 4, false, false, false));
                if (progress >= 0.20f) {
                    target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 5, false, false, false));
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 5, false, false, false));
                }
                if (progress >= 0.60f) {
                    target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 10, false, false, false));
                    if (currentTick % 100 == 0) {
                        DisabledAbilitiesComponent disabledComponent = target.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
                        disabledComponent.disableAbilityUsageForTime("puppeteering_ability_" + serverPlayer.getUUID(), 2 * 20, target);
                    }
                }

                health.set(target.getHealth());
            }, () -> {
                if (stopped.get()) {
                    return;
                }
                removeMassTarget(serverPlayer.getUUID(), target.getUUID());

                EffectManager.playEffect(EffectIds.RING_PULSE, target.getX(), target.getY() + 1, target.getZ(), serverLevel, serverPlayer, EffectParams.ofParams(0.5f, 0.1f, 0.7f));

                MarionetteComponent component = serverPlayer.getData(ModAttachments.MARIONETTE_COMPONENT.get());
                MarionetteOwnerComponent data = serverPlayer.getData(ModAttachments.MARIONETTE_OWNER_COMPONENT);

                if (!component.isMarionette() && data.getMarionettes().size() < getMaxPuppetCount(sequence)) {
                    turnIntoMarionette(target, serverPlayer);
                } else {
                    target.hurt(target.damageSources().generic(), Float.MAX_VALUE);
                }
            }, serverLevel);
        }
    }

    public void puppeteering(Level level, LivingEntity entity) {
        if(entitiesBeingManipulated.containsKey(entity.getUUID())) {
            entitiesBeingManipulated.remove(entity.getUUID());
            return;
        }

        int sequence = AbilityUtil.getSeqWithArt(entity, this);

        if(!BeyonderData.isBeyonder(entity))
            return;


        LivingEntity target = AbilityUtil.getTargetEntity(entity, getManipulationDistance(sequence), 3);
        if(target == null || target == entity || target instanceof Phantom) {
            if(entity instanceof ServerPlayer player) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.translatable("ability.lotmcraft.puppeteering.no_entity_found").withColor(0xFFff124d));
                player.connection.send(packet);
            }
            return;
        }
        int targetSequence = BeyonderData.getSequence(target);
        int time = getManipulationTimeBySequenceAndSequenceDifference(sequence, targetSequence);

        if (time < 0) {
            entity.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 8, 5, false, false, false));
            return;
        }

        SanityComponent sanityComponent = target.getData(ModAttachments.SANITY_COMPONENT);
        if (sanityComponent.getSanity() < 0.8f) {
            time = (int) (time * (0.15f + sanityComponent.getSanity()));
        }

        entitiesBeingManipulated.put(entity.getUUID(), target);

        AtomicBoolean stopped = new AtomicBoolean(false);

        if (LOTMCraft.abilityHandler.getById("divination_ability").hasAbility(target) || targetSequence <= 3) {
            if(target instanceof Mob mob) {
                mob.setTarget(entity);
            }
        }

        AtomicDouble health = new AtomicDouble(target.getHealth());
        AtomicDouble casterHealth = new AtomicDouble(entity.getHealth());
        AtomicDouble elapsedTicks = new AtomicDouble(0.0);

        int finalTime = time;
        ServerScheduler.scheduleForDuration(0, 1, time, () -> {
            if(stopped.get()) {
                return;
            }

            if(!entitiesBeingManipulated.containsKey(entity.getUUID())) {
                entitiesBeingManipulated.remove(entity.getUUID());
                stopped.set(true);
                return;
            }

            double currentTick = elapsedTicks.addAndGet(1.0);
            float progress = (float) currentTick / finalTime;

            if(!target.isAlive() || target.isRemoved() || target.level() != level) {
                entitiesBeingManipulated.remove(entity.getUUID());
                stopped.set(true);
                return;
            }

            if(target.distanceTo(entity) >= getManipulationDistance(sequence)) {
                entitiesBeingManipulated.remove(entity.getUUID());
                stopped.set(true);
                return;
            }

            if(target.getHealth() < health.get() * 0.75) {
                entitiesBeingManipulated.remove(entity.getUUID());
                stopped.set(true);
                return;
            }

            if(entity.getHealth() < casterHealth.get() * 0.5) {
                entitiesBeingManipulated.remove(entity.getUUID());
                stopped.set(true);
                return;
            }

            Vec3 start = VectorUtil.getRelativePosition(entity.getEyePosition(), new Vec3(entity.getLookAngle().x, 0, entity.getLookAngle().z), .1, .35, -.5);
            Vec3 end = target.getEyePosition();
            if (target instanceof ServerPlayer targetPlayer && (LOTMCraft.abilityHandler.getById("divination_ability").hasAbility(target) || targetSequence <= 3)) {
                targetPlayer.sendSystemMessage(Component.translatable("ability.lotmcraft.puppeteering.entity_warning").withColor(0xa26fc9));
            }
            if(entity instanceof ServerPlayer serverPlayer)
                EffectManager.playEffect(EffectIds.MARIONETTE_THREADS, start.x(), start.y(), start.z(), serverPlayer, EffectParams.directionWithParams(2, start.x(), start.y(), start.z(), end.x(), end.y(), end.z(), 0.5f, 0.1f, 0.7f));

            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 4, false, false, false));
            if (progress >= 0.20f) {
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 5, false, false, false));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 5, false, false, false));
            }
            if (progress >= 0.60f) {
                target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 10, false, false, false));
                // every 5 seconds - lose abilities for 2 seconds
                if (currentTick % 100 == 0) {
                    DisabledAbilitiesComponent disabledComponent = target.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
                    disabledComponent.disableAbilityUsageForTime("puppeteering_ability_" + entity.getUUID(), 2 * 20, target);
                }
            }

            health.set(target.getHealth());
        }, () -> {
            if(stopped.get()) {
                return;
            }
            entitiesBeingManipulated.remove(entity.getUUID());

            MarionetteComponent component = entity.getData(ModAttachments.MARIONETTE_COMPONENT.get());
            if(entity instanceof ServerPlayer player)
                EffectManager.playEffect(EffectIds.RING_PULSE, target.getX(), target.getY() + 1, target.getZ(), (ServerLevel) level, player, EffectParams.ofParams(0.5f, 0.1f, 0.7f));

            MarionetteOwnerComponent data = entity.getData(ModAttachments.MARIONETTE_OWNER_COMPONENT);
            if(entity instanceof Player player && !component.isMarionette() && data.getMarionettes().size() < getMaxPuppetCount(sequence)) {
                turnIntoMarionette(target, player);
            }
            else {
                target.hurt(target.damageSources().generic(), Float.MAX_VALUE);
            }
        }, (ServerLevel) level);
    }

    private void turnIntoMarionette(LivingEntity target, Player player) {
        if(target instanceof Player) {
            Vec3 pos = target.position();
            if(BeyonderData.isBeyonder(target)) {
                int sequence = BeyonderData.getSequence(target);
                String pathway = BeyonderData.getPathway(target);
                target.hurt(target.damageSources().generic(), Float.MAX_VALUE);
                target = new BeyonderNPCEntity(ModEntities.BEYONDER_NPC.get(), target.level(), false, pathway, sequence);
            }
            else {
                target.hurt(target.damageSources().generic(), Float.MAX_VALUE);
                target = new BeyonderNPCEntity(ModEntities.BEYONDER_NPC.get(), target.level(), false, "none", 10);
            }

            target.setPos(pos);
            target.level().addFreshEntity(target);
        }
        target.setHealth(target.getMaxHealth());
        if(target instanceof Mob mob) {
            mob.setTarget(null);
            mob.getNavigation().stop();
        }
        if (MarionetteUtils.turnEntityIntoMarionette(target, player)) {
            player.sendSystemMessage(Component.translatable("ability.lotmcraft.puppeteering.entity_turned").withColor(0xa26fc9));
        } else {
                player.sendSystemMessage(Component.translatable("ability.lotmcraft.puppeteering.entity_turned_failed").withColor(0xa26fc9));
        }
    }

    @Override
    public void onHold(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel) || !(entity instanceof ServerPlayer serverPlayer)) return;
        if(entitiesBeingManipulated.containsKey(entity.getUUID())) return;

        for(LivingEntity target : AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), 35)) {
            Vec3 start = VectorUtil.getRelativePosition(entity.getEyePosition(), new Vec3(entity.getLookAngle().x, 0, entity.getLookAngle().z), .1, .35, -.5);
            Vec3 end = target.getEyePosition();
            EffectManager.playDirectionalEffect(EffectIds.MARIONETTE_THREADS, start.x(), start.y(), start.z(), end.x(), end.y(), end.z(), 2, serverPlayer);
        }
    }


    private void removeMassTarget(UUID playerUUID, UUID targetUUID) {
        Set<UUID> targets = massEntitiesBeingManipulated.get(playerUUID);
        if (targets != null) {
            targets.remove(targetUUID);
            if (targets.isEmpty()) {
                massEntitiesBeingManipulated.remove(playerUUID);
            }
        }
    }

    private int getManipulationDistance(int sequence) {
        return switch (sequence) {
            default -> 7;
            case 4 -> 150;
            case 3 -> 400;
            case 2 -> 1000;
            case 1 -> 4000;
            case 0 -> 10000;
        };
    }

    private int getMaxPuppetCount(int sequence) {
        return switch (sequence) {
            default -> 3;
            case 4 -> 20;
            case 3 -> 50;
            case 2 -> 100;
            case 1 -> 250;
            case 0 -> 500;
        };
    }

    private int getManipulationTimeBySequenceAndSequenceDifference(int sequence, int targetSequence) {
        if (sequence == 5) {
            if (targetSequence < 5) return -1;
            int targetClamped = Math.min(targetSequence, 10);
            return 2400 - (20 * 20) * (targetClamped - 5); // so against seq10 9 8 7 6 5 its -> 20s 40s 60s 80s 100s 120s
        }

        if (sequence == 4) {
            if (targetSequence < 3) return -1;
            if (targetSequence == 3) return 20 * 90;
            return 20 * (30 >> (Math.min(targetSequence, 5) - 4));
        }

        if (sequence == 3) {
            if (targetSequence < 3) return -1;
            return 20 * (40 >> (Math.min(targetSequence, 5) - 3));
        }

        if (sequence == 2) {
            if (targetSequence == 0) return -1;
            if (targetSequence == 1) return 20 * 120;
            return 20 * (40 >> (Math.min(targetSequence, 5) - 2));
        }

        if (sequence == 1) {
            if (targetSequence == 0) return -1;
            return 20 * (48 >> (Math.min(targetSequence, 5) - 1));
        }

        if (sequence == 0) {
            if (targetSequence == 0) return 20 * 120;
            return 20 * (16 >> (Math.min(targetSequence, 5) - 1));
        }

        return -1;
    }

}

package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.visionary.prophecy.Prophecy;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.RingEffectManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.checkerframework.checker.units.qual.C;

import java.util.*;

public class PlacateAbility extends SelectableAbility {
    public PlacateAbility(String id) {
        super(id, 5, "morale_boost");
        interactionRadius = 18;
        interactionCacheTicks = 20 * 5;

        canBeCopied = false;
        canBeReplicated = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 7));
    }

    @Override
    public float getSpiritualityCost() {
        return 50;
    }

    @Override
    public String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.placate.self",
                "ability.lotmcraft.placate.others",
                "ability.lotmcraft.placate.check_cue",
                "ability.lotmcraft.placate.remove_cue"
        };
    }

    @Override
    public void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(!(entity instanceof Player))
            abilityIndex = 0;
        switch (abilityIndex) {
            case 0 -> placateYourself(level, entity);
            case 1 -> placateOthers(level, entity);
            case 2 -> checkCue(level, entity);
            case 3 -> removeCue(level, entity);
        }
    }

    private void removeCue(Level level, LivingEntity entity){
        if(level.isClientSide)
            return;
        if (!(entity instanceof ServerPlayer player)) return;

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        var targetPlayer = AbilityUtil.getTargetEntity(entity, 40* (int) Math.max(multiplier(entity)/4,1),
                1f, true, true) == null ?
                entity :  AbilityUtil.getTargetEntity(
                        entity, 40, 1f, true, true);
        if(!(targetPlayer instanceof ServerPlayer)) targetPlayer = entity;

        List<Prophecy> all = new LinkedList<>(
                BeyonderData.playerMap.get(targetPlayer).get().prophecies()
        );

        List<Prophecy> matching = all.stream()
                .filter(obj -> {
                    int seq = BeyonderData.playerMap.get(obj.casterId()).get().sequence();
                    return entitySeq <= obj.trigger().getRequiredSeq()
                            && entitySeq <= obj.trigger().getActionRequiredSeq()
                            && entitySeq <= seq;
                })
                .toList();

        if (matching.isEmpty()) {
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("ability.lotmcraft.placate.check_cue.not_detected"));
            return;
        }

        int amount = getCuesRemovedPerSeq(entitySeq);

        for (int i = 0; i < amount; i++) {
            if(all.isEmpty()) break;;

            Prophecy toRemove = matching.get(i);
            all.remove(toRemove);
        }

        BeyonderData.playerMap.setProphecies(targetPlayer.getUUID(), all);

    }

    private static int getCuesRemovedPerSeq(int seq){
        return switch (seq){
            case 7 -> 1;
            case 6 -> 2;
            case 5 -> 3;
            case 4,3 -> 10;
            case 2 -> 20;
            case 1 -> 30;
            case 0 -> 60;
            default -> 0;
        };
    }

    private static float getSanityPerSeq(int seq){
        return switch (seq){
            case 7 -> 0.15f;
            case 6, 5 -> 0.17f;
            case 4 -> 0.2f;
            case 3 -> 0.22f;
            case 2 -> 0.25f;
            case 1 -> 0.27f;
            case 0 -> 0.35f;
            default -> 0.0f;
        };
    }

    private void checkCue(Level level, LivingEntity entity){
        if(level.isClientSide)
            return;
        if (!(entity instanceof ServerPlayer player)) return;

        var target = AbilityUtil.getTargetEntity(entity, 40, 1f, true, true);

        if(target != null)
            RingEffectManager.createRingForPlayer(target.getEyePosition().subtract(0, .4, 0), 2, 60, 255 / 255f, 211 / 255f, 92 / 255f, 1, .5f, .75f, (ServerLevel) entity.level(), player);

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        List<Prophecy> list;
        if(target instanceof ServerPlayer targetPlayer){
            list = BeyonderData.playerMap.get(targetPlayer).get().prophecies().stream().filter(obj -> {
                int seq = BeyonderData.playerMap.get(obj.casterId()).get().sequence();
                if(entitySeq <= obj.trigger().getRequiredSeq() && entitySeq <= obj.trigger().getActionRequiredSeq()){
                    return entitySeq <= seq;
                }

                return false;
            }).toList();
        }
        else{
            list = BeyonderData.playerMap.get(entity).get().prophecies().stream().filter(obj -> {
                int seq = BeyonderData.playerMap.get(obj.casterId()).get().sequence();
                if(entitySeq <= obj.trigger().getRequiredSeq() && entitySeq <= obj.trigger().getActionRequiredSeq()){
                    if(entitySeq <= seq)
                        return true;
                }

                return false;
            }).toList();
        }

        if(list.isEmpty()){
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.placate.check_cue.not_detected"));
            return;
        }

        for (var obj : list){
            player.sendSystemMessage(Component.literal(
                    "Trigger: " + obj.trigger().getType()
                    + ", action: " + obj.trigger().getActionType()
                    + " set by " + BeyonderData.playerMap.get(obj.casterId()).get().trueName()
            ));
        }
    }

    private void placateOthers(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        level.playSound(null, entity.position().x, entity.position().y, entity.position().z, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1, 1);

        for(LivingEntity e : AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, entity.position(), 18 , false, true)) {
            placateEntity(entity, e);
        }
    }

    private void placateYourself(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        level.playSound(null, entity.position().x, entity.position().y, entity.position().z, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1, 1);

        level.playSound(null, entity.position().x, entity.position().y, entity.position().z, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1, 1);

        placateEntity(entity, entity);
    }

    private void placateEntity(LivingEntity caster, LivingEntity entity) {
        int entitySeq = AbilityUtil.getSeqWithArt(caster, this);
        entity.getData(ModAttachments.SANITY_COMPONENT).increaseSanityWithSequenceDifference(getSanityPerSeq(entitySeq), entity, AbilityUtil.getSeqWithArt(caster, this), BeyonderData.getSequence(entity));
        entity.removeEffect(ModEffects.LOOSING_CONTROL);
        entity.removeEffect(ModEffects.MENTAL_PLAGUE);

        if(caster instanceof ServerPlayer player)
            RingEffectManager.createRingForPlayer(entity.getEyePosition().subtract(0, .4, 0), 2, 60, 255 / 255f, 211 / 255f, 92 / 255f, 1, .5f, .75f, (ServerLevel) caster.level(), player);
    }

    @Override
    public boolean shouldUseAbility(LivingEntity entity) {
        return entity.hasEffect(ModEffects.LOOSING_CONTROL);
    }
}

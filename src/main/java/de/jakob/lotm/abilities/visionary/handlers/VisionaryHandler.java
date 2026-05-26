package de.jakob.lotm.abilities.visionary.handlers;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.visionary.PsychologicalInvisibilityAbility;
import de.jakob.lotm.abilities.visionary.passives.MetaAwarenessAbility;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

public class VisionaryHandler {

    private static boolean requiresAsleep(LivingEntity entity) {
        return BeyonderData.getSequence(entity) > 3;
    }

    public static boolean checkAsleep(LivingEntity entity, LivingEntity target){
        return requiresAsleep(entity) && !(target.hasEffect(ModEffects.ASLEEP) || target.isSleeping());
    }

    public static boolean shouldFailAndTrigger(int seq, LivingEntity caster, LivingEntity target, @Nullable Ability skill){
        return shouldFailAndTrigger(seq, caster, target, skill, true);
    }

    public static boolean shouldFailAndTrigger(int seq, LivingEntity caster, LivingEntity target, @Nullable Ability skill, boolean shouldSendFailMessage){
        int seqTarget = BeyonderData.getSequence(target);
        String pathTarget = BeyonderData.getPathway(target);

        if(caster.equals(target)) return false;

        if(target instanceof ServerPlayer targetPlayer) {
            var data = BeyonderData.playerMap.get(targetPlayer.getUUID()).get();
            seqTarget = data.sequence();
            pathTarget = data.pathway();
        }

        if(!pathTarget.equals("visionary")) return false;

        if(target instanceof ServerPlayer targetPlayer && caster instanceof ServerPlayer entityPlayer){
            if(skill != null)
                MetaAwarenessAbility.sendWithMessage(entityPlayer, targetPlayer, "Tried to use: " + skill.getId());
            else
                MetaAwarenessAbility.onDivined(entityPlayer, targetPlayer);
        }

        if(seqTarget < seq){
            if(shouldSendFailMessage && !isInvisible(target))
                AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.frenzy.failed").withColor(0xFFff124d));

            return true;
        }

        return false;
    }

    public static boolean shouldFail(int seq, LivingEntity target){
        int seqTarget = BeyonderData.getSequence(target);
        String pathTarget = BeyonderData.getPathway(target);

        if(target instanceof ServerPlayer targetPlayer) {
            var data = BeyonderData.playerMap.get(targetPlayer.getUUID()).get();
            seqTarget = data.sequence();
            pathTarget = data.pathway();
        }

        if(!pathTarget.equals("visionary")) return false;

        if(seqTarget < seq){
            return true;
        }

        return false;
    }


    public static boolean isInvisible(LivingEntity entity){
        return PsychologicalInvisibilityAbility.finalInvisiblePlayers.containsKey(entity.getUUID());
    }

    public static boolean shouldStayInvisible(int seq, LivingEntity entity){
        if(isInvisible(entity)){
            return seq >= PsychologicalInvisibilityAbility.finalInvisiblePlayers.get(entity.getUUID());
        }

        return false;
    }
}

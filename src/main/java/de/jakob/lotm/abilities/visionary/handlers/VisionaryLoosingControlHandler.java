package de.jakob.lotm.abilities.visionary.handlers;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.visionary.passives.MetaAwarenessAbility;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.Random;

public class VisionaryLoosingControlHandler {

    public static int getAmplifier(LivingEntity entity, LivingEntity target, Ability skill) {
        int targetSequence = BeyonderData.getSequence(target);
        int sequence = AbilityUtil.getSeqWithArt(entity, skill);
        int diff = targetSequence - sequence;

        return diff + getBasePerSeq(sequence);
    }

    public static void applyEffect(LivingEntity entity, LivingEntity target, Ability skill){
        int amplifier = getAmplifier(entity, target, skill);

        int entitySeq = AbilityUtil.getSeqWithArt(entity, skill);
        int targetSeq = BeyonderData.getSequence(target);

        amplifier = amplifier <= 0 ? 1 : amplifier;

        if(targetSeq >= entitySeq) {
            if (!target.hasEffect(ModEffects.LOOSING_CONTROL) || target.getEffect(ModEffects.LOOSING_CONTROL).getAmplifier() < amplifier)
                target.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 5, amplifier));
        }
    }

    public static void forceApplyEffect(LivingEntity target, int entitySeq, int bonus){
        int targetSequence = BeyonderData.getSequence(target);
        int diff = targetSequence - entitySeq;
        int amplifier = diff + getBasePerSeq(entitySeq) + bonus;

        amplifier = amplifier <= 0 ? 1 : amplifier;
        target.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 5, amplifier));
    }


    public static int getBasePerSeq(int seq){
        return switch (seq){
            case 9,8,7 -> 1;
            case 6, 5 -> 2;
            case 4 -> 6;
            case 3 -> 10;
            case 2 -> 20;
            case 1 -> 30;
            case 0 -> 50;
            default -> 0;
        };
    }
}

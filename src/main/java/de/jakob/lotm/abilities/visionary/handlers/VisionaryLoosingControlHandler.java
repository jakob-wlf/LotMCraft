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

    private static int getAmplifier(LivingEntity entity, LivingEntity target, Ability skill) {
        Random random = new Random();

        if(BeyonderData.isBeyonder(entity) && BeyonderData.isBeyonder(target)) {
            int targetSequence = BeyonderData.getSequence(target);
            int sequence = AbilityUtil.getSeqWithArt(entity, skill);

            if(targetSequence <= sequence) {
                return 2;
            }
            else {
                return random.nextInt(3, 5);
            }
        }

        return 1;
    }

    public static void applyEffect(LivingEntity entity, LivingEntity target, Ability skill){
        int amplifier = getAmplifier(entity, target, skill);

        int entitySeq = AbilityUtil.getSeqWithArt(entity, skill);
        int targetSeq = BeyonderData.getSequence(target);

        if(targetSeq >= entitySeq) {
            if (!target.hasEffect(ModEffects.LOOSING_CONTROL) || target.getEffect(ModEffects.LOOSING_CONTROL).getAmplifier() < amplifier)
                target.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 8, amplifier));
        }
    }


    private static int getBasePerSeq(int seq){
        return switch (seq){
            case 9,8,7 -> 1;
            case 6, 5 -> 2;
            case 4,3 -> 3;
            case 2 -> 4;
            case 1 -> 5;
            case 0 -> 6;
            default -> 0;
        };
    }
}

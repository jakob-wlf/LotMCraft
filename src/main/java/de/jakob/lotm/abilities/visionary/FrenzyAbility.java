package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.visionary.passives.MetaAwarenessAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class FrenzyAbility extends Ability {
    public FrenzyAbility(String id) {
        super(id, 5f, "corruption");
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "visionary", 7
        ));
    }

    @Override
    public float getSpiritualityCost() {
        return 35;
    }

    private final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(250 / 255f, 201 / 255f, 102 / 255f),
            1.5f
    );

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20, 2);

        if (level.isClientSide) {
            if(target != null)
                ParticleUtil.spawnParticles((ClientLevel) level, dust, target.getEyePosition(), 35, .5);
            return;
        }

        if(target == null) {
            if(entity instanceof ServerPlayer player) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.translatable("ability.lotmcraft.frenzy.no_target").withColor(0xFFff124d));
                player.connection.send(packet);
            }
            return;
        }


        int amplifier = getAmplifier(entity, target);

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        int targetSeq = BeyonderData.getSequence(target);
        if(BeyonderData.getPathway(target).equals("visionary") && targetSeq < entitySeq){
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.dream_traversal.failed").withColor(0xFFff124d));

            if(targetSeq <= 1 && target instanceof ServerPlayer targetPlayer && entity instanceof ServerPlayer entityPlayer){
                MetaAwarenessAbility.onDivined(entityPlayer, targetPlayer);
            }

            return;
        }

        if(BeyonderData.getSequence(target) >= entitySeq) {
            if (!target.hasEffect(ModEffects.LOOSING_CONTROL) || target.getEffect(ModEffects.LOOSING_CONTROL).getAmplifier() < amplifier)
                target.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 8, amplifier));
        }

        target.hurt(entity.damageSources().source(ModDamageTypes.LOOSING_CONTROL), (float) (DamageLookup.lookupDamage(7, .85) * (int) Math.max(multiplier(entity)/4,1)));

        target.getData(ModAttachments.SANITY_COMPONENT).decreaseSanityWithSequenceDifference((0.065f * (int) Math.max(multiplier(entity)/4,1)), target, entitySeq, BeyonderData.getSequence(target));
    }

    private int getAmplifier(LivingEntity entity, LivingEntity target) {
        if(AbilityUtil.isTargetSignificantlyWeaker(entity, target)) {
            return 6;
        }

        if(AbilityUtil.isTargetSignificantlyStronger(entity, target)) {
            return 1;
        }

        if(BeyonderData.isBeyonder(entity) && BeyonderData.isBeyonder(target)) {
            int targetSequence = BeyonderData.getSequence(target);
            int sequence = AbilityUtil.getSeqWithArt(entity, this);

            if(targetSequence <= sequence) {
                return 2;
            }
            else {
                return random.nextInt(3, 5);
            }
        }

        return 1;
    }
}

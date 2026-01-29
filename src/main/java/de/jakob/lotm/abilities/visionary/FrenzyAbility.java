package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
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
        super(id, 1.5f);
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
        if(level.isClientSide)
            return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20, 2);

        if(target == null) {
            if(entity instanceof ServerPlayer player) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.translatable("ability.lotmcraft.frenzy.no_target").withColor(0xFFff124d));
                player.connection.send(packet);
            }
            return;
        }

        int amplifier = 0;
        if(!BeyonderData.isBeyonder(target)) {
            amplifier = Math.max(1, 3 + 7 - BeyonderData.getSequence(entity));
        }
        else {
            int sequence = BeyonderData.getSequence(target);
            int difference = sequence - BeyonderData.getSequence(entity);
            if(difference > 0)
                amplifier = 2 + difference;
        }

        if(!BeyonderData.isBeyonder(target) || BeyonderData.getSequence(target) >= BeyonderData.getSequence(entity)) {
            amplifier = (int) Math.round(amplifier * (multiplier(entity) / 2f));
            if (!target.hasEffect(ModEffects.LOOSING_CONTROL) || target.getEffect(ModEffects.LOOSING_CONTROL).getAmplifier() < amplifier)
                target.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 8, amplifier));
        }

        target.hurt(entity.damageSources().source(ModDamageTypes.LOOSING_CONTROL), (float) (DamageLookup.lookupDamage(7, .85) * multiplier(entity)));

        target.getData(ModAttachments.SANITY_COMPONENT).increaseSanityAndSync((float) (-0.1f * multiplier(entity)), target);

        ParticleUtil.spawnParticles((ServerLevel) level, dust, target.getEyePosition(), 80, 0.5f);
    }
}

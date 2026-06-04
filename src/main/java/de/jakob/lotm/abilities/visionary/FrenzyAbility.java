package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.abilities.visionary.handlers.VisionaryLoosingControlHandler;
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
        LivingEntity target = AbilityUtil.getTargetEntity(entity, (int) (20 * Math.max(multiplier(entity), 1)), 2);

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

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        int targetSeq = BeyonderData.getSequence(target);
        if(VisionaryHandler.shouldFailAndTrigger(entitySeq, entity, target, this)){
            return;
        }

        VisionaryLoosingControlHandler.applyEffect(entity, target, this);

        if(entitySeq <= 4) {
            for(int i = entitySeq; i <= 4; i++)
                BattleHypnosisAbility.performRandomEffect((ServerLevel) level, entity, target, entitySeq);
        }

        target.hurt(entity.damageSources().source(ModDamageTypes.LOOSING_CONTROL), (float) (DamageLookup.lookupDamage(7, .85) * (int) Math.max(multiplier(entity)/4,1)));

        target.getData(ModAttachments.SANITY_COMPONENT).decreaseSanityWithSequenceDifference((0.0065f * (int) Math.max(multiplier(entity)/4,1)), target, entitySeq, BeyonderData.getSequence(target));
    }
}

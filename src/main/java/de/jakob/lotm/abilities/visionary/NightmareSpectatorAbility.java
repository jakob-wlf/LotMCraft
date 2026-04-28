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
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class NightmareSpectatorAbility extends Ability {

    public NightmareSpectatorAbility(String id) {
        super(id, 20f);
        canBeCopied = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 5));
    }

    @Override
    public float getSpiritualityCost() {
        return 110;
    }

    private final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(250 / 255f, 201 / 255f, 102 / 255f),
            1f
    );

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 200, 2);

        if(target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.frenzy.no_target").withColor(0xFFff124d));
            return;
        }

        if(level.isClientSide) {
            ParticleUtil.spawnSphereParticles((ClientLevel) level, dust, target.getEyePosition(), 2, 50);
            return;
        }

        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);
        int targetSeq = BeyonderData.getSequence(target);
        if(BeyonderData.getPathway(target).equals("visionary") && targetSeq < entitySeq){
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.dream_traversal.failed").withColor(0xFFff124d));

            if(targetSeq <= 1 && target instanceof ServerPlayer targetPlayer && entity instanceof ServerPlayer entityPlayer){
                MetaAwarenessAbility.onDivined(entityPlayer, targetPlayer);
            }

            return;
        }

        if(!target.hasEffect(ModEffects.ASLEEP) && entitySeq >= 4) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.dream_traversal.must_be_asleep").withColor(0xFFff124d));
            return;
        }

        // Damage target
        target.hurt(new DamageSource(
                serverLevel.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(ModDamageTypes.LOOSING_CONTROL)
        ), (float) DamageLookup.lookupDamage(5, 1.1) * (int) Math.max(multiplier(entity)/4,1));

        // Add effect
        target.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 4, 1));

        // Decrease Sanity
        target.getData(ModAttachments.SANITY_COMPONENT).decreaseSanityWithSequenceDifference((0.165f* (int) Math.max(multiplier(entity)/4,1)), target, AbilityUtil.getSeqWithArt(entity, this), BeyonderData.getSequence(target));
    }
}

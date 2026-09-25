package de.jakob.lotm.beyonders.abilities.visionary;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.beyonders.abilities.visionary.handlers.VisionaryLoosingControlHandler;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static de.jakob.lotm.beyonders.abilities.visionary.handlers.VisionaryHandler.checkAsleep;

public class NightmareSpectatorAbility extends Ability {

    public NightmareSpectatorAbility(String id) {
        super(id, 10f);

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(3, 3, 5, 7, 8, 10, 10, 10, 10, 10));

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(5000f, 3000f, 1000f, 500f, 350f, 250f, 150f, 100f, 40f, 40f));

        baseDamage = 9f;
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
        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        if(VisionaryHandler.shouldBeAffectedWithMindWorldSeal(entitySeq)){
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("ability.lotmcraft.mind_world_authority_ability.is_sealed")
                            .withColor(0xFFff124d));
            return;
        }

        LivingEntity target = null;

        if(DiscernmentAbility.discerning.contains(entity.getUUID())){
            target = AbilityUtil.getTargetEntity(entity, baseDistance, 2, true,
                    false, false, true);
        }
        else{
            target = AbilityUtil.getTargetEntity(entity, baseDistance, 2, true);
        }

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

        VisionaryHandler.shouldTrigger(entitySeq, entity, target, this);

        if(checkAsleep(entity, target)) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.dream_traversal.must_be_asleep").withColor(0xFFff124d));
            return;
        }

        VisionaryLoosingControlHandler.applyEffect(entity, target, this);

        target.hurt(ModDamageTypes.source(level, ModDamageTypes.IMAGINATION, entity), baseDamage);

        target.getData(ModAttachments.SANITY_COMPONENT).decreaseSanityWithSequenceDifference((0.085f), target, entitySeq, BeyonderData.getSequence(target));
    }
}

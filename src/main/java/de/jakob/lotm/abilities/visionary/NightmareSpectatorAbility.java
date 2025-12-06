package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
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
import java.util.Map;

public class NightmareSpectatorAbility extends AbilityItem {

    public NightmareSpectatorAbility(Properties properties) {
        super(properties, 1.5f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 5));
    }

    @Override
    protected float getSpiritualityCost() {
        return 110;
    }

    private final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(250 / 255f, 201 / 255f, 102 / 255f),
            1f
    );

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 200, 2);

        if(target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.frenzy.no_target").withColor(0xFFff124d));
            return;
        }

        if(!target.hasEffect(ModEffects.ASLEEP)) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.dream_traversal.must_be_asleep").withColor(0xFFff124d));
            return;
        }

        // Damage target
        target.hurt(new DamageSource(
                serverLevel.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(ModDamageTypes.LOOSING_CONTROL)
        ), (float) DamageLookup.lookupDamage(5, 1.1) * (float) multiplier(entity));

        // Add effect
        target.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 4, 1));

        // Decrease Sanity
        target.getData(ModAttachments.SANITY_COMPONENT).increaseSanityAndSync((float) (-0.165f * multiplier(entity)), target);

        // Particles
        ParticleUtil.spawnParticles(serverLevel, dust, target.position().add(0, target.getEyeHeight() / 2, 0), 200, .35, target.getEyeHeight() / 2, .35, 0);
    }
}

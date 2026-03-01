package de.jakob.lotm.abilities.abyss;

import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MindFogAbility extends ToggleAbility {
    private final Random random = new Random();
    private final DustParticleOptions fogDust = new DustParticleOptions(new Vector3f(0.4f, 0.2f, 0.6f), 1.0f);

    public MindFogAbility(String id) {
        super(id);
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 8;
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        ServerLevel serverLevel = (ServerLevel) level;
        double fogRadius = 20;

        for (int i = 0; i < 8; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double distance = random.nextDouble() * fogRadius;
            double x = entity.getX() + Math.cos(angle) * distance;
            double z = entity.getZ() + Math.sin(angle) * distance;
            ParticleUtil.spawnParticles(serverLevel, fogDust, new Vec3(x, entity.getY() + 1, z), 1, 0.2, 0.02);
        }

        AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), fogRadius)
                .stream()
                .filter(target -> AbilityUtil.mayDamage(entity, target))
                .forEach(target -> {
                    if (target.hasData(ModAttachments.SANITY_COMPONENT)) {
                        target.getData(ModAttachments.SANITY_COMPONENT)
                                .increaseSanityAndSync(-0.05f, target);
                    }

                    if (random.nextInt(5) == 0) {
                        applyRandomNegativeEffect(target);
                    }
                });
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
    }

    private void applyRandomNegativeEffect(LivingEntity entity) {
        int effectChoice = random.nextInt(6);
        int duration = 20 * 5;

        switch (effectChoice) {
            case 0 -> entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 1, false, false));
            case 1 -> entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 1, false, false));
            case 2 -> entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, duration, 0, false, false));
            case 3 -> entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration, 0, false, false));
            case 4 -> entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, duration, 2, false, false));
            case 5 -> entity.addEffect(new MobEffectInstance(MobEffects.WITHER, duration, 0, false, false));
        }
    }
}
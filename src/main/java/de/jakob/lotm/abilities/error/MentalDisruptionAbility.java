package de.jakob.lotm.abilities.error;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.abilities.error.handler.TheftHandler;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class MentalDisruptionAbility extends Ability {
    public MentalDisruptionAbility(String id) {
        super(id, 2, "charm");
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("error", 8));
    }

    @Override
    public float getSpiritualityCost() {
        return 40;
    }

    private static final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(71 / 255f, 66 / 255f, 201 / 255f),
            1.25f
    );

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20, 1.5f);
        if(target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.theft.no_target").withColor(0x4742c9));
            return;
        }

        target.hurt(ModDamageTypes.source(level, ModDamageTypes.LOOSING_CONTROL, entity), (float) (DamageLookup.lookupDamage(8, .4) * multiplier(entity)));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 6, 8, false, false, false));
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 8, false, false, false));

        ParticleUtil.spawnParticles(serverLevel, ParticleTypes.END_ROD, target.getEyePosition(), 60, .5, .025);
        ParticleUtil.spawnParticles(serverLevel, dust, target.getEyePosition(), 120, .5, .025);

        int seq = AbilityUtil.getSeqWithArt(entity, this);
        final UUID[] taskIdHolder = new UUID[1];
        taskIdHolder[0] = ServerScheduler.scheduleForDuration(0, 2, 20 * 4, () -> {
            Location targetLoc = new Location(target.position(), target.level());
            if(InteractionHandler.isInteractionPossible(targetLoc, "calming", seq)) {
                if(taskIdHolder[0] != null) ServerScheduler.cancel(taskIdHolder[0]);
                return;
            }
            target.setDeltaMovement(new Vec3(0, 0, 0));
            target.hurtMarked = true;
        });
    }
}

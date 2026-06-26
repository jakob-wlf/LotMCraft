package de.jakob.lotm.beyonders.abilities.darkness;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.attachments.LuckComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
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
import java.util.UUID;

public class NightDomainAbility extends Ability {
    public NightDomainAbility(String id) {
        super(id, 30, "darkness");
        autoClear = false;
        interactionRadius = 35;
        interactionCacheTicks = 20 * 25;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("darkness", 4));
    }

    @Override
    public float getSpiritualityCost() {
        return 1800;
    }

    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(0, 0, 0), 5);

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 startPos = entity.position();

        EffectManager.playEffect(EffectManager.Effect.NIGHT_DOMAIN, entity.position().x, entity.position().y, entity.position().z, serverLevel, entity);
        float multiplier = multiplier(entity);
        final UUID[] taskIdHolder = new UUID[1];
        taskIdHolder[0] = ServerScheduler.scheduleForDuration(0, 2, 20 * 25, () -> {
            Location currentLoc = new Location(entity.position(), serverLevel);
            int seq = AbilityUtil.getSeqWithArt(entity, this);

            // Night Domain is completely cancelled by light_strong if the caster is at least 1 sequence higher
            if(InteractionHandler.isInteractionPossibleStrictlyHigher(currentLoc, "light_strong", seq, 1)) {
                EffectManager.cancelEffectsNear(startPos.x, startPos.y, startPos.z, 50, serverLevel);
                if(taskIdHolder[0] != null) ServerScheduler.cancel(taskIdHolder[0]);
                return;
            }

            // Night Domain is weakened by purification interactions
            boolean purified = InteractionHandler.isInteractionPossible(currentLoc, "purification", seq);

            ParticleUtil.spawnParticles(serverLevel, dust, startPos, purified ? 30 : 80, 35*(int) Math.max(multiplier/2,1), .25*(int) Math.max(multiplier/2,1), 35*(int) Math.max(multiplier/2,1), 0);
            if(!purified) {
                AbilityUtil.addPotionEffectToNearbyEntities(serverLevel, entity, 35*(int) Math.max(multiplier/2,1), startPos, new MobEffectInstance(MobEffects.BLINDNESS, 20, 20, false, false, false));
                AbilityUtil.addPotionEffectToNearbyEntities(serverLevel, entity, 35*(int) Math.max(multiplier/2,1), startPos, new MobEffectInstance(MobEffects.DARKNESS, 20, 20, false, false, false));
            }

            AbilityUtil.addPotionEffectToNearbyEntities(serverLevel, entity, 35, startPos, new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, purified ? 1 : 5, false, false, false));

            AbilityUtil.damageNearbyEntities(serverLevel, entity, 35*(int) Math.max(multiplier/2,1), DamageLookup.lookupDps(4, .85, 2, 20) * 20, startPos, true, false, ModDamageTypes.source(level, ModDamageTypes.DARKNESS_GENERIC, entity));

            AbilityUtil.getNearbyEntities(entity, serverLevel, startPos, 35).forEach(e -> {
                LuckComponent luckComponent = e.getData(ModAttachments.LUCK_COMPONENT);
                luckComponent.addLuckWithMin(-120*(int) Math.max(multiplier/2,1), purified ? -240*(int) Math.max(multiplier/2,1) : -960*(int) Math.max(multiplier/2,1));
                BeyonderData.addModifierWithTimeLimit(e, "night_domain_debuff", .65, 2000); 
            });
            BeyonderData.addModifierWithTimeLimit(entity, "night_domain_buff", 1.35f, 2000);

            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 2, 2, false, false, false));
        }, () -> this.clearArtifactScaling(entity), serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(startPos, level)));
    }
}

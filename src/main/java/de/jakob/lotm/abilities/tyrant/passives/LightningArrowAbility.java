package de.jakob.lotm.abilities.tyrant.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class LightningArrowAbility extends PassiveAbilityItem {
    public LightningArrowAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("tyrant", 5));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
    }

    private static final DustParticleOptions dust = new DustParticleOptions(new Vector3f(0, 0.75f, .9f), 2);

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if(!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        if (!(event.getEntity() instanceof AbstractArrow arrow))
            return;

        if (!(arrow.getOwner() instanceof Player player))
            return;

        if(!((LightningArrowAbility) PassiveAbilityHandler.LIGHTNING_ARROW.get()).shouldApplyTo(player)) {
            return;
        }

        ParticleUtil.spawnParticles(
                serverLevel,
                ModParticles.LIGHTNING.get(),
                player.getEyePosition(),
                20, .4, .1
        );

        serverLevel.playSound(null, arrow.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, arrow.getSoundSource(), 1.0f, 1.0f);

        arrow.setDeltaMovement(
                arrow.getDeltaMovement().scale(2.0)
        );

        arrow.setBaseDamage(
                arrow.getBaseDamage() * 3.0
        );
    }

    @SubscribeEvent
    public static void onArrowHit(ProjectileImpactEvent event) {
        if(!(event.getProjectile() instanceof AbstractArrow arrow))
            return;

        if (!(arrow.getOwner() instanceof Player player))
            return;

        if(!((LightningArrowAbility) PassiveAbilityHandler.LIGHTNING_ARROW.get()).shouldApplyTo(player)) {
            return;
        }

        Level level = arrow.level();
        if(!(level instanceof ServerLevel serverLevel)) return;
        ParticleUtil.spawnParticles(
                serverLevel,
                ModParticles.LIGHTNING.get(),
                arrow.position(),
                50, .2, .2
        );
    }
}

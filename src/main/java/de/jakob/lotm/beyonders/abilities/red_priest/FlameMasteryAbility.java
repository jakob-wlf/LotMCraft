package de.jakob.lotm.beyonders.abilities.red_priest;

import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.entity.custom.projectiles.FireballEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class FlameMasteryAbility extends SelectableAbility {

    public FlameMasteryAbility(String id) {
        super(id, 5f, "burning");

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(2, 3, 3, 4, 5));

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(3200f, 1300f, 800f, 600f, 550f));

        baseDamage = 24f;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("red_priest", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 100;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.flame_mastery.fireball_barrage",
                "ability.lotmcraft.flame_mastery.eruption",
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(level.isClientSide)
            return;
        if(!(entity instanceof Player)) {
            abilityIndex = 1;
        }

        switch (abilityIndex) {
            case 0 -> fireballBarrage((ServerLevel) level, entity);
            case 1 -> eruption((ServerLevel) level, entity);
        }
    }

    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(1.0f, .95f, .95f), 2.0f);


    private void eruption(ServerLevel level, LivingEntity entity) {
        Vec3 targetPos = AbilityUtil.getTargetLocation(entity, (int) (20* multiplier(entity)), 1.4f);
        boolean griefing = BeyonderData.isGriefingEnabled(entity);
        level.explode(entity, targetPos.x, targetPos.y, targetPos.z, 9, griefing, griefing ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE);
        level.explode(entity, targetPos.x, targetPos.y + 1, targetPos.z, 9, griefing, griefing ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE);
        level.explode(entity, targetPos.x, targetPos.y + 2, targetPos.z, 9, griefing, griefing ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE);
        ParticleUtil.spawnParticles(level, ParticleTypes.FLAME, targetPos, 1500, 2, 6, 2, .02);
        ParticleUtil.spawnParticles(level, ParticleTypes.SMOKE, targetPos, 300, 2, 6, 2, .02);
        ParticleUtil.spawnParticles(level, ParticleTypes.EXPLOSION, targetPos, 90, 2, 6, 2, .02);
        ParticleUtil.spawnParticles(level, dust, targetPos, 400, 2, 6, 2, 0);

        float damage = baseDamage;
        AbilityUtil.damageNearbyEntities(level, entity, 9, ModDamageTypes.SOUL_FIRE,damage/2, targetPos, true, false);
        AbilityUtil.damageNearbyEntities(level, entity, 9, ModDamageTypes.FIRE,damage/2, targetPos, true, false);

        for(int i = 0; i < 25; i++) {
            FallingBlockEntity falling = FallingBlockEntity.fall(
                    level,
                    BlockPos.containing(targetPos.x, targetPos.y, targetPos.z).offset(random.nextInt(-1, 1), 2, random.nextInt(-1, 1)),
                    i % 2 == 0 ? Blocks.MAGMA_BLOCK.defaultBlockState() : Blocks.BASALT.defaultBlockState()
            );

            double xVel = random.nextDouble(-3, 3);
            double yVel = random.nextDouble(3.5, 5);
            double zVel = random.nextDouble(-3, 3);
            Vec3 motion = new Vec3(xVel, yVel, zVel).normalize().scale(1.4);
            falling.setDeltaMovement(motion);
            ServerScheduler.scheduleForDuration(0, 1, 40, () -> {
                falling.setDeltaMovement(falling.getDeltaMovement().x, falling.getDeltaMovement().y - 0.03, falling.getDeltaMovement().z);
                falling.hurtMarked = true;
            });
            if(!griefing)
                falling.disableDrop();
        }
    }

    private void fireballBarrage(ServerLevel level, LivingEntity entity) {
        double shots = 15;
        Vec3 targetPos = AbilityUtil.getTargetLocation(entity, (int) (50* multiplier(entity)), 1.4f);
        Vec3 pos = entity.getEyePosition();
        Vec3 dir = entity.getLookAngle();
        for (int i = 0; i < shots; i++) {
            ServerScheduler.scheduleDelayed(i * 7, () -> fireball(level, entity, pos, dir, targetPos), level, () -> AbilityUtil.getTimeInArea(entity, new Location(pos, level)));
        }
    }

    private void fireball(ServerLevel level, LivingEntity entity, Vec3 pos, Vec3 dir, Vec3 targetPos) {
        Vec3 startPos = VectorUtil.getRelativePosition(pos.add(entity.getLookAngle().normalize()), dir.normalize(), random.nextDouble(-.2, 2.5f), random.nextDouble(-13, 13f), random.nextDouble(-1, 6));
        Vec3 direction = targetPos.subtract(startPos).normalize();

        level.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 1.0f, 1.0f);

        float damage = baseDamage/2;
        FireballEntity fireball = new FireballEntity(level, entity, damage, BeyonderData.isGriefingEnabled(entity), 1.75f);

        fireball.setPos(startPos.x, startPos.y, startPos.z); // Set initial position,
        fireball.shoot(direction.x, direction.y, direction.z, 1.85f*multiplier(entity), 0);
        level.addFreshEntity(fireball);
    }
}

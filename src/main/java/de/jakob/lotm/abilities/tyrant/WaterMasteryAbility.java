package de.jakob.lotm.abilities.tyrant;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.AbilitySelectionPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class WaterMasteryAbility extends SelectableAbilityItem {
    public WaterMasteryAbility(Properties properties) {
        super(properties, .75f);
    }

    private final DustParticleOptions dustOptions = new DustParticleOptions(
            new Vector3f(30 / 255f, 120 / 255f, 255 / 255f),
            1.5f
    );
    private final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(30 / 255f, 153 / 255f, 255 / 255f),
            10f
    );


    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "tyrant", 4
        ));
    }

    @Override
    protected float getSpiritualityCost() {
        return 300;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.water_mastery.water_wall",
                "ability.lotmcraft.water_mastery.flood",
        };
    }

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(level.isClientSide)
            return;
        switch(abilityIndex) {
            case 0 -> waterWall((ServerLevel) level, entity);
        }
    }

    private void waterWall(ServerLevel level, LivingEntity entity) {
        Vec3 targetPos = AbilityUtil.getTargetLocation(entity, 12, 1.4f);

        Vec3 perpendicular = VectorUtil.getPerpendicularVector(entity.getLookAngle()).normalize();

        ServerScheduler.scheduleForDuration(0, 10, 20 * 30, () -> {
            if(random.nextInt(10) == 0)
                level.playSound(null, targetPos.x, targetPos.y, targetPos.z, SoundEvents.GENERIC_SPLASH, entity.getSoundSource(), 2.0f, 1.0f);


            for(int i = -4; i < 20; i++) {
                for(int j = -30; j < 31; j++) {
                    Vec3 pos = targetPos.add(perpendicular.scale(j)).add(0, i, 0);

                    if(random.nextBoolean())
                        ParticleUtil.spawnParticles(level, dust, pos, 1, 0.5, 0.02);

                    AbilityUtil.damageNearbyEntities(level, entity, 1.2f, 16 * multiplier(entity), pos, true, false, false, 15);

                    for(LivingEntity target : AbilityUtil.getNearbyEntities(entity, level, pos, 1f)) {
                        Vec3 knockback = target.position().subtract(pos).normalize().add(0, .2, 0).scale(1.4f);
                        target.setDeltaMovement(knockback);
                    }
                }
            }
        }, level);
    }

}

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
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class WaterMasteryAbility extends SelectableAbilityItem {
    public WaterMasteryAbility(Properties properties) {
        super(properties, 5f);
    }

    private final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(30 / 255f, 153 / 255f, 255 / 255f),
            20f
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
                "ability.lotmcraft.water_mastery.flooding",
        };
    }

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(level.isClientSide)
            return;
        if(!(entity instanceof Player))
            abilityIndex = 0;

        switch(abilityIndex) {
            case 0 -> waterWall((ServerLevel) level, entity);
            case 1 -> flood((ServerLevel) level, (Player) entity) ;
        }
    }

    private void flood(ServerLevel level, Player player) {
        if(!BeyonderData.isGriefingEnabled(player)) {
            if(player instanceof ServerPlayer serverPlayer) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.translatable("lotmcraft.griefing_enabled_required").withColor(0x456bd6));
                serverPlayer.connection.send(packet);
            }
            return;
        }

        AtomicInteger waterLevel = new AtomicInteger(-3);
        Vec3 startPos = player.position();

        ParticleUtil.spawnParticles(level, dust, startPos, 1500, 25, 8, 25, 0);

        ServerScheduler.scheduleForDuration(0, 10, 10 * 14, () -> {
           AbilityUtil.getBlocksInCircle(level, new Vec3(startPos.x, startPos.y + waterLevel.get(), startPos.z), 80).forEach(b -> {
               BlockState state = level.getBlockState(b);
               if(state.getCollisionShape(level, b).isEmpty()) {
                   level.setBlockAndUpdate(b, Blocks.WATER.defaultBlockState());
               }
           });
          waterLevel.getAndIncrement();
        });
    }

    private void waterWall(ServerLevel level, LivingEntity entity) {
        Vec3 targetPos = AbilityUtil.getTargetLocation(entity, 12, 1.4f);

        Vec3 perpendicular = VectorUtil.getPerpendicularVector(entity.getLookAngle()).normalize();

        ServerScheduler.scheduleForDuration(0, 7, 20 * 30, () -> {
            if(random.nextInt(10) == 0)
                level.playSound(null, targetPos.x, targetPos.y, targetPos.z, SoundEvents.GENERIC_SPLASH, entity.getSoundSource(), 2.0f, 1.0f);


            for(int i = -2; i < 17; i++) {
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

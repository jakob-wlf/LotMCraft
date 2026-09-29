package de.jakob.lotm.beyonders.abilities.demoness;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.beyonders.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.PlayPhotonBlockEffectPacket;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoulFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

//TODO: Create black flame block
public class BlackFlameAbility extends SelectableAbility {

    public BlackFlameAbility(String id) {
        super(id, 1f, "soul_burn", "burning");
        postsUsedAbilityEventManually = true;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "demoness", 7
        ));
    }

    @Override
    protected float getSpiritualityCost() {
        return 30;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.black_flame.burn",
                "ability.lotmcraft.black_flame.shoot",
                "ability.lotmcraft.black_flame.expel"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        switch(abilityIndex) {
            case 0 -> burn(level, entity);
            case 1 -> shoot(level, entity);
            case 2 -> expel(level, entity);
        }
    }


    private void burn(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 targetPos = AbilityUtil.getTargetLocation(entity, (int) (10*multiplier(entity)), 1.4f);
        level.playSound(null, targetPos.x, targetPos.y, targetPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 2.0f, .5f);

        BlockState block = level.getBlockState(BlockPos.containing(targetPos));
        if(block.isAir()) {
            level.setBlockAndUpdate(BlockPos.containing(targetPos), Blocks.LIGHT.defaultBlockState());
        }


        BlockPos blockPos = BlockPos.containing(targetPos);

        double offsetX = targetPos.x - (blockPos.getX() + 0.5);
        double offsetY = targetPos.y - (blockPos.getY() + 0.5) + 1;
        double offsetZ = targetPos.z - (blockPos.getZ() + 0.5);

        ServerScheduler.scheduleDelayed(2, () -> {
            PacketHandler.sendToNearbyPlayers(
                    new PlayPhotonBlockEffectPacket("black_flame_burn", blockPos, offsetX, offsetY, offsetZ, 2.75, null, -1, false, true),
                    (ServerLevel) level, targetPos, 128
            );
        });

        AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 2.5, DamageLookup.lookupDamage(7, .7) *multiplier(entity), targetPos, true, false, true, 0, 20 * 2, ModDamageTypes.source(level, ModDamageTypes.DEMONESS_GENERIC, entity));

        ServerScheduler.scheduleDelayed(51, () -> level.setBlockAndUpdate(BlockPos.containing(targetPos), Blocks.AIR.defaultBlockState()));
        NeoForge.EVENT_BUS.post(new AbilityUsedEvent((ServerLevel) level, targetPos, entity, this, interactionFlags, 4, 10));
    }

    //TODO: Place Black Flames on griefing
    private void expel(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 startPos = entity.position().add(0, .35, 0);

        level.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 1.0f, 1.0f);

        AtomicDouble i = new AtomicDouble(0.6);
        ServerScheduler.scheduleForDuration(0, 2, 80, () -> {
            double radius = i.get() < .71 ? i.get() : i.get() * 2;
            AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, radius - .3, radius, DamageLookup.lookupDamage(7, .8) *multiplier(entity), startPos.subtract(0, 1, 0), true, false, true, 0, 20 * 5, ModDamageTypes.source(level, ModDamageTypes.DEMONESS_GENERIC, entity));
            i.set(i.get() + .1);
        }, null, (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));

        BlockPos blockPos = BlockPos.containing(startPos);

        double offsetX = startPos.x - (blockPos.getX() + 0.5);
        double offsetY = startPos.y - (blockPos.getY() + 0.5);
        double offsetZ = startPos.z - (blockPos.getZ() + 0.5);

        PacketHandler.sendToNearbyPlayers(
                new PlayPhotonBlockEffectPacket("black_flame_wave", blockPos, offsetX, offsetY, offsetZ, 1, null, -1, false, true),
                (ServerLevel) level, startPos, 128
        );

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent((ServerLevel) level, startPos, entity, this, interactionFlags, 9, 50));
    }

    private void shoot(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 startPos = VectorUtil.getRelativePosition(entity.getEyePosition().add(entity.getLookAngle().normalize()), entity.getLookAngle().normalize(), 0, random.nextDouble(-.65, .65), random.nextDouble(-.1, .6));
        Vec3 direction = AbilityUtil.getTargetLocation(entity, (int) (10*multiplier(entity)), 1.4f).subtract(startPos).normalize();

        AtomicReference<Vec3> currentPos = new AtomicReference<>(startPos);

        AtomicBoolean hasHit = new AtomicBoolean(false);

        level.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 1.0f, 1.0f);

        ServerScheduler.scheduleForDuration(0, 1, 20 * 40, () -> {
            if(hasHit.get())
                return;

            Vec3 pos = currentPos.get();

            if(AbilityUtil.damageNearbyEntities(
                    (ServerLevel) level,
                    entity,
                    2.5f,
                    DamageLookup.lookupDamage(7, 1.1)*multiplier(entity),
                    pos,
                    true,
                    false,
                    true,
                    0,
                    20 * 5,
                    ModDamageTypes.source(level, ModDamageTypes.DEMONESS_GENERIC, entity)
            )) {
                NeoForge.EVENT_BUS.post(new AbilityUsedEvent((ServerLevel) level, pos, entity, this, interactionFlags, 2.5, 10));
                hasHit.set(true);
                return;
            }

            if(!level.getBlockState(BlockPos.containing(pos.x, pos.y, pos.z)).isAir()) {
                NeoForge.EVENT_BUS.post(new AbilityUsedEvent((ServerLevel) level, pos, entity, this, interactionFlags, 2.5, 10));
                if(BeyonderData.isGriefingEnabled(entity)) {
                    pos = pos.subtract(direction);
                    level.setBlockAndUpdate(BlockPos.containing(pos.x, pos.y, pos.z), Blocks.FIRE.defaultBlockState());
                }
                hasHit.set(true);
                return;
            }

            BlockPos blockPos = BlockPos.containing(pos);

            double offsetX = pos.x - (blockPos.getX() + 0.5);
            double offsetY = pos.y - (blockPos.getY() + 0.5);
            double offsetZ = pos.z - (blockPos.getZ() + 0.5);

            PacketHandler.sendToNearbyPlayers(
                    new PlayPhotonBlockEffectPacket("black_flame_particle", blockPos, offsetX, offsetY, offsetZ, 1, null, -1, false, true),
                    (ServerLevel) level, pos, 128
            );

            currentPos.set(pos.add(direction));
        }, null, (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));
    }
}

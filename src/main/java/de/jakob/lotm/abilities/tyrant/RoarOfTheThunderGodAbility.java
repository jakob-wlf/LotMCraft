package de.jakob.lotm.abilities.tyrant;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class RoarOfTheThunderGodAbility extends Ability {
    public RoarOfTheThunderGodAbility(String id) {
        super(id, 2);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("tyrant", 1));
    }

    @Override
    public float getSpiritualityCost() {
        return 1800;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            ClientHandler.applyCameraShakeToPlayersInRadius(2, 30, (ClientLevel) level, entity.position(), 60);
            return;
        }

        Vec3 startPos = entity.position();
        boolean griefing = BeyonderData.isGriefingEnabled(entity);

        level.playSound(null, BlockPos.containing(startPos), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.BLOCKS, 10, 1);
        level.playSound(null, BlockPos.containing(startPos), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.BLOCKS, 10, 1);
        level.playSound(null, BlockPos.containing(startPos), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.BLOCKS, 10, 1);
        level.playSound(null, BlockPos.containing(startPos), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.BLOCKS, 10, 1);

        AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, startPos, 50).forEach(e -> {
            e.hurt(e.damageSources().mobAttack(entity), (float) (DamageLookup.lookupDamage(1, .85) * multiplier(entity)));
            Vec3 knockBack = new Vec3(e.position().subtract(startPos).normalize().x, .75, e.position().subtract(startPos).normalize().z).normalize().scale(2.75);
            e.setDeltaMovement(knockBack);
        });

        EffectManager.playEffect(EffectManager.Effect.THUNDER_EXPLOSION, startPos.x, startPos.y + .5, startPos.z, (ServerLevel) level);

        for(int y = 0; y < 3; y++) {
            for (int i = 3; i < 27; i+=2) {
                AbilityUtil.getBlocksInCircleOutline((ServerLevel) level, startPos.subtract(0, 1 - y, 0), i).forEach(b -> {
                    spawnFallingBlocks(level, startPos, b, griefing);
                });
            }
        }
    }

    private void spawnFallingBlocks(Level level, Vec3 startPos, BlockPos b, boolean griefing) {
        BlockState state = level.getBlockState(b);
        BlockState above = level.getBlockState(b.above());
        if(state.getCollisionShape(level, b).isEmpty() || !above.getCollisionShape(level, b.above()).isEmpty())
            return;

        Vec3 vectorFromCenter = new Vec3(b.getX() + 0.5 - startPos.x, 0, b.getZ() + 0.5 - startPos.z).normalize();
        Vec3 movement = (new Vec3(vectorFromCenter.x, 1, vectorFromCenter.z)).normalize().scale(.75);

        FallingBlockEntity block = FallingBlockEntity.fall(level, b.above(), state);
        block.setDeltaMovement(movement);
        if(!griefing)
            block.disableDrop();
        else {
            level.setBlockAndUpdate(b, Blocks.AIR.defaultBlockState());
        }
        block.hurtMarked = true;
    }
}

package de.jakob.lotm.beyonders.abilities.sun;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AnimationUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class HolyLightSummoningAbility extends Ability {
    public HolyLightSummoningAbility(String id) {
        super(id, 1.5f, "purification", "light_source", "light_weak");
        postsUsedAbilityEventManually = true;
        interactionRadius = 8;

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(1, 1, 1, 2, 2, 2, 3, 3));

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(2000f, 1000f, 670f, 380f, 325f, 237f, 170f, 150f));


        baseDamage = 4;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        Map<String, Integer> reqs = new HashMap<>();
        reqs.put("sun", 7);
        return reqs;
    }

    @Override
    protected float getSpiritualityCost() {
        return 64;
    }

    DustParticleOptions dustOptions = new DustParticleOptions(
            new Vector3f(255 / 255f, 180 / 255f, 66 / 255f),
            2.25f
    );

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide) return;

        Vec3 initialPos = AbilityUtil.getTargetLocation(entity, baseDistance, 1.5f, true).add(0, 18, 0);

        List<BlockPos> lights = new ArrayList<>();

        AtomicReference<Vec3> currentPos = new AtomicReference<>(initialPos);

        level.playSound(null, initialPos.x, initialPos.y - 18, initialPos.z, SoundEvents.BEACON_ACTIVATE, entity.getSoundSource(), 3.0f, 1.0f);

        EffectManager.playEffect(EffectManager.Effect.HOLY_LIGHT_SMALL, initialPos.x, initialPos.y - 18, initialPos.z, (ServerLevel) level, entity);

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        ServerScheduler.scheduleForDuration(0, 1, 22, () -> {
            Vec3 pos = currentPos.get();

            BlockPos blockPos = BlockPos.containing(pos);
            //Set the light blocks
            if (level.getBlockState(blockPos).isAir()) {
                level.setBlockAndUpdate(blockPos, Blocks.LIGHT.defaultBlockState());
                lights.add(blockPos);
            }

            if(entitySeq <= 4){
                AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 2.5* (int) multiplier(entity), ModDamageTypes.PURIFICATION, baseDamage * 0.34, pos, true, false, true, 0);
                AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 2.5* (int) multiplier(entity), ModDamageTypes.LIGHT, baseDamage * 0.33, pos, true, false, true, 0);
                AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 2.5* (int) multiplier(entity), ModDamageTypes.FAITH, baseDamage * 0.33, pos, true, false, true, 0);

            }
            else{
                AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 2.5* (int) multiplier(entity), ModDamageTypes.LIGHT, baseDamage, pos, true, false, true, 0);
            }

            currentPos.set(pos.subtract(0, 2.5, 0));
        }, null, (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));

        ServerScheduler.scheduleDelayed(22, () -> {
            NeoForge.EVENT_BUS.post(new AbilityUsedEvent((ServerLevel) level, initialPos.subtract(0, 18, 0), entity, this, interactionFlags, interactionRadius, interactionCacheTicks));
        }, (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));

        ServerScheduler.scheduleDelayed(40, () -> {
            lights.forEach(l -> level.setBlockAndUpdate(l, Blocks.AIR.defaultBlockState()));
        }, (ServerLevel) level, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), level)));

        if(entity instanceof Player) AnimationUtil.playOpenArmAnimation((Player) entity);
    }
}

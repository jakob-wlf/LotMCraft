package de.jakob.lotm.beyonders.abilities.visionary;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.beyonders.abilities.visionary.passives.MetaAwarenessAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ManipulationAbility extends SelectableAbility {

    private static HashMap<UUID, UUID> targetMap = new HashMap<>();

    public ManipulationAbility(String id) {
        super(id, 5);
        canBeUsedByNPC = false;
        canBeCopied = false;
        canBeReplicated = false;
        cannotBeStolen = true;

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(1, 1, 2, 3, 5));

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(1200f, 800f, 470f, 280f, 260f, 160f, 134f, 100f));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 4));
    }

    @Override
    public float getSpiritualityCost() {
        return 1150;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.manipulation.select_target",
                "ability.lotmcraft.manipulation.group_incite",
                "ability.lotmcraft.manipulation.move"
                //"ability.lotmcraft.manipulation.control"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        if(VisionaryHandler.shouldBeAffectedWithMindWorldSeal(entitySeq)){
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("ability.lotmcraft.mind_world_authority_ability.is_sealed")
                            .withColor(0xFFff124d));
            return;
        }

        switch (abilityIndex) {
            case 0 -> selectTarget(level, entity);
            case 1 -> groupIncite(level, entity);
            case 2 -> move(level, entity);
            //case 1 -> control(level, entity);
        }
    }

    private final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(250 / 255f, 201 / 255f, 102 / 255f),
            1.5f
    );

    private void move(Level level, LivingEntity entity){
        if(level.isClientSide)
            return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        LivingEntity target = null;
        if(targetMap.containsKey(entity.getUUID())){
            var buff = serverLevel.getEntity(targetMap.get(entity.getUUID()));
            if(buff instanceof LivingEntity livingBuff)
                target = livingBuff;
        }

        if (target == null) {
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("ability.lotmcraft.frenzy.no_target").withColor(0xFFff124d));
            targetMap.remove(entity.getUUID());
            return;
        }

        int casterSeq = AbilityUtil.getSeqWithArt(entity, this);
        if(VisionaryHandler.shouldFailAndTrigger(casterSeq, entity, target, this)){
            return;
        }

        int maxDistance = getDistance(casterSeq);

        var loc = AbilityUtil.getTargetBlock(entity, baseDistance).getCenter();
        if(loc.distanceTo(target.position()) >= maxDistance){
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("ability.lotmcraft.manipulation.out_of_range").withColor(0xFFff124d));
            return;
        }

        final LivingEntity finalTarget = target;
        ServerScheduler.scheduleForDuration(0, 1, 20 * 5, () -> {
            if (!finalTarget.isAlive()) {
                return;
            }

            Vec3 current = finalTarget.position();

            double dx = loc.x - current.x;
            double dz = loc.z - current.z;

            double distanceSqr = dx * dx + dz * dz;

            if (distanceSqr < 0.05) {
                finalTarget.setDeltaMovement(Vec3.ZERO);
                finalTarget.hurtMarked = true;
                return;
            }

            double distance = Math.sqrt(distanceSqr);

            double dirX = dx / distance;
            double dirZ = dz / distance;

            Vec3 direction = new Vec3(dirX, 0, dirZ);

            BlockPos currentPos = finalTarget.blockPosition();

            BlockPos frontPos = BlockPos.containing(
                    current.x + dirX * 0.7,
                    current.y,
                    current.z + dirZ * 0.7
            );

            BlockState frontBlock = finalTarget.level().getBlockState(frontPos);

            boolean blocked = !frontBlock.getCollisionShape(finalTarget.level(), frontPos).isEmpty();

            Vec3 velocity = finalTarget.getDeltaMovement();

            if (blocked && finalTarget.onGround()) {
                finalTarget.setDeltaMovement(
                        dirX * 0.4,
                        0.42,
                        dirZ * 0.4
                );

                finalTarget.hurtMarked = true;
                return;
            }

            finalTarget.setDeltaMovement(
                    dirX * 0.4,
                    velocity.y,
                    dirZ * 0.4
            );

            finalTarget.hurtMarked = true;
        });
    }

    private static int getDistance(int seq){
        return switch (seq){
            case 4 -> 25;
            case 3 -> 50;
            case 2 -> 75;
            case 1 -> 100;
            case 0 -> 125;
            default -> 0;
        };
    }

    private void selectTarget(Level level, LivingEntity entity){
        if (level.isClientSide) {
            LivingEntity target = AbilityUtil.getTargetEntity(entity, baseDistance, 2);
            if(target == null) return;
            ParticleUtil.spawnSphereParticles((ClientLevel) level, ParticleTypes.SMOKE, target.getEyePosition(), 1, 30);
            ParticleUtil.spawnParticles((ClientLevel) level, dust,  target.getEyePosition(), 40, .5);
            return;
        }
        if (!(level instanceof ServerLevel serverLevel)) return;

        LivingEntity target = null;

        if(DiscernmentAbility.discerning.contains(entity.getUUID())){
            target = AbilityUtil.getTargetEntity(entity, baseDistance, 2, true,
                    false, false, true);
        }
        else{
            target = AbilityUtil.getTargetEntity(entity, baseDistance, 2, true);
        }

        if (target == null) {
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("ability.lotmcraft.frenzy.no_target").withColor(0xFFff124d));
            return;
        }

        int casterSeq = AbilityUtil.getSeqWithArt(entity, this);
        if(VisionaryHandler.shouldFailAndTrigger(casterSeq, entity, target, this)){
            return;
        }

        AbilityUtil.sendActionBar(entity,
                Component.translatable("ability.lotmcraft.manipulation.target_selected").withColor(0xFFff124d));

        if(targetMap.containsKey(entity.getUUID()) &&
                targetMap.get(entity.getUUID()).equals(target.getUUID())){
            targetMap.remove(entity.getUUID());
            return;
        }

        targetMap.put(entity.getUUID(), target.getUUID());
    }

    public void groupIncite(Level level, LivingEntity entity) {
        if (level.isClientSide) {
            LivingEntity target = AbilityUtil.getTargetEntity(entity, baseDistance, 2);
            if(target == null) return;
            ParticleUtil.spawnSphereParticles((ClientLevel) level, ParticleTypes.SMOKE, target.getEyePosition(), 1, 30);
            ParticleUtil.spawnParticles((ClientLevel) level, dust,  target.getEyePosition(), 40, .5);
            return;
        }
        if (!(level instanceof ServerLevel serverLevel)) return;

        LivingEntity target = null;
        if(targetMap.containsKey(entity.getUUID())){
            var buff = serverLevel.getEntity(targetMap.get(entity.getUUID()));
            if(buff instanceof LivingEntity livingBuff)
                target = livingBuff;
        }
        else
            target = AbilityUtil.getTargetEntity(entity, baseDistance, 2);

        if (target == null) {
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("ability.lotmcraft.frenzy.no_target").withColor(0xFFff124d));
            targetMap.remove(entity.getUUID());
            return;
        }

        int casterSeq = AbilityUtil.getSeqWithArt(entity, this);
        List<LivingEntity> nearby = AbilityUtil.getNearbyEntities(
                entity, serverLevel, entity.position(), 80, false, true);

        if(VisionaryHandler.shouldFailAndTrigger(casterSeq, entity, target, this)){
            return;
        }

        for (LivingEntity nearby_entity : nearby) {
            if (nearby_entity.getUUID().equals(entity.getUUID())) continue;
            if (nearby_entity.getUUID().equals(target.getUUID())) continue;

            if (nearby_entity instanceof ServerPlayer nearbyPlayer) {
                // Force beyonder players of lower sequence to use abilities
                if (!BeyonderData.isBeyonder(nearbyPlayer)) continue;

                if(VisionaryHandler.shouldFailAndTrigger(casterSeq, entity, nearby_entity, this)){
                   continue;
                }

                if (BeyonderData.getSequence(nearbyPlayer) < casterSeq) continue;
                forcePlayerAbilities(nearbyPlayer, target, serverLevel);
            } else if (nearby_entity instanceof Mob mob) {
                // For beyonder mobs, check sequence. For non-beyonder mobs, always incite.
                if (BeyonderData.isBeyonder(mob) && BeyonderData.getSequence(mob) < casterSeq) continue;

                mob.setTarget(target);
            }
        }
    }

    private void forcePlayerAbilities(ServerPlayer player, LivingEntity target, ServerLevel level) {
        int interval = 20 * 3;
        int duration = 20 * 7;

        String pathway = BeyonderData.getPathway(player);
        int sequence = BeyonderData.getSequence(player);

        ServerScheduler.scheduleForDuration(0, interval, duration, () -> {
            if (player.isRemoved() || !player.isAlive()) return;
            if (target.isRemoved() || !target.isAlive()) return;

            List<Ability> abilities = new ArrayList<>(
                    LOTMCraft.abilityHandler.getByPathwayAndSequence(pathway, sequence));
            if (abilities.isEmpty()) return;

            Ability chosen = abilities.get(random.nextInt(abilities.size()));
            chosen.useAbility(level, player);
        }, level);
    }
}

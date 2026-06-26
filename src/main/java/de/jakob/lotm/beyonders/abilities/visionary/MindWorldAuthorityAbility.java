package de.jakob.lotm.beyonders.abilities.visionary;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.entity.custom.AvatarEntity;
import de.jakob.lotm.gamerule.ModGameRules;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.playerMap.StoredData;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class MindWorldAuthorityAbility extends SelectableAbility {
    private MindWorldAuthorityEnvisioningAbility envisioningToggle;

    public MindWorldAuthorityAbility(String id) {
        super(id, 5f);

        canBeCopied = false;
        canBeReplicated = false;
        cannotBeStolen = true;
        canBeUsedByNPC = false;
        canBeUsedInArtifact = false;
        doesNotIncreaseDigestion = true;
        canBeShared = false;

        envisioningToggle = null;
    }

    private final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(250 / 255f, 201 / 255f, 102 / 255f),
            1.5f
    );

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.mind_world_authority_ability.envisioning",
                "ability.lotmcraft.mind_world_authority_ability.seal_mind_world",
                "ability.lotmcraft.mind_world_authority_ability.split_characteristics"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        switch (selectedAbility){
            case 0 -> envisioning(level, entity);
            case 1 -> sealMindWorld(level, entity);
            case 2 -> split(level, entity);
        }
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 0));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1000;
    }

    private void split(Level level, LivingEntity entity){
        if(!(level instanceof ServerLevel serverLevel)) return;

        var component = entity.getData(ModAttachments.ENVISION_SPLIT.get());

        var target = AbilityUtil.getTargetEntity(entity, 30, 1f, true);
        if(target == null){
            StringBuilder resultBuilder = new StringBuilder("List of granted players:");
            for(var obj : component.names){
                resultBuilder.append("\n").append(obj);
            }
            var result = resultBuilder.toString();

            entity.sendSystemMessage(Component.literal("\n\n" + result + "\nAmount of granted avatars: "
                            + component.avatars.size())
                    .withColor(0xf5c56c));

            return;
        }

        if(target instanceof AvatarEntity avatar){
            if(avatar.getOriginalOwner().equals(entity.getUUID())){
                grantSeq(serverLevel, entity, target);
            }
        }
        else if(target instanceof ServerPlayer){
            grantSeq(serverLevel, entity, target);
        }
        else{
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.frenzy.failed").withColor(0xFFff124d));
        }
    }

    private void envisioning(Level level, LivingEntity player){
        if(level.isClientSide) return;

        if(envisioningToggle == null)
            envisioningToggle = (MindWorldAuthorityEnvisioningAbility) LOTMCraft.abilityHandler.getById("mind_world_authority_envisioning_ability");

        if(envisioningToggle == null) return;

        envisioningToggle.useAbility((ServerLevel) level, player);
    }

    private void sealMindWorld(Level level, LivingEntity entity){
        if(!(level instanceof ServerLevel serverLevel)) return;

        VisionaryHandler.setMindWorldSeal(!VisionaryHandler.isMindWorldSealed());

        if(VisionaryHandler.isMindWorldSealed()){
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("ability.lotmcraft.mind_world_authority_ability.is_sealed")
                            .withColor(0xFFff124d));
        }else{
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("ability.lotmcraft.mind_world_authority_ability.is_unsealed")
                            .withColor(0xFFff124d));
        }

        var loc = entity.position();

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if(!BeyonderData.getPathway(player).equals("visionary")) continue;

            player.level().playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.ENDER_DRAGON_GROWL,
                    SoundSource.MASTER,
                    1.5F,
                    0.6F
            );
            player.level().playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.ENDER_DRAGON_GROWL,
                    SoundSource.MASTER,
                    1.0F,
                    1.0F
            );
            player.level().playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.ENDER_DRAGON_GROWL,
                    SoundSource.MASTER,
                    0.7F,
                    1.5F
            );
        }

        ParticleOptions particleType = new DustParticleOptions(new Vector3f(131 / 255f, 225 / 255f, 235 / 255f), 2f);
        ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.DRAGON_BREATH, loc.add(0, .5, 0), 5, 450, .05);

        AtomicInteger radius = new AtomicInteger(1);
        ServerScheduler.scheduleForDuration(0, 1, 80, () -> {
            ParticleUtil.spawnParticles(serverLevel, particleType, loc.add(0, 5, 0), 30, .2, 6, .2, 0);
            ParticleUtil.spawnCircleParticles(serverLevel, dust, loc, radius.get(), radius.getAndAdd(1) * 20);
        });
    }

    public void grantSeq(ServerLevel serverLevel, LivingEntity entity, LivingEntity target){
        int seq1Amount = serverLevel.getGameRules().getInt(ModGameRules.SEQ_1_AMOUNT);
        var component = entity.getData(ModAttachments.ENVISION_SPLIT.get());

        var targetComponent = target.getData(ModAttachments.ENVISION_SPLIT.get());

        boolean isFailed;
        if(target instanceof ServerPlayer targetPlayer)
            isFailed = BeyonderData.getSequence(target) != 2
                    || !BeyonderData.getPathway(target).equals("visionary")
                    || BeyonderData.getDigestionProgress(targetPlayer) != 1.0f;
        else
            isFailed = BeyonderData.getSequence(target) != 2
                    || !BeyonderData.getPathway(target).equals("visionary");

        if(!component.contains(target)) {
            if (component.willBeOutOfSlots(seq1Amount)) {
                AbilityUtil.sendActionBar(entity,
                        Component.translatable("ability.lotmcraft.mind_world_authority_ability.out_of_slots")
                                .withColor(0xFFff124d));
                return;
            }

            if(isFailed){
                AbilityUtil.sendActionBar(entity,
                        Component.translatable("ability.lotmcraft.mind_world_authority_ability.unworthy_candidate")
                                .withColor(0xFFff124d));
                return;
            }

            if(target instanceof ServerPlayer targetPlayer) {
                String targetName = targetPlayer.getName().getString();
                component.add(targetName);
            }
            else{
                component.addAsAvatar(target.getUUID());
            }

            targetComponent.setEnvisioned(true);

            BeyonderData.setBeyonder(target, "visionary", 1, true, false, true, false);
        }
        else{
            if(target instanceof ServerPlayer targetPlayer) {
                String targetName = targetPlayer.getName().getString();
                component.remove(targetName);
            }
            else{
                component.removeAsAvatar(target.getUUID());
            }

            target.kill();
        }

        ParticleUtil.spawnCircleParticles(serverLevel, dust, target.getEyePosition(), 2, 20);
        ParticleUtil.spawnCircleParticles(serverLevel, dust, target.getEyePosition(), new Vec3(0, 0, 1), 2, 20);
        ParticleUtil.spawnCircleParticles(serverLevel, dust, target.getEyePosition(), new Vec3(1, 0, 0), 2, 20);
        ParticleUtil.createParticleSpirals(serverLevel, dust, target.position(), entity.getBbWidth() + .25, entity.getBbWidth() + .25, entity.getEyeHeight(), 1, 5, 30, 150, 1);
        ParticleUtil.createParticleSpirals(serverLevel, dust, target.position(), 5, entity.getBbWidth() + .25, 5, 1, 5, 20, 150, 1);
        serverLevel.playSound(
                null,
                target.blockPosition(),
                SoundEvents.DRAGON_FIREBALL_EXPLODE,
                SoundSource.MASTER,
                1.0F,
                1.5F
        );
        serverLevel.playSound(
                null,
                target.blockPosition(),
                SoundEvents.ENDER_DRAGON_GROWL,
                SoundSource.MASTER,
                1.0F,
                1.2F
        );
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if(!(player.level() instanceof ServerLevel serverLevel)) return;

            var component = player.getData(ModAttachments.ENVISION_SPLIT.get());
            if(component.isEnvisioned()) return;

            LOTMCraft.LOGGER.info("in event");

            if(!component.avatars.isEmpty()) {
                var id = component.avatars.removeFirst();
                var target = serverLevel.getEntity(id);

                LOTMCraft.LOGGER.info("in event, id:{}", id);

                while (target == null && !component.avatars.isEmpty()) {
                    id = component.avatars.removeFirst();
                    target = serverLevel.getEntity(id);
                }

                LOTMCraft.LOGGER.info("after loop");

                if (target == null && component.avatars.isEmpty()) return;

                if (!(target.level() instanceof ServerLevel targetLevel)) return;

                Vec3 pos = target.position();
                player.teleportTo(targetLevel, pos.x, pos.y, pos.z, target.getYRot(), target.getXRot());
                target.kill();

                player.setHealth(player.getMaxHealth());
                event.setCanceled(true);

                return;
            }

            if(component.names.isEmpty()) return;

            String name = component.names.removeFirst();
            var id = BeyonderData.playerMap.getKeyByName(name);

            var target = serverLevel.getPlayerByUUID(id);
            if(target == null){
                var data = BeyonderData.playerMap.get(id).get();

                var pos = data.lastPosition();

                player.teleportTo(pos.x, pos.y, pos.z);
                BeyonderData.playerMap.put(id, StoredData.builder.copyFrom(data).sequence(2).modified(true).build());
            }
            else{
                if(!(target.level() instanceof ServerLevel targetLevel)) return;

                Vec3 pos = target.position();
                player.teleportTo(targetLevel, pos.x, pos.y, pos.z,target.getYRot(), target.getXRot());
                target.kill();
            }

            player.setHealth(player.getMaxHealth());
            event.setCanceled(true);
        }
    }
}

package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.gamerule.ModGameRules;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.playerMap.StoredData;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.client.multiplayer.ClientLevel;
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
import net.minecraft.world.entity.player.Player;
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

            entity.sendSystemMessage(Component.literal("\n\n" + result)
                    .withColor(0xf5c56c));

            return;
        }

        if(!(target instanceof ServerPlayer targetPlayer)){
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.frenzy.no_target").withColor(0xFFff124d));
            return;
        }

        int seq1Amount = serverLevel.getGameRules().getInt(ModGameRules.SEQ_1_AMOUNT);

        String targetName = targetPlayer.getName().getString();
        var targetComponent = targetPlayer.getData(ModAttachments.ENVISION_SPLIT.get());

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

        if(!component.contains(targetName)) {
            if (component.names.size() + 1 >= seq1Amount) {
                AbilityUtil.sendActionBar(entity,
                        Component.translatable("ability.lotmcraft.mind_world_authority_ability.out_of_slots")
                                .withColor(0xFFff124d));
                return;
            }

            if(BeyonderData.getSequence(targetPlayer) != 2
                    || !BeyonderData.getPathway(targetPlayer).equals("visionary")
                    || BeyonderData.getDigestionProgress(targetPlayer) != 1.0f){

                    AbilityUtil.sendActionBar(entity,
                            Component.translatable("ability.lotmcraft.mind_world_authority_ability.unworthy_candidate")
                                    .withColor(0xFFff124d));
                    return;
            }

            component.add(targetName);
            targetComponent.setEnvisioned(true);

            BeyonderData.setBeyonder(targetPlayer, "visionary", 1, true, false, true, false);
        }
        else{
            component.remove(targetName);
            target.kill();
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

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if(!(player.level() instanceof ServerLevel serverLevel)) return;

            var component = player.getData(ModAttachments.ENVISION_SPLIT.get());
            if(component.isEnvisioned())return;

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

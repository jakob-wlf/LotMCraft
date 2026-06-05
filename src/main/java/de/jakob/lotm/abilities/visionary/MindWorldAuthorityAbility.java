package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
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
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
                "ability.lotmcraft.mind_world_authority_ability.seal_mind_world"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        switch (selectedAbility){
            case 0 -> envisioning(level, entity);
            case 1 -> sealMindWorld(level, entity);
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

}

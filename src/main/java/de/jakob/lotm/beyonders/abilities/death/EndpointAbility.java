package de.jakob.lotm.beyonders.abilities.death;

import de.jakob.lotm.attachments.EndpointComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class EndpointAbility extends Ability {

    private static final int RANGE = 5;

    public EndpointAbility(String id) {
        super(id, 1f, "death");
        canBeCopied = false;
        canBeReplicated = false;
        cannotBeStolen = true;
        canBeUsedByNPC = false;
        canBeUsedInArtifact = false;
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("death", 0));
    }

    @Override
    protected float getSpiritualityCost() {
        return 40000;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity caster) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        int casterSeq = BeyonderData.getSequence(caster);

        if (InteractionHandler.isInteractionPossibleStrictlyHigher(new Location(caster.position(), serverLevel), "purification", casterSeq, -1)) {
            AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.endpoint.blocked").withColor(0x1a0d24));
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(caster, RANGE, 1.5f);
        if (target == null) {
            AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.endpoint.no_target").withColor(0x1a0d24));
            return;
        }

        EndpointComponent endpointComponent = target.getData(ModAttachments.ENDPOINT_COMPONENT);
        endpointComponent.apply(casterSeq);

        playEndpointEffects(serverLevel, target);
        AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.endpoint.applied").withColor(0x1a0d24));
    }

    private void playEndpointEffects(ServerLevel level, LivingEntity target) {
        Vec3 center = target.position().add(0, 1, 0);

        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.4f, 0.4f);
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 0.6f, 1.6f);

        level.sendParticles(ParticleTypes.SOUL, center.x, center.y, center.z, 100, 0.5, 0.8, 0.5, 0.1);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y, center.z, 80, 0.5, 0.7, 0.5, 0.2);
        level.sendParticles(ParticleTypes.SCULK_SOUL, center.x, center.y, center.z, 40, 0.4, 0.6, 0.4, 0.05);
        level.sendParticles(ParticleTypes.SMOKE, center.x, center.y, center.z, 30, 0.3, 0.5, 0.3, 0.02);
    }
}

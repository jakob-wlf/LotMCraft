package de.jakob.lotm.beyonders.abilities.death;

import de.jakob.lotm.attachments.DeathDecreeMarkComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.damage.ModDamageTypes;
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

public class DeathDecreeAbility extends Ability {

    private static final int RANGE = 3;
    public static final int STACKS_TO_KILL = 3;

    public DeathDecreeAbility(String id) {
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
        return new HashMap<>(Map.of("death", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 30000;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity caster) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (InteractionHandler.isInteractionPossibleStrictlyHigher(new Location(caster.position(), serverLevel), "purification", BeyonderData.getSequence(caster), -1)) {
            AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.death_decree.blocked").withColor(0x334f23));
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(caster, RANGE, 1.5f);
        if (target == null) {
            AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.death_decree.no_target").withColor(0x334f23));
            return;
        }

        Vec3 center = target.position().add(0, 1, 0);

        playDeathEffects(serverLevel, target, center);

        int targetSeq = BeyonderData.getSequence(target);
        int casterSeq = BeyonderData.getSequence(caster);
        int seqDiff = targetSeq - casterSeq;

        // Target stronger than caster, or only one sequence weaker: too tough to outright kill.
        if (seqDiff < 0 || seqDiff == 1) {
            target.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.BEYONDER_GENERIC, caster), 0.01f);
            ModDamageTypes.trueDamage(target, target.getMaxHealth() * 0.5f, serverLevel, caster);
            AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.death_decree.too_strong").withColor(0x334f23));
            return;
        }

        // Target at least 2 sequences weaker: instantly dies.
        if (seqDiff >= 2) {
            ModDamageTypes.trueDamage(target, target.getMaxHealth(), serverLevel, caster);
            return;
        }

        // Same sequence: stack the mark up to STACKS_TO_KILL.
        DeathDecreeMarkComponent markComponent = target.getData(ModAttachments.DEATH_DECREE_MARK);
        markComponent.addStack(casterSeq);

        if (markComponent.getStacks() >= STACKS_TO_KILL) {
            ModDamageTypes.trueDamage(target, target.getMaxHealth(), serverLevel, caster);
            return;
        }

        target.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.BEYONDER_GENERIC, caster), 0.01f);
        AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.death_decree.marked",
                markComponent.getStacks(), STACKS_TO_KILL).withColor(0x334f23));
    }

    private void playDeathEffects(ServerLevel level, LivingEntity target, Vec3 center) {
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 1.2f, 0.5f);
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0f, 0.4f);

        level.sendParticles(ParticleTypes.SOUL, center.x, center.y, center.z, 120, 0.5, 0.8, 0.5, 0.15);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y, center.z, 100, 0.6, 0.8, 0.6, 0.3);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, center.x, center.y, center.z, 40, 0.4, 0.6, 0.4, 0.05);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x, center.y, center.z, 25, 0.3, 0.5, 0.3, 0.02);
    }
}

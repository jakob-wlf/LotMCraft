package de.jakob.lotm.beyonders.abilities.death;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class SoulControlAbility extends Ability {

    private static final int RANGE = 20;
    private static final float BASE_KILL_CHANCE = 0.25f;
    private static final float KILL_CHANCE_STEP_PER_SEQUENCE = 0.25f;
    private static final float FAIL_HP_DRAIN = 0.20f;
    private static final float FAIL_SANITY_DRAIN = 0.40f;

    private static final DustParticleOptions SOUL_DUST = new DustParticleOptions(new Vector3f(0.30f, 0.0f, 0.55f), 1.4f);

    public SoulControlAbility(String id) {
        super(id, 45f, "death");
        canBeCopied = false;
        canBeReplicated = false;
        cannotBeStolen = true;
        canBeUsedInArtifact = false;
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("death", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 25000;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity caster) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        int casterSeq = AbilityUtil.getSeqWithArt(caster, this);

        if (InteractionHandler.isInteractionPossibleStrictlyHigher(new Location(caster.position(), serverLevel), "purification", casterSeq, -1)) {
            AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.soul_control.blocked").withColor(0xFF1a0d24));
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(caster, RANGE, 1.5f, true);
        if (target == null) {
            AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.soul_control.no_target").withColor(0xFF1a0d24));
            return;
        }

        int targetSeq = BeyonderData.getSequence(target);
        int seqDiff = targetSeq - casterSeq;

        if (seqDiff < 0) {
            AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.soul_control.too_strong").withColor(0xFF1a0d24));
            return;
        }

        if (seqDiff >= 2) {
            instantKill(serverLevel, caster, target);
            return;
        }

        float killChance = BASE_KILL_CHANCE + (seqDiff * KILL_CHANCE_STEP_PER_SEQUENCE);
        if (casterSeq == 0 && targetSeq == 0) {
            killChance = 0f;
        }

        if (random.nextFloat() < killChance) {
            instantKill(serverLevel, caster, target);
            return;
        }

        drainFail(serverLevel, caster, target);
    }

    private void instantKill(ServerLevel level, LivingEntity caster, LivingEntity target) {
        Vec3 center = target.position().add(0, 1, 0);

        ParticleUtil.spawnSphereParticles(level, ParticleTypes.SOUL, center, 2.0, 100, 0.35f);
        ParticleUtil.spawnSphereParticles(level, ParticleTypes.REVERSE_PORTAL, center, 2.5, 80, 0.2f);
        ParticleUtil.spawnCircleParticles(level, SOUL_DUST, target.position(), 2.5, 40);

        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 1.2f, 0.5f);

        target.hurt(ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, caster), 0.01f);
        ModDamageTypes.trueDamage(target, target.getMaxHealth(), level, caster);

        AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.soul_control.killed", target.getDisplayName()).withColor(0xFF1a0d24));
    }

    private void drainFail(ServerLevel level, LivingEntity caster, LivingEntity target) {
        Vec3 center = target.position().add(0, 1, 0);

        ParticleUtil.spawnSphereParticles(level, ParticleTypes.SOUL, center, 1.2, 50, 0.3f);
        ParticleUtil.spawnCircleParticles(level, SOUL_DUST, target.position(), 1.5, 24);

        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 1.0f, 0.6f);

        target.hurt(ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, caster), 0.01f);
        ModDamageTypes.trueDamage(target, target.getMaxHealth() * FAIL_HP_DRAIN, level, caster);

        SanityComponent sanityComponent = target.getData(ModAttachments.SANITY_COMPONENT);
        sanityComponent.decreaseSanityAndSync(FAIL_SANITY_DRAIN, target);

        AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.soul_control.resisted", target.getDisplayName()).withColor(0xFF1a0d24));
        AbilityUtil.sendActionBar(target, Component.translatable("ability.lotmcraft.soul_control.you_resisted").withColor(0xFF1a0d24));
    }
}

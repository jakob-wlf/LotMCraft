package de.jakob.lotm.beyonders.abilities.death;

import de.jakob.lotm.attachments.DisabledAbilitiesComponent;
import de.jakob.lotm.attachments.EnslavementComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AllyUtil;
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
import java.util.UUID;

public class EnslavementAbility extends SelectableAbility {

    private static final int RANGE = 20;

    private static final DustParticleOptions CHAIN_DUST = new DustParticleOptions(new Vector3f(0.35f, 0.0f, 0.05f), 1.6f);

    public EnslavementAbility(String id) {
        super(id, 30f);
        canBeCopied = false;
        canBeReplicated = false;
        cannotBeStolen = true;
        canBeUsedInArtifact = false;
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("death", 2));
    }

    @Override
    protected float getSpiritualityCost() {
        return 6000;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.enslavement.enslave",
                "ability.lotmcraft.enslavement.seal_abilities",
                "ability.lotmcraft.enslavement.kill",
                "ability.lotmcraft.enslavement.release"
        };
    }

    public static boolean isEnslavedBy(LivingEntity victim, LivingEntity master) {
        if (victim == null || master == null) return false;
        UUID masterUUID = victim.getData(ModAttachments.ENSLAVEMENT_COMPONENT).getMaster();
        return masterUUID != null && masterUUID.equals(master.getUUID());
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (InteractionHandler.isInteractionPossibleStrictlyHigher(new Location(entity.position(), serverLevel), "purification", BeyonderData.getSequence(entity), -1)) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, RANGE, 1.5f, true);
        if (target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.enslavement.no_target").withColor(0xFF3d0a0a));
            return;
        }

        switch (abilityIndex) {
            case 0 -> enslave(serverLevel, entity, target);
            case 1 -> sealAbilities(serverLevel, entity, target);
            case 2 -> kill(serverLevel, entity, target);
            case 3 -> release(serverLevel, entity, target);
        }
    }

    private void enslave(ServerLevel level, LivingEntity caster, LivingEntity target) {
        EnslavementComponent casterComponent = caster.getData(ModAttachments.ENSLAVEMENT_COMPONENT);
        if (casterComponent.getSlave() != null) {
            AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.enslavement.already_have_slave").withColor(0xFF3d0a0a));
            return;
        }

        int casterSeq = BeyonderData.getSequence(caster);
        boolean anyPathway = casterSeq <= 1;

        if (!anyPathway && (!BeyonderData.isBeyonder(target) || !BeyonderData.getPathway(target).equals("death"))) {
            AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.enslavement.not_death").withColor(0xFF3d0a0a));
            return;
        }

        if (AbilityUtil.getSequenceDifference(caster, target) <= 0) {
            AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.enslavement.too_strong").withColor(0xFF3d0a0a));
            return;
        }

        casterComponent.setSlave(target.getUUID());

        EnslavementComponent targetComponent = target.getData(ModAttachments.ENSLAVEMENT_COMPONENT);
        targetComponent.setMaster(caster.getUUID());

        Vec3 center = target.position().add(0, 1, 0);
        Vec3 feet = target.position();

        ParticleUtil.spawnSphereParticles(level, ParticleTypes.SOUL, center, 1.2, 60, 0.3f);
        ParticleUtil.spawnCircleParticles(level, CHAIN_DUST, feet, 2.0, 32);
        ParticleUtil.spawnCircleParticles(level, CHAIN_DUST, feet.add(0, 1.5, 0), 1.4, 24);
        ParticleUtil.createParticleSpirals(level, CHAIN_DUST, center, 0.3, 1.8, 4.0, 1.5, 2.0, 40, 3, 6);
        ParticleUtil.drawParticleLine(level, CHAIN_DUST, caster.position().add(0, 1.2, 0), center, 0.25, 1);

        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 1.2f, 0.5f);
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 1.0f, 0.6f);

        AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.enslavement.enslaved", target.getDisplayName()).withColor(0xFF3d0a0a));
        AbilityUtil.sendActionBar(target, Component.translatable("ability.lotmcraft.enslavement.you_are_enslaved", caster.getDisplayName()).withColor(0xFF3d0a0a));
    }

    private void sealAbilities(ServerLevel level, LivingEntity caster, LivingEntity target) {
        if (!isEnslavedBy(target, caster)) {
            AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.enslavement.not_enslaved").withColor(0xFF3d0a0a));
            return;
        }

        target.getData(ModAttachments.ENSLAVEMENT_COMPONENT).setSealed(true);

        DisabledAbilitiesComponent component = target.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
        component.disableAbilityUsage("enslaved");

        Vec3 center = target.position().add(0, 1, 0);
        ParticleUtil.spawnSphereParticles(level, ParticleTypes.SOUL, center, 1.5, 70, 0.25f);
        ParticleUtil.spawnCircleParticles(level, CHAIN_DUST, center, 1.6, 30);
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 0.6f);

        AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.enslavement.sealed", target.getDisplayName()).withColor(0xFF3d0a0a));
        AbilityUtil.sendActionBar(target, Component.translatable("ability.lotmcraft.enslavement.you_are_sealed").withColor(0xFF3d0a0a));
    }

    private void kill(ServerLevel level, LivingEntity caster, LivingEntity target) {
        if (!isEnslavedBy(target, caster)) {
            AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.enslavement.not_enslaved").withColor(0xFF3d0a0a));
            return;
        }

        Vec3 center = target.position().add(0, 1, 0);
        ParticleUtil.spawnSphereParticles(level, ParticleTypes.SOUL, center, 2.0, 100, 0.35f);
        ParticleUtil.spawnSphereParticles(level, ParticleTypes.REVERSE_PORTAL, center, 2.5, 80, 0.2f);
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 1.2f, 0.5f);

        target.hurt(ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, caster), 0.01f);
        ModDamageTypes.trueDamage(target, target.getMaxHealth(), level, caster);

        clearEnslavement(caster, target);
    }

    private void release(ServerLevel level, LivingEntity caster, LivingEntity target) {
        if (!isEnslavedBy(target, caster)) {
            AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.enslavement.not_enslaved").withColor(0xFF3d0a0a));
            return;
        }

        clearEnslavement(caster, target);

        Vec3 center = target.position().add(0, 1, 0);
        ParticleUtil.spawnSphereParticles(level, ParticleTypes.SOUL, center, 1.2, 50, 0.3f);
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.CHAIN_BREAK, SoundSource.PLAYERS, 1.2f, 0.8f);

        AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.enslavement.released", target.getDisplayName()).withColor(0xFF3d0a0a));
        AbilityUtil.sendActionBar(target, Component.translatable("ability.lotmcraft.enslavement.you_are_released").withColor(0xFF3d0a0a));
    }

    private static void clearEnslavement(LivingEntity caster, LivingEntity target) {
        EnslavementComponent targetComponent = target.getData(ModAttachments.ENSLAVEMENT_COMPONENT);
        boolean wasSealed = targetComponent.isSealed();
        targetComponent.clearMaster();

        if (wasSealed) {
            DisabledAbilitiesComponent component = target.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
            component.enableAbilityUsage("enslaved");
        }

        caster.getData(ModAttachments.ENSLAVEMENT_COMPONENT).clearSlave();
    }

    public static boolean mayEnslavedDamage(LivingEntity source, LivingEntity target) {
        UUID masterUUID = source.getData(ModAttachments.ENSLAVEMENT_COMPONENT).getMaster();
        if (masterUUID == null) return true;

        if (masterUUID.equals(target.getUUID())) return false;

        LivingEntity master = null;
        if (source.level() instanceof ServerLevel serverLevel) {
            master = (LivingEntity) serverLevel.getEntity(masterUUID);
        }
        if (master != null && AllyUtil.areAllies(master, target)) return false;

        return true;
    }
}

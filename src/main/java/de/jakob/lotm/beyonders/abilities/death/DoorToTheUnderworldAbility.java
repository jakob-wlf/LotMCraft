package de.jakob.lotm.beyonders.abilities.death;

import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.entity.custom.ability_entities.death_pathway.UnderworldGateEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.VectorUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class DoorToTheUnderworldAbility extends SelectableAbility {

    private static final int MODE_SPIRITS = 0;
    private static final int MODE_TENTACLES = 1;
    private static final int MODE_RELEASE = 2;
    private static final int MODE_UNDERWORLD_GATE = 3;

    private static final String[] MODES = {
            "ability.lotmcraft.door_to_the_underworld.spirits",
            "ability.lotmcraft.door_to_the_underworld.tentacles",
            "ability.lotmcraft.door_to_the_underworld.release",
            "ability.lotmcraft.door_to_the_underworld.underworld_gate"
    };

    public DoorToTheUnderworldAbility(String id) {
        super(id, 10f, "death");
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("death", 5));
    }

    @Override
    protected float getSpiritualityCost() {
        return 600;
    }

    @Override
    protected String[] getAbilityNames() {
        return MODES;
    }

    @Override
    public void useAbility(ServerLevel serverLevel, LivingEntity entity, boolean consumeSpirituality, boolean hasToHaveAbility, boolean hasToMeetRequirements, boolean isCopied) {
        if (getSelectedAbilityIndex(entity.getUUID()) == MODE_RELEASE) {
            onAbilityUse(serverLevel, entity);
            return;
        }
        super.useAbility(serverLevel, entity, consumeSpirituality, hasToHaveAbility, hasToMeetRequirements, isCopied);
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!(entity instanceof ServerPlayer)) selectedAbility = MODE_TENTACLES;

        switch (selectedAbility) {
            case MODE_SPIRITS -> {
                if (isPurified(serverLevel, entity)) return;
                openSpiritGate(serverLevel, (ServerPlayer) entity);
            }
            case MODE_TENTACLES -> {
                if (isPurified(serverLevel, entity)) return;
                openTentacleGate(serverLevel, entity);
            }
            case MODE_RELEASE -> release((ServerPlayer) entity);
            case MODE_UNDERWORLD_GATE -> openUnderworldGate(serverLevel, entity);
        }
    }

    private boolean isPurified(ServerLevel level, LivingEntity entity) {
        return InteractionHandler.isInteractionPossibleStrictlyHigher(
                new Location(entity.position(), level), "purification", BeyonderData.getSequence(entity), -1);
    }

    private void openSpiritGate(ServerLevel level, ServerPlayer player) {
        if (UnderworldGateEntity.closeOpenGate(player.getUUID())) return;

        Vec3 center = AbilityUtil.getTargetLocation(player, 6, 1);
        LivingEntity lookAt = AbilityUtil.getTargetEntity(player, 6, 1);
        if (lookAt == null) lookAt = player;

        UnderworldGateEntity.open(level, player, UnderworldGateEntity.Mode.SPIRITS, center, lookAt.position());
    }

    private void openTentacleGate(ServerLevel level, LivingEntity caster) {
        if (UnderworldGateEntity.closeOpenGate(caster.getUUID())) return;

        Vec3 center = AbilityUtil.getTargetLocation(caster, 6, 1);
        LivingEntity lookAt = AbilityUtil.getTargetEntity(caster, 8, 2);
        if (lookAt != null) {
            center = VectorUtil.getRelativePosition(center, VectorUtil.getPerpendicularVector(caster.getLookAngle()), -4, 0, 0);
        } else {
            lookAt = caster;
        }

        UnderworldGateEntity gate = UnderworldGateEntity.open(level, caster, UnderworldGateEntity.Mode.TENTACLES, center, lookAt.position());
        gate.setDamageFunction(owner -> DamageLookup.lookupDamage(5, .85) * multiplier(owner));
    }

    private void openUnderworldGate(ServerLevel level, LivingEntity caster) {
        if (UnderworldGateEntity.closeOpenGate(caster.getUUID())) return;

        Vec3 center = AbilityUtil.getTargetLocation(caster, 6, 1);
        UnderworldGateEntity.open(level, caster, UnderworldGateEntity.Mode.GATE, center, caster.position());
    }

    private void release(ServerPlayer player) {
        if (!UnderworldGateEntity.release(player.getUUID())) {
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.door_to_the_underworld.none_summoned").withColor(0xFF334f23));
            return;
        }
        AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.door_to_the_underworld.released").withColor(0xFF334f23));
    }
}
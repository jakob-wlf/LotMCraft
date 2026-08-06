package de.jakob.lotm.beyonders.abilities.tyrant;

import de.jakob.lotm.attachments.DisabledFlightComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.TransformationComponent;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.beyonders.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.entity.custom.ability_entities.tyrant_pathway.WindBladeEntity;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.AbilitySelectionPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class WindManipulationFlightAbility extends ToggleAbility {
    public WindManipulationFlightAbility(String id) {
        super(id);

        this.canBeUsedByNPC = false;

        this.shouldBeHidden = true;
        this.canBeCopied = false;
        this.cannotBeStolen = true;
        this.canBeReplicated = false;
        this.canBeUsedInArtifact = false;
        this.doesNotIncreaseDigestion = true;

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(150f, 100f, 75f, 20f, 15f));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("tyrant", 6));
    }

    @Override
    public float getSpiritualityCost() {
        return 30;
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            return;
        }

        // If disabled by cooldown, stop
        DisabledFlightComponent disabledFlightComponent = entity.getData(ModAttachments.FLIGHT_DISABLE_COMPONENT);
        if(disabledFlightComponent.getCooldownTicks() > 0) {
            cancel((ServerLevel) level, entity);
            return;
        }

        // Allow Flying
        if(entity instanceof Player player) {
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.getAbilities().setFlyingSpeed(.025f);
            player.onUpdateAbilities();
        }


        // Stop when overridden by another transformation
        TransformationComponent transformationComponent = entity.getData(ModAttachments.TRANSFORMATION_COMPONENT);
        int index = transformationComponent.getTransformationIndex();
        int mythical_index = TransformationComponent.TransformationType.MYTHICAL_CREATURE.getIndex();

        if (transformationComponent.isTransformed() && !(index ==  mythical_index) ) {
            cancel((ServerLevel) level, entity);
            return;
        }
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        DisabledFlightComponent disabledFlightComponent = entity.getData(ModAttachments.FLIGHT_DISABLE_COMPONENT);
        if(disabledFlightComponent.getCooldownTicks() > 0) {
            cancel((ServerLevel) level, entity);
            return;
        }
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if(entity instanceof Player player) {
            if(!player.isCreative()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
            }
            player.getAbilities().setFlyingSpeed(.05f);
            player.onUpdateAbilities();
        }

        ServerScheduler.scheduleForDuration(0, 1, 50, () -> {
            entity.resetFallDistance();
            entity.fallDistance = 0;
        });
    }
}

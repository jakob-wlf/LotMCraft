package de.jakob.lotm.entity.custom.goals;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.AbilityItemHandler;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.abilities.fool.passives.PuppeteeringEnhancementsAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.helper.marionettes.MarionetteComponent;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class MarionetteUseAbilityGoal extends TargetGoal {
    private final Mob marionette;
    private Player controller;

    public MarionetteUseAbilityGoal(Mob marionette) {
        super(marionette, false);
        this.marionette = marionette;
        this.setFlags(EnumSet.noneOf(Goal.Flag.class));
    }

    @Override
    public boolean canUse() {
        if (!getControllerAndCheckValid()) return false;

        MarionetteComponent component = marionette.getData(ModAttachments.MARIONETTE_COMPONENT.get());

        if (!component.isFollowMode() || marionette.getTarget() == null) return false;

        if(!component.shouldAttack()) return false;

        if(!((PuppeteeringEnhancementsAbility) PassiveAbilityHandler.PUPPETEERING_ENHANCEMENTS.get()).shouldApplyTo(controller)) return false;

        return true;
    }

    private List<AbilityItem> usableAbilities() {
        return AbilityItemHandler.ITEMS.getEntries().stream().filter(i -> i.get() instanceof AbilityItem).map(i -> (AbilityItem) i.get()).filter(a -> a.canUse(controller)).toList();
    }

    private final Random random = new Random();

    @Override
    public void tick() {
        if(random.nextInt(100) >= 40) {
            List<AbilityItem> abilityItems = usableAbilities();
            abilityItems.get(random.nextInt(abilityItems.size())).useAsNpcAbility(marionette.level(), marionette);
        }
    }


    private boolean getControllerAndCheckValid() {
        MarionetteComponent component = marionette.getData(ModAttachments.MARIONETTE_COMPONENT.get());
        if (!component.isMarionette()) return false;

        try {
            UUID controllerUUID = UUID.fromString(component.getControllerUUID());
            controller = marionette.level().getPlayerByUUID(controllerUUID);
        } catch (IllegalArgumentException e) {
            return false;
        }

        return controller != null && controller.isAlive();
    }
}
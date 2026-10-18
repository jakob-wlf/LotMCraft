package de.jakob.lotm.beyonders.abilities.death.handlers;

import de.jakob.lotm.entity.client.spirits.grim_reaper.GrimReaperAnimations;
import de.jakob.lotm.entity.custom.spirits.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.hoglin.Hoglin;

public class SpiritHandler {
    public static int getSequenceOfSpirit(Entity spirit) {
        if(
                spirit instanceof SpiritBubblesEntity
        ) return 10;
        else if (
                spirit instanceof SpiritDervishEntity
        ) return 9;
        else if (
                spirit instanceof SpiritMalmouthEntity ||
                spirit instanceof SpiritGhostEntity ||
                spirit instanceof RabbitOfKnowledgeEntity
        ) return 8;
        else if (
                spirit instanceof SpiritBlueWizardEntity ||
                spirit instanceof SpiritTranslucentWizardEntity
        ) return 6;
        else if (
                spirit instanceof AbscessedHandEntity
        ) return 5;
        else if (
                spirit instanceof SpiritGrimReaperEntity ||
                spirit instanceof HeadlessBrideEntity ||
                spirit instanceof SpiritBizarroBaneEntity
        ) return 4;
        else if(
                spirit instanceof SpiritBaneEntity
        ) return 3;
        return 10;
    }

    public static boolean isCapturableSpirit(Entity spirit) {
        return
                spirit instanceof SpiritBubblesEntity ||
                spirit instanceof SpiritDervishEntity ||
                spirit instanceof SpiritMalmouthEntity ||
                spirit instanceof SpiritGhostEntity ||
                spirit instanceof RabbitOfKnowledgeEntity ||
                spirit instanceof SpiritBlueWizardEntity ||
                spirit instanceof SpiritTranslucentWizardEntity ||
                spirit instanceof AbscessedHandEntity ||
                spirit instanceof SpiritGrimReaperEntity ||
                spirit instanceof HeadlessBrideEntity ||
                spirit instanceof SpiritBizarroBaneEntity ||
                spirit instanceof ZombieVillager ||
                spirit instanceof Husk ||
                spirit instanceof Drowned ||
                spirit instanceof ZombifiedPiglin ||
                spirit instanceof Skeleton ||
                spirit instanceof WitherSkeleton ||
                spirit instanceof Stray ||
                spirit instanceof Phantom ||
                spirit instanceof Vex ||
                spirit instanceof Hoglin;
    }
}

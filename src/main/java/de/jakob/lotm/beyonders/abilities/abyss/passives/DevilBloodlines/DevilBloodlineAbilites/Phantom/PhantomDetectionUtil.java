package de.jakob.lotm.beyonders.abilities.abyss.passives.DevilBloodlines.DevilBloodlineAbilites.Phantom;

import de.jakob.lotm.beyonders.abilities.abyss.passives.DevilBloodlines.DevilBloodlineAbilites.Shapeshifter.SpiritDisguiseUtil;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.world.entity.LivingEntity;

public final class PhantomDetectionUtil {

    private PhantomDetectionUtil() {}
    public static boolean isHiddenFromSpiritVision(LivingEntity viewer, LivingEntity target) {
        if (!BloodlinePhantomAbility.isWraith(target)) return false;
        // Special spirit vision always sees through the ghost
        if (SpiritDisguiseUtil.hasSpecialVision(viewer)) return false;
        return BeyonderData.getSequence(viewer) >= BeyonderData.getSequence(target)+1;
    }
}
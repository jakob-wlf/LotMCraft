package de.jakob.lotm.beyonders.abilities.abyss.passives.DevilBloodlines.DevilBloodlineAbilites.Shapeshifter;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public final class SpiritDisguiseUtil {

    public static final int UPGRADE_SEQUENCE = 5;
    public static final int SEE_THROUGH_MARGIN = 1;
    private static final List<String> SPECIAL_VISION_ABILITY_IDS = List.of(
            "spectating_ability",
            "cull_ability",
            "decryption_ability",
            "eye_of_death_ability"
    );
    private SpiritDisguiseUtil() {}
    public static boolean isUpgradedDisguise(LivingEntity target) {
        if (!(target instanceof ServerPlayer)) return false;
        CompoundTag data = target.getPersistentData();
        if (!data.getBoolean(BloodlineSkinshifterAbility.MASK_FORM_TAG)) return false;
        if (!data.getBoolean(BloodlineSkinshifterAbility.MASK_UPGRADED_TAG)) return false;
        return !target.getData(ModAttachments.SHAPE_SHIFT).getShape().isEmpty();
    }
    public static boolean hasSpecialVision(LivingEntity viewer) {
        for (String id : SPECIAL_VISION_ABILITY_IDS) {
            Ability ability = LOTMCraft.abilityHandler.getById(id);
            if (ability instanceof ToggleAbility toggle && toggle.isActiveForEntity(viewer)) {
                return true;
            }
        }
        return false;
    }
    public static boolean canSeeThroughDisguise(LivingEntity viewer, LivingEntity target) {
        if (!hasSpecialVision(viewer)) return false;
        return BeyonderData.getSequence(viewer) <= BeyonderData.getSequence(target) + SEE_THROUGH_MARGIN;
    }
    public static boolean isFooledByDisguise(LivingEntity viewer, LivingEntity target) {
        return isUpgradedDisguise(target) && !canSeeThroughDisguise(viewer, target);
    }
}
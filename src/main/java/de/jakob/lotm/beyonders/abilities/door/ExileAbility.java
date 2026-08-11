package de.jakob.lotm.beyonders.abilities.door;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ability_entities.door_pathway.ExileDoorsEntity;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ExileAbility extends Ability {
    public ExileAbility(String id) {
        super(id, 18, "sealing");
        canBeCopied = false;

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(5, 7, 8, 10, 13));

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(18000f, 6700f, 3750f, 2500f, 2340f));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 4));
    }

    @Override
    public float getSpiritualityCost() {
        return 4000;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 targetPos = AbilityUtil.getTargetLocation(entity, baseDistance, 2);

        ExileDoorsEntity door = new ExileDoorsEntity(ModEntities.EXILE_DOORS.get(), level, 20 * 5, entity, AbilityUtil.getSeqWithArt(entity, this), multiplier(entity));
        door.setPos(targetPos.x, targetPos.y, targetPos.z);
        level.addFreshEntity(door);

        level.playSound(null, BlockPos.containing(targetPos), SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 2.0f, 1.0f);
    }
}

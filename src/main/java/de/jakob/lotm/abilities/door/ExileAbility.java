package de.jakob.lotm.abilities.door;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ExileDoorsEntity;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class ExileAbility extends AbilityItem {
    public ExileAbility(Properties properties) {
        super(properties, 10);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 500;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        Vec3 targetPos = AbilityUtil.getTargetLocation(entity, 20, 2);

        ExileDoorsEntity door = new ExileDoorsEntity(ModEntities.EXILE_DOORS.get(), level, 20 * 20, entity);
        door.setPos(targetPos);
        level.addFreshEntity(door);

        level.playSound(null, BlockPos.containing(targetPos), SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 2.0f, 1.0f);
    }
}

package de.jakob.lotm.abilities.abyss;

import de.jakob.lotm.abilities.ToggleAbilityItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class DevilTransformationAbility extends ToggleAbilityItem {
    public DevilTransformationAbility(Properties properties) {
        super(properties);
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 6));
    }

    @Override
    protected void start(Level level, LivingEntity entity) {

    }

    @Override
    protected void tick(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        if(entity instanceof ServerPlayer player) {
            Component message = Component.translatable("lotm.not_implemented_yet").withStyle(ChatFormatting.RED);
            player.sendSystemMessage(message);
        }

        cancel((ServerLevel) level, entity);
    }

    @Override
    protected void stop(Level level, LivingEntity entity) {

    }
}

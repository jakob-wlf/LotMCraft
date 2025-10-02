package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.abilities.AbilityItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class TelepathyAbility extends AbilityItem {
    public TelepathyAbility(Properties properties) {
        super(properties, 1);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 8));
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        if(entity instanceof ServerPlayer player) {
            Component message = Component.translatable("lotm.not_implemented_yet").withStyle(ChatFormatting.RED);
            player.sendSystemMessage(message);
        }
    }
}

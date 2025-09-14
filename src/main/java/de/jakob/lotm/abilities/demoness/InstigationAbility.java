package de.jakob.lotm.abilities.demoness;

import de.jakob.lotm.abilities.AbilityItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class InstigationAbility extends AbilityItem {
    public InstigationAbility(Properties properties) {
        super(properties, 1);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("demoness", 8));
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

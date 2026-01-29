package de.jakob.lotm.abilities.fool;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.gui.custom.UsernameInput.UsernameInputScreen;
import de.jakob.lotm.util.helper.SkinChanger;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class ShapeShiftingAbility extends Ability {
    public static final HashMap<UUID, String> attemptingToChangeSkin = new HashMap<>();

    public ShapeShiftingAbility(String id) {
        super(id, 60);

        canBeCopied = false;
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("fool", 6));
    }

    @Override
    public float getSpiritualityCost() {
        return 100;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        if(entity instanceof ServerPlayer player) {
            Component message = Component.translatable("lotm.not_implemented_yet").withStyle(ChatFormatting.RED);
            player.sendSystemMessage(message);
        }

        if(true)
            return;

        if(level.isClientSide) {
            Minecraft.getInstance().setScreen(new UsernameInputScreen(entity));
            return;
        }

        if(attemptingToChangeSkin.containsKey(entity.getUUID()))
            return;

        if(!(entity instanceof ServerPlayer player))
            return;

        attemptingToChangeSkin.put(player.getUUID(), "None");

        AtomicBoolean shouldStop = new AtomicBoolean(false);

        ServerScheduler.scheduleUntil((ServerLevel) level, () -> {
            if(!attemptingToChangeSkin.containsKey(entity.getUUID())) {
                shouldStop.set(true);
                return;
            }

            if(!attemptingToChangeSkin.get(entity.getUUID()).equals("None")) {
                shouldStop.set(true);
                String username = attemptingToChangeSkin.get(entity.getUUID());
                SkinChanger.exampleUsageWithDebug(player, username);
                attemptingToChangeSkin.remove(entity.getUUID());
                return;
            }
        }, 5, () -> {
            attemptingToChangeSkin.remove(entity.getUUID());
        }, shouldStop);
    }

    public static void changeSkin(String username, LivingEntity entity) {
        if(!attemptingToChangeSkin.containsKey(entity.getUUID()))
            return;

        attemptingToChangeSkin.replace(entity.getUUID(), username);
    }
}

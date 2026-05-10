package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.lord_of_mysteries.LordOfMysteriesAbility;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.LordOfMysteriesUtil;
import de.jakob.lotm.util.helper.AbilityWheelHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public final class LordOfMysteriesAscensionEventHandler {
    private LordOfMysteriesAscensionEventHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide() || player.tickCount % 20 != 0) {
            return;
        }
        if (LordOfMysteriesUtil.isLordOfMysteries(player) || !LordOfMysteriesUtil.canAscendToLordOfMysteries(player)) {
            return;
        }

        BeyonderData.setBeyonder(player, LordOfMysteriesUtil.PATHWAY_ID, LordOfMysteriesUtil.SEQUENCE,
                true, false, false, true, true);
        AbilityWheelHelper.setAbilities(player, LordOfMysteriesAbility.getGrantedAbilityIds());

        player.sendSystemMessage(Component.translatable("lotmcraft.lord_of_mysteries.ascended")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.END_PORTAL_SPAWN, SoundSource.MASTER, 1.8f, 0.7f);
        player.serverLevel().getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("lotmcraft.lord_of_mysteries.broadcast", player.getDisplayName())
                        .withStyle(ChatFormatting.LIGHT_PURPLE),
                false
        );
    }
}

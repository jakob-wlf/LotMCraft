package de.jakob.lotm.item.custom;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Right-click this item to open the Characteristics Exchange selection screen.
 * The item is NOT consumed; it is a reusable access token.
 *
 * All client-only classes are accessed via {@link ClientProxy} so this class
 * remains safe to load on a dedicated server.
 */
public class CharacteristicsExchangeTabletItem extends Item {

    public CharacteristicsExchangeTabletItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        if (level.isClientSide()) {
            ClientProxy.openExchangeScreen();
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    /** Inner class — only referenced (and thus loaded) on the client side. */
    @net.neoforged.api.distmarker.OnlyIn(net.neoforged.api.distmarker.Dist.CLIENT)
    private static final class ClientProxy {
        static void openExchangeScreen() {
            net.minecraft.client.Minecraft.getInstance().setScreen(
                    new de.jakob.lotm.gui.custom.CharExchange.CharExchangeSelectScreen());
        }
    }
}

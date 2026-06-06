package de.jakob.lotm.item.custom;

import de.jakob.lotm.gui.custom.CharExchange.CharExchangeSelectScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;

/**
 * Right-click this item to open the Characteristics Exchange selection screen.
 * The item is NOT consumed; it is a reusable access token.
 */
public class CharacteristicsExchangeTabletItem extends Item {

    public CharacteristicsExchangeTabletItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        if (level.isClientSide() && FMLEnvironment.dist == Dist.CLIENT) {
            openExchangeScreen();
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    // Called only on the client dist — isolated here to avoid class-loading on server
    private static void openExchangeScreen() {
        Minecraft.getInstance().setScreen(new CharExchangeSelectScreen());
    }
}

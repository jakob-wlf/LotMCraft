package de.jakob.lotm.item.custom;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.sefirah.RiverOfEternalDarknessEventHandler;
import de.jakob.lotm.beyonders.sefirah.SefirahHandler;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.Set;

/**
 * A bottle of Drops of Eternal Darkness collected from the river.
 *
 * Hold right-click to drink (32 ticks, like a potion). When finished:
 *  - Allowed pathways → begin river accommodation ritual, returns sealed bottle
 *  - Other pathways   → strips beyonder status, returns sealed bottle
 */
public class EternalDarknessRiverWaterBottleItem extends Item {

    private static final Set<String> ALLOWED = Set.of("darkness", "death", "twilight_giant");

    public EternalDarknessRiverWaterBottleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer serverPlayer) {
            String pathway = BeyonderData.getPathway(serverPlayer);

            // River owner: drinking grants +0.5 digestion and full heal at the cost of half their sanity
            if (SefirahHandler.getClaimedSefirot(serverPlayer).equals("river_of_eternal_darkness")) {
                float currentDigestion = BeyonderData.getDigestionProgress(serverPlayer);
                BeyonderData.setDigestionProgress(serverPlayer,
                        Math.min(1.0f, currentDigestion + 0.5f));
                serverPlayer.heal(serverPlayer.getMaxHealth());
                serverPlayer.getData(ModAttachments.SANITY_COMPONENT)
                        .increaseSanityAndSync(-0.5f, serverPlayer);
                serverPlayer.sendSystemMessage(Component.literal(
                        "The River's essence flows through you — your being is renewed, but your mind dims."));
                stack.shrink(1);
                return stack;
            }

            if (ALLOWED.contains(pathway)) {
                RiverOfEternalDarknessEventHandler.beginAccommodationFromBottle(serverPlayer);
            } else {
                BeyonderData.clearBeyonderData(serverPlayer);
                serverPlayer.sendSystemMessage(Component.literal(
                        "§4The River's essence overwhelms your soul, washing away all beyonder power..."));
            }
        }

        // Consume the bottle entirely — the sealed bottle is returned only if the ritual is cancelled/interrupted
        stack.shrink(1);
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32; // same as a potion
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }
}

package de.jakob.lotm.abilities.visionary.handlers.mixin;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.visionary.MindWorldAuthorityEnvisioningAbility;
import de.jakob.lotm.potions.BeyonderCharacteristicItem;
import de.jakob.lotm.potions.BeyonderPotion;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class CreativeSlotMixin {

    @Inject(
            method = "handleSetCreativeModeSlot",
            at = @At("HEAD"),
            cancellable = true
    )
    private void lotm$creativeSlot(ServerboundSetCreativeModeSlotPacket packet, CallbackInfo ci) {

        ServerGamePacketListenerImpl handler = (ServerGamePacketListenerImpl)(Object)this;
        ServerPlayer player = handler.player;

        if (!MindWorldAuthorityEnvisioningAbility.active.contains(player.getUUID())) {
            return;
        }

        int slot = packet.slotNum();
        ItemStack stack = packet.itemStack();

        if(stack.getItem() instanceof BeyonderCharacteristicItem bchar){
            if(bchar.getSequence() <= 2) return;
        }
        else if(stack.getItem() instanceof BeyonderPotion potion){
            if(potion.getSequence() <= 2) return;
        }

        if (slot < 0) {
            player.containerMenu.setCarried(stack.copy());
            player.inventoryMenu.broadcastChanges();
            player.inventoryMenu.sendAllDataToRemote();
            ci.cancel();
            return;
        }

        if (slot >= 36 && slot <= 44) {
            int hotbarIndex = slot - 36;

            player.getInventory().items.set(hotbarIndex, stack.copy());
            player.getInventory().setChanged();

            // force selected slot sync
            player.getInventory().selected = hotbarIndex;

            player.inventoryMenu.broadcastChanges();

            ci.cancel();
            return;
        }

        if (slot >= 0 && slot < player.getInventory().items.size()) {
            player.getInventory().items.set(slot, stack.copy());
            player.getInventory().setChanged();
        }

        player.inventoryMenu.broadcastChanges();
        ci.cancel();
    }
}
package de.jakob.lotm.abilities.visionary.prophecy.actions.implementations;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionsHelper;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.implementations.ActionItemsContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class DropItemAction extends ActionBase {
    public DropItemAction(ActionContextBase context) {
        super(context);
    }

    @Override
    public void action(Level level, LivingEntity entity, UUID casterId) {
        if (entity instanceof ServerPlayer serverPlayer) {
            
            if(!(context instanceof ActionItemsContext itemsContext)) return;
            
            if(itemsContext.all) {
                serverPlayer.getInventory().dropAll();
            } else if (!itemsContext.stacksList.isEmpty()) {
                var inventory = serverPlayer.getInventory();

                for (int i = 0; i < inventory.getContainerSize(); i++) {
                    ItemStack invStack = inventory.getItem(i);

                    if (invStack.isEmpty()) continue;

                    for (ItemStack target : itemsContext.stacksList) {
                        
                        if (ItemStack.isSameItemSameComponents(invStack, target)) {
                            int removeCount = invStack.getCount();

                            ItemStack toDrop = invStack.copy();
                            toDrop.setCount(removeCount);

                            invStack.shrink(removeCount);

                            serverPlayer.drop(toDrop, false);
                        }
                    }
                }
            }
        }
    }

    @Override
    public ActionsEnum getType() {
        return ActionsEnum.DROP_ITEM;
    }

    @Override
    public int getRequiredSeq() {
        return 7;
    }

    public static DropItemAction load(CompoundTag tag, HolderLookup.Provider provider) {
        return new DropItemAction(ActionContextBase.load(ActionsHelper.getContextType(ActionsEnum.DROP_ITEM), tag, provider));
    }
}

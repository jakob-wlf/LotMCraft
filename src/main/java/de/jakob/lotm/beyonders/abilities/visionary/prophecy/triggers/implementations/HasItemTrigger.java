package de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.implementations;

import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.TriggerBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.TriggerEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.TriggerContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.TriggerContextEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.triggers.context.implementations.TriggerItemsContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class HasItemTrigger extends TriggerBase {
    public HasItemTrigger(ActionBase action, TriggerContextBase context) {
        super(action, context);
    }

    @Override
    public TriggerEnum getType() {
        return TriggerEnum.PICK_UP;
    }

    @Override
    public int getRequiredSeq() {
        return 7;
    }

    @Override
    public int checkTrigger(Level level, LivingEntity entity, UUID casterId) {
        if(!(entity instanceof ServerPlayer player)) return -1;

        if(context instanceof TriggerItemsContext items){
            for(var obj : items.stacksList){
                if(player.getInventory().contains(obj)){
                    action.action(level, entity, casterId);
                    return 1;
                }
            }
        }

        return 0;
    }

    public static HasItemTrigger load(CompoundTag tag,
                                      ActionsEnum actionType,
                                      TriggerContextEnum contextType,
                                      HolderLookup.Provider provider){
        return new HasItemTrigger(ActionBase.load(actionType, tag, provider),
                TriggerContextBase.load(contextType, tag, provider));
    }
}

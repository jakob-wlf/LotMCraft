package de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.implementations;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class SuicideAction extends ActionBase {
    public SuicideAction(ActionContextBase context) {
        super(context);
    }

    @Override
    public ActionsEnum getType() {
        return ActionsEnum.SUICIDE;
    }

    @Override
    public int getRequiredSeq() {
        return 4;
    }

    @Override
    public void action(Level level, LivingEntity entity, UUID casterId) {
        var dataOp = BeyonderData.playerMap.get(casterId);
        if(dataOp.isEmpty()) return;

        var data = dataOp.get();
        int seq = data.sequence();
        int targetSeq = BeyonderData.getSequence(entity);

        int diff = targetSeq - seq;

        if(diff >= 2)
            entity.kill();
        else{
            var virtual = entity.getData(ModAttachments.VIRTUAL_PERSONAS.get());
            var affectedBy = virtual.getAffectedBy(seq);

            if(affectedBy.contains(data.trueName())){
                var sanity = entity.getData(ModAttachments.SANITY_COMPONENT.get());
                float damage = 0.005f;

                sanity.setSanityAndSync(sanity.getSanity() - damage, entity);
            }
        }
    }

    public static SuicideAction load(CompoundTag tag, HolderLookup.Provider provider) {
        return new SuicideAction(ActionContextBase.load(ActionContextEnum.EMPTY, tag, provider));
    }
}

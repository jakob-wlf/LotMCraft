package de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.implementations;

import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

public class AggroAction extends ActionBase {
    public AggroAction(ActionContextBase context) {
        super(context);
    }

    @Override
    public ActionsEnum getType() {
        return ActionsEnum.CONFUSION;
    }

    @Override
    public int getRequiredSeq() {
        return 4;
    }

    @Override
    public void action(Level level, LivingEntity entity, UUID casterId) {
        if(!(level instanceof ServerLevel serverLevel)) return;

        List<LivingEntity> nearby = AbilityUtil.getNearbyEntities(
                entity, serverLevel, entity.position(), 80, false, true);

        int casterSeq = BeyonderData.playerMap.get(casterId).get().sequence();

        for (LivingEntity nearby_entity : nearby) {
            if (nearby_entity.getUUID().equals(entity.getUUID())) continue;

            if(nearby_entity instanceof Mob mob) {
                if (BeyonderData.isBeyonder(mob) &&
                        BeyonderData.getSequence(mob) < casterSeq) continue;

                mob.setTarget(entity);
            }
        }
    }

    public static AggroAction load(CompoundTag tag, HolderLookup.Provider provider) {
            return new AggroAction(ActionContextBase.load(ActionContextEnum.EMPTY, tag, provider));
    }
}

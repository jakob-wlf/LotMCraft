package de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.implementations;

import de.jakob.lotm.beyonders.abilities.visionary.prophecy.TokenStream;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.implementations.ActionStringContext;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ability_entities.TimeChangeEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class TimeAction extends ActionBase {
    public TimeAction(ActionContextBase context) {
        super(context);
    }

    @Override
    public ActionsEnum getType() {
        return ActionsEnum.TIME;
    }

    @Override
    public int getRequiredSeq() {
        return 0;
    }

    @Override
    public void action(Level level, LivingEntity entity, UUID casterId) {
        if(!(context instanceof ActionStringContext string)) return;
        if(!(level instanceof ServerLevel serverLevel)) return;

        int time = 0;

        var stream = new TokenStream(string.string);

        switch (stream.peek()){
            case "day" -> time = 1000;
            case "noon" -> time = 6000;
            case "sunset" -> time = 12000;
            case "night" -> time = 13000;
            case "midnight" -> time = 18000;
            case "sunrise" -> time = 23000;
            case "stop" -> {
                ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.ENCHANT, entity.getEyePosition(), 400, 10, 2, 10, 0.05);
                ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.END_ROD, entity.getEyePosition(), 100, 10, 2, 10, 0.05);

                TimeChangeEntity timeChangeEntity = new TimeChangeEntity(ModEntities.TIME_CHANGE.get(), level, 20 * 15, null, 50 * Math.max((int) BeyonderData.getMultiplier(entity), 1), 0.001f);
                timeChangeEntity.setPos(entity.getX(), entity.getY(), entity.getZ());
                level.addFreshEntity(timeChangeEntity);
                return;
            }
        }

        serverLevel.setDayTime(time);
    }

    public static TimeAction load(CompoundTag tag, HolderLookup.Provider provider) {
        return new TimeAction(ActionContextBase.load(ActionContextEnum.STRING, tag, provider));
    }
}

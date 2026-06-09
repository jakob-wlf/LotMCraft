package de.jakob.lotm.abilities.visionary.prophecy.actions.implementations;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.visionary.prophecy.TokenStream;
import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.implementations.ActionStringContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class WeatherAction extends ActionBase {
    public WeatherAction(ActionContextBase context) {
        super(context);
    }

    @Override
    public ActionsEnum getType() {
        return ActionsEnum.WEATHER;
    }

    @Override
    public int getRequiredSeq() {
        return 0;
    }

    @Override
    public void action(Level level, LivingEntity entity, UUID casterId) {
        if(!(context instanceof ActionStringContext string)) return;
        if(!(level instanceof ServerLevel serverLevel)) return;

        int duration = 20 * 60 * 10;
        var stream = new TokenStream(string.string);

        switch (stream.peek()) {
            case "clear" -> {
                serverLevel.setWeatherParameters(duration, 0, false, false);
            }
            case "rain" -> {
                serverLevel.setWeatherParameters(0, duration, true, false);
            }
            case "thunder" -> {
                serverLevel.setWeatherParameters(0, duration, true, true);
            }
        }
    }

    public static WeatherAction load(CompoundTag tag, HolderLookup.Provider provider) {
        return new WeatherAction(ActionContextBase.load(ActionContextEnum.STRING, tag, provider));
    }
}

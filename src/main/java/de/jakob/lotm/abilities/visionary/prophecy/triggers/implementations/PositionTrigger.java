package de.jakob.lotm.abilities.visionary.prophecy.triggers.implementations;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.abyss.LanguageOfFoulnessAbility;
import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.abilities.visionary.prophecy.triggers.TriggerBase;
import de.jakob.lotm.abilities.visionary.prophecy.triggers.TriggerEnum;
import de.jakob.lotm.abilities.visionary.prophecy.triggers.context.TriggerContextBase;
import de.jakob.lotm.abilities.visionary.prophecy.triggers.context.TriggerContextEnum;
import de.jakob.lotm.abilities.visionary.prophecy.triggers.context.implementations.TriggerPositionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class PositionTrigger extends TriggerBase {
    public PositionTrigger(ActionBase action, TriggerContextBase context) {
        super(action, context);
    }

    @Override
    public TriggerEnum getType() {
        return TriggerEnum.POSITION;
    }

    @Override
    public int getRequiredSeq() {
        return 7;
    }

    @Override
    public int checkTrigger(Level level, LivingEntity entity, UUID casterId) {
        if (!(context instanceof TriggerPositionContext position)) return -1;
        if (!(entity instanceof ServerPlayer serverPlayer)) return -1;

        ResourceLocation id = null;
        if(!position.dimension.isEmpty()){
            try {
                id = ResourceLocation.tryParse(position.dimension);
            }catch (NullPointerException ignored) {}
        }

        if(id != null) {
            ResourceKey<Level> dimension = ResourceKey.create(
                    Registries.DIMENSION,
                    id
            );

            if(serverPlayer.level().dimension() != dimension) return 0;
        }

        Vec3 pos = entity.position();
        Vec3 target = position.pos;

        double dist = pos.distanceTo(target);

        if (dist <= position.range) {
            action.action(level, entity, casterId);

            return 1;
        }

        return 0;
    }

    public static PositionTrigger load(CompoundTag tag,
                                       ActionsEnum actionType,
                                       TriggerContextEnum contextType,
                                       HolderLookup.Provider provider){
        return new PositionTrigger(ActionBase.load(actionType, tag, provider),
                TriggerContextBase.load(contextType, tag, provider));
    }
}

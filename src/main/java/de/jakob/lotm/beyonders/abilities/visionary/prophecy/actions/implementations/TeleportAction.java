package de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.implementations;

import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.actions.context.implementations.ActionPositionContext;
import de.jakob.lotm.util.helper.TeleportationUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class TeleportAction extends ActionBase {
    public TeleportAction(ActionContextBase context) {
        super(context);
    }

    @Override
    public ActionsEnum getType() {
        return ActionsEnum.TELEPORT;
    }

    @Override
    public int getRequiredSeq() {
        return 0;
    }

    @Override
    public void action(Level level, LivingEntity entity, UUID casterId) {
        if (!(entity instanceof ServerPlayer serverPlayer)) return;

        if(!(context instanceof ActionPositionContext position)) return;

        ResourceLocation id = null;
        if(!position.dimension.isEmpty()){
            try {
                id = ResourceLocation.tryParse(position.dimension);
            }catch (NullPointerException ignored) {}
        }

        if(id != null) {
            ServerLevel target = null;

            try {
               target = serverPlayer.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, id));
            }catch (NullPointerException ignored) {}

            if(target != null){
                var validated = TeleportationUtil.clampToBorder(target, position.pos);
                serverPlayer.teleportTo(target, validated.x, validated.y, validated.z, serverPlayer.getYRot(), serverPlayer.getXRot());
                return;
            }
        }

        var validated = TeleportationUtil.clampToBorder((ServerLevel) serverPlayer.level(), position.pos);
        entity.teleportTo(validated.x, validated.y, validated.z);
    }

    public static TeleportAction load(CompoundTag tag, HolderLookup.Provider provider) {
        return new TeleportAction(ActionContextBase.load(ActionContextEnum.POSITION, tag, provider));
    }
}

package de.jakob.lotm.abilities.visionary.prophecy.actions.implementations;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.visionary.prophecy.TokenStream;
import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionBase;
import de.jakob.lotm.abilities.visionary.prophecy.actions.ActionsEnum;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.ActionContextBase;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.ActionContextEnum;
import de.jakob.lotm.abilities.visionary.prophecy.actions.context.implementations.ActionStringContext;
import de.jakob.lotm.rendering.TelepathyOverlayRenderer;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class PlayerAction extends ActionBase {
    public PlayerAction(ActionContextBase context) {
        super(context);
    }

    @Override
    public ActionsEnum getType() {
        return ActionsEnum.PLAYER;
    }

    @Override
    public int getRequiredSeq() {
        return 4;
    }

    @Override
    public void action(Level level, LivingEntity entity, UUID casterId) {
        if(!(context instanceof ActionStringContext string)) return;
        if(!(level instanceof ServerLevel serverLevel)) return;
        if(!(entity instanceof ServerPlayer player)) return;

        var stream = new TokenStream(string.string);

        var id = BeyonderData.playerMap.getKeyByName(stream.peek());
        if(id == null) return;

        var target = serverLevel.getPlayerByUUID(id);

        Vec3 targetPos;

        if(target == null){
            targetPos = BeyonderData.playerMap.get(id).get().lastPosition();
        }
        else{
            targetPos = target.position();
        }

        int seq = BeyonderData.playerMap.get(casterId).get().sequence();
        if(player.position().distanceTo(targetPos) > getDistance(seq)) return;

        player.lookAt(
                EntityAnchorArgument.Anchor.EYES,
                targetPos
        );
    }

    public static PlayerAction load(CompoundTag tag, HolderLookup.Provider provider) {
        return new PlayerAction(ActionContextBase.load(ActionContextEnum.STRING, tag, provider));
    }

    private static int getDistance(int seq){
        return switch (seq){
            case 4 -> 1000;
            case 3 -> 2500;
            case 2 -> 6000;
            case 1 -> 10000;
            case 0 -> 20000;
            default -> 0;
        };
    }
}

package de.jakob.lotm.abilities.fool;

import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.attachments.MemorisedEntities;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.network.packets.toClient.OpenShapeShiftingScreenPacket;
import de.jakob.lotm.network.packets.toClient.ShapeShiftingSyncPacket;
import de.jakob.lotm.util.shapeShifting.DimensionsRefresher;
import de.jakob.lotm.util.shapeShifting.NameUtils;
import de.jakob.lotm.util.shapeShifting.TransformData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShapeShiftingAbility extends SelectableAbility {
    public ShapeShiftingAbility(String id) {
        super(id, 5);
        canBeCopied = false;
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("fool", 6));
    }

    @Override
    public float getSpiritualityCost() {
        return 100;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.shape_shifting_ability.change_shape",
                "ability.lotmcraft.shape_shifting_ability.reset_shape"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (!(entity instanceof ServerPlayer player)) return;
        switch (abilityIndex){
            case 0 -> changeShape(player);
            case 1 -> resetShape(player);
        }
    }

    public void changeShape(ServerPlayer player) {
        MemorisedEntities memorisedEntities = player.getData(ModAttachments.MEMORISED_ENTITIES);
        List<String> entityTypes = memorisedEntities.getMemorisedEntityTypes();
        PacketDistributor.sendToPlayer(
                player,
                new OpenShapeShiftingScreenPacket(entityTypes)
        );
    }

    public void resetShape(ServerPlayer player) {
        TransformData data = (TransformData) player;
        data.setCurrentShape(null);
        ((DimensionsRefresher) player).shape_refreshDimensions();
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new ShapeShiftingSyncPacket(player.getUUID(), null));
        NameUtils.resetPlayerName(player);

        if (player.isCreative()) return;
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.onUpdateAbilities();
    }

//    @Override
//    public void onAbilityUse(Level level, LivingEntity entity) {
//        if(level.isClientSide)
//            return;
//
//        if(entity instanceof ServerPlayer player) {
//            Component message = Component.translatable("lotm.not_implemented_yet").withStyle(ChatFormatting.RED);
//            player.sendSystemMessage(message);
//        }
//        Minecraft.getInstance().setScreen(new UsernameInputScreen(entity));
//
//
//
//        if(attemptingToChangeSkin.containsKey(entity.getUUID()))
//            return;
//
//        if(!(entity instanceof ServerPlayer player))
//            return;
//
//        attemptingToChangeSkin.put(player.getUUID(), "None");
//
//        AtomicBoolean shouldStop = new AtomicBoolean(false);
//
//        ServerScheduler.scheduleUntil((ServerLevel) level, () -> {
//            if(!attemptingToChangeSkin.containsKey(entity.getUUID())) {
//                shouldStop.set(true);
//                return;
//            }
//
//            if(!attemptingToChangeSkin.get(entity.getUUID()).equals("None")) {
//                shouldStop.set(true);
//                String username = attemptingToChangeSkin.get(entity.getUUID());
//                SkinChanger.exampleUsageWithDebug(player, username);
//                attemptingToChangeSkin.remove(entity.getUUID());
//                return;
//            }
//        }, 5, () -> {
//            attemptingToChangeSkin.remove(entity.getUUID());
//        }, shouldStop);
//    }

}

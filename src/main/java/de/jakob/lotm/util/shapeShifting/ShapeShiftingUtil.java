package de.jakob.lotm.util.shapeShifting;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.ShapeShiftComponent;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.network.packets.toClient.ShapeShiftingSyncPacket;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ShapeShiftingUtil {

    public static void shapeShift(ServerPlayer player, Entity entity, boolean sequenceRestrict) {
        String entityType = getEntityTypeString(entity);
        shapeShift(player, entityType, sequenceRestrict);
    }

    public static void shapeShift(ServerPlayer player, String entityType, boolean sequenceRestrict) {
        if (!player.isCreative() && !player.isSpectator()) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }

        if (sequenceRestrict) {
            String entityName = entityType;
            entityName = entityName.contains(":") ? entityName.split(":")[1] : entityName;
            if (List.of("bat", "phantom", "blaze", "allay", "bee", "ghast", "parrot", "vex").contains(entityName)) {
                if (BeyonderData.getSequence(player) > 4) {
                    return;
                }
            }
        }
        ShapeShiftComponent data = player.getData(ModAttachments.SHAPE_SHIFT);
        data.setShape(entityType);
        data.setSkinOnly(false);

        player.refreshDimensions();

        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new ShapeShiftingSyncPacket(player.getUUID(), entityType));

        if (entityType.startsWith("player:")){
            data.setSkinOnly(true);
            String playerName = entityType.split(":")[1];
            NameUtils.setPlayerName(player, playerName);
        }else {
            String entityName = entityType;
            entityName = entityName.contains(":") ? entityName.split(":")[1] : entityName;

            if (List.of("bat", "phantom", "blaze", "allay", "bee", "ghast", "parrot", "vex").contains(entityName)) {
                player.getAbilities().mayfly = true;
                player.getAbilities().flying = true;
                player.onUpdateAbilities();
            }
            if (List.of("bat", "phantom", "allay", "bee", "parrot", "vex").contains(entityName)) {
                player.getAttribute(Attributes.SCALE).setBaseValue(2);
            }
        }

        // set attributes one time when shape shifting
        var stepHeightAttribute = player.getAttribute(Attributes.STEP_HEIGHT);
        var entityReachAttribute = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
        var blockReachAttribute = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);

        if (stepHeightAttribute != null) {
            stepHeightAttribute.removeModifier(ResourceLocation.fromNamespaceAndPath("lotmcraft", "shape_shifting_step_height"));
            AttributeModifier stepHeightModifier = new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath("lotmcraft", "shape_shifting_step_height"),
                    (1 - calculateScale(player)),
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );
            stepHeightAttribute.addTransientModifier(stepHeightModifier);
        }

        if (entityReachAttribute != null) {
            entityReachAttribute.removeModifier(ResourceLocation.fromNamespaceAndPath("lotmcraft", "shape_shifting_entity_reach"));
            AttributeModifier entityReachModifier = new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath("lotmcraft", "shape_shifting_entity_reach"),
                    (1 - calculateScale(player)),
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );
            entityReachAttribute.addTransientModifier(entityReachModifier);
        }

        if (blockReachAttribute != null) {
            blockReachAttribute.removeModifier(ResourceLocation.fromNamespaceAndPath("lotmcraft", "shape_shifting_block_reach"));
            AttributeModifier blockReachModifier = new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath("lotmcraft", "shape_shifting_block_reach"),
                    (1 - calculateScale(player)),
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );
            blockReachAttribute.addTransientModifier(blockReachModifier);
        }
    }

    public static void resetShape(ServerPlayer serverPlayer){
        ShapeShiftComponent data = serverPlayer.getData(ModAttachments.SHAPE_SHIFT);
        data.setShape("");
        data.setSkinOnly(false);
        serverPlayer.refreshDimensions();

        PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverPlayer,
                new ShapeShiftingSyncPacket(serverPlayer.getUUID(), ""));

        NameUtils.resetPlayerName(serverPlayer);
        if (!serverPlayer.isCreative() && !serverPlayer.isSpectator()) {
            serverPlayer.getAbilities().mayfly = false;
            serverPlayer.getAbilities().flying = false;
            serverPlayer.onUpdateAbilities();
        }
    }

    public static String getEntityTypeString(Entity entity) {
        if (entity instanceof ServerPlayer player) {
            return String.format("player:%s:%s", player.getGameProfile().getName(), player.getUUID());
        }
        if (entity instanceof BeyonderNPCEntity npc) {
            return "lotmcraft:beyonder_npc:" + npc.getSkinName();
        }
        return EntityType.getKey(entity.getType()).toString();
    }

    // sync shapes for tracked players
    @SubscribeEvent
    public static void onEntityTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof Player serverPlayer) {
            ShapeShiftComponent data = serverPlayer.getData(ModAttachments.SHAPE_SHIFT);
            String shape = data.getShape();
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverPlayer,
                    new ShapeShiftingSyncPacket(serverPlayer.getUUID(), shape));
        }
    }

    @SubscribeEvent
    public static void onEntityTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ShapeShiftComponent data = serverPlayer.getData(ModAttachments.SHAPE_SHIFT);
            String shape = data.getShape();
            boolean skinOnly = data.isSkinOnly();

            // set up attributes
            var stepHeightAttribute = serverPlayer.getAttribute(Attributes.STEP_HEIGHT);
            var entityReachAttribute = serverPlayer.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
            var blockReachAttribute = serverPlayer.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);

            // reset the modifiers if no shape or if player shape
            if (skinOnly || shape.isEmpty()) {
                if (stepHeightAttribute != null) {
                    stepHeightAttribute.removeModifier(ResourceLocation.fromNamespaceAndPath("lotmcraft", "shape_shifting_step_height"));
                }
                if (entityReachAttribute != null) {
                    entityReachAttribute.removeModifier(ResourceLocation.fromNamespaceAndPath("lotmcraft", "shape_shifting_entity_reach"));
                }
                if (blockReachAttribute != null) {
                    blockReachAttribute.removeModifier(ResourceLocation.fromNamespaceAndPath("lotmcraft", "shape_shifting_block_reach"));
                }
            }
        }
    }

    public static float calculateScale(Player player) {
        ShapeShiftComponent data = player.getData(ModAttachments.SHAPE_SHIFT);
        String shape = data.getShape();
        boolean onlySkin = data.isSkinOnly();
        if (!onlySkin) return 1.0f;

        // skip for players and beyonders because the same model
        if (shape != null && !shape.startsWith("player:") && !shape.startsWith("lotmcraft:beyonder_npc:")) {
            EntityType<?> type = null;

            ResourceLocation id = ResourceLocation.tryParse(shape);
            if (id != null) {
                type = BuiltInRegistries.ENTITY_TYPE.get(id);
            }

            if (type != null) {
                Entity entity = type.create(player.level());
                if (entity != null) {
                    return Math.max(entity.getBbHeight() / 2.0f, 1.0f);
                }
            }
        }
        return 1.0f;
    }
}

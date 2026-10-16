package de.jakob.lotm.dimension.abyss.Flith;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.dimension.abyss.fog.AbyssFogNetwork;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class LiquidFilthEffects {
    private static final int IMMUNE_SEQUENCE = 1;
    private static final double SINK_PER_TICK = 0.05;
    private static final double MAX_SINK_SPEED = 0.25;
    private static final double HORIZONTAL_DRAG = 0.92;
    private static final float SANITY_DAMAGE = 0.001f;
    private static final int SANITY_SOURCE_SEQUENCE = 0;

    public static boolean isImmune(LivingEntity entity) {
        String pathway = BeyonderData.getPathway(entity);
        if (!"abyss".equals(pathway)) return false;
        return BeyonderData.getSequence(entity) <= IMMUNE_SEQUENCE;
    }
    private static boolean inFilth(LivingEntity entity) {
        var type = ModFilth.LIQUID_FILTH_TYPE.get();
        // Check at the body's middle so standing at the surface still counts.
        BlockPos body = BlockPos.containing(entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ());
        return entity.level().getFluidState(body).getFluidType() == type;
    }
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (!inFilth(entity)) return;
        boolean clientSide = entity.level().isClientSide();
        if (clientSide) {
            if (!(entity instanceof Player player) || !player.isLocalPlayer()) return;
            if (player.isCreative() || player.isSpectator()) return;
            if (AbyssFogNetwork.clientFilthImmune) return;
            sink(entity);
            return;
        }
        if (entity instanceof ServerPlayer player) {
            if (player.isCreative() || player.isSpectator()) return;
            if (isImmune(player)) return;
            player.getData(ModAttachments.SANITY_COMPONENT).decreaseSanityWithSequenceDifference(SANITY_DAMAGE, player, SANITY_SOURCE_SEQUENCE, BeyonderData.getSequence(player));
            //corruption goes here once it exists.
            return;
        }
        // Mobs and other non-player creatures are moved by the server.
        if (isImmune(entity)) return;
        sink(entity);
    }
    private static void sink(LivingEntity entity) {
        Vec3 motion = entity.getDeltaMovement();
        double y = Math.max(motion.y - SINK_PER_TICK, -MAX_SINK_SPEED);
        entity.setDeltaMovement(motion.x * HORIZONTAL_DRAG, y, motion.z * HORIZONTAL_DRAG);
        entity.fallDistance = 0.0f;
    }
}

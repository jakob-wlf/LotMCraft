package de.jakob.lotm.abilities.demoness;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.attachments.MirrorWorldTraversalComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncMirrorWorldPacket;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class MirrorWorldTraversalAbility extends AbilityItem {
    public MirrorWorldTraversalAbility(Properties properties) {
        super(properties, 2);

        canBeCopied = false;
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("demoness", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 200;
    }


    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if(!(entity instanceof ServerPlayer player)) {
            return;
        }

        MirrorWorldTraversalComponent component = entity.getData(ModAttachments.MIRROR_WORLD_COMPONENT.get());
        if(!component.isInMirrorWorld()) {
            enterMirrorWorld(serverLevel, player, component);
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();

        if(!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        MirrorWorldTraversalComponent component = player.getData(ModAttachments.MIRROR_WORLD_COMPONENT.get());
        PacketHandler.sendToPlayer(serverPlayer, new SyncMirrorWorldPacket(component.isInMirrorWorld()));
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if(!(player instanceof ServerPlayer serverPlayer) || !(serverPlayer.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        MirrorWorldTraversalComponent component = player.getData(ModAttachments.MIRROR_WORLD_COMPONENT.get());
        if(!component.isInMirrorWorld()) {
            return;
        }

        if(component.isOnCooldown()) {
            return;
        }

        if(serverPlayer.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
            component.setInMirrorWorld(false);
            component.setPreviousGameModeIndex(0);
            PacketHandler.sendToPlayer(serverPlayer, new SyncMirrorWorldPacket(false));
            return;
        }

        BlockPos glassPos = getNearestGlassBlock(serverLevel, player.position(), 4);

        if (glassPos == null) {
            return;
        }

        Vec3 targetPos = glassPos.getCenter();
        serverPlayer.teleportTo(targetPos.x, targetPos.y + 1, targetPos.z);

        serverPlayer.setGameMode(component.getPreviousGameMode());

        component.setInMirrorWorld(false);
        component.setPreviousGameModeIndex(0);

        PacketHandler.sendToPlayer(serverPlayer, new SyncMirrorWorldPacket(false));
    }

    private void enterMirrorWorld(ServerLevel level, ServerPlayer player, MirrorWorldTraversalComponent component) {
        BlockPos glassPos = getNearestGlassBlock(level, player.position(), 20);
        if(glassPos == null) {
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotm.mirror_world_traversal.no_glass"));
            return;
        }

        component.setInMirrorWorld(true);
        component.setPreviousGameMode(player.gameMode.getGameModeForPlayer());
        component.setOnCooldown();

        PacketHandler.sendToPlayer(player, new SyncMirrorWorldPacket(true));

        player.setGameMode(GameType.SPECTATOR);
        player.hurtMarked = true;

        Vec3 targetPos = glassPos.getCenter();
        player.teleportTo(targetPos.x, targetPos.y, targetPos.z);
    }

    private static BlockPos getNearestGlassBlock(ServerLevel level, Vec3 pos, int searchRadius) {
        return AbilityUtil.getBlocksInSphereRadius(level, pos, searchRadius, true, true, false).stream().filter(b -> {
            BlockState state = level.getBlockState(b);
            return state.is(Tags.Blocks.GLASS_BLOCKS) || state.is(Tags.Blocks.GLASS_PANES);
        }).min((b1, b2) -> {
            double d1 = b1.distToCenterSqr(pos);
            double d2 = b2.distToCenterSqr(pos);
            return Double.compare(d1, d2);
        }).orElse(null);
    }
}

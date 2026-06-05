package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.gui.custom.Envisioning.EnvisioningScreen;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncEnvisioningPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class MindWorldAuthorityEnvisioningAbility extends ToggleAbility {
    public static Set<UUID> active = new HashSet<>();
    public static boolean canEnvisionClient = false;
    private static Vec3 pos;

    public MindWorldAuthorityEnvisioningAbility(String id) {
        super(id);

        canBeCopied = false;
        canBeReplicated = false;
        cannotBeStolen = true;
        canBeUsedByNPC = false;
        canBeUsedInArtifact = false;
        doesNotIncreaseDigestion = true;

        this.shouldBeHidden = true;
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (!(entity instanceof ServerPlayer serverPlayer)) return;

        Abilities abilities = serverPlayer.getAbilities();

        abilities.mayfly = true;
        abilities.flying = true;
        abilities.instabuild = true;
        serverPlayer.setNoGravity(true);

        serverPlayer.onUpdateAbilities();
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if (!(entity instanceof ServerPlayer serverPlayer)) return;

        active.add(entity.getUUID());

        Abilities abilities = serverPlayer.getAbilities();

        abilities.mayfly = true;
        abilities.flying = true;
        abilities.instabuild = true;
        serverPlayer.setNoGravity(true);

        serverPlayer.onUpdateAbilities();

        PacketHandler.sendToPlayer(serverPlayer, new SyncEnvisioningPacket(true));
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if (!(entity instanceof ServerPlayer serverPlayer)) return;

        active.remove(entity.getUUID());

        Abilities abilities = serverPlayer.getAbilities();

        abilities.mayfly = false;
        abilities.flying = false;
        abilities.instabuild = false;
        serverPlayer.setNoGravity(false);

        serverPlayer.onUpdateAbilities();

        PacketHandler.sendToPlayer(serverPlayer, new SyncEnvisioningPacket(false));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 0));
    }

    @Override
    protected float getSpiritualityCost() {
        return 10;
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();

        if (active.contains(player.getUUID())) {
            Level level = player.level();
            BlockPos pos = event.getPos();

            level.destroyBlock(pos, true);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || !canEnvisionClient) {
            return;
        }

        while (mc.options.keyInventory.consumeClick()) {
            mc.gameMode.setLocalMode(GameType.CREATIVE);

            mc.setScreen(new EnvisioningScreen(mc.player));
        }
    }

    @SubscribeEvent
    public static void onScreenClose(ScreenEvent.Closing event) {
        Screen screen = event.getScreen();

        if(screen instanceof EnvisioningScreen){
            Minecraft.getInstance().gameMode.setLocalMode(GameType.SURVIVAL);

        }
    }
}

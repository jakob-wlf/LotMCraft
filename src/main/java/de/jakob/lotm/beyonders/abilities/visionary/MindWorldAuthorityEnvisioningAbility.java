package de.jakob.lotm.beyonders.abilities.visionary;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncEnvisioningPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class MindWorldAuthorityEnvisioningAbility extends ToggleAbility {
    public static Set<UUID> active = new HashSet<>();
    public static boolean canEnvisionClient = false;

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
        return 1;
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
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer player)) return;

        if(!active.contains(player.getUUID())) return;

        player.getData(ModAttachments.SANITY_COMPONENT.get()).decreaseSanityAndSync(0.3f,player);
    }
}

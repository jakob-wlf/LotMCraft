package de.jakob.lotm.beyonders.abilities.fool;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.AvatarEntity;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.PlayerLeftClickWhileSummonSelfPacket;
import de.jakob.lotm.rendering.effectRendering.EffectIds;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class HistoricalVoidSummonSelfAbility extends Ability {
    public HistoricalVoidSummonSelfAbility(String id) {
        super(id, 60 * 4);

        canBeCopied = false;
        canBeReplicated = false;
        cannotBeStolen = true;
        canBeUsedByNPC = false;
        canBeUsedInArtifact = false;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            return;
        }

        ServerLevel historicalVoid = ((ServerLevel) level).getServer().getLevel(ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "historical_void")));

        if(historicalVoid == null) {
            return;
        }

        if(historicalVoid == level) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.historical_void_summon_self.cannot_use_in_historical_void"));
            return;
        }

        Location returnLocation = new Location(entity.position(), level);
        HistoricalVoidHidingAbility.addReturnPosition(entity, returnLocation);

        Vec3 avatarPos = correctAvatarPosition(entity.position().add((new Vec3(entity.getLookAngle().x, 0, entity.getLookAngle().z)).normalize().scale(2)), level);

        AvatarEntity avatar = new AvatarEntity(ModEntities.AVATAR.get(), level, entity.getUUID(), "fool", 9);
        avatar.setPos(avatarPos);
        avatar.setNoGravity(true);
        level.addFreshEntity(avatar);
        EffectManager.playEffect(EffectIds.HISTORICAL_VOID_SUMMONING, avatar.getX(), avatar.getY(), avatar.getZ(), (ServerLevel) level);

        long currentTime = System.currentTimeMillis();

        ServerScheduler.scheduleDelayed(35
                , () -> {
            entity.teleportTo((ServerLevel) avatar.level(), avatar.getX(), avatar.getY(), avatar.getZ(), Set.of(), avatar.getYRot(), avatar.getXRot());
            avatar.discard();
            EffectManager.playEffect(EffectIds.SEFIRAH_CASTLE, entity.getX(), entity.getY(), entity.getZ(), (ServerLevel) level);
            entity.getData(ModAttachments.HISTORICAL_VOID_COMPONENT).hasSummonedSelf = true;
            entity.getData(ModAttachments.HISTORICAL_VOID_COMPONENT).summonedSelfMillis = currentTime;
        });

        ServerScheduler.scheduleDelayed(20 * 60 * 2, () -> {
            if(!entity.getData(ModAttachments.HISTORICAL_VOID_COMPONENT).hasSummonedSelf || entity.getData(ModAttachments.HISTORICAL_VOID_COMPONENT).summonedSelfMillis != currentTime) {
                return;
            }

            entity.teleportTo(historicalVoid, returnLocation.getPosition().x(), HistoricalVoidHidingAbility.findSafeY(historicalVoid, (int) returnLocation.getPosition().x(), (int) returnLocation.getPosition().z()), returnLocation.getPosition().z(), Set.of(), entity.getYRot(), entity.getXRot());
            entity.playSound(SoundEvents.ENDER_CHEST_OPEN);
            EffectManager.playEffect(EffectIds.SEFIRAH_CASTLE, entity.getX(), entity.getY(), entity.getZ(), (ServerLevel) level);

            entity.getData(ModAttachments.HISTORICAL_VOID_COMPONENT).hasSummonedSelf = false;
            entity.getData(ModAttachments.HISTORICAL_VOID_COMPONENT).summonedSelfMillis = 0;
        });
    }

    private Vec3 correctAvatarPosition(Vec3 avatarPos, Level level) {
        while (level.getBlockState(BlockPos.containing(avatarPos)).getCollisionShape(level, BlockPos.containing(avatarPos)).isEmpty() &&
                level.getBlockState(BlockPos.containing(avatarPos.subtract(0, 1, 0))).getCollisionShape(level, BlockPos.containing(avatarPos)).isEmpty()
        ) {
            avatarPos = avatarPos.subtract(0, 1, 0);
        }
        while (!level.getBlockState(BlockPos.containing(avatarPos)).getCollisionShape(level, BlockPos.containing(avatarPos)).isEmpty()) {
            avatarPos = avatarPos.add(0, 1, 0);
        }

        return avatarPos;
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerInteractEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        Level level = player.level();
        if(player.getData(ModAttachments.HISTORICAL_VOID_COMPONENT).hasSummonedSelf) {
            player.getData(ModAttachments.HISTORICAL_VOID_COMPONENT).hasSummonedSelf = false;
            player.getData(ModAttachments.HISTORICAL_VOID_COMPONENT).summonedSelfMillis = 0;

            ServerLevel historicalVoid = ((ServerLevel) level).getServer().getLevel(ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "historical_void")));

            if(historicalVoid == null) {
                return;
            }

            player.teleportTo(historicalVoid, player.getX(), HistoricalVoidHidingAbility.findSafeY(historicalVoid, (int) player.getX(), (int) player.getZ()), player.getZ(), Set.of(), player.getYRot(), player.getXRot());
            player.playSound(SoundEvents.ENDER_CHEST_OPEN);
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingIncomingDamageEvent event) {
        if(event.getAmount() < event.getEntity().getHealth()) {
            return;
        }

        LivingEntity entity = event.getEntity();
        Level level = entity.level();

        if(entity.getData(ModAttachments.HISTORICAL_VOID_COMPONENT).hasSummonedSelf) {
            event.setCanceled(true);
            entity.setHealth(entity.getMaxHealth());

            entity.getData(ModAttachments.HISTORICAL_VOID_COMPONENT).hasSummonedSelf = false;
            entity.getData(ModAttachments.HISTORICAL_VOID_COMPONENT).summonedSelfMillis = 0;

            ServerLevel historicalVoid = ((ServerLevel) level).getServer().getLevel(ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "historical_void")));

            if(historicalVoid == null) {
                return;
            }

            entity.teleportTo(historicalVoid, entity.getX(), HistoricalVoidHidingAbility.findSafeY(historicalVoid, (int) entity.getX(), (int) entity.getZ()), entity.getZ(), Set.of(), entity.getYRot(), entity.getXRot());
            entity.playSound(SoundEvents.ENDER_CHEST_OPEN);
        }
    }

    @SubscribeEvent
    public static void onPlayerLeftClick(PlayerInteractEvent.LeftClickEmpty event) {
        Player player = event.getEntity();
        if(player.level().isClientSide) {
            PacketHandler.sendToServer(new PlayerLeftClickWhileSummonSelfPacket());
        }

    }

    public static void onPlayerLeftClickServer(ServerPlayer player) {
        if(!player.isShiftKeyDown()) {
            return;
        }
        if(player.getData(ModAttachments.HISTORICAL_VOID_COMPONENT).hasSummonedSelf) {
            player.getData(ModAttachments.HISTORICAL_VOID_COMPONENT).hasSummonedSelf = false;
            player.getData(ModAttachments.HISTORICAL_VOID_COMPONENT).summonedSelfMillis = 0;

            ServerLevel historicalVoid = ((ServerLevel) player.level()).getServer().getLevel(ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "historical_void")));

            if(historicalVoid == null) {
                return;
            }

            player.teleportTo(historicalVoid, player.getX(), HistoricalVoidHidingAbility.findSafeY(historicalVoid, (int) player.getX(), (int) player.getZ()), player.getZ(), Set.of(), player.getYRot(), player.getXRot());
            player.playSound(SoundEvents.ENDER_CHEST_OPEN);
        }
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of("fool", 3);
    }

    @Override
    protected float getSpiritualityCost() {
        return 6000;
    }
}

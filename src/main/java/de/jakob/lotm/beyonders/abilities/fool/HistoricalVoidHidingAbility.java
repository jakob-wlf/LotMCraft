package de.jakob.lotm.beyonders.abilities.fool;

import com.zigythebird.playeranimcore.math.Vec3f;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.attachments.DisabledAbilitiesComponent;
import de.jakob.lotm.attachments.FogComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.TransformationComponent;
import de.jakob.lotm.rendering.effectRendering.EffectIds;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class HistoricalVoidHidingAbility extends ToggleAbility {
    private static final HashMap<UUID, Location> locations = new HashMap<>();
    private static HashSet<UUID> dontReturnOnStop = new HashSet<>();

    public HistoricalVoidHidingAbility(String id) {
        super(id);

        canAlwaysBeUsed = true;
        cannotBeStolen = true;
        canBeCopied = false;
        canBeReplicated = false;
        canBeUsedInArtifact = false;
        canBeShared = false;
    }

    @Override
    public float getSpiritualityCost() {
        return 35;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("fool", 3));
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            return;
        }

        if(entity.getData(ModAttachments.HISTORICAL_VOID_COMPONENT).hasSummonedSelf) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.historical_void_hiding.cannot_use_while_summoned_self"));
            dontReturnOnStop.add(entity.getUUID());
            cancel((ServerLevel) level, entity);
            return;
        }

        ServerLevel historicalVoid = ((ServerLevel) level).getServer().getLevel(ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "historical_void")));

        if(historicalVoid == null) {
            return;
        }

        if(historicalVoid == level) {
            returnEntity(entity);
            dontReturnOnStop.add(entity.getUUID());
            cancel((ServerLevel) level, entity);
            return;
        }

        Location playerLocation = new Location(entity.position(), level);
        locations.put(entity.getUUID(), playerLocation);

        entity.teleportTo(historicalVoid, entity.getX(), findSafeY(historicalVoid, (int) entity.getX(), (int) entity.getZ()), entity.getZ(), Set.of(), entity.getYRot(), entity.getXRot());
        entity.playSound(SoundEvents.ENDER_CHEST_OPEN);

        EffectManager.playEffect(EffectIds.SEFIRAH_CASTLE, playerLocation.getPosition().x(), playerLocation.getPosition().y(), playerLocation.getPosition().z(), (ServerLevel) level);
        EffectManager.playEffect(EffectIds.SEFIRAH_CASTLE, entity.position().x(), entity.position().y(), entity.position().z(), (ServerLevel) entity.level());

    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            return;
        }

        // Fog effect
        FogComponent fogComponent = entity.getData(ModAttachments.FOG_COMPONENT);
        fogComponent.setActiveAndSync(true, entity);
        fogComponent.setFogIndexAndSync(FogComponent.FOG_TYPE.FOG_OF_HISTORY, entity);
        fogComponent.setFogColorAndSync(new Vec3f(1, 1, 1), entity);

    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        if(!dontReturnOnStop.contains(entity.getUUID())) {
            returnEntity(entity);
        }

        dontReturnOnStop.remove(entity.getUUID());

    }

    private static void returnEntity(LivingEntity entity) {
        Location returnLocation = getReturnPosition(entity);
        entity.teleportTo((ServerLevel) returnLocation.getLevel(), returnLocation.getPosition().x(), returnLocation.getPosition().y(), returnLocation.getPosition().z(), Set.of(), entity.getYRot(), entity.getXRot());
        entity.playSound(SoundEvents.ENDER_CHEST_CLOSE);
        EffectManager.playEffect(EffectIds.SEFIRAH_CASTLE, returnLocation.getPosition().x(), returnLocation.getPosition().y(), returnLocation.getPosition().z(), (ServerLevel) returnLocation.getLevel());

        locations.remove(entity.getUUID());
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerInteractEvent.PlayerLoggedOutEvent event) {
        if(((ToggleAbility) LOTMCraft.abilityHandler.getById("historical_void_hiding_ability")).isActiveForEntity(event.getEntity())) {
            returnEntity(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if(((ToggleAbility) LOTMCraft.abilityHandler.getById("historical_void_hiding_ability")).isActiveForEntity(event.getEntity())) {
            returnEntity(event.getEntity());
        }
    }

    public static void addReturnPosition(LivingEntity entity, Location location) {
        locations.put(entity.getUUID(), location);
    }

    public static int findSafeY(Level level, int x, int z) {
        int startY = level.getMinBuildHeight() + 1;
        int endY = level.getMaxBuildHeight() - 1;

        for (int y = startY; y <= endY; y++) {
            if (level.getBlockState(new net.minecraft.core.BlockPos(x, y, z)).isAir() &&
                    level.getBlockState(new net.minecraft.core.BlockPos(x, y + 1, z)).isAir() &&
                    !level.getBlockState(new net.minecraft.core.BlockPos(x, y - 1, z)).isAir()) {
                return y;
            }
        }
        return level.getMaxBuildHeight() + 1; // No safe Y found
    }

    private static Location getReturnPosition(LivingEntity entity) {
        if(entity.level().isClientSide)
            return null;
        else {
            ServerLevel overWorld = entity.getServer().overworld();
            if(locations.containsKey(entity.getUUID()) && locations.get(entity.getUUID()).getLevel() == entity.level()) {
                return new Location(new Vec3(entity.getX(), overWorld.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) entity.getX(), (int) entity.getZ()), entity.getZ()), overWorld);
            }
            return locations.getOrDefault(entity.getUUID(), new Location(new Vec3(entity.getX(), findSafeY(overWorld, (int) entity.getX(), (int) entity.getZ()), entity.getZ()), overWorld));
        }
    }
}

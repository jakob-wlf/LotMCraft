package de.jakob.lotm.sefirah;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SefirotData;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.fluid.ModFluids;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncSefirotAccommodationPacket;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class RiverOfEternalDarknessEventHandler {

    private static final String RIVER_SEFIROT_ID = "river_of_eternal_darkness";
    private static final int REQUIRED_TICKS = 20 * 60 * 5;
    private static final float SANITY_DRAIN_PER_SECOND = 0.05f;
    private static final float DAMAGE_PER_SECOND = 5.0f;

    private static final Set<String> ALLOWED_PATHWAYS = Set.of(
            "darkness",
            "death",
            "twilight_giant"
    );

    private static final Map<UUID, Integer> ritualTicks = new HashMap<>();
    private static final Set<UUID> ritualAnnounced = new HashSet<>();

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if (entity.level().isClientSide) {
            return;
        }

        if (entity.tickCount % 20 != 0) {
            return;
        }

        if (!isInDarkWater(entity)) {
            if (entity instanceof ServerPlayer player) {
                resetRitual(player);
            }
            return;
        }

        applyDarkWaterEffects(entity);

        if (entity instanceof ServerPlayer player) {
            tickRitual(player);
        }
    }

    private static void applyDarkWaterEffects(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            if (isRiverOwner(player) || isAccommodating(player) || isEligibleAccommodator(player)) {
                return;
            }
            ensureWellBase(player);
        }

        entity.hurt(ModDamageTypes.source(entity.level(), ModDamageTypes.DARKNESS_GENERIC), DAMAGE_PER_SECOND);

        if (entity instanceof Player player) {
            player.getData(ModAttachments.SANITY_COMPONENT).increaseSanityAndSync(-SANITY_DRAIN_PER_SECOND, player);
        }
    }

    private static boolean isAccommodating(ServerPlayer player) {
        if (!BeyonderData.isBeyonder(player)) {
            return false;
        }

        if (SefirotData.get(player.server).isSefirotClaimed(RIVER_SEFIROT_ID)) {
            return false;
        }

        if (SefirahHandler.hasSefirot(player)) {
            return false;
        }

        String pathway = BeyonderData.getPathway(player);
        if (!ALLOWED_PATHWAYS.contains(pathway)) {
            return false;
        }

        Ability ability = LOTMCraft.abilityHandler.getById("cogitation_ability");
        return ability instanceof ToggleAbility toggleAbility && toggleAbility.isActiveForEntity(player);
    }

    private static boolean isEligibleAccommodator(ServerPlayer player) {
        if (!BeyonderData.isBeyonder(player)) return false;
        if (SefirotData.get(player.server).isSefirotClaimed(RIVER_SEFIROT_ID)) return false;
        if (SefirahHandler.hasSefirot(player)) return false;
        String pathway = BeyonderData.getPathway(player);
        return ALLOWED_PATHWAYS.contains(pathway);
    }

    private static boolean isRiverOwner(ServerPlayer player) {
        return RIVER_SEFIROT_ID.equals(SefirahHandler.getClaimedSefirot(player));
    }

    private static void tickRitual(ServerPlayer player) {
        if (!BeyonderData.isBeyonder(player)) {
            resetRitual(player);
            return;
        }

        if (SefirotData.get(player.server).isSefirotClaimed(RIVER_SEFIROT_ID)) {
            resetRitual(player);
            return;
        }

        if (SefirahHandler.hasSefirot(player)) {
            resetRitual(player);
            return;
        }

        String pathway = BeyonderData.getPathway(player);
        if (!ALLOWED_PATHWAYS.contains(pathway)) {
            resetRitual(player);
            return;
        }

        Ability ability = LOTMCraft.abilityHandler.getById("cogitation_ability");
        if (!(ability instanceof ToggleAbility toggleAbility) || !toggleAbility.isActiveForEntity(player)) {
            resetRitual(player);
            return;
        }

        if (!ritualAnnounced.contains(player.getUUID())) {
            player.getServer().getPlayerList().broadcastSystemMessage(
                    net.minecraft.network.chat.Component.translatable(
                            "lotm.sefirot.river_started",
                            player.getName().getString()),
                    false);
            ritualAnnounced.add(player.getUUID());
        }

        int ticks = ritualTicks.getOrDefault(player.getUUID(), 0) + 20;
        ritualTicks.put(player.getUUID(), ticks);

        PacketHandler.sendToPlayer(player, new SyncSefirotAccommodationPacket(ticks, REQUIRED_TICKS));

        if (ticks < REQUIRED_TICKS) {
            return;
        }

        boolean claimed = SefirahHandler.claimSefirot(player, RIVER_SEFIROT_ID, true);
        if (claimed) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("lotm.sefirot.river_claimed"));
        } else {
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("lotm.sefirot.river_already_occupied"));
        }

        resetRitual(player);
    }

    private static void resetRitual(ServerPlayer player) {
        ritualTicks.remove(player.getUUID());
        ritualAnnounced.remove(player.getUUID());
        PacketHandler.sendToPlayer(player, new SyncSefirotAccommodationPacket(0, 0));
    }

    private static boolean isInDarkWater(LivingEntity entity) {
        BlockPos feetPos = entity.blockPosition();
        if (isDarkWaterBlock(entity.level(), feetPos)) {
            return true;
        }

        BlockPos eyePos = BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ());
        return isDarkWaterBlock(entity.level(), eyePos);
    }

    private static boolean isDarkWaterBlock(Level level, BlockPos pos) {
        FluidState state = level.getFluidState(pos);
        return state.is(ModFluids.DROPS_OF_ETERNAL_DARKNESS_SOURCE.get())
                || state.is(ModFluids.DROPS_OF_ETERNAL_DARKNESS_FLOWING.get());
    }

    private static void ensureWellBase(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        if (!level.dimension().equals(Level.OVERWORLD)) {
            return;
        }

        ResourceKey<Structure> key = ResourceKey.create(
                Registries.STRUCTURE,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "river_of_eternal_darkness_well")
        );

        StructureStart start = level.registryAccess()
                .registry(Registries.STRUCTURE)
                .flatMap(registry -> registry.getHolder(key)
                        .map(holder -> level.structureManager()
                                .getStructureWithPieceAt(player.blockPosition(), HolderSet.direct(holder))))
                .orElse(null);

        if (start == null || !start.isValid()) {
            return;
        }

        BoundingBox box = start.getBoundingBox();
        int y = box.minY() - 1;

        for (int x = box.minX(); x <= box.maxX(); x++) {
            for (int z = box.minZ(); z <= box.maxZ(); z++) {
                BlockPos pos = new BlockPos(x, y, z);
                if (level.getBlockState(pos).isAir()) {
                    level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
                }
            }
        }
    }

}

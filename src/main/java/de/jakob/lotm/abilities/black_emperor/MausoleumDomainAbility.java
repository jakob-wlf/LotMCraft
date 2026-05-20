package de.jakob.lotm.abilities.black_emperor;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public final class MausoleumDomainAbility extends Ability {

    private static final ResourceKey<Level> MAUSOLEUM_DIMENSION = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "mausoleum")
    );

    private static final BlockPos MAUSOLEUM_SPAWN = new BlockPos(75, 1, 75);
    private static final int ROOM_HALF_XZ = 75;
    private static final int ROOM_HALF_Y = 32;
    private static final double CAST_RANGE = 25.0D;

    private static final Map<UUID, Session> SESSIONS = new HashMap<>();
    private static final Map<UUID, UUID> PLAYER_TO_CASTER = new HashMap<>();

    private static final Set<UUID> PENDING_DEATHS = new HashSet<>();

    private record SavedReturn(
            ResourceKey<Level> dimension,
            Vec3 position,
            float yRot,
            float xRot,
            Vec3 motion
    ) {}

    private static final class Session {
        private final UUID casterId;
        private final int casterSeq;
        private final Map<UUID, SavedReturn> returns = new HashMap<>();
        private final Set<UUID> members = new HashSet<>();

        private Session(UUID casterId, int casterSeq) {
            this.casterId = casterId;
            this.casterSeq = casterSeq;
        }
    }

    public MausoleumDomainAbility(String id) {
        super(id, 1.0f);
        canBeCopied = false;
        canBeReplicated = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("black_emperor", 1));
    }

    @Override
    public float getSpiritualityCost() {
        return 0.0f;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (!(entity instanceof ServerPlayer caster)) {
            AbilityUtil.sendActionBar(entity,
                    Component.literal("Mausoleum Domain is for players.").withColor(0xFF5555));
            return;
        }

        castMausoleumDomain(serverLevel, caster);
    }

    private void castMausoleumDomain(ServerLevel overworld, ServerPlayer caster) {
        ServerLevel mausoleum = overworld.getServer().getLevel(MAUSOLEUM_DIMENSION);
        if (mausoleum == null) {
            AbilityUtil.sendActionBar(caster,
                    Component.literal("Mausoleum dimension is missing.").withColor(0xFF5555));
            return;
        }

        if (SESSIONS.containsKey(caster.getUUID())) {
            AbilityUtil.sendActionBar(caster,
                    Component.literal("The mausoleum is already sealed.").withColor(0xFF5555));
            return;
        }

        AABB range = caster.getBoundingBox().inflate(CAST_RANGE);
        Set<ServerPlayer> targets = new HashSet<>(overworld.getEntitiesOfClass(
                ServerPlayer.class,
                range,
                player -> player.isAlive()
        ));

        if (targets.isEmpty()) {
            AbilityUtil.sendActionBar(caster,
                    Component.literal("No players in range.").withColor(0xFF5555));
            return;
        }

        int casterSeq = BeyonderData.isBeyonder(caster) ? BeyonderData.getSequence(caster) : 999;
        Session session = new Session(caster.getUUID(), casterSeq);
        SESSIONS.put(caster.getUUID(), session);

        BeyonderData.reduceSpirituality(caster, BeyonderData.getMaxSpirituality(BeyonderData.getPathway(caster), BeyonderData.getSequence(caster)) * 0.25f);

        for (ServerPlayer player : targets) {
            session.members.add(player.getUUID());
            session.returns.put(player.getUUID(), snapshot(player));
            PLAYER_TO_CASTER.put(player.getUUID(), caster.getUUID());
        }

        teleportGroupIntoMausoleum(mausoleum, caster, targets);

        for (ServerPlayer player : targets) {
            ToggleAbility.cleanUp(mausoleum, player);
        }

        AbilityUtil.sendActionBar(caster,
                Component.literal("The mausoleum seals shut.").withColor(0xAA77FF));
    }

    private void teleportGroupIntoMausoleum(ServerLevel mausoleum, ServerPlayer caster, Set<ServerPlayer> targets) {
        Vec3 center = new Vec3(75.5D, 14.0D, 75.5D);

        int index = 0;
        int total = Math.max(1, targets.size());

        for (ServerPlayer player : targets) {
            Vec3 destination;
            if (player.getUUID().equals(caster.getUUID())) {
                destination = center;
            } else {
                double angle = (Math.PI * 2.0D) * (index / (double) total);
                double radius = 2.5D + (index % 3) * 1.5D;

                double x = Mth.clamp(Math.cos(angle) * radius, -(ROOM_HALF_XZ - 1), ROOM_HALF_XZ - 1);
                double z = Mth.clamp(Math.sin(angle) * radius, -(ROOM_HALF_XZ - 1), ROOM_HALF_XZ - 1);

                destination = center.add(x, 0.0D, z);
                index++;
            }

            teleportPlayer(player, mausoleum, destination);
        }
    }

    private static double findGroundY(ServerLevel level, int x, int z) {
        for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
            BlockPos pos = new BlockPos(x, y, z);
            if (!level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()) {
                return y + 1.0D;
            }
        }
        return 1.0D;
    }

    private static void teleportPlayer(ServerPlayer player, ServerLevel targetLevel, Vec3 destination) {
        player.teleportTo(targetLevel, destination.x, destination.y, destination.z, player.getYRot(), player.getXRot());
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0;
        player.hurtMarked = true;
    }

    private static SavedReturn snapshot(ServerPlayer player) {
        return new SavedReturn(
                player.level().dimension(),
                player.position(),
                player.getYRot(),
                player.getXRot(),
                player.getDeltaMovement()
        );
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        UUID playerId = player.getUUID();
        if (!PLAYER_TO_CASTER.containsKey(playerId)) return;

        PENDING_DEATHS.add(playerId);

        UUID casterId = PLAYER_TO_CASTER.get(playerId);
        Session session = SESSIONS.get(casterId);
        if (session != null) {
            session.members.remove(playerId);
            session.returns.remove(playerId);
        }

        PLAYER_TO_CASTER.remove(playerId);
    }

    private static boolean structurePlaced = false;

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (!structurePlaced) {
            MinecraftServer server = player.getServer();
            if (server != null) {
                prePlaceStructure(server);
                structurePlaced = true;
            }
        }

        if (PENDING_DEATHS.contains(player.getUUID())) {
            player.invulnerableTime = 0;
            killPlayer(player);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        structurePlaced = false;
        SESSIONS.clear();
        PLAYER_TO_CASTER.clear();
        PENDING_DEATHS.clear();
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;

        if (PENDING_DEATHS.contains(sp.getUUID())) {
            sp.invulnerableTime = 0;
            killPlayer(sp);

            if (!sp.isAlive()) {
                PENDING_DEATHS.remove(sp.getUUID());
            }
            return;
        }

        UUID casterId = PLAYER_TO_CASTER.get(sp.getUUID());
        if (casterId == null) return;

        Session session = SESSIONS.get(casterId);
        if (session == null) {
            PLAYER_TO_CASTER.remove(sp.getUUID());
            return;
        }

        MinecraftServer server = sp.getServer();
        if (server == null) return;

        if (sp.getUUID().equals(casterId) && BeyonderData.getSpirituality(sp) <= 0.0f) {
            endSession(server, casterId);
            return;
        }

        ServerLevel mausoleum = server.getLevel(MAUSOLEUM_DIMENSION);
        if (mausoleum == null) return;

        if (!sp.level().dimension().equals(MAUSOLEUM_DIMENSION)) {
            teleportPlayer(sp, mausoleum, new Vec3(75.5D, findGroundY(mausoleum, 75, 75), 75.5D));
            return;
        }

        if (mausoleum.getGameTime() % 20 == 0) {
            float drain = getDrainAmount(sp, session);
            if (drain > 0.0f && BeyonderData.isBeyonder(sp)) {
                BeyonderData.reduceSpirituality(sp, drain);
            }

            if (sp.getUUID().equals(casterId) && BeyonderData.getSpirituality(sp) <= 0.0f) {
                endSession(server, casterId);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (!isSealed(player)) return;

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isSealed(player)) return;

        event.setCanceled(true);
    }

    private static boolean isSealed(ServerPlayer player) {
        return PLAYER_TO_CASTER.containsKey(player.getUUID());
    }

    private static float getDrainAmount(ServerPlayer player, Session session) {
        if (player.getUUID().equals(session.casterId)) {
            return 550.0f;
        }

        int playerSeq = BeyonderData.isBeyonder(player) ? BeyonderData.getSequence(player) : 999;
        return 0.5f + Math.max(0, playerSeq - session.casterSeq) * 0.15f;
    }

    // Structure is 150x61x150 placed at (0,0,0)
    private static final int STRUCT_WIDTH = 150;
    private static final int STRUCT_HEIGHT = 61;
    private static final int STRUCT_LENGTH = 150;

    private static boolean isInsideMausoleum(Vec3 pos) {
        return pos.x >= 1 && pos.x <= STRUCT_WIDTH - 1
            && pos.y >= 1 && pos.y <= STRUCT_HEIGHT - 1
            && pos.z >= 1 && pos.z <= STRUCT_LENGTH - 1;
    }

    private static Vec3 clampInsideRoom(Vec3 pos) {
        double x = Mth.clamp(pos.x, 1.0D, STRUCT_WIDTH - 1.0D);
        double y = Mth.clamp(pos.y, 1.0D, STRUCT_HEIGHT - 1.0D);
        double z = Mth.clamp(pos.z, 1.0D, STRUCT_LENGTH - 1.0D);
        return new Vec3(x, pos.y, z);
    }

    private static final ResourceLocation STRUCTURE_ID =
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "mausoleum_room");
    public static void prePlaceStructure(net.minecraft.server.MinecraftServer server) {
        ServerLevel mausoleum = server.getLevel(MAUSOLEUM_DIMENSION);
        if (mausoleum == null) {
            LOTMCraft.LOGGER.warn("Mausoleum dimension not found on server start.");
            return;
        }

        // Check if structure is already placed by testing a block above the bedrock floor
        BlockState check = mausoleum.getBlockState(new BlockPos(75, 1, 75));
        if (!check.isAir()) {
            LOTMCraft.LOGGER.info("Mausoleum structure already present, skipping placement.");
            return;
        }

        try {
            StructureTemplate structure = server.getStructureManager().getOrCreate(STRUCTURE_ID);
            StructurePlaceSettings settings = new StructurePlaceSettings();
            structure.placeInWorld(mausoleum, BlockPos.ZERO, BlockPos.ZERO, settings, mausoleum.random, 2);
            LOTMCraft.LOGGER.info("Mausoleum structure pre-placed, saving chunks...");
            mausoleum.save(null, true, false);
            LOTMCraft.LOGGER.info("Mausoleum chunks saved.");
        } catch (Exception e) {
            LOTMCraft.LOGGER.error("Failed to pre-place mausoleum structure: {}", e.getMessage());
        }
    }

    private static void endSession(MinecraftServer server, UUID casterId) {
        Session session = SESSIONS.remove(casterId);
        if (session == null) return;

        Set<UUID> members = new HashSet<>(session.members);
        for (UUID memberId : members) {
            PLAYER_TO_CASTER.remove(memberId);
            PENDING_DEATHS.remove(memberId);
        }

        for (UUID memberId : members) {
            ServerPlayer player = server.getPlayerList().getPlayer(memberId);
            if (player == null) continue;

            SavedReturn ret = session.returns.get(memberId);
            if (ret == null) continue;

            ServerLevel targetLevel = server.getLevel(ret.dimension());
            if (targetLevel == null) continue;

            player.teleportTo(
                    targetLevel,
                    ret.position().x,
                    ret.position().y,
                    ret.position().z,
                    ret.yRot(),
                    ret.xRot()
            );
            player.setDeltaMovement(ret.motion());
            player.fallDistance = 0;
            player.hurtMarked = true;
        }
    }

    private static void killPlayer(ServerPlayer player) {
        player.hurt(player.damageSources().fellOutOfWorld(), Float.MAX_VALUE);
    }

    public static boolean isInsideMausoleumDomain(UUID playerId) {
        return PLAYER_TO_CASTER.containsKey(playerId);
    }
}
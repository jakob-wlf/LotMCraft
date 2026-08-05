package de.jakob.lotm.beyonders.sefirah;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SefirotData;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.ProphecyAbility;
import de.jakob.lotm.block.ModBlocks;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncSefirotAccommodationPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.RingEffectManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public final class KeyOfLightEventHandler {
	public static final String sefirotId = "key_of_light";
	private static final ResourceLocation templeStructure =
		ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "key_of_light_temple");
	private static final BlockPos statueTemplateOffset = new BlockPos(4, 2, 4);
	private static final int alignmentDurationSeconds = 10 * 60;
	private static final int requiredTicks = 20 * alignmentDurationSeconds;
	private static final int templeHorizontalRadius = 4;
	private static final int templeMinYOffset = -2;
	private static final int templeMaxYOffset = 8;
	private static final int maxTempleDistanceFromSpawn = 5_000;
	private static final int templeSiteAttempts = 24;
	private static final int preferredMountainHeight = 100;
	private static final float alignmentRadius = 50.0f;
	private static final int boundaryRefreshTicks = 30;
	private static final int boundaryDurationTicks = 60;
	private static final String boundaryKeyPrefix = "key_of_light_alignment_";
	private static final float boundaryCorruptionPerSecond = 0.05f;
	private static final double[] successChances = {1.0, 0.85, 0.65, 0.40, 0.20, 0.10, 0.05, 0.02, 0.005, 0.001};
	private static final Map<UUID, AlignmentState> alignments = new HashMap<>();
	private static final DustParticleOptions wheelDust =
		new DustParticleOptions(new Vector3f(0.73f, 0.82f, 0.96f), 1.1f);

	private KeyOfLightEventHandler() {
	}

	@SubscribeEvent
	public static void onServerStarted(ServerStartedEvent event) {
		ServerLevel level = event.getServer().overworld();
		SefirotData data = SefirotData.get(event.getServer());
		BlockPos existingPos = data.getKeyOfLightShrinePos().orElse(null);
		if (existingPos != null) {
			buildShrine(level, existingPos);
			return;
		}

		BlockPos spawn = level.getSharedSpawnPos();
		int direction = level.random.nextInt(4);
		BlockPos statuePos = findTempleSite(level, spawn, direction);
		buildShrine(level, statuePos);
		data.setKeyOfLightShrinePos(statuePos);
	}

	private static BlockPos findTempleSite(ServerLevel level, BlockPos spawn, int direction) {
		TempleSite bestSite = null;
		for (int attempt = 0; attempt < templeSiteAttempts; attempt++) {
			int distance = level.random.nextInt(maxTempleDistanceFromSpawn) + 1;
			int x = spawn.getX();
			int z = spawn.getZ();
			switch (direction) {
				case 0 -> x += distance;
				case 1 -> x -= distance;
				case 2 -> z += distance;
				case 3 -> z -= distance;
				default -> throw new IllegalStateException("Unexpected cardinal direction: " + direction);
			}

			TempleSite site = evaluateTempleSite(level, x, z);
			if (site != null && (bestSite == null || site.score > bestSite.score)) {
				bestSite = site;
			}
		}
		if (bestSite != null) {
			return bestSite.statuePos;
		}

		int fallbackDistance = level.random.nextInt(maxTempleDistanceFromSpawn) + 1;
		int fallbackX = spawn.getX() + (direction == 0 ? fallbackDistance : direction == 1 ? -fallbackDistance : 0);
		int fallbackZ = spawn.getZ() + (direction == 2 ? fallbackDistance : direction == 3 ? -fallbackDistance : 0);
		int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, fallbackX, fallbackZ);
		return new BlockPos(fallbackX, surfaceY + statueTemplateOffset.getY(), fallbackZ);
	}

	private static TempleSite evaluateTempleSite(ServerLevel level, int centerX, int centerZ) {
		int minimumHeight = Integer.MAX_VALUE;
		int maximumHeight = Integer.MIN_VALUE;
		for (int offsetX = -templeHorizontalRadius; offsetX <= templeHorizontalRadius; offsetX++) {
			for (int offsetZ = -templeHorizontalRadius; offsetZ <= templeHorizontalRadius; offsetZ++) {
				int x = centerX + offsetX;
				int z = centerZ + offsetZ;
				level.getChunk(x >> 4, z >> 4);
				int height = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
				BlockPos groundPos = new BlockPos(x, height - 1, z);
				if (!level.getFluidState(groundPos).isEmpty()) {
					return null;
				}
				minimumHeight = Math.min(minimumHeight, height);
				maximumHeight = Math.max(maximumHeight, height);
			}
		}

		int relief = maximumHeight - minimumHeight;
		int mountainBonus = maximumHeight >= preferredMountainHeight ? 200 : 0;
		double score = maximumHeight + mountainBonus - relief * 8.0;
		BlockPos statuePos = new BlockPos(centerX,
			maximumHeight + statueTemplateOffset.getY(), centerZ);
		return new TempleSite(statuePos, score);
	}

	public static void tryStartAlignment(ServerPlayer player, BlockPos statuePos) {
		BlockPos shrinePos = SefirotData.get(player.server).getKeyOfLightShrinePos().orElse(null);
		if (shrinePos == null || !shrinePos.equals(statuePos)
			|| player.level().dimension() != Level.OVERWORLD) {
			return;
		}
		if (!isWheelOfFortune(player)) {
			ProphecyAbility.applyMaximumMisfortuneProphecy(player);
			return;
		}
		if (SefirahHandler.hasSefirot(player)) {
			player.sendSystemMessage(Component.literal("§eYou already carry a Sefirot."));
			return;
		}
		if (SefirotData.get(player.server).isSefirotClaimed(sefirotId)) {
			player.sendSystemMessage(Component.literal("§eThe Key of Light has already aligned with another."));
			return;
		}
		if (alignments.containsKey(player.getUUID())) {
			return;
		}

		alignments.put(player.getUUID(), new AlignmentState(statuePos.immutable()));
		player.sendSystemMessage(Component.literal(
			"§dAlignment has begun. Remain within the marked 50-block boundary for 10 minutes."));
		PacketHandler.sendToPlayer(player, new SyncSefirotAccommodationPacket(0, requiredTicks));
		spawnAlignmentBoundary(player.serverLevel(), statuePos);
		notifyWheelOfFortuneBeyonders(player, statuePos, 0);
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		AlignmentState state = alignments.get(player.getUUID());
		if (state == null) return;

		if (!canContinue(player, state)) {
			cancelAlignment(player, "§cYour alignment with the Key of Light was broken.");
			return;
		}

		state.ticks++;
		if (state.ticks % boundaryRefreshTicks == 0) {
			spawnAlignmentBoundary(player.serverLevel(), state.statuePos);
		}
		if (state.ticks % 2 == 0) {
			spawnAlignmentParticles(player, state.statuePos, state.ticks);
		}
		if (state.ticks % 20 == 0) {
			applyBoundaryCorruption(player.serverLevel(), state.statuePos);
			PacketHandler.sendToPlayer(player,
				new SyncSefirotAccommodationPacket(state.ticks, requiredTicks));
		}

		int progressStep = state.ticks * 10 / requiredTicks;
		if (progressStep > state.lastAnnouncedStep && progressStep <= 10) {
			state.lastAnnouncedStep = progressStep;
			notifyWheelOfFortuneBeyonders(player, state.statuePos, progressStep * 10);
		}
		if (state.ticks >= requiredTicks) {
			finishAlignment(player, state);
		}
	}

	@SubscribeEvent
	public static void onDeath(LivingDeathEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			cancelAlignment(player, null);
		}
	}

	@SubscribeEvent
	public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			cancelAlignment(player, null);
		}
	}

	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event) {
		if (event.getLevel() instanceof ServerLevel level && isProtectedShrineBlock(level, event.getPos())) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
		if (event.getLevel() instanceof ServerLevel level && isProtectedShrineBlock(level, event.getPos())) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onExplosion(ExplosionEvent.Detonate event) {
		if (!(event.getLevel() instanceof ServerLevel level)) return;
		event.getAffectedBlocks().removeIf(pos -> isProtectedShrineBlock(level, pos));
	}

	private static boolean canContinue(ServerPlayer player, AlignmentState state) {
		return isWheelOfFortune(player)
			&& !SefirahHandler.hasSefirot(player)
			&& !SefirotData.get(player.server).isSefirotClaimed(sefirotId)
			&& player.level().dimension() == Level.OVERWORLD
			&& horizontalDistanceSquared(player.position(), Vec3.atCenterOf(state.statuePos))
				<= alignmentRadius * alignmentRadius;
	}

	private static double horizontalDistanceSquared(Vec3 first, Vec3 second) {
		double deltaX = first.x - second.x;
		double deltaZ = first.z - second.z;
		return deltaX * deltaX + deltaZ * deltaZ;
	}

	private static void finishAlignment(ServerPlayer player, AlignmentState state) {
		alignments.remove(player.getUUID());
		removeAlignmentBoundaryIfInactive(player.serverLevel());
		PacketHandler.sendToPlayer(player, new SyncSefirotAccommodationPacket(0, 0));
		int sequence = Math.clamp(BeyonderData.getSequence(player), 0, 9);
		if (player.getRandom().nextDouble() <= successChances[sequence]
			&& SefirahHandler.claimSefirot(player, sefirotId, true)) {
			player.getServer().getPlayerList().broadcastSystemMessage(Component.literal(
				"§d" + player.getName().getString() + " has aligned with the Key of Light."), false);
			return;
		}

		ProphecyAbility.applyMaximumMisfortuneProphecy(player);
		player.sendSystemMessage(Component.literal("§5The alignment failed, and fate turned against you."));
	}

	private static void cancelAlignment(ServerPlayer player, String message) {
		if (alignments.remove(player.getUUID()) == null) return;
		removeAlignmentBoundaryIfInactive(player.serverLevel());
		PacketHandler.sendToPlayer(player, new SyncSefirotAccommodationPacket(0, 0));
		if (message != null) player.sendSystemMessage(Component.literal(message));
	}

	private static boolean isWheelOfFortune(ServerPlayer player) {
		return BeyonderData.isBeyonder(player)
			&& "wheel_of_fortune".equalsIgnoreCase(BeyonderData.getPathway(player));
	}

	private static void notifyWheelOfFortuneBeyonders(ServerPlayer aligningPlayer,
													   BlockPos statuePos, int percent) {
		for (ServerPlayer observer : aligningPlayer.server.getPlayerList().getPlayers()) {
			if (!isWheelOfFortune(observer) || observer == aligningPlayer) continue;
			double distance = observer.position().distanceTo(Vec3.atCenterOf(statuePos));
			String direction = direction(observer.getX(), observer.getZ(), statuePos.getX(), statuePos.getZ());
			observer.sendSystemMessage(Component.literal(String.format(
				"§dYou sense distortions in fate %s, %.0f blocks away. Alignment: %d%%.",
				direction, distance, percent)));
		}
	}

	private static String direction(double fromX, double fromZ, double toX, double toZ) {
		double degrees = Math.toDegrees(Math.atan2(toZ - fromZ, toX - fromX));
		String[] directions = {"east", "south-east", "south", "south-west",
			"west", "north-west", "north", "north-east"};
		return directions[Math.floorMod((int) Math.round(degrees / 45.0), directions.length)];
	}

	private static void spawnAlignmentParticles(ServerPlayer player, BlockPos statuePos, int ticks) {
		ServerLevel level = player.serverLevel();
		double phase = ticks * 0.08;
		for (int ring = 0; ring < 3; ring++) {
			double y = player.getY() + ((ticks * 0.025 + ring) % 3.0);
			for (int point = 0; point < 12; point++) {
				double angle = phase + point * Math.PI * 2 / 12 + ring * 0.5;
				double x = player.getX() + Math.cos(angle) * (0.8 + ring * 0.18);
				double z = player.getZ() + Math.sin(angle) * (0.8 + ring * 0.18);
				level.sendParticles(point % 2 == 0 ? ParticleTypes.DRAGON_BREATH : wheelDust,
					x, y, z, 1, 0, 0.025, 0, 0);
			}
		}

		for (int point = 0; point < 24; point++) {
			double angle = phase * 0.5 + point * Math.PI * 2 / 24;
			double x = statuePos.getX() + 0.5 + Math.cos(angle) * 3.5;
			double z = statuePos.getZ() + 0.5 + Math.sin(angle) * 3.5;
			level.sendParticles(point % 3 == 0 ? ParticleTypes.END_ROD : wheelDust,
				x, statuePos.getY() + 0.15, z, 1, 0, 0.01, 0, 0);
		}
	}

	private static void spawnAlignmentBoundary(ServerLevel level, BlockPos statuePos) {
		Vec3 center = Vec3.atCenterOf(statuePos);
		RingEffectManager.createWorldHeightRingForAll(boundaryKeyPrefix + "inner",
			center, alignmentRadius - 0.45f,
			boundaryDurationTicks, 0.73f, 0.82f, 0.96f, 0.55f, 0.35f, level);
		RingEffectManager.createWorldHeightRingForAll(boundaryKeyPrefix + "middle",
			center, alignmentRadius,
			boundaryDurationTicks, 0.60f, 0.39f, 0.85f, 0.5f, 0.35f, level);
		RingEffectManager.createWorldHeightRingForAll(boundaryKeyPrefix + "outer",
			center, alignmentRadius + 0.45f,
			boundaryDurationTicks, 0.94f, 0.84f, 0.47f, 0.45f, 0.35f, level);
	}

	private static void removeAlignmentBoundaryIfInactive(ServerLevel level) {
		if (!alignments.isEmpty()) return;
		RingEffectManager.removeRingForAll(boundaryKeyPrefix + "inner", level);
		RingEffectManager.removeRingForAll(boundaryKeyPrefix + "middle", level);
		RingEffectManager.removeRingForAll(boundaryKeyPrefix + "outer", level);
	}

	private static void applyBoundaryCorruption(ServerLevel level, BlockPos statuePos) {
		Vec3 center = Vec3.atCenterOf(statuePos);
		AABB boundary = new AABB(
			center.x - alignmentRadius, level.getMinBuildHeight(), center.z - alignmentRadius,
			center.x + alignmentRadius, level.getMaxBuildHeight(), center.z + alignmentRadius);
		for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, boundary)) {
			if (!BeyonderData.isBeyonder(entity)
				|| "wheel_of_fortune".equalsIgnoreCase(BeyonderData.getPathway(entity))
				|| horizontalDistanceSquared(entity.position(), center) > alignmentRadius * alignmentRadius) {
				continue;
			}
			entity.getData(ModAttachments.CORRUPTION_COMPONENT.get())
				.increaseCorruptionAndSync(boundaryCorruptionPerSecond, entity);
		}
	}

	private static void buildShrine(ServerLevel level, BlockPos statuePos) {
		StructureTemplate temple = level.getServer().getStructureManager().getOrCreate(templeStructure);
		BlockPos templateOrigin = statuePos.subtract(statueTemplateOffset);
		boolean placed = temple.placeInWorld(level, templateOrigin, templateOrigin,
			new StructurePlaceSettings(), level.random, 2);
		if (!placed) {
			LOTMCraft.LOGGER.error("Failed to place Key of Light temple at {}", templateOrigin);
			return;
		}
		level.setBlockAndUpdate(statuePos, ModBlocks.KEY_OF_LIGHT_STATUE.get().defaultBlockState());
	}

	private static boolean isProtectedShrineBlock(ServerLevel level, BlockPos pos) {
		if (level.dimension() != Level.OVERWORLD) return false;
		BlockPos center = SefirotData.get(level.getServer()).getKeyOfLightShrinePos().orElse(null);
		if (center == null) return false;
		return Math.abs(pos.getX() - center.getX()) <= templeHorizontalRadius
			&& Math.abs(pos.getZ() - center.getZ()) <= templeHorizontalRadius
			&& pos.getY() >= center.getY() + templeMinYOffset
			&& pos.getY() <= center.getY() + templeMaxYOffset;
	}

	private static final class AlignmentState {
		private final BlockPos statuePos;
		private int ticks;
		private int lastAnnouncedStep;

		private AlignmentState(BlockPos statuePos) {
			this.statuePos = statuePos;
		}
	}

	private record TempleSite(BlockPos statuePos, double score) {
	}
}

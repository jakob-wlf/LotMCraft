package de.jakob.lotm.dimension.spirit_world.spirit_patch;

import de.jakob.lotm.block.ModBlocks;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.dimension.spirit_world.SpiritWorldHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

public final class SpiritPortalTeleporter {

    public static final int SPIRIT_SHRINE_Y = 96;

    private SpiritPortalTeleporter() {}

    public static void use(ServerLevel from, BlockPos portalPos, Entity entity) {
        MinecraftServer server = from.getServer();
        BlockPos anchor = findAnchor(from, portalPos);
        SpiritPortalLinks links = SpiritPortalLinks.get(server);

        ServerLevel dest;
        BlockPos destAnchor;

        if (from.dimension().equals(Level.OVERWORLD)) {
            dest = server.getLevel(ModDimensions.SPIRIT_WORLD_DIMENSION_KEY);
            if (dest == null) return;

            Long linked = links.spiritFor(anchor);
            if (linked != null) {
                destAnchor = BlockPos.of(linked);
            } else {
                Vec3 c = SpiritWorldHandler.getCoordinatesInSpiritWorld(Vec3.atCenterOf(anchor), dest);
                destAnchor = links.freeSpiritAnchor(new BlockPos(Mth.floor(c.x), SPIRIT_SHRINE_Y, Mth.floor(c.z)));
                links.link(anchor, destAnchor);
            }
            ensureShrine(dest, destAnchor, true);

        } else if (from.dimension().equals(ModDimensions.SPIRIT_WORLD_DIMENSION_KEY)) {
            dest = server.overworld();
            Long linked = links.overworldFor(anchor);
            if (linked != null) {
                destAnchor = BlockPos.of(linked);
                ensureShrine(dest, destAnchor, false);
            } else {
                Vec3 c = SpiritWorldHandler.getCoordinatesInOverworld(Vec3.atCenterOf(anchor), dest);
                int x = Mth.floor(c.x), z = Mth.floor(c.z);
                int y = dest.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                destAnchor = new BlockPos(x, y, z);
                SpiritShrineBlueprint.placeRuntime(dest, destAnchor, false);
                SpiritShrineBlueprint.placeWorldgen(dest, destAnchor, SpiritShrineBlueprint.boundsFor(destAnchor));
                links.link(destAnchor, anchor);
            }
        } else {
            return; // portals only work between the overworld and the spirit world
        }

        Vec3 arrival = new Vec3(destAnchor.getX() + 0.5, destAnchor.getY(), destAnchor.getZ() + 3.5);
        Entity moved = entity.changeDimension(new DimensionTransition(
                dest, arrival, Vec3.ZERO, 180.0f, 0.0f, DimensionTransition.PLAY_PORTAL_SOUND));
        if (moved != null) moved.setPortalCooldown();
    }

    private static void ensureShrine(ServerLevel level, BlockPos anchor, boolean floating) {
        if (!level.getBlockState(anchor).is(ModBlocks.SPIRIT_PORTAL.get())) {
            SpiritShrineBlueprint.placeRuntime(level, anchor, floating);
        }
    }

    public static BlockPos findAnchor(ServerLevel level, BlockPos pos) {
        var portal = ModBlocks.SPIRIT_PORTAL.get();
        BlockPos p = pos;
        while (level.getBlockState(p.below()).is(portal)) p = p.below();
        int x1 = p.getX(), x2 = p.getX();
        while (level.getBlockState(new BlockPos(x1 - 1, p.getY(), p.getZ())).is(portal)) x1--;
        while (level.getBlockState(new BlockPos(x2 + 1, p.getY(), p.getZ())).is(portal)) x2++;
        return new BlockPos((x1 + x2) / 2, p.getY(), p.getZ());
    }
}

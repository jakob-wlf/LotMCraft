package de.jakob.lotm.rendering;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class DawnWeaponGeometry implements IUnbakedGeometry<DawnWeaponGeometry> {
    public static final IGeometryLoader<DawnWeaponGeometry> LOADER = DawnWeaponGeometry::read;

    private final int textureWidth;
    private final int textureHeight;
    private final List<Cube> cubes;

    private DawnWeaponGeometry(int textureWidth, int textureHeight, List<Cube> cubes) {
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.cubes = cubes;
    }

    private static DawnWeaponGeometry read(JsonObject json, JsonDeserializationContext context) {
        int width = 16;
        int height = 16;
        if (json.has("texture_size")) {
            var size = json.getAsJsonArray("texture_size");
            width = size.get(0).getAsInt();
            height = size.get(1).getAsInt();
        }
        List<Cube> cubes = new ArrayList<>();
        for (var element : json.getAsJsonArray("cubes")) {
            JsonObject cube = element.getAsJsonObject();
            float[] from = vec(cube, "from");
            float[] to = vec(cube, "to");
            float[] origin = cube.has("origin") ? vec(cube, "origin") : new float[]{(from[0] + to[0]) / 2f, (from[1] + to[1]) / 2f, (from[2] + to[2]) / 2f};
            float[] rotation = cube.has("rotation") ? vec(cube, "rotation") : new float[]{0, 0, 0};
            List<Face> faces = new ArrayList<>();
            JsonObject faceJson = cube.getAsJsonObject("faces");
            for (String name : faceJson.keySet()) {
                Direction direction = Direction.byName(name);
                if (direction == null) continue;
                var uvJson = faceJson.getAsJsonObject(name).getAsJsonArray("uv");
                faces.add(new Face(direction, new float[]{uvJson.get(0).getAsFloat(), uvJson.get(1).getAsFloat(), uvJson.get(2).getAsFloat(), uvJson.get(3).getAsFloat()}));
            }
            cubes.add(new Cube(from, to, origin, rotation, faces));
        }
        return new DawnWeaponGeometry(width, height, cubes);
    }

    private static float[] vec(JsonObject json, String key) {
        var array = json.getAsJsonArray(key);
        return new float[]{array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat()};
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, net.minecraft.client.resources.model.ModelBaker baker, Function<net.minecraft.client.resources.model.Material, TextureAtlasSprite> spriteGetter, net.minecraft.client.resources.model.ModelState modelState, ItemOverrides overrides) {
        TextureAtlasSprite sprite = spriteGetter.apply(context.getMaterial("weapon"));
        List<Vector3f> corners = new ArrayList<>();
        for (Cube cube : cubes) {
            for (int i = 0; i < 8; i++) corners.add(rotate(cube.corner(i), cube.origin, cube.rotation));
        }
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxZ = -Float.MAX_VALUE;
        for (Vector3f corner : corners) {
            minX = Math.min(minX, corner.x);
            minY = Math.min(minY, corner.y);
            minZ = Math.min(minZ, corner.z);
            maxX = Math.max(maxX, corner.x);
            maxZ = Math.max(maxZ, corner.z);
        }
        float centerX = (minX + maxX) * 0.5F;
        float centerZ = (minZ + maxZ) * 0.5F;
        List<BakedQuad> quads = new ArrayList<>();
        for (Cube cube : cubes) {
            for (Face face : cube.faces) {
                int[] data = new int[32];
                Vector3f normal = rotate(new Vector3f(face.direction.getStepX(), face.direction.getStepY(), face.direction.getStepZ()), new float[]{0, 0, 0}, cube.rotation);
                for (int vertex = 0; vertex < 4; vertex++) {
                    FaceInfo.VertexInfo info = FaceInfo.fromFacing(face.direction).getVertexInfo(vertex);
                    float[] box = cube.box();
                    Vector3f point = rotate(new Vector3f(box[info.xFace], box[info.yFace], box[info.zFace]), cube.origin, cube.rotation);
                    point.set((point.x - centerX) / 16.0F, (point.y - minY) / 16.0F, (point.z - centerZ) / 16.0F);
                    float u = (vertex == 0 || vertex == 1) ? face.uv[0] : face.uv[2];
                    float v = (vertex == 0 || vertex == 3) ? face.uv[1] : face.uv[3];
                    int index = vertex * 8;
                    data[index] = Float.floatToRawIntBits(point.x);
                    data[index + 1] = Float.floatToRawIntBits(point.y);
                    data[index + 2] = Float.floatToRawIntBits(point.z);
                    data[index + 3] = -1;
                    data[index + 4] = Float.floatToRawIntBits(sprite.getU(u / textureWidth));
                    data[index + 5] = Float.floatToRawIntBits(sprite.getV(v / textureHeight));
                }
                quads.add(new BakedQuad(data, -1, Direction.getNearest(normal.x, normal.y, normal.z), sprite, true, false));
            }
        }
        Map<Direction, List<BakedQuad>> culled = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) culled.put(direction, List.of());
        ResourceLocation renderType = context.getRenderTypeHint();
        RenderTypeGroup renderTypes = renderType == null ? RenderTypeGroup.EMPTY : context.getRenderType(renderType);
        return new SimpleBakedModel(List.copyOf(quads), culled, false, false, true, sprite, context.getTransforms(), ItemOverrides.EMPTY, renderTypes);
    }

    private static Vector3f rotate(Vector3f point, float[] origin, float[] rotation) {
        Vector3f vector = new Vector3f(point).sub(origin[0], origin[1], origin[2]);
        if (rotation[0] != 0) vector.rotateX(rotation[0] * Mth.DEG_TO_RAD);
        if (rotation[1] != 0) vector.rotateY(rotation[1] * Mth.DEG_TO_RAD);
        if (rotation[2] != 0) vector.rotateZ(rotation[2] * Mth.DEG_TO_RAD);
        return vector.add(origin[0], origin[1], origin[2]);
    }

    private record Cube(float[] from, float[] to, float[] origin, float[] rotation, List<Face> faces) {
        private float[] box() {
            float[] box = new float[6];
            box[Direction.WEST.get3DDataValue()] = Math.min(from[0], to[0]);
            box[Direction.EAST.get3DDataValue()] = Math.max(from[0], to[0]);
            box[Direction.DOWN.get3DDataValue()] = Math.min(from[1], to[1]);
            box[Direction.UP.get3DDataValue()] = Math.max(from[1], to[1]);
            box[Direction.NORTH.get3DDataValue()] = Math.min(from[2], to[2]);
            box[Direction.SOUTH.get3DDataValue()] = Math.max(from[2], to[2]);
            return box;
        }

        private Vector3f corner(int index) {
            float[] box = box();
            int x = (index & 1) == 0 ? Direction.WEST.get3DDataValue() : Direction.EAST.get3DDataValue();
            int y = (index & 2) == 0 ? Direction.DOWN.get3DDataValue() : Direction.UP.get3DDataValue();
            int z = (index & 4) == 0 ? Direction.NORTH.get3DDataValue() : Direction.SOUTH.get3DDataValue();
            return new Vector3f(box[x], box[y], box[z]);
        }
    }

    private record Face(Direction direction, float[] uv) {
    }
}

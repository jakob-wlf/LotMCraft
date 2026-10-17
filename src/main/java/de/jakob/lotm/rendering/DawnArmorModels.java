package de.jakob.lotm.rendering;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DawnArmorModels {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "textures/models/armor/dawn_armor.png");
    private static final ResourceLocation WORN = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "models/armor/dawn_armor.json");
    private static volatile Map<String, List<Quad>> bones;
    private static volatile boolean failed;

    private DawnArmorModels() {
    }

    public static void render(PoseStack pose, VertexConsumer consumer, int light, int overlay, ModelPart part, String bone) {
        if (!part.visible) return;
        List<Quad> quads = bone(bone);
        if (quads.isEmpty()) return;
        pose.pushPose();
        part.translateAndRotate(pose);
        pose.scale(1.0F / 16.0F, 1.0F / 16.0F, 1.0F / 16.0F);
        Matrix4f matrix = pose.last().pose();
        for (Quad quad : quads) {
            Vector3f normal = new Vector3f(quad.nx, quad.ny, quad.nz);
            pose.last().normal().transform(normal);
            normal.normalize();
            for (int vertex = 0; vertex < 4; vertex++) {
                int position = vertex * 3;
                int uv = vertex * 2;
                consumer.addVertex(matrix, quad.pos[position], quad.pos[position + 1], quad.pos[position + 2])
                        .setColor(1.0F, 1.0F, 1.0F, 1.0F)
                        .setUv(quad.uv[uv], quad.uv[uv + 1])
                        .setOverlay(overlay)
                        .setLight(light)
                        .setNormal(normal.x, normal.y, normal.z);
            }
        }
        pose.popPose();
    }

    private static List<Quad> bone(String name) {
        Map<String, List<Quad>> loaded = bones;
        if (loaded == null) {
            if (failed) return List.of();
            try {
                loaded = load();
            } catch (Exception exception) {
                failed = true;
                LOTMCraft.LOGGER.error("Failed to load dawn armor", exception);
                return List.of();
            }
            bones = loaded;
        }
        return loaded.getOrDefault(name, List.of());
    }

    private static Map<String, List<Quad>> load() throws IOException {
        var resource = Minecraft.getInstance().getResourceManager().getResource(WORN).orElseThrow();
        try (Reader reader = resource.openAsReader()) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            float width = 64;
            float height = 64;
            if (json.has("texture_size")) {
                var size = json.getAsJsonArray("texture_size");
                width = size.get(0).getAsFloat();
                height = size.get(1).getAsFloat();
            }
            Map<String, List<Quad>> loaded = new HashMap<>();
            JsonObject boneJson = json.getAsJsonObject("bones");
            for (String name : boneJson.keySet()) {
                List<Quad> quads = new ArrayList<>();
                for (var element : boneJson.getAsJsonArray(name)) {
                    quads.addAll(bake(element.getAsJsonObject(), width, height));
                }
                loaded.put(name, List.copyOf(quads));
            }
            return Map.copyOf(loaded);
        }
    }

    private static List<Quad> bake(JsonObject cube, float textureWidth, float textureHeight) {
        float[] from = vec(cube, "from");
        float[] to = vec(cube, "to");
        float inflate = cube.has("inflate") ? cube.get("inflate").getAsFloat() : 0;
        float[] origin = cube.has("origin") ? vec(cube, "origin") : new float[]{(from[0] + to[0]) / 2.0F, (from[1] + to[1]) / 2.0F, (from[2] + to[2]) / 2.0F};
        float[] rotation = cube.has("rotation") ? vec(cube, "rotation") : new float[]{0, 0, 0};
        float[] pivot = vec(cube, "pivot");
        float[] box = new float[6];
        box[Direction.WEST.get3DDataValue()] = Math.min(from[0], to[0]) - inflate;
        box[Direction.EAST.get3DDataValue()] = Math.max(from[0], to[0]) + inflate;
        box[Direction.DOWN.get3DDataValue()] = Math.min(from[1], to[1]) - inflate;
        box[Direction.UP.get3DDataValue()] = Math.max(from[1], to[1]) + inflate;
        box[Direction.NORTH.get3DDataValue()] = Math.min(from[2], to[2]) - inflate;
        box[Direction.SOUTH.get3DDataValue()] = Math.max(from[2], to[2]) + inflate;
        List<Quad> quads = new ArrayList<>();
        JsonObject faces = cube.getAsJsonObject("faces");
        for (String name : faces.keySet()) {
            Direction direction = Direction.byName(name);
            if (direction == null) continue;
            var uvJson = faces.getAsJsonObject(name).getAsJsonArray("uv");
            float[] faceUv = new float[]{uvJson.get(0).getAsFloat(), uvJson.get(1).getAsFloat(), uvJson.get(2).getAsFloat(), uvJson.get(3).getAsFloat()};
            float[] pos = new float[12];
            float[] uv = new float[8];
            for (int vertex = 0; vertex < 4; vertex++) {
                FaceInfo.VertexInfo info = FaceInfo.fromFacing(direction).getVertexInfo(vertex);
                Vector3f point = rotate(new Vector3f(box[info.xFace], box[info.yFace], box[info.zFace]), origin, rotation);
                pos[vertex * 3] = -(point.x - pivot[0]);
                pos[vertex * 3 + 1] = -(point.y - pivot[1]);
                pos[vertex * 3 + 2] = point.z - pivot[2];
                uv[vertex * 2] = ((vertex == 0 || vertex == 1) ? faceUv[0] : faceUv[2]) / textureWidth;
                uv[vertex * 2 + 1] = ((vertex == 0 || vertex == 3) ? faceUv[1] : faceUv[3]) / textureHeight;
            }
            Vector3f normal = rotate(new Vector3f(direction.getStepX(), direction.getStepY(), direction.getStepZ()), new float[]{0, 0, 0}, rotation);
            quads.add(new Quad(pos, uv, -normal.x, -normal.y, normal.z));
        }
        return quads;
    }

    private static float[] vec(JsonObject json, String key) {
        var array = json.getAsJsonArray(key);
        return new float[]{array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat()};
    }

    private static Vector3f rotate(Vector3f point, float[] origin, float[] rotation) {
        Vector3f vector = new Vector3f(point).sub(origin[0], origin[1], origin[2]);
        if (rotation[0] != 0) vector.rotateX(rotation[0] * Mth.DEG_TO_RAD);
        if (rotation[1] != 0) vector.rotateY(rotation[1] * Mth.DEG_TO_RAD);
        if (rotation[2] != 0) vector.rotateZ(rotation[2] * Mth.DEG_TO_RAD);
        return vector.add(origin[0], origin[1], origin[2]);
    }

    private record Quad(float[] pos, float[] uv, float nx, float ny, float nz) {
    }
}

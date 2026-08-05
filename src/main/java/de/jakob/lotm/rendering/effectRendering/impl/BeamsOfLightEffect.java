package de.jakob.lotm.rendering.effectRendering.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import de.jakob.lotm.rendering.effectRendering.ActiveMovableEffect;
import de.jakob.lotm.util.data.Location;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Beams of light (black, white, purple) radiating outward in all directions
 * from a single point in 3D space.
 *
 * <p><b>Geometry:</b> Each beam is a camera-aligned ribbon quad. Its near end
 * sits at the origin with full width; it tapers to a transparent point at
 * {@code length} units out. The billboard axis is computed as the component of
 * the camera-right vector that is perpendicular to the beam direction, which
 * keeps the quad face-on regardless of viewing angle.
 *
 * <p><b>Animation:</b> All beams share a global Y-axis precession that
 * completes exactly one full revolution per {@link #LOOP_PERIOD}, guaranteeing
 * a seamless loop. Individual beams also breathe (alpha pulse) with unique
 * phase offsets.
 *
 * <p><b>Blend passes:</b>
 * <ol>
 *   <li>Standard blend — dark/void beams that visibly darken whatever is
 *       behind them.</li>
 *   <li>Additive blend — white and purple beams that brighten and glow
 *       wherever they overlap.</li>
 * </ol>
 */
public class BeamsOfLightEffect extends ActiveMovableEffect {

    private static int LOOP_PERIOD = 120;

    // ─── Counts ───────────────────────────────────────────────────────────────
    /** White + purple beams — additive pass. */
    private static final int BRIGHT_COUNT = 40;
    /** Dark / void beams — standard-blend pass. */
    private static final int DARK_COUNT   = 20;

    private final RandomSource rng = RandomSource.create();

    // ─── Bright beam baked state ──────────────────────────────────────────────
    /** Baked unit-length direction vectors (world-space, pre-normalised). */
    private final float[] bDirX    = new float[BRIGHT_COUNT];
    private final float[] bDirY    = new float[BRIGHT_COUNT];
    private final float[] bDirZ    = new float[BRIGHT_COUNT];
    private final float[] bLength  = new float[BRIGHT_COUNT];
    /** Half-width of the ribbon at the near (origin) end. */
    private final float[] bHalfW   = new float[BRIGHT_COUNT];
    /** Phase offset for the alpha-pulse sine wave. */
    private final float[] bPhase   = new float[BRIGHT_COUNT];
    /** 0 = white, 1 = purple. */
    private final int[]   bColor   = new int[BRIGHT_COUNT];

    // ─── Dark beam baked state ────────────────────────────────────────────────
    private final float[] dDirX    = new float[DARK_COUNT];
    private final float[] dDirY    = new float[DARK_COUNT];
    private final float[] dDirZ    = new float[DARK_COUNT];
    private final float[] dLength  = new float[DARK_COUNT];
    private final float[] dHalfW   = new float[DARK_COUNT];
    private final float[] dPhase   = new float[DARK_COUNT];

    // ─────────────────────────────────────────────────────────────────────────

    public BeamsOfLightEffect(Location location, int duration) {
        // Infinite duration — caller controls lifetime via cancel().
        super(location, duration);
        LOOP_PERIOD = duration;
        bakeBeams();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Baking
    // ─────────────────────────────────────────────────────────────────────────

    private void bakeBeams() {
        for (int i = 0; i < BRIGHT_COUNT; i++) {
            float[] dir = uniformSphereDir();
            bDirX[i]   = dir[0];
            bDirY[i]   = dir[1];
            bDirZ[i]   = dir[2];
            bLength[i] = 5f + rng.nextFloat() * 8f;
            bHalfW[i]  = 0.04f + rng.nextFloat() * 0.10f;
            bPhase[i]  = rng.nextFloat() * Mth.TWO_PI;
            bColor[i]  = rng.nextFloat() < 0.55f ? 0 : 1; // 0=white 1=purple
        }

        for (int i = 0; i < DARK_COUNT; i++) {
            float[] dir = uniformSphereDir();
            dDirX[i]   = dir[0];
            dDirY[i]   = dir[1];
            dDirZ[i]   = dir[2];
            dLength[i] = 4f + rng.nextFloat() * 6f;
            dHalfW[i]  = 0.06f + rng.nextFloat() * 0.10f;
            dPhase[i]  = rng.nextFloat() * Mth.TWO_PI;
        }
    }

    /**
     * Samples a uniformly-distributed direction on the unit sphere.
     * phi = acos(1 - 2u) gives uniform area coverage — naive phi = u*PI
     * over-samples the poles.
     */
    private float[] uniformSphereDir() {
        float theta = rng.nextFloat() * Mth.TWO_PI;
        float phi   = (float) Math.acos(1f - 2f * rng.nextFloat());
        float sinPhi = Mth.sin(phi);
        return new float[]{
                sinPhi * Mth.cos(theta),
                Mth.cos(phi),
                sinPhi * Mth.sin(theta)
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Main render entry
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void render(PoseStack poseStack, float tick) {
        if (Minecraft.getInstance().level == null) return;

        // Clamp age into one loop period (ensures seamless looping).
        float age = tick % LOOP_PERIOD;

        // Global Y-axis precession: exactly one full revolution per loop so
        // the start and end states are identical — perfect loop.
        float precession = age / LOOP_PERIOD * Mth.TWO_PI;
        float cosP = Mth.cos(precession);
        float sinP = Mth.sin(precession);

        // Camera orientation vectors for billboard math.
        Quaternionf camRot = Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
        Vector3f camRight  = new Vector3f(1f, 0f, 0f).rotate(camRot);
        Vector3f camUp     = new Vector3f(0f, 1f, 0f).rotate(camRot);

        poseStack.pushPose();
        poseStack.translate(location.getPosition().x, location.getPosition().y, location.getPosition().z);
        Matrix4f m = poseStack.last().pose();

        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();

        // Pass 1: standard blend — dark beams darken the scene behind them.
        RenderSystem.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        renderDarkBeams(m, age, cosP, sinP, camRight, camUp);

        // Pass 2: additive blend — bright beams glow and bloom.
        RenderSystem.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE);
        renderBrightBeams(m, age, cosP, sinP, camRight, camUp);
        renderCore(m, age, camRight, camUp);

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Per-layer renderers
    // ─────────────────────────────────────────────────────────────────────────

    private void renderBrightBeams(Matrix4f m, float age,
                                   float cosP, float sinP,
                                   Vector3f camRight, Vector3f camUp) {
        BufferBuilder buf = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int drawn = 0;

        for (int i = 0; i < BRIGHT_COUNT; i++) {
            // Apply global Y-axis precession to the baked direction.
            float dx =  cosP * bDirX[i] + sinP * bDirZ[i];
            float dy =  bDirY[i];
            float dz = -sinP * bDirX[i] + cosP * bDirZ[i];

            // Alpha breath — full sine cycle maps onto LOOP_PERIOD for seamlessness.
            float alpha = 0.30f + 0.70f * (0.5f + 0.5f * Mth.sin(
                    age / LOOP_PERIOD * Mth.TWO_PI + bPhase[i]));
            if (alpha < 0.01f) continue;

            float r, g, b;
            if (bColor[i] == 0) { r = 1f;    g = 1f;    b = 1f;    } // white
            else                 { r = 0.65f; g = 0.05f; b = 0.95f; } // purple

            beamQuad(buf, m, dx, dy, dz, bLength[i], bHalfW[i], camRight, camUp,
                    r, g, b, alpha);
            drawn++;
        }

        if (drawn > 0) BufferUploader.drawWithShader(buf.buildOrThrow());
    }

    private void renderDarkBeams(Matrix4f m, float age,
                                 float cosP, float sinP,
                                 Vector3f camRight, Vector3f camUp) {
        BufferBuilder buf = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int drawn = 0;

        for (int i = 0; i < DARK_COUNT; i++) {
            float dx =  cosP * dDirX[i] + sinP * dDirZ[i];
            float dy =  dDirY[i];
            float dz = -sinP * dDirX[i] + cosP * dDirZ[i];

            float alpha = 0.20f + 0.50f * (0.5f + 0.5f * Mth.sin(
                    age / LOOP_PERIOD * Mth.TWO_PI + dPhase[i]));

            // Near-black void colour — darkens the scene behind each beam.
            beamQuad(buf, m, dx, dy, dz, dLength[i], dHalfW[i], camRight, camUp,
                    0.02f, 0f, 0.05f, alpha);
            drawn++;
        }

        if (drawn > 0) BufferUploader.drawWithShader(buf.buildOrThrow());
    }

    /**
     * Three concentric billboard quads at the origin: white inner orb,
     * lavender mid-corona, and deep-purple outer corona.
     * All pulse harmonics are multiples of the loop period — seamless loop.
     */
    private void renderCore(Matrix4f m, float age, Vector3f camRight, Vector3f camUp) {
        float t = age / LOOP_PERIOD * Mth.TWO_PI;

        float innerPulse = 0.80f + 0.20f * Mth.sin(t * 3f);
        singleQuad(m, 0f, 0f, 0f, 0.30f * innerPulse, camRight, camUp,
                1f, 1f, 1f, 0.95f);

        float midPulse = 0.70f + 0.30f * Mth.sin(t * 1.5f);
        singleQuad(m, 0f, 0f, 0f, 0.80f * midPulse, camRight, camUp,
                0.80f, 0.50f, 1f, 0.55f);

        float outerPulse = 0.60f + 0.40f * Mth.sin(t + 1.1f);
        singleQuad(m, 0f, 0f, 0f, 1.60f * outerPulse, camRight, camUp,
                0.40f, 0f, 0.70f, 0.30f);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Geometry: 3-D camera-aligned ribbon beam
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Renders a single tapered ribbon quad along an arbitrary 3-D direction.
     *
     * <p>The ribbon is camera-facing: its width axis is the component of the
     * camera-right vector that is perpendicular to the beam direction. When the
     * beam points directly at/away from the camera (degenerate case), the
     * algorithm falls back to the camera-up vector.
     *
     * <p>Vertex layout (QUADS winding):
     * <pre>
     *  near-left ─── near-right    &lt;- origin end, full width, full alpha
     *      |                |
     *  far-left  ─── far-right     &lt;- tip end, near-zero width, alpha = 0
     * </pre>
     *
     * @param dx,dy,dz  beam direction (need not be unit-length — normalised internally)
     * @param length    beam length in world units
     * @param halfW     half-width of the ribbon at the near end
     */
    private static void beamQuad(
            BufferBuilder buf, Matrix4f m,
            float dx, float dy, float dz,
            float length, float halfW,
            Vector3f camRight, Vector3f camUp,
            float r, float g, float b, float a) {

        // Normalise direction.
        float dLen = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dLen < 0.0001f) return;
        dx /= dLen; dy /= dLen; dz /= dLen;

        // ── Billboard perpendicular axis ───────────────────────────────────────
        // Project camRight onto the plane perpendicular to the beam direction.
        float dotR = dx * camRight.x + dy * camRight.y + dz * camRight.z;
        float px   = camRight.x - dotR * dx;
        float py   = camRight.y - dotR * dy;
        float pz   = camRight.z - dotR * dz;

        float pLen = (float) Math.sqrt(px * px + py * py + pz * pz);
        if (pLen < 0.01f) {
            // Degenerate: beam aims straight at/away from camera — use up instead.
            float dotU = dx * camUp.x + dy * camUp.y + dz * camUp.z;
            px = camUp.x - dotU * dx;
            py = camUp.y - dotU * dy;
            pz = camUp.z - dotU * dz;
            pLen = (float) Math.sqrt(px * px + py * py + pz * pz);
            if (pLen < 0.001f) return; // fully degenerate — skip
        }
        float invP = halfW / pLen;
        px *= invP; py *= invP; pz *= invP;

        // ── Vertex positions ───────────────────────────────────────────────────
        // Offset the near end slightly along the beam so the core glow hides
        // the convergence point where all beams meet.
        float nearOff = 0.05f;
        float nearX = dx * nearOff, nearY = dy * nearOff, nearZ = dz * nearOff;

        // Tip: tapers to ~3 % of base width so it reads as a point.
        float tipX = dx * length, tipY = dy * length, tipZ = dz * length;
        float tipW = 0.03f;
        float tpx = px * tipW, tpy = py * tipW, tpz = pz * tipW;

        // Winding: near-left, near-right, far-right, far-left.
        buf.addVertex(m, nearX - px, nearY - py, nearZ - pz).setColor(r, g, b, a);
        buf.addVertex(m, nearX + px, nearY + py, nearZ + pz).setColor(r, g, b, a);
        buf.addVertex(m, tipX  + tpx, tipY + tpy, tipZ + tpz).setColor(r, g, b, 0f);
        buf.addVertex(m, tipX  - tpx, tipY - tpy, tipZ - tpz).setColor(r, g, b, 0f);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Geometry helpers
    // ─────────────────────────────────────────────────────────────────────────

    private static void quad(
            BufferBuilder buf, Matrix4f m,
            float cx, float cy, float cz, float size,
            Vector3f right, Vector3f up,
            float r, float g, float b, float a) {

        float rx = right.x * size, ry = right.y * size, rz = right.z * size;
        float ux = up.x    * size, uy = up.y    * size, uz = up.z    * size;

        buf.addVertex(m, cx - rx - ux, cy - ry - uy, cz - rz - uz).setColor(r, g, b, a);
        buf.addVertex(m, cx + rx - ux, cy + ry - uy, cz + rz - uz).setColor(r, g, b, a);
        buf.addVertex(m, cx + rx + ux, cy + ry + uy, cz + rz + uz).setColor(r, g, b, a);
        buf.addVertex(m, cx - rx + ux, cy - ry + uy, cz - rz + uz).setColor(r, g, b, a);
    }

    private static void singleQuad(
            Matrix4f m,
            float cx, float cy, float cz, float size,
            Vector3f right, Vector3f up,
            float r, float g, float b, float a) {

        BufferBuilder buf = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        quad(buf, m, cx, cy, cz, size, right, up, r, g, b, a);
        BufferUploader.drawWithShader(buf.buildOrThrow());
    }
}
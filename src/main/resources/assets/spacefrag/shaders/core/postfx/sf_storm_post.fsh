#version 150
// Space-Time Storm v2 screen looks (one shader, look chosen by the Mode constant of each json):
// 0 VIOLET GRADE (the sky darkens and turns violet while the storm is up), 1 VIOLET INK impact frame,
// 2 VIOLET LINE ART impact frame, 3 VIOLET NEGATIVE impact frame, 4 TIME STUTTER. Strength = Photon effect weight (0 = untouched).
uniform sampler2D DiffuseSampler;
uniform float GameTime;
uniform float Strength;
uniform float Mode;

in vec2 texCoord;
out vec4 fragColor;

float lum(vec3 c) { return dot(c, vec3(0.299, 0.587, 0.114)); }
vec3 tex(vec2 uv) { return texture(DiffuseSampler, clamp(uv, 0.001, 0.999)).rgb; }

const vec3 NIGHT = vec3(0.02, 0.015, 0.05);
const vec3 VIOLET = vec3(0.36, 0.27, 0.84);
const vec3 LILAC = vec3(0.65, 0.55, 1.0);
const vec3 WHITE = vec3(0.94, 0.92, 1.0);

void main() {
    float s = clamp(Strength, 0.0, 1.0);
    vec2 size = vec2(textureSize(DiffuseSampler, 0));
    vec2 px = 1.0 / size;
    vec2 uv = texCoord;
    vec3 src = tex(uv);
    float L = lum(src);
    float Lg = pow(clamp(L, 0.0, 1.0), 0.55);
    vec3 col = src;
    int m = int(Mode + 0.5);
    if (m == 0) {
        // keep bright things (the effect's own light) bright, pull everything else down into violet night
        vec3 tinted = mix(vec3(L), src, 0.55) * vec3(0.72, 0.62, 1.0);
        float keep = smoothstep(0.55, 0.95, L);
        col = mix(tinted * 0.55 + NIGHT * 0.5, src, keep);
        vec2 d = uv - 0.5;
        col *= 1.0 - dot(d, d) * 0.9;
    } else if (m == 1) {
        float a = smoothstep(0.44, 0.5, Lg);
        col = mix(vec3(0.0), VIOLET * (0.6 + Lg), a);
        col = mix(col, WHITE, smoothstep(0.78, 0.93, Lg));
    } else if (m == 2) {
        float tl = lum(tex(uv + px * vec2(-1.0, -1.0))), t = lum(tex(uv + px * vec2(0.0, -1.0))), tr = lum(tex(uv + px * vec2(1.0, -1.0)));
        float l = lum(tex(uv + px * vec2(-1.0, 0.0))), r2 = lum(tex(uv + px * vec2(1.0, 0.0)));
        float bl = lum(tex(uv + px * vec2(-1.0, 1.0))), b = lum(tex(uv + px * vec2(0.0, 1.0))), br = lum(tex(uv + px * vec2(1.0, 1.0)));
        float gx = (tr + 2.0 * r2 + br) - (tl + 2.0 * l + bl);
        float gy = (bl + 2.0 * b + br) - (tl + 2.0 * t + tr);
        float e = smoothstep(0.05, 0.22, length(vec2(gx, gy)) * 1.6);
        col = mix(vec3(0.0), mix(LILAC * 1.3, WHITE, e * e), e);
    } else if (m == 3) {
        col = vec3(1.0 - Lg) * mix(LILAC, WHITE, 0.4 * (1.0 - Lg)) + vec3(0.02, 0.0, 0.05);
    } else if (m == 6) {
        // TYRANT INK IMPACT FRAME: white paper, black ink where the picture is dark, radial speed lines from the centre
        vec2 d = (uv - 0.5) * vec2(size.x / size.y, 1.0);
        float r = length(d);
        float ang = atan(d.y, d.x);
        float cell = floor(ang * 48.0 + 0.5);
        float hv = fract(sin(cell * 91.7 + 13.1) * 43758.5453);
        float lines = step(0.55, hv) * smoothstep(0.12, 0.45, r) * smoothstep(0.0, 0.12, abs(fract(ang * 48.0 + 0.5) - 0.5));
        float ink = 1.0 - smoothstep(0.30, 0.46, Lg);
        float paper = mix(1.0, 0.0, max(ink, lines * 0.85));
        col = mix(vec3(0.02, 0.03, 0.05), vec3(0.97, 0.98, 1.0), paper);
    } else if (m == 5) {
        // TYRANT GRADE: storm night - the world goes black-navy and cold, keeps the effect's own light (lightning, foam)
        vec3 cold = mix(vec3(L), src, 0.45) * vec3(0.55, 0.72, 1.0);
        float keep = smoothstep(0.6, 0.95, L);
        col = mix(cold * 0.5 + vec3(0.004, 0.01, 0.025), src * vec3(0.85, 0.95, 1.05), keep);
        vec2 d = uv - 0.5;
        col *= 1.0 - dot(d, d) * 1.1;
    } else {
        // TIME STUTTER (every damage pulse): the frame splits in time (RGB ghosts pushed out from the centre), rows of the
        // picture slip sideways, and the colour drains toward violet-grey
        float row = floor(uv.y * 90.0);
        float slip = (fract(sin(row * 12.9898 + floor(mod(GameTime * 24000.0, 1000.0))) * 43758.5453) - 0.5) * 0.014;
        vec2 d = uv - 0.5;
        vec2 u2 = uv + vec2(slip, 0.0);
        vec3 c = vec3(tex(u2 + d * 0.014).r, tex(u2).g, tex(u2 - d * 0.014).b);
        float l = lum(c);
        col = mix(c, vec3(l) * vec3(0.8, 0.72, 1.12), 0.55);
        col *= 1.0 - dot(d, d) * 0.7;
    }
    fragColor = vec4(mix(src, col, s), 1.0);
}

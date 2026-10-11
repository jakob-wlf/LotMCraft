#version 150
// Space Fragmentation — procedural planet surface. One shader, several surface kinds (uniform Kind):
//   0 rock   1 gas giant   2 cracked lava   3 ice   4 moon   5 atmosphere shell (additive)
// Needs the model particle path with GPU instancing so vObj stays glued to the mesh.

uniform float GameTime;
uniform vec4 ColorA;      // base / low
uniform vec4 ColorB;      // base / high
uniform vec4 ColorC;      // accent (bands, snow, maria)
uniform vec4 CrackColor;  // fissure emission colour
uniform vec4 RimColor;
uniform vec4 LightColor;
uniform vec3 LightDir;    // world-space direction TOWARD the light
uniform float Kind;
uniform float Seed;
uniform float Scale;      // noise frequency
uniform float Crack;      // 0..1 how broken the planet is (can also be driven by particle colour red)
uniform float Emission;   // fissure brightness
uniform float Ambient;
uniform float RimPower;
uniform float RimIntensity;
uniform float BandCount;
uniform float TimeSpeed;
uniform float BumpStrength;
uniform float BakeMode;   // 0 = normal rendering, 1 = albedo only, 2 = emission only (used by the offline texture bake)

in vec3 vObj;
in vec3 vN;
in vec3 vPos;
in vec2 uv;
in vec4 vColor;
out vec4 fragColor;

float hash13(vec3 p) {
    p = fract(p * 0.1031);
    p += dot(p, p.zyx + 31.32);
    return fract((p.x + p.y) * p.z);
}
vec3 hash33(vec3 p) {
    p = vec3(dot(p, vec3(127.1, 311.7, 74.7)), dot(p, vec3(269.5, 183.3, 246.1)), dot(p, vec3(113.5, 271.9, 124.6)));
    return fract(sin(p) * 43758.5453123);
}
float vnoise(vec3 p) {
    vec3 i = floor(p), f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(mix(hash13(i), hash13(i + vec3(1, 0, 0)), f.x), mix(hash13(i + vec3(0, 1, 0)), hash13(i + vec3(1, 1, 0)), f.x), f.y),
               mix(mix(hash13(i + vec3(0, 0, 1)), hash13(i + vec3(1, 0, 1)), f.x), mix(hash13(i + vec3(0, 1, 1)), hash13(i + vec3(1, 1, 1)), f.x), f.y), f.z);
}
float fbm(vec3 p) {
    float a = 0.5, s = 0.0;
    for (int i = 0; i < 5; i++) { s += a * vnoise(p); p = p * 2.03 + 11.7; a *= 0.5; }
    return s;
}
// Worley: x = nearest feature distance, y = second nearest
vec2 worley(vec3 p) {
    vec3 ip = floor(p), fp = fract(p);
    float d1 = 9.0, d2 = 9.0;
    for (int z = -1; z <= 1; z++) for (int y = -1; y <= 1; y++) for (int x = -1; x <= 1; x++) {
        vec3 g = vec3(x, y, z);
        vec3 o = hash33(ip + g);
        float d = length(g + o - fp);
        if (d < d1) { d2 = d1; d1 = d; } else if (d < d2) { d2 = d; }
    }
    return vec2(d1, d2);
}
float fissure(vec3 p, float width) {
    vec2 w = worley(p);
    return 1.0 - smoothstep(0.0, width, w.y - w.x);
}

// surface height used for the derivative bump map
float heightOf(vec3 p, float kind) {
    if (kind < 0.5 || kind > 3.5) {                       // rock / moon: fbm + craters
        vec2 w = worley(p * 1.4);
        float crater = smoothstep(0.50, 0.06, w.x) * 0.55 - smoothstep(0.34, 0.46, w.x) * smoothstep(0.62, 0.46, w.x) * 0.20;
        return fbm(p * 1.3) * 0.8 - crater;
    }
    if (kind < 1.5) return 0.0;                           // gas giants stay smooth
    if (kind < 2.5) return fbm(p * 1.8) * 0.7 - fissure(p * 0.9, 0.10) * 0.45;   // lava rock
    return fbm(p * 1.6) * 0.3 - fissure(p * 1.6, 0.10) * 0.12;                  // ice
}

void main() {
    vec3 N = normalize(vN);
    vec3 V = normalize(-vPos);
    float ndv = clamp(dot(N, V), 0.0, 1.0);
    float rim = pow(1.0 - ndv, RimPower) * RimIntensity;

    if (Kind > 4.5) {                                     // atmosphere shell
        float a = clamp(rim, 0.0, 1.0) * vColor.a;
        if (a < 0.004) discard;
        fragColor = vec4(RimColor.rgb * a, a);
        return;
    }

    float t = GameTime * 1200.0 * TimeSpeed;
    vec3 po = normalize(vObj) * Scale + Seed;
    // particle colour is a control channel: r = brightness, g = 1-crack drive (animate g down to break the planet), a = opacity
    float crack = clamp(Crack + (1.0 - vColor.g), 0.0, 1.0);

    vec3 albedo = ColorA.rgb;
    vec3 emit = vec3(0.0);
    float spec = 0.0;

    if (Kind < 0.5) {                                      // ---- rock
        float h = fbm(po * 1.3);
        albedo = mix(ColorA.rgb, ColorB.rgb, smoothstep(0.30, 0.72, h));
        vec2 w = worley(po * 1.4);
        albedo *= 1.0 - 0.35 * smoothstep(0.30, 0.12, w.x);
        albedo = mix(albedo, ColorC.rgb, smoothstep(0.62, 0.8, fbm(po * 3.1 + 5.0)) * 0.5);
        float f = fissure(po * 0.9, mix(0.03, 0.22, crack));
        emit = CrackColor.rgb * Emission * (pow(f, 3.0) + f * 0.25) * crack * (0.7 + 0.3 * vnoise(po * 4.0 + t));
        albedo *= 1.0 - 0.8 * f * crack;
    } else if (Kind < 1.5) {                               // ---- gas giant
        vec3 q = vec3(vObj.x * 1.5, vObj.y * 5.0, vObj.z * 1.5) * (Scale * 0.3) + Seed;
        float warp = fbm(q * 0.8 + vec3(t * 0.06, 0.0, 0.0));
        float lat = normalize(vObj).y;
        float band = sin((lat + (warp - 0.5) * 0.22) * BandCount * 3.14159);
        float fine = fbm(vec3(lat * 14.0, warp * 3.0, Seed) + vec3(t * 0.12, 0.0, 0.0));
        albedo = mix(ColorA.rgb, ColorB.rgb, 0.5 + 0.5 * band);
        albedo = mix(albedo, ColorC.rgb, smoothstep(0.55, 0.9, fine) * 0.55);
        // a storm eye
        vec3 sp = normalize(vObj) - normalize(vec3(0.5, -0.25, 0.8));
        float storm = smoothstep(0.34, 0.08, length(sp * vec3(1.0, 1.9, 1.0)));
        albedo = mix(albedo, ColorC.rgb * 1.15, storm * 0.8);
        float f = fissure(po * 0.7, mix(0.0, 0.2, crack));
        emit = CrackColor.rgb * Emission * f * crack;
        spec = 0.12;
    } else if (Kind < 2.5) {                               // ---- cracked lava world
        float h = fbm(po * 1.8);
        albedo = mix(ColorA.rgb, ColorB.rgb, smoothstep(0.25, 0.75, h));
        float f = fissure(po * 0.9, mix(0.05, 0.16, crack));
        float f2 = fissure(po * 2.3 + 3.0, mix(0.035, 0.09, crack));
        float pulse = 0.65 + 0.35 * vnoise(po * 3.0 + vec3(t * 0.8));
        float core = pow(f, 3.2) + 0.6 * pow(f2, 3.2);
        float halo = f * 0.35 + f2 * 0.2;
        emit = (CrackColor.rgb * (core + halo) + vec3(1.0, 0.85, 0.55) * core * 0.35) * Emission * pulse * (0.45 + crack * 0.8);
        albedo *= 1.0 - 0.9 * clamp(f + f2 * 0.6, 0.0, 1.0);
    } else if (Kind < 3.5) {                               // ---- ice
        float h = fbm(po * 2.2);
        albedo = mix(ColorA.rgb, ColorB.rgb, smoothstep(0.3, 0.8, h));
        float f = fissure(po * 1.6, mix(0.025, 0.12, crack));
        albedo = mix(albedo, ColorC.rgb, f * 0.9);
        emit = CrackColor.rgb * Emission * (pow(f, 3.0) + f * 0.2) * crack * 0.7;
        spec = 0.55;
    } else {                                               // ---- moon
        float h = fbm(po * 1.3);
        float maria = smoothstep(0.42, 0.62, fbm(po * 0.55 + 2.0));
        albedo = mix(ColorB.rgb, ColorA.rgb, maria);
        vec2 w = worley(po * 1.8);
        albedo *= 1.0 - 0.4 * smoothstep(0.28, 0.10, w.x) + 0.18 * smoothstep(0.30, 0.34, w.x) * smoothstep(0.42, 0.34, w.x);
        albedo = mix(albedo, ColorC.rgb, smoothstep(0.7, 0.9, h) * 0.25);
        float f = fissure(po * 0.8, mix(0.02, 0.18, crack));
        emit = CrackColor.rgb * Emission * f * crack;
        albedo *= 1.0 - 0.7 * f * crack;
    }

    // freshly cut faces of a fractured planet (mesh UVs live in [2,3]) glow like the planet's molten heart
    float inner = step(1.5, uv.x);
    if (inner > 0.5) {
        float core = 0.55 + 0.9 * fbm(po * 4.0 + vec3(t * 0.4));
        albedo = vec3(0.015);
        emit = CrackColor.rgb * Emission * core + vec3(1.0, 0.8, 0.5) * pow(core, 4.0) * Emission * 0.15;
        spec = 0.0;
    }

    if (BakeMode > 0.5 && BakeMode < 1.5) { fragColor = vec4(albedo, 1.0); return; }
    if (BakeMode > 1.5) { fragColor = vec4(clamp(emit * 0.125, 0.0, 1.0), 1.0); return; }

    // derivative bump mapping (world space, view-independent of mesh rotation)
    float h0 = (inner > 0.5) ? 0.0 : heightOf(po, Kind);
    vec3 dpdx = dFdx(vPos), dpdy = dFdy(vPos);
    float dhdx = dFdx(h0), dhdy = dFdy(h0);
    vec3 r1 = cross(dpdy, N), r2 = cross(N, dpdx);
    float det = dot(dpdx, r1);
    vec3 grad = sign(det) * (dhdx * r1 + dhdy * r2);
    vec3 Nb = normalize(abs(det) * N - BumpStrength * grad);

    vec3 L = normalize(LightDir);
    float ndl = dot(Nb, L);
    float diff = smoothstep(-0.12, 0.65, ndl);              // soft terminator
    vec3 H = normalize(L + V);
    float sp = pow(max(dot(Nb, H), 0.0), 36.0) * spec;

    vec3 lit = albedo * (Ambient + diff * LightColor.rgb) + sp * LightColor.rgb;
    lit += RimColor.rgb * rim * (0.35 + 0.65 * diff);       // lit-side rim light
    lit += RimColor.rgb * pow(1.0 - ndv, 6.0) * 0.5;        // thin atmosphere edge everywhere
    lit += emit;

    float a = vColor.a;
    if (a < 0.003) discard;
    fragColor = vec4(lit * vColor.r, a);
}

#version 150
// Space-Time Storm v2: one shader for every solid piece (doors, columns, rocks, clockwork, crystals).
// UV regions (from the Blender kit): u in [0,1] surface, [2,3] violet glow (door rims, runes, broken edges), [4,5] bright gold relief.
// Mode: 0 stone, 1 gold metal, 2 dark violet glass (clock plate), 3 amethyst crystal.
// Every piece varies by its seed (vColor.g): stone tint (cool / warm / pale), glowing violet veins on about half the stones,
// crystal hue (indigo .. violet .. lilac .. near white .. magenta-violet).
// vColor.r = brightness of the glowing parts (pulse flares), vColor.a = alpha. Needs gpu=true (vObj = mesh space).
uniform float GameTime;
uniform vec4 BaseColor;
uniform vec4 RimColor;
uniform vec4 GlowColor;
uniform vec4 HiColor;
uniform vec3 LightDir;
uniform float Mode;
uniform float Intensity;
uniform sampler2D Texture;   // a Minecraft block texture (deepslate bricks, purpur pillar, amethyst...), mapped triplanar in mesh space
uniform float TexMix;        // 0 = procedural only, 1 = full texture detail
uniform float TexScale;      // texture repeats per mesh unit
uniform float TexHue;        // how much of the texture's own colour survives the violet grade

in vec3 vObj;
in vec3 vN;
in vec3 vPos;
in vec2 uv;
in vec4 vColor;
out vec4 fragColor;

float h31(vec3 p) { p = fract(p * 0.1031); p += dot(p, p.zyx + 31.32); return fract((p.x + p.y) * p.z); }
float vnoise(vec3 x) {
    vec3 i = floor(x); vec3 f = fract(x); f = f * f * (3.0 - 2.0 * f);
    return mix(mix(mix(h31(i), h31(i + vec3(1, 0, 0)), f.x), mix(h31(i + vec3(0, 1, 0)), h31(i + vec3(1, 1, 0)), f.x), f.y),
               mix(mix(h31(i + vec3(0, 0, 1)), h31(i + vec3(1, 0, 1)), f.x), mix(h31(i + vec3(0, 1, 1)), h31(i + vec3(1, 1, 1)), f.x), f.y), f.z);
}
vec3 meshNormal() {
    vec3 c = cross(dFdx(vObj), dFdy(vObj));
    float l = length(c);
    return l > 1e-10 ? c / l : vec3(0.0, 1.0, 0.0);
}
vec3 triplanar(vec3 p, vec3 n) {
    vec3 w = pow(abs(n), vec3(4.0)); w /= (w.x + w.y + w.z + 1e-4);
    vec3 a = texture(Texture, fract(p.zy)).rgb, b = texture(Texture, fract(p.xz)).rgb, c = texture(Texture, fract(p.xy)).rgb;
    return a * w.x + b * w.y + c * w.z;
}
vec3 crystalHue(float h) {
    vec3 a = vec3(0.22, 0.18, 0.95);      // indigo
    vec3 b = vec3(0.5, 0.32, 1.0);        // violet
    vec3 c = vec3(0.78, 0.68, 1.0);       // lilac
    vec3 d = vec3(0.95, 0.93, 1.0);       // near white
    vec3 e = vec3(0.82, 0.32, 1.0);       // magenta-violet
    if (h < 0.25) return mix(a, b, h / 0.25);
    if (h < 0.5) return mix(b, c, (h - 0.25) / 0.25);
    if (h < 0.7) return mix(c, d, (h - 0.5) / 0.2);
    return mix(b, e, (h - 0.7) / 0.3);
}

vec4 safe(vec4 c) {
    if (any(isnan(c)) || any(isinf(c))) return vec4(0.0);
    return vec4(clamp(c.rgb, 0.0, 8.0), clamp(c.a, 0.0, 1.0));
}

void main() {
    float tick = GameTime * 24000.0;
    float seed = vColor.g;
    float s1 = (int(Mode + 0.5) == 3) ? fract(uv.y) : fract(seed * 7.13 + 0.17);   // crystals: hue baked per crystal into UV v
    float s2 = fract(seed * 13.7 + 0.53);
    float bright = vColor.r;
    vec3 N = normalize(vN);
    vec3 V = normalize(-vPos);
    if (dot(N, V) < 0.0) N = -N;                      // two-sided (no culling)
    vec3 L = normalize(LightDir);
    float ndv = clamp(dot(N, V), 0.0, 1.0);
    float fres = pow(1.0 - ndv, 3.0);
    float diff = max(dot(N, L), 0.0);
    float up = N.y * 0.5 + 0.5;                       // violet light falling from the vortex
    float spec = pow(max(dot(reflect(-L, N), V), 0.0), 24.0);
    float pulse = mod(tick, 1000.0);

    if (uv.x > 3.5) {                                 // bright gold relief: numerals, ticks, hub
        vec3 col = HiColor.rgb * (0.75 + 0.6 * diff) + mix(HiColor.rgb, vec3(1.0), 0.6) * spec * 2.0 + HiColor.rgb * fres * 1.0;
        col *= 0.85 + 0.6 * bright;
        fragColor = safe(vec4(col * Intensity, vColor.a));
        return;
    }
    int m = int(Mode + 0.5);
    if (uv.x > 1.5) {                                 // violet glow band (crystal facets get their own hue)
        float flick = 0.82 + 0.18 * sin(dot(vObj, vec3(3.1, 4.7, 2.3)) * 1.2 + pulse * 0.22 + seed * 40.0);
        vec3 g = (m == 3) ? mix(crystalHue(s1), vec3(1.0), 0.35) : GlowColor.rgb;
        fragColor = safe(vec4(g * flick * (0.6 + 0.7 * bright) * Intensity * 1.7, vColor.a));   // stays violet (not clipped to white); bloom does the rest
        return;
    }
    vec3 col;
    float alpha = BaseColor.a;
    if (m == 0) {                                     // stone: tinted per piece, weathered, violet rim light, some with glowing veins
        vec3 o = vObj + seed * 17.0;
        float n = vnoise(o * 1.3) * 0.5 + vnoise(o * 4.7) * 0.3 + vnoise(o * 13.0) * 0.2;
        vec3 tint = s1 < 0.33 ? vec3(0.8, 0.85, 1.25) : (s1 < 0.66 ? vec3(1.15, 1.0, 0.92) : vec3(1.45, 1.4, 1.6));
        vec3 base = BaseColor.rgb * tint * (0.5 + 1.0 * n);
        if (TexMix > 0.0) {
            vec3 nO = meshNormal();
            vec3 tc = triplanar(vObj * TexScale, nO);
            float tl = dot(tc, vec3(0.3, 0.59, 0.11));
            vec3 tcol = mix(vec3(tl), tc, TexHue) * BaseColor.rgb * tint * 3.2;
            base = mix(base, tcol * (0.75 + 0.5 * n), TexMix);
        }
        col = base * (0.45 + 1.0 * diff) + RimColor.rgb * (fres * 1.5 + up * 0.35) * (0.8 + 0.4 * bright);
        float vn = abs(vnoise(o * 0.7) * 2.0 - 1.0);
        float vein = smoothstep(0.07, 0.0, vn) * step(0.45, s2);
        float vflick = 0.7 + 0.3 * sin(pulse * 0.15 + seed * 30.0 + dot(o, vec3(0.7)));
        col += GlowColor.rgb * vein * 1.8 * vflick * (0.6 + 0.6 * bright);
    } else if (m == 1) {                              // brushed gold, warm and bright
        float brushed = 0.8 + 0.2 * vnoise(vObj * vec3(30.0, 30.0, 2.0) + seed * 9.0);
        vec3 env = mix(RimColor.rgb * 1.6, HiColor.rgb, up);
        col = BaseColor.rgb * brushed * (0.5 + 0.95 * diff) + mix(HiColor.rgb, vec3(1.0), 0.6) * spec * 2.0 + env * fres * 1.2 + BaseColor.rgb * 0.3 * (0.6 + 0.6 * bright);
    } else if (m == 2) {                              // dark violet glass back plate, a slow swirl of light inside
        float sw = vnoise(vec3(vObj.xy * 3.0, pulse * 0.01 + seed * 5.0));
        col = BaseColor.rgb + RimColor.rgb * (fres * 1.2 + sw * 0.35) + vec3(0.8, 0.75, 1.0) * spec * 0.6;
        alpha = BaseColor.a * (0.85 + 0.15 * fres);
    } else {                                          // crystal: hue per piece, deep inside, bright faces and edges, a glowing core
        vec3 hue = crystalHue(s1);
        float n = vnoise(vObj * 2.5 + seed * 11.0);
        float core = pow(n, 2.0) * (0.7 + 0.3 * sin(pulse * 0.2 + seed * 20.0));
        if (TexMix > 0.0) {
            vec3 nO = meshNormal();
            float tl = dot(triplanar(vObj * TexScale, nO), vec3(0.3, 0.59, 0.11));
            core += TexMix * pow(tl, 2.0) * 1.4;
        }
        col = hue * (0.35 + 0.5 * diff) + mix(hue, vec3(1.0), 0.5) * fres * 1.6 + hue * core * 1.6 * (0.6 + 0.6 * bright) + vec3(1.0) * spec * 1.6;
        alpha = clamp(BaseColor.a + fres * 0.35, 0.0, 1.0);
    }
    alpha *= vColor.a;
    if (alpha < 0.003) discard;
    fragColor = safe(vec4(col * Intensity, alpha));
}

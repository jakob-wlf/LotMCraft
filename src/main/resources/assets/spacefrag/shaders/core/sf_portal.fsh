#version 150
// Space-Time Storm v2: what you see through a Door portal (st_portal_plane, uv 0..1 over the pointed-arch opening):
// a slowly turning violet galaxy with star fields at two depths and a bright inner rim.
// vColor.r = how far the portal has opened (0 shut .. 1 open: an iris from the centre), vColor.b = flare brightness,
// vColor.g = per-portal seed, vColor.a = fade.
uniform float GameTime;
uniform vec4 DeepColor;
uniform vec4 ArmColor;
uniform vec4 HotColor;
uniform float Intensity;

in vec3 vObj;
in vec3 vN;
in vec3 vPos;
in vec2 uv;
in vec4 vColor;
out vec4 fragColor;

float h21(vec2 p) { vec3 p3 = fract(vec3(p.xyx) * 0.1031); p3 += dot(p3, p3.yzx + 33.33); return fract((p3.x + p3.y) * p3.z); }
float vn(vec2 p) {
    vec2 i = floor(p); vec2 f = fract(p); f = f * f * (3.0 - 2.0 * f);
    return mix(mix(h21(i), h21(i + vec2(1, 0)), f.x), mix(h21(i + vec2(0, 1)), h21(i + vec2(1, 1)), f.x), f.y);
}
float fbm(vec2 p) {
    float s = 0.0, a = 0.5;
    for (int i = 0; i < 5; i++) { s += a * vn(p); p = mat2(1.6, 1.2, -1.2, 1.6) * p + vec2(3.1, 7.7); a *= 0.5; }
    return s;
}
float stars(vec2 p, float dens) {
    vec2 c = floor(p); vec2 f = fract(p) - 0.5;
    float h = h21(c);
    vec2 o = vec2(h21(c + 7.1), h21(c + 3.3)) - 0.5;
    float d = length(f - o * 0.7);
    return step(1.0 - dens, h) * smoothstep(0.08, 0.0, d) * (0.5 + 0.5 * h21(c + 1.9));
}

vec4 safe(vec4 c) {
    if (any(isnan(c)) || any(isinf(c))) return vec4(0.0);
    return vec4(clamp(c.rgb, 0.0, 8.0), clamp(c.a, 0.0, 1.0));
}

void main() {
    float tick = mod(GameTime * 24000.0, 6000.0);
    float seed = vColor.g * 255.0;
    vec2 q = (uv - vec2(0.5, 0.42)) * vec2(1.0, 0.62);             // the opening is ~1.6x taller than wide
    float r = length(q);
    float a = atan(q.y, q.x);
    float spin = tick * 0.006 + seed;
    // galaxy: two log-spiral arms, gas, a hot core
    float arms = 0.5 + 0.5 * cos(2.0 * a - 4.5 * log(r + 0.03) - spin * 2.0 + fbm(q * 5.0 + seed) * 2.5);
    float gas = fbm(vec2(cos(a + spin * 0.5), sin(a + spin * 0.5)) * (r * 6.0 + 0.5) + seed);
    float dens = pow(arms, 2.0) * (0.35 + gas) * smoothstep(0.75, 0.05, r);
    float core = exp(-r * r / 0.004) * 1.6 + exp(-r * r / 0.03) * 0.5;
    vec3 col = DeepColor.rgb + ArmColor.rgb * dens * 1.6 + HotColor.rgb * core;
    // two star layers drifting at different speeds (fake depth)
    col += HotColor.rgb * stars(uv * 34.0 + vec2(tick * 0.002, 0.0) + seed, 0.06) * 1.2;
    col += ArmColor.rgb * stars(uv * 70.0 - vec2(tick * 0.001, 0.0) + seed * 2.0, 0.1) * 0.9;
    // inner rim: hot light along the sides of the opening
    float side = min(uv.x, 1.0 - uv.x);
    col += HotColor.rgb * exp(-side / 0.03) * 0.8 + ArmColor.rgb * exp(-side / 0.1) * 0.6;
    col *= vColor.b * Intensity;
    // opening: an iris from the centre with a bright front
    float open = vColor.r * 1.2;
    float m = 1.0 - smoothstep(open - 0.06, open, r);
    col += HotColor.rgb * exp(-pow((r - open + 0.02) / 0.025, 2.0)) * step(0.01, open) * step(open, 1.0) * 2.0;
    float alpha = 0.97 * m * vColor.a;
    if (alpha < 0.003) discard;
    fragColor = safe(vec4(col, alpha));
}

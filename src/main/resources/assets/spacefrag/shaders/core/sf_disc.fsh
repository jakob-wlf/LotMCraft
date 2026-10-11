#version 150

uniform float GameTime;
uniform vec4 ColorHot;
uniform vec4 ColorMid;
uniform vec4 ColorOuter;
uniform float Intensity;
uniform float Spin;
uniform float Arms;
uniform float Twist;
uniform float InnerFade;
uniform float Seed;

in vec3 vObj;
in vec3 vN;
in vec3 vPos;
in vec2 uv;
in vec4 vColor;
out vec4 fragColor;

float hash12(vec2 p) { p = fract(p * vec2(123.34, 456.21)); p += dot(p, p + 45.32); return fract(p.x * p.y); }
float vn(vec2 p) {
    vec2 i = floor(p), f = fract(p); f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash12(i), hash12(i + vec2(1, 0)), f.x), mix(hash12(i + vec2(0, 1)), hash12(i + vec2(1, 1)), f.x), f.y);
}
float fbm2(vec2 p) { float a = 0.5, s = 0.0; for (int i = 0; i < 5; i++) { s += a * vn(p); p = p * 2.03 + 7.1; a *= 0.5; } return s; }

void main() {
    float ang = uv.x;                 // 0..1 around
    float r = uv.y;                   // 0 inner .. 1 outer
    float t = GameTime * 1200.0 * Spin;
    // logarithmic spiral arms: angle offset grows with radius, whole thing rotates; inner parts spin faster
    float spiral = ang * Arms + log(r + 0.08) * Twist * 0.35 - t * (0.35 / (r + 0.15));
    float arms = pow(0.5 + 0.5 * sin(spiral * 6.28318), 1.6);
    float cloud = fbm2(vec2(ang * 18.0 + Seed, r * 9.0 - t * 0.6));
    float streak = fbm2(vec2(ang * 60.0 + t * 1.2 / (r + 0.2), r * 3.0 + Seed));
    float density = clamp(0.25 + arms * 0.75 * (0.55 + 0.9 * cloud) + streak * 0.35, 0.0, 1.6);

    // colour ramp: white-hot inner edge -> amber -> crimson -> fades out
    vec3 col = mix(ColorHot.rgb, ColorMid.rgb, smoothstep(0.0, 0.35, r));
    col = mix(col, ColorOuter.rgb, smoothstep(0.30, 0.95, r));
    float fade = smoothstep(0.0, InnerFade, r) * (1.0 - smoothstep(0.78, 1.0, r));
    float bright = (1.8 - 1.2 * r) * density;
    // doppler-like brightening on one side (uses object x)
    float dop = 0.65 + 0.35 * sin(ang * 6.28318 + 0.8);
    vec3 outc = col * bright * dop * Intensity;
    float a = clamp(fade * density, 0.0, 1.0) * vColor.a;
    if (a < 0.003) discard;
    fragColor = vec4(outc * a, a);       // premultiplied, blended ONE/ONE
}
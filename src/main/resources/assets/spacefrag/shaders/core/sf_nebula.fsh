#version 150
// Space-Time Storm v2: nebula clouds (procedural, no texture) on big billboards, additive.
// Each particle draws its own cloud (vertexColor.g = seed): soft indigo body, violet knots, a sprinkle of stars.
// vertexColor.r = brightness, vertexColor.a = fade.
uniform float GameTime;
uniform vec4 ColorA;
uniform vec4 ColorB;
uniform vec4 StarColor;
uniform float Intensity;
in vec2 texCoord0;
in vec4 vertexColor;
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

vec4 safe(vec4 c) {
    if (any(isnan(c)) || any(isinf(c))) return vec4(0.0);
    return vec4(clamp(c.rgb, 0.0, 8.0), clamp(c.a, 0.0, 1.0));
}

void main() {
    float tick = mod(GameTime * 24000.0, 6000.0);
    float seed = vertexColor.g * 255.0;
    vec2 p = texCoord0 * 2.0 - 1.0;
    float r = length(p);
    float fall = smoothstep(1.0, 0.15, r);
    vec2 q = p * 2.2 + vec2(seed * 1.7, seed * 0.9);
    vec2 w = vec2(fbm(q + tick * 0.0008), fbm(q + 5.2 - tick * 0.0006));
    float body = fbm(q + w * 1.8);
    float knots = pow(fbm(q * 2.3 + w * 2.5 + 11.0), 3.0) * 2.2;
    float d = smoothstep(0.35, 0.85, body) * fall;
    vec3 col = ColorA.rgb * d + ColorB.rgb * knots * d;
    vec2 sc = floor(texCoord0 * 90.0);
    float sh = h21(sc + seed);
    vec2 sf = fract(texCoord0 * 90.0) - 0.5;
    float star = step(0.985, sh) * smoothstep(0.35, 0.0, length(sf)) * fall * (0.6 + 0.4 * sin(tick * 0.2 + sh * 50.0));
    col += StarColor.rgb * star * 1.5;
    col *= Intensity * vertexColor.r * vertexColor.a;
    float a = clamp(max(col.r, max(col.g, col.b)), 0.0, 1.0);
    if (a < 0.002) discard;
    fragColor = safe(vec4(col, a));
}

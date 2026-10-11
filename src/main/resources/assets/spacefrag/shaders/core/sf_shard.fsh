#version 150

uniform float GameTime;
uniform vec4 BaseColor;
uniform vec4 EdgeColor;
uniform vec4 CrackColor;
uniform vec3 LightDir;
uniform float EdgePower;
uniform float Seed;
uniform float Shimmer;

in vec3 vObj;
in vec3 vN;
in vec3 vPos;
in vec2 uv;
in vec4 vColor;
out vec4 fragColor;

float h13(vec3 p) { p = fract(p * 0.1031); p += dot(p, p.zyx + 31.32); return fract((p.x + p.y) * p.z); }
vec3 h33(vec3 p) { p = vec3(dot(p, vec3(127.1, 311.7, 74.7)), dot(p, vec3(269.5, 183.3, 246.1)), dot(p, vec3(113.5, 271.9, 124.6))); return fract(sin(p) * 43758.5453123); }
float vn3(vec3 p) {
    vec3 i = floor(p), f = fract(p); f = f * f * (3.0 - 2.0 * f);
    return mix(mix(mix(h13(i), h13(i + vec3(1, 0, 0)), f.x), mix(h13(i + vec3(0, 1, 0)), h13(i + vec3(1, 1, 0)), f.x), f.y),
               mix(mix(h13(i + vec3(0, 0, 1)), h13(i + vec3(1, 0, 1)), f.x), mix(h13(i + vec3(0, 1, 1)), h13(i + vec3(1, 1, 1)), f.x), f.y), f.z);
}
vec2 worley(vec3 p) {
    vec3 ip = floor(p), fp = fract(p); float d1 = 9.0, d2 = 9.0;
    for (int z = -1; z <= 1; z++) for (int y = -1; y <= 1; y++) for (int x = -1; x <= 1; x++) {
        vec3 g = vec3(x, y, z); float d = length(g + h33(ip + g) - fp);
        if (d < d1) { d2 = d1; d1 = d; } else if (d < d2) { d2 = d; }
    }
    return vec2(d1, d2);
}

void main() {
    vec3 N = normalize(vN);
    vec3 V = normalize(-vPos);
    float ndv = abs(dot(N, V));
    vec3 L = normalize(LightDir);
    float t = GameTime * 1200.0;

    float edge = pow(1.0 - ndv, EdgePower);
    // faint inner fracture web and a slow sheen band sweeping over the glass
    vec2 w = worley(vObj * 3.2 + Seed);
    float web = 1.0 - smoothstep(0.0, 0.07, w.y - w.x);
    float sheen = pow(max(dot(reflect(-L, N), V), 0.0), 8.0) * Shimmer;
    float bands = 0.5 + 0.5 * sin(dot(vObj, vec3(7.0, 3.0, 5.0)) + t * 0.5 + Seed);

    vec3 col = BaseColor.rgb + EdgeColor.rgb * edge * 0.9 + CrackColor.rgb * web * 0.7 + vec3(1.0, 0.9, 0.7) * sheen * (0.4 + 0.6 * bands);
    float a = clamp(BaseColor.a + edge * 0.5 + web * 0.4 + sheen * 0.4, 0.0, 1.0) * vColor.a;
    if (a < 0.003) discard;
    fragColor = vec4(col * vColor.r, a);
}
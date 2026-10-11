#version 150
// Space Fragmentation crack growth: the crack mask spreads OUTWARD from the centre like a real fracture.
// vertexColor.a = growth progress (0 = nothing, 1 = fully spread), vertexColor.r = brightness.
// Every direction advances at its own speed (smooth angular noise, 'Jag'), with a little per-pixel jitter so fronts are
// ragged, and a hot glowing tip runs along the front of every fissure. Ink = 1 renders a black alpha mask (no glow).
uniform sampler2D Texture;
uniform float GameTime;
uniform vec4 TintColor;
uniform vec4 HaloTint;
uniform float Intensity;
uniform float Halo;
uniform float HaloRadius;
uniform float HeadGlow;
uniform float Jag;
uniform float Seed;
uniform float Ink;

in vec2 texCoord0;
in vec4 vertexColor;
out vec4 fragColor;

float h1(float n) { return fract(sin(n * 12.9898 + Seed * 78.233) * 43758.5453); }
float n1(float x) { float i = floor(x), f = fract(x); f = f * f * (3.0 - 2.0 * f); return mix(h1(i), h1(i + 1.0), f); }
float h2(vec2 p) { return fract(sin(dot(p, vec2(127.1, 311.7)) + Seed) * 43758.5453); }

void main() {
    float progress = clamp(vertexColor.a, 0.0, 1.0);
    float bright = vertexColor.r;

    float core = texture(Texture, texCoord0).a;
    float halo = 0.0;
    if (Halo > 0.001 && Ink < 0.5) {
        for (int i = 0; i < 8; i++) {
            float a = 6.2831853 * float(i) / 8.0;
            vec2 o = vec2(cos(a), sin(a));
            halo += texture(Texture, texCoord0 + o * HaloRadius).a + texture(Texture, texCoord0 + o * HaloRadius * 2.5).a * 0.5;
        }
        halo /= 12.0;
    }

    // distance from the impact point, stretched per direction so some fissures race ahead of others
    vec2 p = texCoord0 - 0.5;
    float r = length(p) / 0.7071;                                     // 0 at the centre, 1 at the corners
    float ang = atan(p.y, p.x) / 6.2831853 + 0.5;
    float lag = pow(n1(ang * 9.0) * 0.7 + n1(ang * 31.0 + 7.0) * 0.3, 1.6);  // 0..1 around the circle; low = a fissure that races ahead
    float jit = (h2(floor(texCoord0 * 512.0)) - 0.5) * 0.03;           // ragged front
    float d = r * (1.0 + Jag * lag) + jit;
    float front = progress * (1.0 + Jag) * 1.02;

    float drawn = 1.0 - smoothstep(front - 0.015, front, d);
    float tip = exp(-pow((d - front + 0.01) / 0.022, 2.0)) * step(0.001, progress) * (1.0 - step(0.999, progress));

    if (Ink > 0.5) {
        float a = core * drawn * bright;
        if (a < 0.003) discard;
        fragColor = vec4(0.0, 0.0, 0.0, clamp(a * TintColor.a, 0.0, 1.0));
        return;
    }
    float shape = core + halo * Halo;
    vec3 rgb = Intensity * (TintColor.rgb * core * drawn + HaloTint.rgb * halo * Halo * drawn + TintColor.rgb * core * tip * HeadGlow);
    rgb += vec3(1.0, 0.85, 0.6) * core * tip * HeadGlow * 0.6 * Intensity;   // white-hot tip
    float alpha = clamp(shape * max(drawn, tip), 0.0, 1.0);
    rgb *= bright;
    alpha *= min(bright, 1.0);
    if (alpha < 0.002) discard;
    fragColor = vec4(rgb, alpha);
}
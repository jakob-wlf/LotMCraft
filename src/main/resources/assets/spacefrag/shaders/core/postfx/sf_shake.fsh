#version 150
// Space Fragmentation screen shake: jolting offset + punch-in zoom + radial colour split (no flash; the flash lives in sf_fracture)
uniform sampler2D DiffuseSampler;
uniform float GameTime;
uniform float Strength;
uniform float Amplitude;
uniform float Frequency;
uniform float Chroma;

in vec2 texCoord;
out vec4 fragColor;

float hash(float n) { return fract(sin(n) * 43758.5453123); }
vec2 jolt(float k) { return vec2(hash(k * 1.731 + 0.3), hash(k * 2.917 + 7.1)) * 2.0 - 1.0; }

void main() {
    float s = clamp(Strength, 0.0, 1.0);
    float t = GameTime * 1200.0 * Frequency;
    float k = floor(t);
    float f = fract(t);
    vec2 offset = mix(jolt(k), jolt(k + 1.0), f * f * (3.0 - 2.0 * f)) * Amplitude * s;
    float zoom = 1.0 - (Amplitude * 1.6 + 0.02) * s;
    vec2 uv = (texCoord - 0.5) * zoom + 0.5 + offset;
    vec2 dir = (uv - 0.5) * Chroma * s;
    vec3 c;
    c.r = texture(DiffuseSampler, uv + dir).r;
    c.g = texture(DiffuseSampler, uv).g;
    c.b = texture(DiffuseSampler, uv - dir).b;
    c = c * 0.62 + texture(DiffuseSampler, uv - offset * 0.5).rgb * 0.38;
    fragColor = vec4(c, 1.0);
}
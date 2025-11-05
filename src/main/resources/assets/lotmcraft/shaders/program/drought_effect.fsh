#version 150

uniform sampler2D DiffuseSampler;
uniform float Time;

in vec2 texCoord;
out vec4 fragColor;

// Faster lightweight noise
float hash(vec2 p) {
    return fract(sin(dot(p, vec2(12.9898,78.233))) * 43758.5453);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f*f*(3.0-2.0*f);

    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));

    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    for(int i = 0; i < 3; i++){
        v += a * noise(p);
        p *= 2.0;
        a *= 0.5;
    }
    return v;
}

// Heat haze simplified (1 trig op)
vec2 heatDistortion(vec2 uv, float t) {
    float d = sin(uv.y * 20.0 + t * 1.6) * 0.004;
    return vec2(d, 0.0);
}

void main() {
    vec2 uv = texCoord;

    // Apply heat haze
    vec2 distortedUV = uv + heatDistortion(uv, Time);

    vec4 color = texture(DiffuseSampler, distortedUV);

    // Desaturate slightly
    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    color.rgb = mix(color.rgb, vec3(gray), 0.35);

    // Stronger warm orange tone
    color.rgb *= vec3(1.35, 0.85, 0.55);

    // Boost contrast slightly
    color.rgb = (color.rgb - 0.5) * 1.25 + 0.5;

    float brightness = dot(color.rgb, vec3(0.4, 0.5, 0.1));

    // Highlight overexposure without branching
    color.rgb += vec3(0.25, 0.16, 0.05) *
    smoothstep(0.55, 0.9, brightness);

    // Dust clouds + particles (cheaper FBM)
    float dust = fbm(uv * 60.0 + Time * 0.25);
    color.rgb += vec3(0.35, 0.25, 0.1) *
    smoothstep(0.65, 0.8, dust) * 0.12;

    // Hazy overlay
    float haze = fbm(uv * 2.5 + Time * 0.08);
    color.rgb = mix(color.rgb, vec3(0.85, 0.7, 0.45) * haze, 0.18);

    // Vignette
    float dist = length(uv - 0.5);
    color.rgb *= smoothstep(0.85, 0.25, dist);

    // Heat pulse
    color.rgb *= sin(Time * 1.15) * 0.025 + 0.98;

    // Grain
    float grain = hash(uv * 900.0 + Time) * 0.03 - 0.015;
    color.rgb += grain;

    // Final exposure fix
    color.rgb *= 0.97;

    fragColor = vec4(color.rgb, 1.0);
}

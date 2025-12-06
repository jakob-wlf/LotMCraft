#version 150

uniform sampler2D DiffuseSampler;
uniform float Time;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

// Simple noise function for subtle distortion
float noise(vec2 p) {
    return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
    // Subtle vignette effect from edges
    vec2 center = texCoord - 0.5;
    float vignette = 1.0 - dot(center, center) * 0.3;

    // Very subtle chromatic aberration (color separation)
    float aberration = 0.001;
    vec2 distortion = center * aberration;

    float r = texture(DiffuseSampler, texCoord + distortion).r;
    float g = texture(DiffuseSampler, texCoord).g;
    float b = texture(DiffuseSampler, texCoord - distortion).b;

    vec3 color = vec3(r, g, b);

    // Apply vignette
    color *= vignette;

    // Yellowish/golden/egg-white tint
    // Subtle warm overlay
    vec3 tint = vec3(1.0, 0.96, 0.85); // Warm egg-white/pale yellow
    color = mix(color, color * tint, 0.25); // 25% tint strength

    // Slight desaturation for unsettling feel
    float gray = dot(color, vec3(0.299, 0.587, 0.114));
    color = mix(color, vec3(gray), 0.15);

    // Very subtle noise/grain
    float noiseVal = noise(texCoord * Time * 0.5) * 0.02;
    color += noiseVal;

    // Subtle pulsing effect (very gentle)
    float pulse = sin(Time * 0.5) * 0.02 + 1.0;
    color *= pulse;

    fragColor = vec4(color, 1.0);
}
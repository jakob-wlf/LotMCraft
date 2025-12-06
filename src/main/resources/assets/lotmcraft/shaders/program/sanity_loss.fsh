#version 150

uniform sampler2D DiffuseSampler;
uniform float Time;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

// Simple noise function
float noise(vec2 p) {
    return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
    vec2 uv = texCoord;

    // Subtle wavy distortion
    float distortAmount = 0.003;
    float wavyX = sin(uv.y * 15.0 + Time * 2.0) * distortAmount;
    float wavyY = cos(uv.x * 12.0 + Time * 1.5) * distortAmount;

    uv += vec2(wavyX, wavyY);

    // Clamp UV to prevent sampling outside texture
    uv = clamp(uv, vec2(0.0), vec2(1.0));

    // Sample texture
    vec3 col = texture(DiffuseSampler, uv).rgb;

    // Yellowish/golden/egg-white color grading
    vec3 sanityTint = vec3(1.05, 0.98, 0.82);
    col *= sanityTint;

    // Slightly desaturate
    float gray = dot(col, vec3(0.299, 0.587, 0.114));
    col = mix(col, vec3(gray), 0.15);

    // Vignette effect
    vec2 vigUV = uv * 2.0 - 1.0;
    float dist = length(vigUV);
    float vignette = smoothstep(0.8, 0.3, dist);
    vignette = mix(0.75, 1.0, vignette);
    col *= vignette;

    // Very subtle film grain
    float grainAmount = 0.02;
    float grain = (noise(uv * 1000.0 + Time * 0.1) - 0.5) * grainAmount;
    col += grain;

    // Subtle pulsing effect
    float pulse = sin(Time * 0.8) * 0.02 + 1.0;
    col *= pulse;

    fragColor = vec4(col, 1.0);
}
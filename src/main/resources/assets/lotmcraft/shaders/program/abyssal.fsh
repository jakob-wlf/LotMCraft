#version 150

// Abyssal Distortion Post-Process Shader for NeoForge 1.21.1

uniform sampler2D DiffuseSampler;
uniform float Time;

in vec2 texCoord;
out vec4 fragColor;

// Noise function for distortion
float noise(vec2 p) {
    return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
}

float smoothNoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);

    float a = noise(i);
    float b = noise(i + vec2(1.0, 0.0));
    float c = noise(i + vec2(0.0, 1.0));
    float d = noise(i + vec2(1.0, 1.0));

    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    float frequency = 1.0;

    for(int i = 0; i < 5; i++) {
        value += amplitude * smoothNoise(p * frequency);
        frequency *= 2.0;
        amplitude *= 0.5;
    }

    return value;
}

void main() {
    vec2 uv = texCoord;

    // Create flowing abyssal distortion
    float timeFlow = Time * 0.3;
    vec2 distortionCoord = uv * 3.0 + vec2(timeFlow * 0.2, timeFlow * 0.1);

    // Multiple layers of distortion for depth
    float distortion1 = fbm(distortionCoord) * 2.0 - 1.0;
    float distortion2 = fbm(distortionCoord * 1.7 + vec2(timeFlow * 0.15, -timeFlow * 0.12)) * 2.0 - 1.0;

    // Combine distortions
    vec2 distortedUV = uv + vec2(distortion1, distortion2) * 0.05;

    // Add wavering effect from center
    vec2 centerOffset = uv - 0.5;
    float distanceFromCenter = length(centerOffset);
    float waveDistort = sin(distanceFromCenter * 15.0 - timeFlow * 2.0) * 0.01;
    distortedUV += centerOffset * waveDistort;

    // Sample the distorted texture
    vec4 texColor = texture(DiffuseSampler, distortedUV);

    // Create abyssal color palette
    vec3 deepPurple = vec3(0.15, 0.05, 0.25);
    vec3 voidPurple = vec3(0.3, 0.1, 0.4);
    vec3 darkVoid = vec3(0.05, 0.0, 0.1);

    // Create pulsing darkness
    float pulse = sin(timeFlow * 1.5) * 0.5 + 0.5;
    float vignette = 1.0 - distanceFromCenter * 1.2;
    vignette = smoothstep(0.0, 1.0, vignette);

    // Mix colors based on brightness and distortion
    float brightness = dot(texColor.rgb, vec3(0.299, 0.587, 0.114));
    vec3 abyssalColor = mix(darkVoid, deepPurple, brightness * 0.7);
    abyssalColor = mix(abyssalColor, voidPurple, distortion1 * 0.3 + 0.5);

    // Apply the abyssal transformation
    vec3 finalColor = mix(texColor.rgb, abyssalColor, 0.6);

    // Add ethereal purple highlights
    float highlight = smoothNoise(distortionCoord * 2.0 + timeFlow);
    finalColor += voidPurple * highlight * 0.2 * pulse;

    // Apply vignette darkening
    finalColor *= mix(0.3, 1.0, vignette);

    // Add subtle chromatic aberration for unsettling effect
    float aberration = 0.003;
    float r = texture(DiffuseSampler, distortedUV + vec2(aberration, 0.0)).r;
    float g = texture(DiffuseSampler, distortedUV).g;
    float b = texture(DiffuseSampler, distortedUV - vec2(aberration, 0.0)).b;
    vec3 chromaticColor = vec3(r, g, b);

    finalColor = mix(finalColor, chromaticColor, 0.3);

    fragColor = vec4(finalColor, 1.0);
}
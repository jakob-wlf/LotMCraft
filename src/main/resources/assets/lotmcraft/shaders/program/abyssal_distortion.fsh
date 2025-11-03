#version 150

uniform sampler2D DiffuseSampler;
uniform float Time;

in vec2 texCoord;

out vec4 fragColor;

// Noise function for distortion
float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);

    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));

    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

// Fractal noise for more complex distortion
float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    float frequency = 1.0;

    for (int i = 0; i < 5; i++) {
        value += amplitude * noise(p * frequency);
        frequency *= 2.0;
        amplitude *= 0.5;
    }

    return value;
}

// Vortex distortion
vec2 vortex(vec2 uv, vec2 center, float strength) {
    vec2 delta = uv - center;
    float dist = length(delta);
    float angle = atan(delta.y, delta.x);

    float spiral = sin(dist * 10.0 - Time * 2.0) * strength;
    angle += spiral * (1.0 - dist);

    return center + vec2(cos(angle), sin(angle)) * dist;
}

void main() {
    vec2 uv = texCoord;
    vec2 center = vec2(0.5, 0.5);

    // Animated distortion offset
    float timeScale = Time * 0.5;

    // Multiple layers of distortion
    float distortion1 = fbm(uv * 3.0 + vec2(timeScale * 0.3, timeScale * 0.2));
    float distortion2 = fbm(uv * 5.0 - vec2(timeScale * 0.2, timeScale * 0.4));

    // Apply warping (reduced intensity)
    vec2 warp = vec2(
    distortion1 * 0.015 + sin(uv.y * 10.0 + timeScale) * 0.005,
    distortion2 * 0.015 + cos(uv.x * 10.0 + timeScale * 1.2) * 0.005
    );

    // Vortex effect from center (reduced intensity)
    vec2 distortedUV = vortex(uv + warp, center, 0.05);

    // Chromatic aberration for abyssal feel (reduced)
    float aberration = 0.003;
    vec2 direction = normalize(distortedUV - center);

    float r = texture(DiffuseSampler, distortedUV + direction * aberration).r;
    float g = texture(DiffuseSampler, distortedUV).g;
    float b = texture(DiffuseSampler, distortedUV - direction * aberration).b;

    vec3 color = vec3(r, g, b);

    // Apply purple-black tint (reduced intensity)
    vec3 abyssalTint = vec3(0.4, 0.1, 0.6); // Purple
    vec3 deepAbyss = vec3(0.05, 0.0, 0.1); // Very dark purple-black

    // Mix between deep abyss and tinted color (less intense)
    float tintStrength = 0.5;
    color = mix(color, color * abyssalTint, tintStrength);
    color = mix(color, deepAbyss, 0.15);

    // Vignette effect for depth (less intense)
    float distFromCenter = length(distortedUV - center);
    float vignette = smoothstep(0.9, 0.4, distFromCenter);
    color *= mix(0.6, 1.0, vignette);

    // Pulsing darkness
    float pulse = sin(timeScale * 2.0) * 0.1 + 0.9;
    color *= pulse;

    // Animated void tendrils (fewer and smaller)
    float tendrils = fbm(vec2(
    uv.x * 4.0 + timeScale * 0.5,
    uv.y * 4.0 - timeScale * 0.3
    ));
    tendrils = smoothstep(0.25, 0.75, tendrils);
    color = mix(deepAbyss * 0.7, color, tendrils);

    // Add subtle purple glow (reduced)
    float glow = 1.0 - smoothstep(0.0, 0.5, distFromCenter);
    color += vec3(0.15, 0.0, 0.2) * glow * 0.2 * sin(timeScale * 3.0);

    fragColor = vec4(color, 1.0);
}
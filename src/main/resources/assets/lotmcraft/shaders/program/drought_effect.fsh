#version 150

uniform sampler2D DiffuseSampler;
uniform float Time;

in vec2 texCoord;

out vec4 fragColor;

// Noise function for dust particles
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

// Fractal noise for dust clouds
float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    float frequency = 1.0;

    for (int i = 0; i < 4; i++) {
        value += amplitude * noise(p * frequency);
        frequency *= 2.0;
        amplitude *= 0.5;
    }

    return value;
}

// Heat haze distortion
vec2 heatDistortion(vec2 uv, float time) {
    float distortion = sin(uv.y * 15.0 + time * 2.0) * 0.003;
    distortion += sin(uv.y * 25.0 - time * 1.5) * 0.002;
    return vec2(distortion, 0.0);
}

void main() {
    vec2 uv = texCoord;

    // Apply heat haze distortion
    vec2 distortion = heatDistortion(uv, Time);
    vec2 distortedUV = uv + distortion;

    // Get original color
    vec4 color = texture(DiffuseSampler, distortedUV);

    // Desaturate - remove most color saturation
    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    color.rgb = mix(color.rgb, vec3(gray), 0.5); // 50% desaturation

    // Apply drought color grading (warm, dusty browns and oranges)
    vec3 droughtTint = vec3(1.2, 0.95, 0.7); // Warm, orange-brown tint
    color.rgb *= droughtTint;

    // Increase contrast - darker darks, brighter brights
    color.rgb = (color.rgb - 0.5) * 1.3 + 0.5;

    // Add harsh sun overexposure to bright areas
    float brightness = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    if(brightness > 0.6) {
        color.rgb += vec3(0.3, 0.25, 0.15) * (brightness - 0.6) * 1.5;
    }

    // Dust particles floating across screen
    float dust1 = fbm(uv * 50.0 + vec2(Time * 0.3, Time * 0.1));
    float dust2 = fbm(uv * 80.0 - vec2(Time * 0.2, Time * 0.15));
    float dustLayer = (dust1 + dust2) * 0.5;

    // Create visible dust particles
    float dustParticles = smoothstep(0.6, 0.8, dustLayer);
    color.rgb += vec3(0.4, 0.35, 0.25) * dustParticles * 0.15;

    // Hazy atmosphere - add fog/dust overlay
    float haze = fbm(uv * 3.0 + Time * 0.1);
    color.rgb = mix(color.rgb, vec3(0.8, 0.7, 0.5) * haze, 0.15);

    // Vignette - darker edges like harsh sun exposure
    vec2 center = vec2(0.5, 0.5);
    float distFromCenter = length(uv - center);
    float vignette = smoothstep(0.8, 0.3, distFromCenter);
    color.rgb *= mix(0.6, 1.0, vignette);

    // Subtle pulsing heat effect
    float heatPulse = sin(Time * 1.2) * 0.03 + 0.97;
    color.rgb *= heatPulse;

    // Add slight color shift towards yellow/orange in hot spots
    float hotSpots = smoothstep(0.7, 1.0, brightness);
    color.rgb += vec3(0.1, 0.08, 0.0) * hotSpots;

    // Reduce overall brightness slightly for that parched look
    color.rgb *= 0.95;

    // Film grain for gritty texture
    float grain = hash(uv * 1000.0 + Time) * 0.03;
    color.rgb += grain - 0.015;

    fragColor = vec4(color.rgb, 1.0);
}
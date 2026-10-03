#version 150

uniform sampler2D DiffuseSampler;
uniform float Time;

in vec2 texCoord;

out vec4 fragColor;

// Simple hash for randomness
float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

// Smooth noise
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

// Layered noise for fog
float fogNoise(vec2 p) {
    float n = 0.0;
    float amp = 1.0;
    float freq = 1.0;

    for(int i = 0; i < 5; i++) {
        n += noise(p * freq) * amp;
        freq *= 2.2;
        amp *= 0.45;
    }

    return n;
}

void main() {
    vec2 uv = texCoord;

    // Create flowing fog movement
    vec2 flowDir1 = vec2(Time * 0.03, Time * 0.02);
    vec2 flowDir2 = vec2(-Time * 0.025, Time * 0.035);
    vec2 flowDir3 = vec2(Time * 0.04, -Time * 0.03);

    // Generate multiple fog layers that move independently
    float fog1 = fogNoise(uv * 2.5 + flowDir1);
    float fog2 = fogNoise(uv * 3.8 - flowDir2);
    float fog3 = fogNoise(uv * 1.8 + flowDir3);

    // Combine fog layers with different intensities
    float fogDensity = (fog1 * 0.5 + fog2 * 0.3 + fog3 * 0.2);
    fogDensity = smoothstep(0.25, 0.75, fogDensity);

    // Create gentle wave distortion in the fog
    float waveX = sin(uv.y * 4.0 + Time * 0.4) * 0.008;
    float waveY = cos(uv.x * 3.5 - Time * 0.35) * 0.006;
    vec2 distortedUV = uv + vec2(waveX, waveY);

    // Sample the original scene
    vec4 color = texture(DiffuseSampler, distortedUV);

    // Create ethereal color palette - soft grays with hint of teal/lavender
    vec3 fogColor1 = vec3(0.75, 0.78, 0.82);
    vec3 fogColor2 = vec3(0.68, 0.72, 0.80);
    vec3 fogColor3 = vec3(0.72, 0.75, 0.85);

    // Mix fog colors based on position and time
    float colorShift = sin(fogDensity * 3.14 + Time * 0.2) * 0.5 + 0.5;
    vec3 fogColor = mix(fogColor1, fogColor2, colorShift);
    fogColor = mix(fogColor, fogColor3, fog3 * 0.3);

    // Apply fog to scene - much more intense
    color.rgb = mix(color.rgb, fogColor, fogDensity * 0.85);

    // Add depth - more fog in distance
    float depthGradient = smoothstep(0.1, 1.0, uv.y);
    color.rgb = mix(color.rgb, fogColor2, depthGradient * 0.5);

    // Mysterious floating orbs of light in the fog
    for(float i = 0.0; i < 4.0; i++) {
        vec2 orbPos = vec2(
        0.5 + sin(Time * (0.15 + i * 0.05) + i * 2.0) * 0.3,
        0.5 + cos(Time * (0.12 + i * 0.04) + i * 1.5) * 0.25
        );

        float orbDist = length(uv - orbPos);
        float orbGlow = exp(-orbDist * 15.0) * 0.4;

        // Each orb has slightly different color
        vec3 orbColor = vec3(0.85, 0.88, 0.92) + vec3(sin(i), cos(i * 1.5), sin(i * 0.8)) * 0.1;
        color.rgb += orbColor * orbGlow * fogDensity;
    }

    // Ghostly trails that drift through the fog
    float trail1 = noise(vec2(uv.x * 2.0 + Time * 0.1, uv.y * 6.0));
    float trail2 = noise(vec2(uv.x * 3.0 - Time * 0.15, uv.y * 5.0 + 2.0));

    trail1 = smoothstep(0.55, 0.65, trail1);
    trail2 = smoothstep(0.52, 0.62, trail2);

    vec3 trailColor = vec3(0.80, 0.83, 0.88);
    color.rgb = mix(color.rgb, trailColor, (trail1 + trail2) * 0.1);

    // Pockets of thicker fog that drift by
    float thickFog = fogNoise(uv * 1.2 + vec2(Time * 0.05, Time * 0.04));
    thickFog = smoothstep(0.6, 0.8, thickFog);
    color.rgb = mix(color.rgb, vec3(0.65, 0.68, 0.75), thickFog * 0.35);

    // Subtle shimmer effect in the fog
    float shimmer = sin(uv.x * 40.0 + Time) * cos(uv.y * 35.0 - Time * 0.8);
    shimmer = shimmer * 0.5 + 0.5;
    shimmer *= fogDensity;
    color.rgb += vec3(0.05, 0.06, 0.08) * shimmer * 0.15;

    // Veils of fog that sweep across the screen
    float veil = sin((uv.x + uv.y) * 2.0 + Time * 0.3) * 0.5 + 0.5;
    veil *= noise(uv * 4.0 + Time * 0.1);
    color.rgb = mix(color.rgb, fogColor1, veil * 0.12);

    // Edge glow - fog is more luminous at the periphery
    vec2 edgeUV = abs(uv - 0.5) * 2.0;
    float edgeMask = max(edgeUV.x, edgeUV.y);
    float edgeGlow = smoothstep(0.6, 1.0, edgeMask) * fogDensity;
    color.rgb += vec3(0.1, 0.12, 0.15) * edgeGlow;

    // Ancient runes or symbols that fade in and out
    float rune1 = noise(floor(uv * 8.0) / 8.0 + floor(Time * 0.2));
    float rune2 = noise(floor(uv * 10.0) / 10.0 + floor(Time * 0.25 + 3.0));

    rune1 = step(0.95, rune1);
    rune2 = step(0.97, rune2);

    float runeAlpha = sin(Time * 2.0) * 0.5 + 0.5;
    color.rgb += vec3(0.7, 0.75, 0.85) * (rune1 + rune2) * runeAlpha * 0.2 * fogDensity;

    // Spiral patterns in the fog
    vec2 centered = uv - 0.5;
    float angle = atan(centered.y, centered.x);
    float radius = length(centered);

    float spiral = sin(angle * 3.0 - radius * 8.0 + Time * 0.5);
    spiral = smoothstep(-0.3, 0.3, spiral) * smoothstep(0.6, 0.2, radius);
    color.rgb = mix(color.rgb, vec3(0.78, 0.81, 0.87), spiral * 0.08 * fogDensity);

    // Soft desaturation for dreamlike quality
    float luminance = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    color.rgb = mix(color.rgb, vec3(luminance), 0.25);

    // Gentle breathing pulse
    float pulse = sin(Time * 0.6) * 0.03 + 0.97;
    color.rgb *= pulse;

    fragColor = vec4(color.rgb, 1.0);
}
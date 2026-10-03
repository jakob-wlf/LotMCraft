#version 150

uniform sampler2D DiffuseSampler;
uniform float Time;

in vec2 texCoord;

out vec4 fragColor;

// Noise function for subtle texture
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

// Smooth bloom effect
vec4 getBloom(vec2 uv) {
    vec4 bloom = vec4(0.0);
    float total = 0.0;

    // Sample in a circular pattern
    for(float angle = 0.0; angle < 6.28; angle += 0.4) {
        for(float dist = 0.001; dist < 0.008; dist += 0.002) {
            vec2 offset = vec2(cos(angle), sin(angle)) * dist;
            bloom += texture(DiffuseSampler, uv + offset);
            total += 1.0;
        }
    }

    return bloom / total;
}

// God rays / light rays effect
float godRays(vec2 uv, vec2 lightPos) {
    vec2 delta = uv - lightPos;
    float dist = length(delta);
    float angle = atan(delta.y, delta.x);

    // Radial rays
    float rays = sin(angle * 12.0 + Time * 0.5) * 0.5 + 0.5;
    rays *= smoothstep(0.6, 0.0, dist);

    return rays;
}

void main() {
    vec2 uv = texCoord;
    vec2 center = vec2(0.5, 0.5);

    // Get original color
    vec4 color = texture(DiffuseSampler, uv);

    // Apply golden color grading
    vec3 golden = vec3(1.3, 1.1, 0.7); // Warm golden tint
    color.rgb *= golden;

    // Increase overall brightness
    color.rgb *= 1.2;

    // Add bloom effect
    vec4 bloom = getBloom(uv);
    bloom.rgb *= vec3(1.4, 1.2, 0.8); // Golden bloom
    color = mix(color, bloom, 0.25);

    // God rays from center
    float rays = godRays(uv, center);
    color.rgb += vec3(1.0, 0.9, 0.6) * rays * 0.15;

    // Inverse vignette (brighter at center)
    float distFromCenter = length(uv - center);
    float glow = 1.0 - smoothstep(0.0, 0.8, distFromCenter);
    color.rgb += vec3(1.2, 1.0, 0.6) * glow * 0.2;

    // Pulsing holy light
    float pulse = sin(Time * 1.5) * 0.08 + 0.92;
    color.rgb *= pulse;

    // Add subtle golden particles/sparkles
    float sparkle = noise(uv * 300.0 + Time * 2.0);
    sparkle = smoothstep(0.95, 1.0, sparkle);
    color.rgb += vec3(1.5, 1.3, 0.7) * sparkle * 0.4;

    // Soft radial gradient
    float radialGlow = 1.0 - (distFromCenter * 0.3);
    color.rgb *= radialGlow;

    // Warm highlights
    float brightness = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    if(brightness > 0.8) {
        color.rgb += vec3(0.3, 0.25, 0.1) * (brightness - 0.8);
    }

    fragColor = vec4(color.rgb, 1.0);
}
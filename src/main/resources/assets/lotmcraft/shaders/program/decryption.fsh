#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D PrevSampler;
uniform float Time;
uniform vec2 InSize;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

// Random function for noise
float random(vec2 st) {
    return fract(sin(dot(st.xy, vec2(12.9898, 78.233))) * 43758.5453123);
}

// Hash function for better randomness
float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

void main() {
    vec2 uv = texCoord;
    vec4 center = texture(DiffuseSampler, uv);

    // Time-based animation
    float t = Time * 0.5;

    // Create scanline effect
    float scanline = sin(uv.y * InSize.y * 1.5 + t * 10.0) * 0.1;

    // Digital noise/glitch effect
    vec2 noiseUV = uv * InSize * 0.5;
    float noise = random(noiseUV + fract(t));
    float blockNoise = hash(floor(noiseUV * 8.0 + vec2(t * 2.0, 0.0)));

    // Matrix-style falling code effect
    float columns = floor(uv.x * 60.0);
    float fallSpeed = fract(columns * 0.123 + t * 0.3);
    float matrixEffect = step(0.98, fract(uv.y * 40.0 - fallSpeed * 8.0)) * blockNoise;

    // Horizontal glitch lines
    float glitchLine = step(0.995, hash(vec2(floor(uv.y * 100.0 + t * 5.0), 0.0)));
    vec2 glitchOffset = vec2(glitchLine * (hash(vec2(t, uv.y)) - 0.5) * 0.1, 0.0);

    // Sample with glitch offset
    vec4 color = texture(DiffuseSampler, uv + glitchOffset);

    // Green/Purple color shift for decryption aesthetic
    vec3 greenTint = vec3(0.2, 1.0, 0.4);
    vec3 purpleTint = vec3(0.8, 0.2, 1.0);

    // Mix between green and purple based on position and time
    float colorMix = sin(uv.y * 10.0 + t) * 0.5 + 0.5;
    vec3 tintColor = mix(greenTint, purpleTint, colorMix);

    // Apply chromatic aberration for digital feel
    float aberration = 0.003 * (noise * 2.0);
    vec4 rChannel = texture(DiffuseSampler, uv + glitchOffset + vec2(aberration, 0.0));
    vec4 gChannel = color;
    vec4 bChannel = texture(DiffuseSampler, uv + glitchOffset - vec2(aberration, 0.0));

    color = vec4(rChannel.r, gChannel.g, bChannel.b, color.a);

    // Apply tint to the image
    color.rgb = mix(color.rgb, color.rgb * tintColor, 0.4);

    // Add scanlines
    color.rgb += scanline;

    // Add matrix effect
    color.rgb += matrixEffect * tintColor * 0.5;

    // Add digital noise overlay
    color.rgb += (noise - 0.5) * 0.1;

    // Block corruption effect
    if (blockNoise > 0.95) {
        float blockSize = 8.0;
        vec2 blockUV = floor(uv * InSize / blockSize) * blockSize / InSize;
        color = texture(DiffuseSampler, blockUV);
        color.rgb *= tintColor;
    }

    // Add edge glow effect
    float vignette = length(uv - 0.5);
    color.rgb += tintColor * (1.0 - vignette) * 0.1;

    // Subtle screen flicker
    color.rgb *= 0.95 + sin(t * 30.0) * 0.05;

    // Mix with previous frame for motion blur/trail effect
    vec4 prevColor = texture(PrevSampler, uv);
    color = mix(prevColor, color, 0.7);

    fragColor = color;
}
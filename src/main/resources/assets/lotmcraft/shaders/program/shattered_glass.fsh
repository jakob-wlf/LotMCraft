#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 OutSize;
uniform float Time;

in vec2 texCoord;
out vec4 fragColor;

// Simple noise function
float hash(vec2 p) {
    return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
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

void main() {
    vec2 uv = texCoord;

    // Much stronger wavy distortion
    float distortAmount = 0.8;
    float wavyX = sin(uv.y * 15.0 + Time * 2.0) * distortAmount;
    float wavyY = cos(uv.x * 12.0 + Time * 1.5) * distortAmount;

    // Add secondary distortion layer
    wavyX += sin(uv.y * 8.0 - Time * 1.5) * distortAmount * 0.5;
    wavyY += cos(uv.x * 10.0 + Time * 2.5) * distortAmount * 0.5;


    vec2 distortedUV = uv + vec2(wavyX, wavyY);

    // Sample texture
    vec4 color = texture(DiffuseSampler, distortedUV);

    // Yellowish/golden/egg-white color grading
    vec3 sanityTint = vec3(1.05, 0.98, 0.82);
    color.rgb *= sanityTint;

    // Slight desaturation
    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    color.rgb = mix(color.rgb, vec3(gray), 0.15);

    // Much stronger vignette effect
    vec2 vigUV = uv - 0.5;
    float dist = length(vigUV);
    float vignette = smoothstep(0.7, 0.2, dist);
    vignette = mix(0.05, 1.0, vignette);
    color.rgb *= vignette;

    // Film grain
    float grain = (noise(uv * 500.0 + Time * 0.1) - 0.5) * 0.02;
    color.rgb += grain;

    // Subtle pulsing
    float pulse = sin(Time * 0.8) * 0.02 + 1.0;
    color.rgb *= pulse;

    fragColor = color;
}
#version 150

uniform sampler2D DiffuseSampler;
uniform float Time;
in vec2 texCoord;
out vec4 fragColor;

// Lightweight noise
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

    return mix(mix(a,b,f.x), mix(c,d,f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 3; i++) {
        v += a * noise(p);
        p *= 2.0;
        a *= 0.5;
    }
    return v;
}

// Efficient snowflakes
float snow(vec2 uv, vec2 off, float speed, float scale) {
    vec2 pos = uv + off;
    pos.y = fract(pos.y + Time * speed);
    vec2 cell = floor(pos * scale);
    vec2 fracUV = fract(pos * scale);
    vec2 rnd = vec2(hash(cell), hash(cell + 1.0));
    return smoothstep(0.08, 0.0, length(fracUV - rnd));
}

void main() {
    vec2 uv = texCoord;

    // Wind distortion (cheap)
    uv.x += fbm(vec2(uv.y * 3.0 + Time * 0.6)) * 0.015;

    // Grab scene
    vec4 color = texture(DiffuseSampler, uv);

    // Cold grading — blue
    float g = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    color.rgb = mix(color.rgb, vec3(g), 0.45);
    color.rgb *= vec3(0.75, 0.85, 1.15);
    color.rgb *= 0.75; // whiteout

    // Snow layers (reduced from 4 → 2)
    float snowA = snow(uv, vec2(0.0), 0.4, 60.0);
    float snowB = snow(uv, vec2(0.3, 0.7), 0.65, 75.0);
    float snowTotal = snowA + snowB;

    // Wind-streaked appearance
    float wind = fbm(uv * vec2(6.0, 10.0) + Time * vec2(2.0, -2.0));
    snowTotal += smoothstep(0.55, 0.7, wind) * 0.25;

    // Apply snow overlay
    color.rgb += vec3(1.0) * snowTotal * 0.65;

    // Visibility fog — cheap
    float fog = fbm(uv * 2.0 + Time * 0.3);
    color.rgb = mix(color.rgb, vec3(0.82, 0.88, 0.95), fog * 0.3);

    // Vignette — peripheral snow
    float d = length(uv - 0.5);
    color.rgb = mix(vec3(0.9, 0.93, 0.97), color.rgb, smoothstep(0.85, 0.2, d));

    fragColor = vec4(color.rgb, 1.0);
}

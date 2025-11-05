#version 150

uniform sampler2D DiffuseSampler;
uniform float Time;

in vec2 texCoord;

out vec4 fragColor;

// Noise functions
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

// Fractal noise for wind and snow clouds
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

// Snowflake generation
float snowflake(vec2 uv, vec2 offset, float speed, float size) {
    vec2 pos = uv + offset;
    pos.y += Time * speed;
    pos.y = fract(pos.y);

    vec2 cell = floor(pos * 80.0 / size);
    vec2 cellUV = fract(pos * 80.0 / size);

    float random = hash(cell);
    vec2 snowPos = vec2(random, hash(cell + vec2(1.0, 0.0)));

    float dist = length(cellUV - snowPos);
    return smoothstep(size, 0.0, dist);
}

// Wind distortion
vec2 windDistortion(vec2 uv, float time) {
    float wind = fbm(vec2(uv.y * 3.0 + time * 0.5, time * 0.3));
    return vec2(wind * 0.02, 0.0);
}

void main() {
    vec2 uv = texCoord;

    // Apply wind distortion
    vec2 distortion = windDistortion(uv, Time);
    vec2 distortedUV = uv + distortion;

    // Get original color
    vec4 color = texture(DiffuseSampler, distortedUV);

    // Cold color grading - desaturate and add blue tint
    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    color.rgb = mix(color.rgb, vec3(gray), 0.4); // Desaturate 40%
    color.rgb *= vec3(0.8, 0.9, 1.2); // Blue-white tint

    // Reduce overall brightness (whiteout effect)
    color.rgb *= 0.7;

    // Multiple layers of snowflakes at different speeds and sizes
    float snow1 = snowflake(uv, vec2(0.0), 0.3, 0.15);
    float snow2 = snowflake(uv, vec2(0.3, 0.7), 0.5, 0.12);
    float snow3 = snowflake(uv, vec2(0.6, 0.2), 0.7, 0.1);
    float snow4 = snowflake(uv, vec2(0.1, 0.5), 0.4, 0.18);

    float totalSnow = snow1 + snow2 + snow3 + snow4;

    // Add diagonal wind streaks to snowflakes
    float windStreaks = fbm(vec2(uv.x * 5.0 - uv.y * 8.0 + Time * 2.0, uv.y * 10.0));
    windStreaks = smoothstep(0.4, 0.6, windStreaks);
    totalSnow += windStreaks * 0.3;

    // Apply snow overlay
    color.rgb += vec3(1.0) * totalSnow * 0.6;

    // Blowing snow fog - reduces visibility
    float fog = fbm(uv * 3.0 + vec2(Time * 0.4, Time * 0.2));
    fog = smoothstep(0.3, 0.7, fog);
    color.rgb = mix(color.rgb, vec3(0.85, 0.9, 0.95), fog * 0.4);

    // Ice crystals in air - small bright specks
    float crystals = hash(uv * 200.0 + Time * 5.0);
    if(crystals > 0.97) {
        color.rgb += vec3(0.9, 0.95, 1.0) * 0.5;
    }

    // Vignette - snow accumulation at edges obscuring view
    vec2 center = vec2(0.5, 0.5);
    float distFromCenter = length(uv - center);
    float vignette = smoothstep(0.7, 0.4, distFromCenter);
    color.rgb = mix(vec3(0.9, 0.92, 0.95), color.rgb, vignette);

    // Frost on "lens" - edge frost effect
    float frost = fbm(uv * 15.0);
    frost *= 1.0 - smoothstep(0.3, 0.8, distFromCenter); // Only at edges
    color.rgb = mix(color.rgb, vec3(0.95, 0.97, 1.0), frost * 0.2);

    // Snow accumulation streaks - diagonal lines
    float accumulation = sin((uv.x - uv.y * 0.5) * 40.0 + Time * 0.5);
    accumulation = smoothstep(0.7, 0.9, accumulation);
    accumulation *= smoothstep(0.5, 0.9, distFromCenter); // More at edges
    color.rgb += vec3(0.9, 0.92, 0.95) * accumulation * 0.15;

    // Blizzard wind gusts - occasional white flashes
    float windGust = fbm(vec2(Time * 0.8, uv.y * 2.0));
    windGust = smoothstep(0.75, 0.85, windGust);
    color.rgb += vec3(0.3, 0.32, 0.35) * windGust;

    // Motion blur streaks for fast-moving snow
    vec2 motionDir = vec2(0.02, -0.05);
    vec4 motionBlur = vec4(0.0);
    for(float i = 0.0; i < 5.0; i++) {
        motionBlur += texture(DiffuseSampler, distortedUV + motionDir * i * 0.002);
    }
    motionBlur /= 5.0;
    color = mix(color, motionBlur, totalSnow * 0.3);

    // Icy shimmer effect
    float shimmer = sin(uv.x * 50.0 + uv.y * 30.0 + Time * 3.0) * 0.5 + 0.5;
    shimmer *= noise(uv * 20.0);
    color.rgb += vec3(0.8, 0.85, 1.0) * shimmer * 0.05;

    // Depth fog layers - closer snow is brighter
    float depthFog1 = fbm(uv * 4.0 + vec2(Time * 0.3, Time * 0.15));
    float depthFog2 = fbm(uv * 2.0 - vec2(Time * 0.2, Time * 0.1));
    float combinedFog = (depthFog1 + depthFog2) * 0.5;
    color.rgb = mix(color.rgb, vec3(0.8, 0.85, 0.92), combinedFog * 0.25);

    // Chromatic aberration from ice crystals in air
    float aberration = totalSnow * 0.002;
    vec2 dir = normalize(uv - center);
    float r = texture(DiffuseSampler, distortedUV + dir * aberration).r;
    float b = texture(DiffuseSampler, distortedUV - dir * aberration).b;
    color.r = mix(color.r, r, 0.3);
    color.b = mix(color.b, b, 0.3);

    // Temperature color shift - add slight blue glow
    color.rgb += vec3(0.0, 0.05, 0.1) * 0.3;

    fragColor = vec4(color.rgb, 1.0);
}
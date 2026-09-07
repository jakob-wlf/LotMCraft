#version 150

uniform sampler2D DiffuseSampler;

uniform float GameTime;    // 0..1, wraps every MC day - used for a very slow shimmer
uniform float ShardScale;  // roughly how many shards across the screen
uniform float ShardStrength; // how far each shard's image is displaced (UV units)
uniform float CrackWidth;  // thickness of the crack lines (UV units)

in vec2 texCoord;
in vec2 oneTexel;
in vec2 screenAspect;

out vec4 fragColor;

// ---- hash / noise helpers ----
vec2 hash2(vec2 p) {
    p = vec2(dot(p, vec2(127.1, 311.7)), dot(p, vec2(269.5, 183.3)));
    return fract(sin(p) * 43758.5453123);
}

// Voronoi that returns:
//   x = distance to the nearest cell point
//   y = distance to the nearest cell edge (for crack lines)
//   zw = integer id of the nearest cell (for per-shard randomness)
vec4 voronoi(vec2 x) {
    vec2 p = floor(x);
    vec2 f = fract(x);

    vec2 nearestCell = vec2(0.0);
    vec2 nearestOffset = vec2(0.0);
    float nearestDist = 8.0;

    for (int j = -1; j <= 1; j++) {
        for (int i = -1; i <= 1; i++) {
            vec2 cell = vec2(float(i), float(j));
            vec2 jitter = hash2(p + cell);
            vec2 r = cell + jitter - f;
            float d = dot(r, r);
            if (d < nearestDist) {
                nearestDist = d;
                nearestOffset = r;
                nearestCell = cell;
            }
        }
    }

    float edgeDist = 8.0;
    for (int j = -2; j <= 2; j++) {
        for (int i = -2; i <= 2; i++) {
            vec2 cell = nearestCell + vec2(float(i), float(j));
            vec2 jitter = hash2(p + cell);
            vec2 r = cell + jitter - f;
            vec2 diff = r - nearestOffset;
            if (dot(diff, diff) > 0.0001) {
                float d = dot(0.5 * (nearestOffset + r), normalize(diff));
                edgeDist = min(edgeDist, d);
            }
        }
    }

    return vec4(sqrt(nearestDist), edgeDist, p + nearestCell);
}

void main() {
    vec2 uv = texCoord;

    // sample space stretched to be aspect-correct so shards look like real
    // glass fragments instead of squashed rectangles
    vec2 cellSpace = uv * screenAspect * ShardScale;

    vec4 vor = voronoi(cellSpace);
    float edgeDist = vor.y;
    vec2 cellId = vor.zw;

    // per-shard pseudo-random values
    vec2 cellRand = hash2(cellId) - 0.5;
    float cellRand2 = hash2(cellId + 17.23).x;

    // slow shimmer so shards subtly "catch the light" over time
    float shimmer = sin(GameTime * 6.2831 * 40.0 + cellRand2 * 6.2831) * 0.05;

    // each shard samples the scene from a slightly shifted UV, as if the
    // fragment of glass is refracting/displacing what's behind it
    vec2 shardOffset = cellRand * ShardStrength;
    vec2 distortedUV = clamp(uv + shardOffset, oneTexel, 1.0 - oneTexel);

    vec3 color = texture(DiffuseSampler, distortedUV).rgb;

    // per-shard brightness/tint variance sells the "separate piece of glass" look
    float cellShade = 0.85 + 0.3 * cellRand.x + shimmer;
    color *= cellShade;

    // slight blue-white tint per shard, like light glinting off glass
    color += vec3(0.02, 0.03, 0.04) * (0.5 + cellRand.y);

    // crack lines: dark right at the boundary, thin white highlight just inside it
    float crackMask = smoothstep(0.0, CrackWidth, edgeDist);
    color *= mix(0.08, 1.0, crackMask);

    float highlight = 1.0 - smoothstep(0.0, CrackWidth * 0.4, edgeDist);
    color += highlight * 0.5;

    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}

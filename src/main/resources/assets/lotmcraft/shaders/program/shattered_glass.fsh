#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 OutSize;

in vec2 texCoord;
out vec4 fragColor;

// Better hash function for randomness
float hash(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

// 2D noise
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

// Voronoi for irregular cells (shattered pieces)
vec3 voronoi(vec2 p) {
    vec2 n = floor(p);
    vec2 f = fract(p);

    float minDist = 8.0;
    vec2 minPoint = vec2(0.0);
    vec2 minCell = vec2(0.0);

    // Check 3x3 neighborhood
    for(int j = -1; j <= 1; j++) {
        for(int i = -1; i <= 1; i++) {
            vec2 neighbor = vec2(float(i), float(j));
            vec2 cell = n + neighbor;

            // Random point within cell
            vec2 point = neighbor + vec2(
            hash(cell),
            hash(cell + vec2(43.21, 57.43))
            );

            float dist = length(point - f);

            if(dist < minDist) {
                minDist = dist;
                minPoint = point;
                minCell = cell;
            }
        }
    }

    return vec3(minDist, minCell);
}

// Create crack pattern
float cracks(vec2 uv) {
    float crack = 0.0;

    // Multiple crack layers with different scales (increased density)
    vec3 v1 = voronoi(uv * 15.0);
    vec3 v2 = voronoi(uv * 20.0);
    vec3 v3 = voronoi(uv * 10.0);

    // Main cracks (thick) - tightened threshold to remove dots
    float edge1 = smoothstep(0.015, 0.008, v1.x);

    // Secondary cracks (medium)
    float edge2 = smoothstep(0.012, 0.004, v2.x) * 0.7;

    // Fine cracks (thin)
    float edge3 = smoothstep(0.008, 0.002, v3.x) * 0.4;

    crack = max(edge1, max(edge2, edge3));

    // Add some noise to cracks
    crack *= 0.7 + 0.3 * noise(uv * 50.0);

    return crack;
}

void main() {
    vec2 uv = texCoord;

    // Get voronoi cell info
    vec3 voronoiData = voronoi(uv * 8.0);
    float cellDist = voronoiData.x;
    vec2 cellId = voronoiData.yz;

    // Create unique random values per cell
    float cellRand1 = hash(cellId);
    float cellRand2 = hash(cellId + vec2(12.34, 56.78));
    float cellRand3 = hash(cellId + vec2(91.23, 45.67));

    // Distortion: each shard is offset/rotated slightly differently
    vec2 distortion = vec2(
    sin(cellRand1 * 6.28318) * 0.015,
    cos(cellRand2 * 6.28318) * 0.015
    );

    // Add rotational distortion based on distance from cell center
    float angle = cellRand3 * 0.3 - 0.15; // Random rotation per shard
    float s = sin(angle);
    float c = cos(angle);
    vec2 centered = uv - 0.5;
    vec2 rotated = vec2(
    c * centered.x - s * centered.y,
    s * centered.x + c * centered.y
    ) + 0.5;

    // Combine distortions
    vec2 sampleUv = mix(uv, rotated, cellDist * 2.0) + distortion;

    // Sample the texture
    vec4 color = texture(DiffuseSampler, sampleUv);

    // Get crack pattern
    float crackValue = cracks(uv);

    // Darken cracks significantly
    vec3 crackColor = vec3(0.1, 0.12, 0.15);
    color.rgb = mix(color.rgb, crackColor, crackValue * 0.9);

    // Add slight darkening at cell edges (shadow effect)
    float edgeShadow = smoothstep(0.1, 0.05, cellDist) * 0.15;
    color.rgb *= 1.0 - edgeShadow;

    // Add subtle glass reflections on some shards
    if(cellRand1 > 0.6) {
        float highlight = smoothstep(0.15, 0.05, cellDist) * (cellRand2 * 0.15);
        color.rgb += vec3(highlight * 1.2, highlight * 1.3, highlight * 1.4);
    }

    // Optional: Add slight chromatic aberration at cracks
    if(crackValue > 0.5) {
        vec2 aberration = normalize(vec2(cellRand1 - 0.5, cellRand2 - 0.5)) * 0.003;
        float r = texture(DiffuseSampler, sampleUv + aberration).r;
        float b = texture(DiffuseSampler, sampleUv - aberration).b;
        color.r = mix(color.r, r, 0.5);
        color.b = mix(color.b, b, 0.5);
    }

    fragColor = color;
}
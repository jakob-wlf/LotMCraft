#version 150
// Space Fragmentation impact frame: the SCREEN itself breaks into Voronoi panes that slide apart along glowing
// verdigris seams, with a negative/ink pass at the peak. Strength (= Photon effect weight) drives everything.
uniform sampler2D DiffuseSampler;
uniform vec4 DiffuseSampler_TexelSize;
uniform float GameTime;
uniform float Strength;
uniform float Cells;
uniform float Displace;
uniform vec4 CrackColor;
uniform vec4 CoreColor;

in vec2 texCoord;
out vec4 fragColor;

vec2 h2(vec2 p) { p = vec2(dot(p, vec2(127.1, 311.7)), dot(p, vec2(269.5, 183.3))); return fract(sin(p) * 43758.5453); }

void main() {
    float s = clamp(Strength, 0.0, 1.0);
    float aspect = DiffuseSampler_TexelSize.x > 0.0 ? DiffuseSampler_TexelSize.y / DiffuseSampler_TexelSize.x : 16.0 / 9.0;
    if (aspect < 0.2 || aspect > 5.0) aspect = 16.0 / 9.0;
    // cells are denser near the centre (radial warp) so the break reads like an impact point
    vec2 q = (texCoord - 0.5) * vec2(aspect, 1.0);
    float r = length(q);
    vec2 w = q * (1.0 + 2.2 / (1.0 + r * 9.0)) * Cells;

    vec2 ip = floor(w), fp = fract(w);
    float d1 = 8.0, d2 = 8.0; vec2 cid = vec2(0.0), c1 = vec2(0.0);
    for (int j = -1; j <= 1; j++) for (int i = -1; i <= 1; i++) {
        vec2 g = vec2(float(i), float(j));
        vec2 o = h2(ip + g) * 0.85 + 0.075;
        vec2 dv = g + o - fp;
        float d = dot(dv, dv);
        if (d < d1) { d2 = d1; d1 = d; cid = ip + g; c1 = dv; } else if (d < d2) { d2 = d; }
    }
    // distance to the nearest seam (second pass, exact border distance)
    float edge = 8.0;
    for (int j = -2; j <= 2; j++) for (int i = -2; i <= 2; i++) {
        vec2 g = vec2(float(i), float(j));
        vec2 cell = ip + g;
        if (cell == cid) continue;
        vec2 o = h2(cell) * 0.85 + 0.075;
        vec2 dv = g + o - fp;
        edge = min(edge, dot(0.5 * (c1 + dv), normalize(dv - c1)));
    }

    // every pane slides outward from the centre and tilts a little; the pane under the centre stays put
    vec2 rnd = h2(cid * 1.37 + 4.1);
    vec2 cellCentre = (cid + 0.5) / Cells;
    vec2 away = length(cellCentre) > 1e-3 ? normalize(cellCentre) : vec2(0.0);
    vec2 shift = (away * (0.35 + rnd.x) + (rnd - 0.5) * 0.6) * Displace * s;
    shift.x /= aspect;
    float zoom = 1.0 + (rnd.y - 0.5) * 0.06 * s;
    vec2 uv = (texCoord - 0.5) * zoom + 0.5 - shift;

    vec3 c = texture(DiffuseSampler, uv).rgb;
    // each pane catches the light differently
    c *= 1.0 + (rnd.x - 0.5) * 0.55 * s;
    // ink/negative snap at the very peak: panes alternate between inverted and dark
    float peak = smoothstep(0.82, 1.0, s);
    float lum = dot(c, vec3(0.299, 0.587, 0.114));
    vec3 ink = rnd.y > 0.5 ? vec3(1.0 - lum) * vec3(1.0, 0.97, 0.9) : vec3(lum * 0.25, lum * 0.05, lum * 0.04);
    c = mix(c, ink, peak);

    // glowing seams: silver-white core, verdigris halo
    float px = DiffuseSampler_TexelSize.y > 0.0 ? DiffuseSampler_TexelSize.y * Cells * 2.2 : 0.004;
    float width = px * (0.5 + 1.2 * s);
    float seam = 1.0 - smoothstep(0.0, width, edge);
    float halo = 1.0 - smoothstep(0.0, width * 4.0, edge);
    // cracks race outward from the impact point as the strength rises
    float reach = smoothstep(s * 1.15, s * 1.15 - 0.12, r);
    c += (CoreColor.rgb * seam + CrackColor.rgb * halo * 0.4) * s * reach;
    fragColor = vec4(c, 1.0);
}
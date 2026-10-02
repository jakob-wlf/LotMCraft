#version 150

uniform float GameTime;
uniform vec4 RimColor;
uniform vec4 GlowColor;
uniform float RimPower;
uniform float EdgeSharp;
uniform float Pulse;

in vec3 vObj;
in vec3 vN;
in vec3 vPos;
in vec2 uv;
in vec4 vColor;
out vec4 fragColor;

void main() {
    vec3 N = normalize(vN);
    vec3 V = normalize(-vPos);
    float ndv = clamp(dot(N, V), 0.0, 1.0);
    float t = GameTime * 1200.0;
    // pure black body with a razor-thin bright photon ring at the silhouette
    float ring = smoothstep(EdgeSharp * 1.6, EdgeSharp * 0.15, ndv);
    float line = pow(1.0 - ndv, RimPower) * (1.0 + Pulse * sin(t * 6.0));
    vec3 col = RimColor.rgb * ring * 0.9 + GlowColor.rgb * line * 0.55;
    float a = vColor.a;
    if (a < 0.003) discard;
    // alpha blended: black core stays opaque, rim is HDR-bright
    fragColor = vec4(col, a);
}
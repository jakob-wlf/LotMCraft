#version 150
// Space-Time Storm: ground shockwave (st_wave model, a low ring crest, uv.x = across the crest). Additive.
// vColor.r = brightness, vColor.a = fade.
uniform float GameTime;
uniform vec4 EdgeColor;
uniform vec4 HotColor;
uniform float Intensity;

in vec3 vObj;
in vec3 vN;
in vec3 vPos;
in vec2 uv;
in vec4 vColor;
out vec4 fragColor;

void main() {
    float u = clamp(uv.x, 0.0, 1.0);
    float g = pow(sin(u * 3.14159), 1.6);
    float crest = pow(g, 6.0);
    vec3 col = (EdgeColor.rgb * g + HotColor.rgb * crest) * Intensity * vColor.r;
    float a = clamp(g, 0.0, 1.0) * vColor.a;
    if (a < 0.003) discard;
    fragColor = vec4(col * vColor.a, a);
}
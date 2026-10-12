#version 150
// Space-Time Storm v2: the sky vortex on st_vortex_dish (uv.x = angle / 2pi, uv.y = 0 at the eye .. 1 at the rim).
// Log-spiral arms of violet light with gas detail, flowing inward; the eye is black with a white-violet ring.
// Layer 0 = the main body (alpha: darkens the sky between the arms), Layer 1 = a thin additive wisp layer on top.
// vColor.r = how far the vortex has formed (0 = nothing, 1 = all of it, grows from the eye out),
// vColor.b = arm brightness (pulse flares), vColor.a = overall fade.
uniform float GameTime;
uniform vec4 DeepColor;
uniform vec4 ArmColor;
uniform vec4 HotColor;
uniform float Arms;
uniform float Twist;
uniform float Flow;
uniform float Layer;
uniform float EyeR;
uniform float Intensity;

in vec3 vObj;
in vec3 vN;
in vec3 vPos;
in vec2 uv;
in vec4 vColor;
out vec4 fragColor;

float h21(vec2 p) { vec3 p3 = fract(vec3(p.xyx) * 0.1031); p3 += dot(p3, p3.yzx + 33.33); return fract((p3.x + p3.y) * p3.z); }
float vn(vec2 p) {
    vec2 i = floor(p); vec2 f = fract(p); f = f * f * (3.0 - 2.0 * f);
    return mix(mix(h21(i), h21(i + vec2(1, 0)), f.x), mix(h21(i + vec2(0, 1)), h21(i + vec2(1, 1)), f.x), f.y);
}
float fbm(vec2 p) {
    float s = 0.0, a = 0.5;
    for (int i = 0; i < 5; i++) { s += a * vn(p); p = mat2(1.6, 1.2, -1.2, 1.6) * p + vec2(3.1, 7.7); a *= 0.5; }
    return s;
}

vec4 safe(vec4 c) {
    if (any(isnan(c)) || any(isinf(c))) return vec4(0.0);
    return vec4(clamp(c.rgb, 0.0, 8.0), clamp(c.a, 0.0, 1.0));
}

void main() {
    float tick = mod(GameTime * 24000.0, 6000.0);
    float th = uv.x * 6.2831853;
    float rr = clamp(uv.y, 0.0, 1.0);
    float lr = log(rr + 0.06);
    // spiral-warped Cartesian domain (no seam at the angle wrap)
    float sw = -Twist * lr + tick * Flow;
    vec2 p = vec2(cos(th + sw * 0.35), sin(th + sw * 0.35)) * (0.15 + rr);
    float warp = fbm(p * 3.0 + vec2(0.0, tick * 0.004));
    float arm = 0.5 + 0.5 * cos(Arms * th - Twist * 2.0 * lr + tick * Flow * 6.0 + warp * 3.0);
    float gas = fbm(p * 7.0 - vec2(tick * 0.003, 0.0));
    float fil = fbm(vec2(th * 6.0 + lr * 9.0, lr * 22.0 + tick * 0.02));          // fine streaks along the arms
    float dens;
    if (Layer < 0.5) {
        arm = pow(arm, 2.2);
        dens = arm * (0.45 + 0.8 * gas) + 0.12 * fil;
    } else {
        arm = pow(arm, 7.0);
        dens = arm * pow(fil, 2.0) * 2.2;
    }
    // the eye: black, ringed by white-violet light
    float eyeR = EyeR;
    float ring = exp(-pow((rr - eyeR) / 0.035, 2.0)) * (0.6 + 0.8 * gas);
    float inner = smoothstep(eyeR - 0.06, eyeR + 0.12, rr);
    vec3 armCol = mix(HotColor.rgb, ArmColor.rgb, smoothstep(eyeR, 0.65, rr));
    vec3 emis = armCol * dens * (1.5 - rr) * inner + HotColor.rgb * ring * 1.6;
    emis *= vColor.b * Intensity;
    // formation: grows from the eye outward with a bright leading edge
    float reach = vColor.r * 1.2;                                               // vertex colours stop at 1
    float mask = 1.0 - smoothstep(reach - 0.14, reach, rr);
    float front = exp(-pow((rr - reach + 0.04) / 0.05, 2.0)) * step(0.01, reach) * step(reach, 1.1);
    emis += ArmColor.rgb * front * 0.9 * (0.5 + gas);
    float edge = 1.0 - smoothstep(0.72, 1.0, rr);
    if (Layer < 0.5) {
        float a = clamp(0.55 + 0.45 * dens, 0.0, 1.0) * edge * mask * vColor.a;
        a = max(a, (1.0 - inner) * 0.92 * mask * vColor.a);                          // keep the eye dark
        if (a < 0.003) discard;
        fragColor = safe(vec4(DeepColor.rgb * (1.0 - min(dens, 1.0)) + emis, a));
    } else {
        float a = clamp(dens, 0.0, 1.0) * edge * mask * vColor.a;
        if (a < 0.003) discard;
        fragColor = safe(vec4(emis * a, a));
    }
}

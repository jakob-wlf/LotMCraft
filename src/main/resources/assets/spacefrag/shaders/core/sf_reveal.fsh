#version 150

uniform sampler2D Texture;
uniform vec4 TintColor;
uniform float Intensity;
uniform float Halo;
uniform float HaloRadius;
uniform float HeadGlow;

in vec2 texCoord0;
in vec4 vertexColor;
out vec4 fragColor;

void main() {
    float progress = vertexColor.a;
    float fade = vertexColor.r;

    float core = texture(Texture, texCoord0).a;
    float halo = 0.0;
    if (Halo > 0.001) {      // skip the 16 extra fetches entirely when the layer has no halo (most big glow quads)
        for (int i = 0; i < 8; i++) {
            float a = 6.2831853 * float(i) / 8.0;
            vec2 o = vec2(cos(a), sin(a));
            halo += texture(Texture, texCoord0 + o * HaloRadius).a;
            halo += texture(Texture, texCoord0 + o * HaloRadius * 2.4).a * 0.55;
        }
        halo /= 12.4;
    }

    // polar sweep: angle 0..1 around the centre, drawn clockwise
    vec2 p = texCoord0 - 0.5;
    float ang = fract(atan(p.x, -p.y) / 6.2831853 + 1.0);
    float drawn = 1.0 - smoothstep(progress - 0.004, progress + 0.001, ang);
    float live = step(0.001, progress) * (1.0 - step(0.999, progress));
    float head = exp(-pow((ang - progress) / 0.018, 2.0)) * live;

    float shape = core + halo * Halo;
    vec3 rgb = TintColor.rgb * Intensity * (core * drawn + halo * Halo * drawn + shape * head * HeadGlow);
    float alpha = clamp(shape * max(drawn, head), 0.0, 1.0) * fade;
    rgb *= fade;
    if (alpha < 0.002) {
        discard;
    }
    fragColor = vec4(rgb, alpha);
}
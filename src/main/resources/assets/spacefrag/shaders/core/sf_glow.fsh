#version 150

uniform sampler2D Texture;
uniform float GameTime;
uniform vec4 TintColor;
uniform float Intensity;
uniform float Halo;
uniform float HaloRadius;
uniform float Shimmer;
uniform float ShimmerSpeed;

in vec2 texCoord0;
in vec4 vertexColor;
out vec4 fragColor;

void main() {
    float core = texture(Texture, texCoord0).a;

    // soft halo: two rings of taps around the texel give a cheap blur of the mask
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

    // bright band sweeping around the ring
    vec2 p = texCoord0 - 0.5;
    float ang = atan(p.y, p.x);
    float t = GameTime * 1200.0;
    float sweep = pow(max(0.0, cos(ang - t * ShimmerSpeed)), 28.0) * Shimmer;

    vec3 rgb = TintColor.rgb * Intensity * (core * (1.0 + sweep * 4.0) + halo * Halo);
    float alpha = clamp(core + halo * Halo, 0.0, 1.0);
    float fade = vertexColor.a;
    rgb *= vertexColor.rgb * fade;
    if (alpha * fade < 0.002) {
        discard;
    }
    // premultiplied output, blended ONE/ONE (additive)
    fragColor = vec4(rgb, alpha * fade);
}
#version 150
// Space-Time Storm: glass rain / wind streaks on stretched billboards. Orientation-free soft streak (the quad is stretched
// along the velocity by Photon), additive. vertexColor.a = fade.
uniform float GameTime;
uniform vec4 TintColor;
uniform float Intensity;
in vec2 texCoord0;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec2 p = texCoord0 * 2.0 - 1.0;
    float d = length(p);
    float a = pow(max(0.0, 1.0 - d), 1.6) * vertexColor.a;
    if (a < 0.003) discard;
    fragColor = vec4(TintColor.rgb * Intensity * a * vertexColor.r, a);
}
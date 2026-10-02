#version 150

uniform sampler2D Texture;
uniform vec4 TintColor;

in vec2 texCoord0;
in vec4 vertexColor;
out vec4 fragColor;

void main() {
    float a = texture(Texture, texCoord0).a * TintColor.a * vertexColor.a;
    if (a < 0.003) {
        discard;
    }
    fragColor = vec4(TintColor.rgb * vertexColor.rgb, a);
}
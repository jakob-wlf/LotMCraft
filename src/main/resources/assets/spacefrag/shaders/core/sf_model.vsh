#version 330 core
#moj_import <photon:particle.glsl>

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec3 vObj;
out vec3 vN;
out vec3 vPos;
out vec2 uv;
out vec4 vColor;

void main() {
    ParticleData data = getParticleData();
    gl_Position = ProjMat * ModelViewMat * vec4(data.Position, 1.0);
    vObj = data.ObjectPosition;
    vN = data.Normal;
    vPos = data.Position;
    uv = data.UV;
    vColor = data.Color;
}
#version 330 core

layout (location = 0) in vec4 position;
layout (location = 1) in vec4 color;

uniform mat4 PROJECTION;
uniform mat4 MODELVIEW;

out vec4 fragColor;

void main() {
    gl_Position = PROJECTION * MODELVIEW * position;
    fragColor = color;
}

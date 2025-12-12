#version 320 es

layout(location = 0) in vec2 in_position;
layout(location = 1) in vec2 in_texcoord;

layout(location = 0) out vec2 out_texcoord;

void main() {
    gl_Position = vec4(in_position, 0f, 1f);
    out_texcoord = in_texcoord;
}

#version 320 es
#extension GL_OES_EGL_image_external_essl3 : require
precision mediump float;

layout(location = 0) in vec2 in_texcoord;

uniform samplerExternalOES camera_texture;

out vec4 frag_color;

void main() {
    frag_color = texture(camera_texture, in_texcoord);
}
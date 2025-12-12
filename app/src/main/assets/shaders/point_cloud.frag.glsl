#version 320 es
precision mediump float;

layout(location = 0) flat in int in_vertexID;

out vec4 frag_color;

uniform vec4 point_color;
uniform int picked_index;

void main() {

    float d = length(gl_PointCoord - vec2(0.5));
    if (d > 0.5) discard;

    float ring = smoothstep(0.5, 0.45, d);
    vec3 color = mix(point_color.xyz, vec3(1, 0, 0), in_vertexID == picked_index);
    frag_color = vec4(mix(color, vec3(0, 0, 0), d > 0.4), ring);
}

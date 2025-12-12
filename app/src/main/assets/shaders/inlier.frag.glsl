#version 320 es
precision mediump float;

out vec4 frag_color;

uniform vec4 point_color;

void main() {

    float d = length(gl_PointCoord - vec2(0.5));
    if (d > 0.5) discard;

    float ring = smoothstep(0.5, 0.45, d);
    vec3 color = point_color.xyz;
    frag_color = vec4(mix(color, vec3(0, 0, 0), d > 0.4), ring);
}

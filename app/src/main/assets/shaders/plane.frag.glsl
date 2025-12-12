#version 320 es
precision mediump float;

layout(std140, binding = 1) uniform PlaneUniform {
    vec3 lower_left;
    vec3 upper_left;
    vec3 upper_right;
    vec3 lower_right;
    vec3 color;
    float opacity;
    vec3 line_color;
};

layout(location = 0) in vec3 in_barycentric_coordinate;

out vec4 frag_color;

float edge_factor(vec3 bary, float line_width) {
    vec3 d = fwidth(bary);
    vec3 a = bary / d;
    float edge = min(min(a.x, a.y), a.z);
    return smoothstep(0.0, line_width, edge);
}

void main() {

    float line_width = 1.0f;
    float factor = edge_factor(in_barycentric_coordinate, line_width);

    vec3 final_color = mix(line_color, color, clamp(factor, 0.0, 1.0));
    frag_color = vec4(final_color, opacity);
}

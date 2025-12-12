#version 320 es

const ivec2 triangle_corners[6] = ivec2[6](
    ivec2(0, 0), ivec2(1, 0), ivec2(0, 1),
    ivec2(1, 0), ivec2(1, 1), ivec2(0, 1)
);

void uv_from_vertex(int vertexID, ivec2 grid, out float u, out float v) {
    int nu = grid.x;
    int nv = grid.y;

    int tri_vertex = vertexID % 6;
    int cell = vertexID / 6;
    int i = cell % nu;
    int j = cell / nu;

    ivec2 c = triangle_corners[tri_vertex];
    u = (float(i) + float(c.x)) / float(nu);
    v = (float(j) + float(c.y)) / float(nv);
}

const vec3 triangle_barycentric_coordinates[6] = vec3[6](
    vec3(1, 0, 0), vec3(0, 1, 0), vec3(0, 0, 1),
    vec3(0, 1, 0), vec3(1, 0, 0), vec3(0, 0, 1)
);

layout(std140, binding = 0) uniform Transform {
    mat4 view_matrix;
    mat4 projection_matrix;
    mat4 view_projection_matrix;
};

layout(std140, binding = 1) uniform PlaneUniform {
    vec3 lower_left;
    vec3 upper_left;
    vec3 upper_right;
    vec3 lower_right;
    vec3 color;
    float opacity;
    vec3 line_color;
};

uniform ivec2 grid;

layout(location = 0) out vec3 out_barycentric_coordinate;

void main() {

    float u, v;
    uv_from_vertex(gl_VertexID, grid, u, v);

    vec3 upper = mix(upper_left, upper_right, u);
    vec3 lower = mix(lower_left, lower_right, u);
    vec3 wpos = mix(upper, lower, v);

    gl_Position = view_projection_matrix * vec4(wpos, 1.0);
    out_barycentric_coordinate = triangle_barycentric_coordinates[gl_VertexID % 6];
}


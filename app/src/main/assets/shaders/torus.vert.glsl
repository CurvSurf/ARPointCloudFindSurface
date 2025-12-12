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

void make_orthonormal_bases(in vec3 n, out vec3 t, out vec3 b) {
    if (n.z < -0.999999) {
        t = vec3(0.0, -1.0, 0.0);
        b = vec3(-1.0, 0.0, 0.0);
    } else {
        float a = 1.0 / (1.0 + n.z);
        float bx = -n.x * n.y * a;
        t = vec3(1.0 - n.x * n.x * a, bx, -n.x);
        b = vec3(bx, 1.0 - n.y * n.y * a, -n.y);
    }
}

layout(std140, binding = 0) uniform Transform {
    mat4 view_matrix;
    mat4 projection_matrix;
    mat4 view_projection_matrix;
};

layout(std140, binding = 1) uniform TorusUniorm {
    vec3 center;
    float mean_radius;
    vec3 axis;
    float tube_radius;
    vec3 color;
    float opacity;
    vec3 line_color;
};

uniform ivec2 grid;

layout(location = 0) out vec3 out_barycentric_coordinate;

void main() {

    float u, v;
    uv_from_vertex(gl_VertexID, grid, u, v);

    vec3 T, B;
    make_orthonormal_bases(axis, T, B);

    float theta = u * 6.2831853;
    float phi = v * 6.2831853;

    float ct = cos(theta);
    float st = sin(theta);
    float cp = cos(phi);
    float sp = sin(phi);

    vec3 ring_dir = ct * T + st * B;
    vec3 wpos = center + (mean_radius + tube_radius * cp) * ring_dir + (tube_radius * sp) * axis;

    gl_Position = view_projection_matrix * vec4(wpos, 1.0);
    out_barycentric_coordinate = triangle_barycentric_coordinates[gl_VertexID % 6];
}

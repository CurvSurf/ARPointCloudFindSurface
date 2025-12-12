#version 320 es

layout(location = 0) in vec3 in_position;

layout(std140, binding = 0) uniform Transform {
    mat4 view_matrix;
    mat4 projection_matrix;
    mat4 view_projection_matrix;
};

layout(location = 0) flat out int out_vertexID;

void main() {

    vec4 cpos = view_projection_matrix * vec4(in_position, 1f);

    float scale = min(projection_matrix[0][0], projection_matrix[1][1]);
    float point_size = max(2.0 * (scale / pow(cpos.w, 0.6)), 4.0) * 3.0;

    gl_Position = cpos;
    gl_PointSize = point_size;
    out_vertexID = gl_VertexID;
}

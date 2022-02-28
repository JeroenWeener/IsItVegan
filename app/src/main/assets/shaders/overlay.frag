#version 300 es

precision mediump float;

uniform sampler2D u_VirtualSceneColorTexture;

in vec2 v_VirtualSceneTexCoord;

layout(location = 0) out vec4 o_FragColor;

void main() {
    o_FragColor = texture(u_VirtualSceneColorTexture, v_VirtualSceneTexCoord);
}

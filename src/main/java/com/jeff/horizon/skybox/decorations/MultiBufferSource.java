package com.jeff.horizon.skybox.decorations;

import com.mojang.blaze3d.vertex.VertexConsumer;

public interface MultiBufferSource {

    VertexConsumer getBuffer(RenderType renderType);

}

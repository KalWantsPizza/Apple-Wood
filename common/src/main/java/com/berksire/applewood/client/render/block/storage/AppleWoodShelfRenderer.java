package com.berksire.applewood.client.render.block.storage;

import com.berksire.applewood.core.block.entity.AppleWoodStorageBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.satisfy.vinery.client.util.ClientUtil;

@Environment(EnvType.CLIENT)
public class AppleWoodShelfRenderer implements AppleWoodStorageTypeRenderer {
    @Override
    public void render(AppleWoodStorageBlockEntity entity, PoseStack matrices, MultiBufferSource vertexConsumers, NonNullList<ItemStack> itemStacks) {

        matrices.translate(-0.4, 0.5, 0.25);
        matrices.mulPose(Axis.YP.rotationDegrees(90));
        matrices.scale(0.5f, 0.5f, 0.5f);

        for (int i = 0; i < itemStacks.size(); i++) {
            ItemStack stack = itemStacks.get(i);
            if (!stack.isEmpty()) {
                matrices.pushPose();
                matrices.translate(0f, 0f, 0.2f * i);
                matrices.mulPose(Axis.YN.rotationDegrees(22.5f));
                ClientUtil.renderItem(stack, matrices, vertexConsumers, entity);
                matrices.popPose();
            }
        }
    }
}

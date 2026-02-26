package kitminty.redefinedlight.PlayerModelAccessories;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import kitminty.redefinedlight.RedefinedLight;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class EnergyCube extends Rendering.Modeler {

    public static final ModelLayerLocation CORE_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(RedefinedLight.modId, "energy_core"), "main");
    private static final ResourceLocation CORE_TEXTURE = ResourceLocation.parse("redefinedlight:energy_core.png");

    private static final Rendering.ModelPartData CUBE = new Rendering.ModelPartData("cube", CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-8, -8, -8, 16, 16, 16));

    public static LayerDefinition createLayerDefinition() {
        return createLayerDefinition(32, 32, CUBE);
    }

    public final RenderType RENDER_TYPE = renderType(CORE_TEXTURE);
    private final ModelPart cube;

    public EnergyCube(EntityModelSet entityModelSet) {
        super(Rendering.RenderHelper.CUBERESOURCERENDERTYPE);
        ModelPart root = entityModelSet.bakeLayer(CORE_LAYER);
        cube = CUBE.getFromRoot(root);
    }

    public void render(@NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int light, int overlayLight, int color) {
        renderToBuffer(matrix, renderer.getBuffer(RENDER_TYPE), light, overlayLight, color);
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack matrix, @NotNull VertexConsumer vertexBuilder, int light, int overlayLight, int color) {
        cube.render(matrix, vertexBuilder, light, overlayLight, color);
    }
}
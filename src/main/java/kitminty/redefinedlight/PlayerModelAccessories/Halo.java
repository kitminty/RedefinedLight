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

public class Halo extends Rendering.Modeler {

    public static final ModelLayerLocation HALO_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(RedefinedLight.modId, "halo"), "main");
    private static final ResourceLocation HALO_TEXTURE = ResourceLocation.parse("redefinedlight:ghalo.png");

    private static final Rendering.ModelPartData HALO = new Rendering.ModelPartData("halo", CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-128, 0, -128, 128, 0.027F, 128));

    public static LayerDefinition createLayerDefinition() {
        return createLayerDefinition(128, 128, HALO);
    }

    public final RenderType RENDER_TYPE = renderType(HALO_TEXTURE);
    private final ModelPart halo;

    public Halo(EntityModelSet entityModelSet) {
        super(Rendering.RenderHelper.HALORESOURCERENDERTYPE);
        ModelPart root = entityModelSet.bakeLayer(HALO_LAYER);
        halo = HALO.getFromRoot(root);
        halo.xScale = 0.19F;
        halo.zScale = 0.19F;
        halo.x = 12.16F;
        halo.z = 12.16F;
    }

    public void render(@NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int light, int overlayLight, int color) {
        renderToBuffer(matrix, renderer.getBuffer(RENDER_TYPE), light, overlayLight, color);
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack matrix, @NotNull VertexConsumer vertexBuilder, int light, int overlayLight, int color) {
        halo.render(matrix, vertexBuilder, light, overlayLight, color);
    }
}
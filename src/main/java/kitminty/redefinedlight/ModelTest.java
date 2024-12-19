package kitminty.redefinedlight;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.Util;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class ModelTest {
    public static class ModelEnergyCore extends MekanismJavaModel {

        public static final ModelLayerLocation CORE_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(RedefinedLight.modId, "energy_core"), "main");
        private static final ResourceLocation CORE_TEXTURE = ResourceLocation.parse("redefinedlight:energy_core.png");

        private static final ModelPartData CUBE = new ModelPartData("cube", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-8, -8, -8, 16, 16, 16));

        public static LayerDefinition createLayerDefinition() {
            return createLayerDefinition(CUBE);
        }

        public final RenderType RENDER_TYPE = renderType(CORE_TEXTURE);
        private final ModelPart cube;

        public ModelEnergyCore(EntityModelSet entityModelSet) {
            super(MekanismRenderType.STANDARD);
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
    public static class MekanismRenderType {
        public static final Function<ResourceLocation, RenderType> STANDARD = Util.memoize(resourceLocation -> RedefinedLight.RenderHelper.CUBE);
    }
    public record ModelPartData(String name, CubeListBuilder cubes, PartPose pose, List<ModelPartData> children) {

        public ModelPartData(String name, CubeListBuilder cubes, PartPose pose, ModelPartData... children) {
            this(name, cubes, pose, List.of(children));
        }

        public ModelPartData(String name, CubeListBuilder cubes, ModelPartData... children) {
            this(name, cubes, PartPose.ZERO, children);
        }

        public void addToDefinition(PartDefinition definition) {
            PartDefinition subDefinition = definition.addOrReplaceChild(name, cubes, pose);
            for (ModelPartData child : children) {
                child.addToDefinition(subDefinition);
            }
        }

        public ModelPart getFromRoot(ModelPart part) {
            return part.getChild(name);
        }
    }
    public abstract static class MekanismJavaModel extends Model {

        public MekanismJavaModel(Function<ResourceLocation, RenderType> renderType) {
            super(renderType);
        }

        protected static LayerDefinition createLayerDefinition(ModelPartData... parts) {
            MeshDefinition meshdefinition = new MeshDefinition();
            PartDefinition partdefinition = meshdefinition.getRoot();
            for (ModelPartData part : parts) {
                part.addToDefinition(partdefinition);
            }
            return LayerDefinition.create(meshdefinition, 32, 32);
        }
    }
}

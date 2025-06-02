package kitminty.redefinedlight;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import kitminty.redefinedlight.mixin.RenderTypeAccessor;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class Rendering {
    private static ShaderInstance halo;

    public interface TriConsumer<R> {
        void accept(R r);
    }
    public static void init(TriConsumer<Consumer<ShaderInstance>> registrations) {
        registrations.accept(inst -> halo = inst);
    }
    public static ShaderInstance halo() {
        return halo;
    }

    public static class Renderer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
        public Renderer(RenderLayerParent<T,M> renderer) {
            super(renderer);
        }
        @Override
        public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float age, float netHeadYaw, float headPitch) {
            if(!livingEntity.isInvisible() && RedefinedLight.ClientEvents.alternator && livingEntity.getName().getString().equals(Objects.requireNonNull(Minecraft.getInstance().player).getName().getString())) {
                poseStack.pushPose();
                poseStack.translate(Config.XPosition.get(), Config.YPosition.get(), Config.ZPosition.get()); //determines halo position
                //Boo - Isaac
                poseStack.mulPose(rotateX(Config.XRotation.get()));
                poseStack.mulPose(rotateZ(Config.ZRotation.get())); //makes halo tilted
                if (Config.EnableClock.get()) {
                    poseStack.mulPose(rotateY((float)((Math.floor((livingEntity.tickCount+partialTicks)*0.1/2)+(((livingEntity.tickCount+partialTicks)*0.1%2<=1)?0:0.5*Math.sin(Math.PI*((livingEntity.tickCount+partialTicks)*0.1-1)-0.5*Math.PI)+0.5))*27)+((livingEntity.tickCount+partialTicks)*0.027F))); //turns the halo like a clock
                } else {
                    poseStack.mulPose(rotateY(Config.YRotation.get()));
                }
                int Color;
                if (Config.EnableRain.get()) {
                    Color = FastColor.ARGB32.color((int) ((java.lang.Math.sin(((livingEntity.tickCount+partialTicks)* Config.RainSpeed.get())/127.5)*127.5)+127.5),(int) ((java.lang.Math.sin((((livingEntity.tickCount+partialTicks)* Config.RainSpeed.get())/127.5)-2)*127.5)+127.5), (int) ((Math.sin((((livingEntity.tickCount+partialTicks)* Config.RainSpeed.get())/127.5)-4)*127.5)+127.5));
                } else {
                    Color = FastColor.ARGB32.color(62,255,255);
                }
                new Halo(Minecraft.getInstance().getEntityModels()).render(poseStack, buffer, LightTexture.FULL_BRIGHT, 1, Color);
                poseStack.popPose();
                //Minecraft.getInstance().player.sendSystemMessage(Component.literal(String.valueOf("works")));
            }
            /* Test Cube Model
            poseStack.pushPose();
            poseStack.scale(1F, -1F, -1F);
            poseStack.translate(0,2.7,0);
            int test = FastColor.ARGB32.color((int) ((java.lang.Math.sin(((livingEntity.tickCount+partialTicks)* RedefinedLight.ClientConfig.RSPEED.get())/127.5)*127.5)+127.5),(int) ((java.lang.Math.sin((((livingEntity.tickCount+partialTicks)* RedefinedLight.ClientConfig.RSPEED.get())/127.5)-2)*127.5)+127.5), (int) ((Math.sin((((livingEntity.tickCount+partialTicks)* RedefinedLight.ClientConfig.RSPEED.get())/127.5)-4)*127.5)+127.5));
            new EnergyCube(Minecraft.getInstance().getEntityModels()).render(poseStack, buffer, LightTexture.FULL_BRIGHT, 1, test);
            poseStack.popPose();
             */
        }
    }

    public static final class RenderHelper extends RenderType {
        public static final RenderType HALO;
        public static final RenderType CUBE;
        public static final Function<ResourceLocation, RenderType> CUBERESOURCERENDERTYPE = Util.memoize(resourceLocation -> RenderHelper.CUBE);
        public static final Function<ResourceLocation, RenderType> HALORESOURCERENDERTYPE = Util.memoize(resourceLocation -> RenderHelper.HALO);
        static {
            TextureStateShard haloTexture = new TextureStateShard(ResourceLocation.parse("redefinedlight:ghalo.png"), false, true);
            CompositeState glState = CompositeState.builder()
                    .setTextureState(haloTexture)
                    .setShaderState(new ShaderStateShard(Rendering::halo))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .createCompositeState(true);
            HALO = RenderTypeAccessor.create("RedefinedLight:halo", DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 64, false, false, glState);

            TextureStateShard cubeTexture = new TextureStateShard(ResourceLocation.parse("redefinedlight:energy_core.png"), false, true);
            glState = CompositeState.builder()
                    .setTextureState(cubeTexture)
                    .setShaderState(RENDERTYPE_EYES_SHADER)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .createCompositeState(true);
            CUBE = RenderTypeAccessor.create("RedefinedLight:energy_core", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false, glState);
        }
        private RenderHelper(String string, VertexFormat vertexFormat, VertexFormat.Mode mode, int i, boolean bl, boolean bl2, Runnable runnable, Runnable runnable2) {
            super(string, vertexFormat, mode, i, bl, bl2, runnable, runnable2);
            throw new UnsupportedOperationException("Should not be instantiated");
        }
    }

    public abstract static class Modeler extends Model {
        public Modeler(Function<ResourceLocation, RenderType> renderType) {
            super(renderType);
        }
        protected static LayerDefinition createLayerDefinition(int texWidth, int texHeight, ModelPartData... parts) {
            MeshDefinition meshdefinition = new MeshDefinition();
            PartDefinition partdefinition = meshdefinition.getRoot();
            for (ModelPartData part : parts) {
                part.addToDefinition(partdefinition);
            }
            return LayerDefinition.create(meshdefinition, texWidth, texHeight);
        }
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

    public static float toRadians(float degrees) {
        return (float) (degrees / 180F * Math.PI);
    }
    public static Quaternionf rotateX(float degrees) {
        return new Quaternionf().rotateX(toRadians(degrees));
    }
    public static Quaternionf rotateY(float degrees) {
        return new Quaternionf().rotateY(toRadians(degrees));
    }
    public static Quaternionf rotateZ(float degrees) {
        return new Quaternionf().rotateZ(toRadians(degrees));
    }
}

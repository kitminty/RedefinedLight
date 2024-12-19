package kitminty.redefinedlight;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import kitminty.redefinedlight.mixin.RenderTypeAccessor;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
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
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.apache.commons.lang3.exception.UncheckedException;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

@Mod(RedefinedLight.modId)
public class RedefinedLight {
    public static final String modId = "redefinedlight"; //Mod ID, you know what the fuck this is
    public static final String RLCAT = "key.category.redefinedlight.rlcat";
    public static final String RLKEY = "key.redefinedlight.activatelight";
    public static final KeyMapping RLKEYM = new KeyMapping(RLKEY, KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, RLCAT);
    private static ShaderInstance halo;

    //------------------------------------------------ Config

    public RedefinedLight(ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, ClientConfig.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    public static class ClientConfig {
        public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
        public static final ModConfigSpec SPEC;
        public static final ModConfigSpec.ConfigValue<Boolean> EnableClock;
        public static final ModConfigSpec.ConfigValue<Integer> XRotation;
        public static final ModConfigSpec.ConfigValue<Integer> YRotation;
        public static final ModConfigSpec.ConfigValue<Integer> ZRotation;
        public static final ModConfigSpec.ConfigValue<Double> XPosition;
        public static final ModConfigSpec.ConfigValue<Double> YPosition;
        public static final ModConfigSpec.ConfigValue<Double> ZPosition;
        public static final ModConfigSpec.ConfigValue<Double> RSPEED;

        static {
            BUILDER.push("Configs");
            EnableClock = BUILDER.comment("Enable Clock Rotation On Halo").define("enable_halo_clock_rotation", true);
            XRotation = BUILDER.comment("X Rotation On Halo").define("halo_x_rotation", 0);
            YRotation = BUILDER.comment("Y Rotation On Halo").define("halo_y_rotation", 0);
            ZRotation = BUILDER.comment("Z Rotation On Halo").define("halo_z_rotation", 30);
            XPosition = BUILDER.comment("X Position On Halo").define("halo_x_position", 0.2);
            YPosition = BUILDER.comment("Y Position On Halo").define("halo_y_position", -0.65);
            ZPosition = BUILDER.comment("Z Position On Halo").define("halo_z_position", 0.0);
            RSPEED = BUILDER.comment("Speed Of Rainbow").define("r_speed", 0.0);
            BUILDER.pop();
            SPEC = BUILDER.build();
        }
    }

    //------------------------------------------------ Make Halo

    public static class Halo extends Modeler {

        public static final ModelLayerLocation HALO_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(RedefinedLight.modId, "halo"), "main");
        private static final ResourceLocation HALO_TEXTURE = ResourceLocation.parse("redefinedlight:halo.png");

        private static final ModelPartData HALO = new ModelPartData("halo", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-128, 0, -128, 128, 0.027F, 128));

        public static LayerDefinition createLayerDefinition() {
            return createLayerDefinition(128, 128, HALO);
        }

        public final RenderType RENDER_TYPE = renderType(HALO_TEXTURE);
        private final ModelPart halo;

        public Halo(EntityModelSet entityModelSet) {
            super(RenderHelper.HALORESOURCERENDERTYPE);
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

    //------------------------------------------------ Make EnergyCube

    public static class EnergyCube extends Modeler {

        public static final ModelLayerLocation CORE_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(RedefinedLight.modId, "energy_core"), "main");
        private static final ResourceLocation CORE_TEXTURE = ResourceLocation.parse("redefinedlight:energy_core.png");

        private static final ModelPartData CUBE = new ModelPartData("cube", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-8, -8, -8, 16, 16, 16));

        public static LayerDefinition createLayerDefinition() {
            return createLayerDefinition(32, 32, CUBE);
        }

        public final RenderType RENDER_TYPE = renderType(CORE_TEXTURE);
        private final ModelPart cube;

        public EnergyCube(EntityModelSet entityModelSet) {
            super(RenderHelper.CUBERESOURCERENDERTYPE);
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

    //------------------------------------------------ Make Model's

    public static final class RenderHelper extends RenderType {
        public static final RenderType HALO;
        public static final RenderType CUBE;
        public static final Function<ResourceLocation, RenderType> CUBERESOURCERENDERTYPE = Util.memoize(resourceLocation -> RedefinedLight.RenderHelper.CUBE);
        public static final Function<ResourceLocation, RenderType> HALORESOURCERENDERTYPE = Util.memoize(resourceLocation -> RedefinedLight.RenderHelper.HALO);
        static {
            TextureStateShard haloTexture = new TextureStateShard(ResourceLocation.parse("redefinedlight:halo.png"), false, true);
            CompositeState glState = CompositeState.builder()
                    .setTextureState(haloTexture)
                    .setShaderState(new ShaderStateShard(RedefinedLight::halo))
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

    //------------------------------------------------ Renderer

    public static class Renderer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
        public Renderer(RenderLayerParent<T,M> renderer) {
            super(renderer);
        }
        @Override
        public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float age, float netHeadYaw, float headPitch) {
            if(!livingEntity.isInvisible() && ClientForgeEvents.alternator && livingEntity.getName().getString().equals(Objects.requireNonNull(Minecraft.getInstance().player).getName().getString())) {
                poseStack.pushPose();
                poseStack.translate(ClientConfig.XPosition.get(), ClientConfig.YPosition.get(), ClientConfig.ZPosition.get()); //determines halo position
                //Boo - Isaac
                poseStack.mulPose(RedefinedLight.rotateX(ClientConfig.XRotation.get()));
                poseStack.mulPose(RedefinedLight.rotateZ(ClientConfig.ZRotation.get())); //makes halo tilted
                if (ClientConfig.EnableClock.get()) {
                    poseStack.mulPose(RedefinedLight.rotateY((float)((Math.floor((livingEntity.tickCount+partialTicks)*0.1/2)+(((livingEntity.tickCount+partialTicks)*0.1%2<=1)?0:0.5*Math.sin(Math.PI*((livingEntity.tickCount+partialTicks)*0.1-1)-0.5*Math.PI)+0.5))*27)+((livingEntity.tickCount+partialTicks)*0.027F))); //turns the halo like a clock
                } else {
                    poseStack.mulPose(RedefinedLight.rotateY(ClientConfig.YRotation.get()));
                }
                int test2 = FastColor.ARGB32.color(102,203,228);
                new Halo(Minecraft.getInstance().getEntityModels()).render(poseStack, buffer, LightTexture.FULL_BRIGHT, 1, test2);
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

    //------------------------------------------------ Make Halo Shaderinstance

    public interface TriConsumer<R> {
        void accept(R r);
    }
    public static void init(TriConsumer<Consumer<ShaderInstance>> registrations) {
        registrations.accept(inst -> halo = inst);
    }
    public static ShaderInstance halo() {
        return halo;
    }

    //------------------------------------------------ Subscribers

    @EventBusSubscriber(modid = RedefinedLight.modId, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static class EventHandlerMod {
        @SubscribeEvent
        public static void registerShaders(RegisterShadersEvent evt) {
            init((onLoaded) -> {
                try {
                    evt.registerShader(new ShaderInstance(evt.getResourceProvider(), ResourceLocation.parse("redefinedlight:halo"), DefaultVertexFormat.POSITION_TEX_COLOR), onLoaded);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
        @SubscribeEvent
        public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
            try {
                event.registerLayerDefinition(EnergyCube.CORE_LAYER, EnergyCube::createLayerDefinition);
                event.registerLayerDefinition(Halo.HALO_LAYER, Halo::createLayerDefinition);
            } catch (Exception e) {
                throw new UncheckedException(e);
            }
        }
        @SubscribeEvent
        public static void addEntityLayers(EntityRenderersEvent.AddLayers event) {
            if(event.getSkin(PlayerSkin.Model.WIDE) instanceof PlayerRenderer playerRenderer) {
                playerRenderer.addLayer(new Renderer<>(playerRenderer));
            }
            if(event.getSkin(PlayerSkin.Model.SLIM) instanceof PlayerRenderer playerRenderer) {
                playerRenderer.addLayer(new Renderer<>(playerRenderer));
            }
        }
        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(RLKEYM);
        }
    }
    @EventBusSubscriber(modid = RedefinedLight.modId, value = Dist.CLIENT)
    public static class ClientForgeEvents {
        public static boolean alternator;
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if(RLKEYM.consumeClick()) {
                alternator = !alternator;
                assert Minecraft.getInstance().player != null;
                Minecraft.getInstance().player.sendSystemMessage(Component.literal(alternator ? "On" : "Off"));
            }
        }
    }

    //------------------------------------------------ Customizer

    /* may be useful later
    public static Vec3 fromEntityCenter(Entity e) {
        return new Vec3(e.getX(), e.getY() + e.getBbHeight() / 2, e.getZ());
    }
     //Rotates {@code v} by {@code theta} radians around {@code axis}
    public static Vec3 rotate(Vec3 v, double theta, Vec3 axis) {
        if (Mth.equal(theta, 0)) {
            return v;
        }
        // Rodrigues rotation formula
        Vec3 k = axis.normalize();
        float cosTheta = Mth.cos((float) theta);
        Vec3 firstTerm = v.scale(cosTheta);
        Vec3 secondTerm = k.cross(v).scale(Mth.sin((float) theta));
        Vec3 thirdTerm = k.scale(k.dot(v) * (1 - cosTheta));
        return new Vec3(firstTerm.x + secondTerm.x + thirdTerm.x,
                firstTerm.y + secondTerm.y + thirdTerm.y,
                firstTerm.z + secondTerm.z + thirdTerm.z);
    }
     */
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
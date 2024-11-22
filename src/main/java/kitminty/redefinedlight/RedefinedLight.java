package kitminty.redefinedlight;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import kitminty.redefinedlight.mixin.RenderTypeAccessor;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.function.Consumer;

@Mod(RedefinedLight.modId)
public class RedefinedLight {
    public static final String modId = "redefinedlight"; //Mod ID, you know what the fuck this is
    public static final String RLCAT = "key.category.redefinedlight.rlcat";
    public static final String RLKEY = "key.redefinedlight.activatelight";
    public static final KeyMapping RLKEYM = new KeyMapping(RLKEY, KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, RLCAT);
    private static ShaderInstance halo;

    public RedefinedLight() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, "redefinedlight-client-config.toml");
    }
    //------------------------------------------------ Make Halo Model

    public static final class RenderHelper extends RenderType {
        public static final RenderType HALO;
        private static RenderType makeLayer(CompositeState glState) {
            return RenderTypeAccessor.create("RedefinedLight:halo", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 64, false, false, glState);
        }
        static {
            TextureStateShard haloTexture = new TextureStateShard(new ResourceLocation("redefinedlight:halo.png"), false, true);
            CompositeState glState = CompositeState.builder().setTextureState(haloTexture)
                    .setShaderState(new ShaderStateShard(RedefinedLight::halo))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .createCompositeState(true);
            HALO = makeLayer(glState);
        }
        private RenderHelper(String string, VertexFormat vertexFormat, VertexFormat.Mode mode, int i, boolean bl, boolean bl2, Runnable runnable, Runnable runnable2) {
            super(string, vertexFormat, mode, i, bl, bl2, runnable, runnable2);
            throw new UnsupportedOperationException("Should not be instantiated");
        }
    }

    //------------------------------------------------ Halo Renderer

    public static class Renderer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
        public Renderer(RenderLayerParent<T,M> renderer) {
            super(renderer);
        }
        @Override
        public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float age, float netHeadYaw, float headPitch) {
            if(!livingEntity.isInvisible() && ClientForgeEvents.alternator && livingEntity.getName().getString().equals(Objects.requireNonNull(Minecraft.getInstance().player).getName().getString())) {
                poseStack.translate(0.2, -0.65, 0); //determines halo position
                //Boo - Isaac
                poseStack.mulPose(RedefinedLight.rotateZ(ClientConfig.ZROT.get())); //makes halo tilted
                poseStack.mulPose(RedefinedLight.rotateY((float)((Math.floor((livingEntity.tickCount+partialTicks)*0.1/2)+(((livingEntity.tickCount+partialTicks)*0.1%2<=1)?0:0.5*Math.sin(Math.PI*((livingEntity.tickCount+partialTicks)*0.1-1)-0.5*Math.PI)+0.5))*27)+((livingEntity.tickCount+partialTicks)*0.027F))); //turns the halo like a clock
                poseStack.scale(0.75F, -0.75F, -0.75F); //sets halo size
                buffer.getBuffer(RenderHelper.HALO).addVertex(poseStack.last().pose(),-1F,0,-1F).setColor(1.0F,1.0F,1.0F,1.0F).setUv(0,0);
                buffer.getBuffer(RenderHelper.HALO).addVertex(poseStack.last().pose(),1F,0,-1F).setColor(1.0F,1.0F,1.0F,1.0F).setUv(1,0);
                buffer.getBuffer(RenderHelper.HALO).addVertex(poseStack.last().pose(),1F,0,1F).setColor(1.0F,1.0F,1.0F,1.0F).setUv(1,1);
                buffer.getBuffer(RenderHelper.HALO).addVertex(poseStack.last().pose(),-1F,0,1F).setColor(1.0F,1.0F,1.0F,1.0F).setUv(0,1);
                //Minecraft.getInstance().player.sendSystemMessage(Component.literal(String.valueOf(ClientConfig.ZROT.get())));
            }
        }
    }

    //------------------------------------------------ Config

    public static class ClientConfig {
        public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
        public static final ForgeConfigSpec SPEC;

        public static final ForgeConfigSpec.ConfigValue<Integer> ZROT;


        static {
            BUILDER.push("Configs");

            ZROT = BUILDER.comment("Rotation of Z Axis").define("ZRotation", 30);

            BUILDER.pop();
            SPEC = BUILDER.build();
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

    @Mod.EventBusSubscriber(modid = RedefinedLight.modId, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class EventHandlerMod {
        @SubscribeEvent
        public static void registerShaders(RegisterShadersEvent evt) {
            init((onLoaded) -> {
                try {
                    evt.registerShader(new ShaderInstance(evt.getResourceProvider(), ResourceLocation.parse("redefinedlight:halo.png"), DefaultVertexFormat.POSITION_COLOR_TEX), onLoaded);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
        @SubscribeEvent
        public static void addEntityLayers(EntityRenderersEvent.AddLayers event) {
            if(event.getSkin("default") instanceof PlayerRenderer playerRenderer) {
                playerRenderer.addLayer(new Renderer<>(playerRenderer));
            }
            if(event.getSkin("slim") instanceof PlayerRenderer playerRenderer) {
                playerRenderer.addLayer(new Renderer<>(playerRenderer));
            }
        }
        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(RLKEYM);
        }
    }
    @Mod.EventBusSubscriber(modid = RedefinedLight.modId, value = Dist.CLIENT)
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

    //------------------------------------------------ Halo Customizer

    public static Vec3 fromEntityCenter(Entity e) {
        return new Vec3(e.getX(), e.getY() + e.getBbHeight() / 2, e.getZ());
    }
    /**
     * Rotates {@code v} by {@code theta} radians around {@code axis}
     */
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
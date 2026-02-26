package kitminty.redefinedlight;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import kitminty.redefinedlight.Commands.CommandTest;
import kitminty.redefinedlight.PlayerModelAccessories.EnergyCube;
import kitminty.redefinedlight.PlayerModelAccessories.Halo;
import kitminty.redefinedlight.PlayerModelAccessories.Rendering;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.apache.commons.lang3.exception.UncheckedException;
import org.lwjgl.glfw.GLFW;
import java.io.IOException;
import java.io.UncheckedIOException;

//Cats are cool

@Mod(RedefinedLight.modId)
public class RedefinedLight {
    public static final String modId = "redefinedlight";
    public static final String RLCAT = "key.category.redefinedlight.rlcat";
    public static final String RLKEY = "key.redefinedlight.activatelight";
    public static final KeyMapping RLKEYM = new KeyMapping(RLKEY, KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, RLCAT);

    public RedefinedLight(ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    //------------------------------------------------ Subscribers

    @EventBusSubscriber(modid = RedefinedLight.modId, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static class ModEventHandler {
        @SubscribeEvent
        public static void registerShaders(RegisterShadersEvent evt) {
            Rendering.init((onLoaded) -> {
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
                playerRenderer.addLayer(new Rendering.Renderer<>(playerRenderer));
            }
            if(event.getSkin(PlayerSkin.Model.SLIM) instanceof PlayerRenderer playerRenderer) {
                playerRenderer.addLayer(new Rendering.Renderer<>(playerRenderer));
            }
        }

        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(RLKEYM);
        }
    }

    @EventBusSubscriber(modid = RedefinedLight.modId, value = Dist.CLIENT)
    public static class ClientEvents {
        public static boolean alternator;
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if(RLKEYM.consumeClick()) {
                alternator = !alternator;
                assert Minecraft.getInstance().player != null;
                Minecraft.getInstance().player.sendSystemMessage(Component.literal(alternator ? "On" : "Off"));
            }
        }

        @SubscribeEvent
        public static void RegisterCommands(RegisterCommandsEvent event) {
            CommandTest.register(event.getDispatcher());
        }
    }
}
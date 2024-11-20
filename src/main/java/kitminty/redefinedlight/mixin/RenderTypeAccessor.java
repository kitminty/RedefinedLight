/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package kitminty.redefinedlight.mixin;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderType.class)
public interface RenderTypeAccessor {
	@Invoker("create")
	static RenderType.CompositeRenderType create(String string, VertexFormat vertexFormat, VertexFormat.Mode mode, int bufSize, boolean hasCrumbling, boolean sortOnUpload, RenderType.CompositeState compositeState) {
		throw new IllegalStateException();
	}
}

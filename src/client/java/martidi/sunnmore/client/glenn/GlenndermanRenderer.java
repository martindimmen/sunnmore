package martidi.sunnmore.client.glenn;

import net.minecraft.client.renderer.entity.EndermanRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.EnderEyesLayer;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.resources.Identifier;

import martidi.sunnmore.Sunnmore;

/** The vanilla Enderman renderer with Glenn's skin (ginger hair, freckles, green hoodie). */
public class GlenndermanRenderer extends EndermanRenderer {
	private static final Identifier TEXTURE = Sunnmore.id("textures/entity/glennderman.png");

	public GlenndermanRenderer(EntityRendererProvider.Context context) {
		super(context);
		// Glenn is a human-looking boy: no glowing purple Enderman eyes.
		this.layers.removeIf(layer -> layer instanceof EnderEyesLayer);
	}

	@Override
	public Identifier getTextureLocation(EndermanRenderState state) {
		return TEXTURE;
	}
}

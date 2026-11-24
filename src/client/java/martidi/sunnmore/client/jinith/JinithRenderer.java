package martidi.sunnmore.client.jinith;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.Identifier;

import martidi.sunnmore.Sunnmore;
import martidi.sunnmore.jinith.JinithEntity;

/** Draws Jinith with his own muscle model; swaps to the red skin while he is angry. */
public class JinithRenderer extends HumanoidMobRenderer<JinithEntity, JinithRenderState, JinithModel> {
	private static final Identifier TEXTURE = Sunnmore.id("textures/entity/jinith/jinith.png");
	private static final Identifier ANGRY_TEXTURE = Sunnmore.id("textures/entity/jinith/jinith_angry.png");

	public JinithRenderer(EntityRendererProvider.Context context) {
		super(context, new JinithModel(context.bakeLayer(JinithModel.LAYER)), 0.6F);
	}

	@Override
	public JinithRenderState createRenderState() {
		return new JinithRenderState();
	}

	@Override
	public void extractRenderState(JinithEntity entity, JinithRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.angry = entity.isAngry();
		state.flexing = entity.isFlexing();
	}

	@Override
	public Identifier getTextureLocation(JinithRenderState state) {
		return state.angry ? ANGRY_TEXTURE : TEXTURE;
	}
}

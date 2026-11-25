package martidi.sunnmore.client.stian;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.Identifier;

import martidi.sunnmore.Sunnmore;
import martidi.sunnmore.stian.StianEntity;

/** Draws Stian with his own model: can in hand, cigarette in mouth, sitting or smoking poses. */
public class StianRenderer extends HumanoidMobRenderer<StianEntity, StianRenderState, StianModel> {
	private static final Identifier TEXTURE = Sunnmore.id("textures/entity/stian/stian.png");

	public StianRenderer(EntityRendererProvider.Context context) {
		super(context, new StianModel(context.bakeLayer(StianModel.LAYER)), 0.5F);
	}

	@Override
	public StianRenderState createRenderState() {
		return new StianRenderState();
	}

	@Override
	public void extractRenderState(StianEntity entity, StianRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.sitting = entity.isSitting();
		state.smoking = entity.isSmoking();
	}

	@Override
	public Identifier getTextureLocation(StianRenderState state) {
		return TEXTURE;
	}
}

package martidi.sunnmore.client.bard;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.Identifier;

import martidi.sunnmore.Sunnmore;
import martidi.sunnmore.bard.BardEntity;

/** Draws Bård with his own model: big butt, phone out while he checks the weather. */
public class BardRenderer extends HumanoidMobRenderer<BardEntity, BardRenderState, BardModel> {
	private static final Identifier TEXTURE = Sunnmore.id("textures/entity/bard/bard.png");

	public BardRenderer(EntityRendererProvider.Context context) {
		super(context, new BardModel(context.bakeLayer(BardModel.LAYER)), 0.5F);
	}

	@Override
	public BardRenderState createRenderState() {
		return new BardRenderState();
	}

	@Override
	public void extractRenderState(BardEntity entity, BardRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.onPhone = entity.isOnPhone();
	}

	@Override
	public Identifier getTextureLocation(BardRenderState state) {
		return TEXTURE;
	}
}

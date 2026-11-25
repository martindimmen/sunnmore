package martidi.sunnmore.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;

import martidi.sunnmore.bard.BardEntities;
import martidi.sunnmore.client.bard.BardModel;
import martidi.sunnmore.client.bard.BardRenderer;
import martidi.sunnmore.client.glenn.GlenndermanRenderer;
import martidi.sunnmore.client.jinith.JinithModel;
import martidi.sunnmore.client.jinith.JinithRenderer;
import martidi.sunnmore.glenn.GlennEntities;
import martidi.sunnmore.jinith.JinithEntities;

public class SunnmoreClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(GlennEntities.GLENNDERMAN, GlenndermanRenderer::new);
		ModelLayerRegistry.registerModelLayer(JinithModel.LAYER, JinithModel::createBodyLayer);
		EntityRendererRegistry.register(JinithEntities.JINITH, JinithRenderer::new);
		ModelLayerRegistry.registerModelLayer(BardModel.LAYER, BardModel::createBodyLayer);
		EntityRendererRegistry.register(BardEntities.BARD, BardRenderer::new);
	}
}

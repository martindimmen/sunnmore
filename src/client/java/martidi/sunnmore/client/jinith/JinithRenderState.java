package martidi.sunnmore.client.jinith;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

public class JinithRenderState extends HumanoidRenderState {
	/** True while he is angry: the renderer uses the red skin. */
	public boolean angry;
	/** True while he flexes over someone he just beat: the model raises both arms. */
	public boolean flexing;
}

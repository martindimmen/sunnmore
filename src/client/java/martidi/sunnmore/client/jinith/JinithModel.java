package martidi.sunnmore.client.jinith;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

import martidi.sunnmore.Sunnmore;

/**
 * Jinith's muscle caricature: a small head, huge arms, a wide chest and short legs. The parts use
 * the vanilla humanoid names, so HumanoidModel animates walking and the punch swing for us.
 * Units are pixels (16 = one block); y = 24 is the ground. Texture is 128x64, see the UV notes.
 */
public class JinithModel extends HumanoidModel<JinithRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(Sunnmore.id("jinith"), "main");

	/** Arms raised up and out to the sides (135 degrees from hanging down). */
	private static final float FLEX_ARM_ANGLE = (float) Math.toRadians(135.0);

	public JinithModel(ModelPart root) {
		super(root);
	}

	/** Vanilla places swinging arms 5 px from the centre; his shoulders are at 10.5 px. */
	private static final float SHOULDER_SCALE = 10.5F / 5.0F;

	@Override
	protected void setupAttackAnimation(JinithRenderState state) {
		super.setupAttackAnimation(state);
		if (state.swingAnimation > 0.0F && state.currentSwing != null) {
			// Push the arms back out to his wide shoulders, or the punch comes out of his chest.
			this.rightArm.x *= SHOULDER_SCALE;
			this.rightArm.z *= SHOULDER_SCALE;
			this.leftArm.x *= SHOULDER_SCALE;
			this.leftArm.z *= SHOULDER_SCALE;
		}
	}

	@Override
	public void setupAnim(JinithRenderState state) {
		super.setupAnim(state);
		if (state.flexing) {
			// Both arms up in a flex, pumping a little; chin up.
			float pump = Mth.sin(state.ageInTicks * 0.5F) * 0.12F;
			this.rightArm.xRot = 0.0F;
			this.rightArm.yRot = 0.0F;
			this.rightArm.zRot = FLEX_ARM_ANGLE + pump;
			this.leftArm.xRot = 0.0F;
			this.leftArm.yRot = 0.0F;
			this.leftArm.zRot = -FLEX_ARM_ANGLE - pump;
			this.head.xRot = -0.2F;
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		// Head 7x7x7 on top of the chest. UV (0,0).
		PartDefinition head = root.addOrReplaceChild("head",
			CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -7.0F, -3.5F, 7.0F, 7.0F, 7.0F),
			PartPose.offset(0.0F, 5.0F, 0.0F));
		// Hair layer, slightly larger than the head. UV (28,0).
		head.addOrReplaceChild("hat",
			CubeListBuilder.create().texOffs(28, 0).addBox(-3.5F, -7.0F, -3.5F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.25F)),
			PartPose.ZERO);
		// Chest 14 wide, 12 tall, 8 deep. UV (0,14).
		root.addOrReplaceChild("body",
			CubeListBuilder.create().texOffs(0, 14).addBox(-7.0F, 0.0F, -4.0F, 14.0F, 12.0F, 8.0F),
			PartPose.offset(0.0F, 5.0F, 0.0F));
		// Huge arms, 7x14x7, hanging from the shoulders. UV (44,14), left one mirrored.
		root.addOrReplaceChild("right_arm",
			CubeListBuilder.create().texOffs(44, 14).addBox(-3.5F, -2.0F, -3.5F, 7.0F, 14.0F, 7.0F),
			PartPose.offset(-10.5F, 7.0F, 0.0F));
		root.addOrReplaceChild("left_arm",
			CubeListBuilder.create().texOffs(44, 14).mirror().addBox(-3.5F, -2.0F, -3.5F, 7.0F, 14.0F, 7.0F),
			PartPose.offset(10.5F, 7.0F, 0.0F));
		// Short stubby legs, 5x7x5. UV (72,14), left one mirrored.
		root.addOrReplaceChild("right_leg",
			CubeListBuilder.create().texOffs(72, 14).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 7.0F, 5.0F),
			PartPose.offset(-3.0F, 17.0F, 0.0F));
		root.addOrReplaceChild("left_leg",
			CubeListBuilder.create().texOffs(72, 14).mirror().addBox(-2.5F, 0.0F, -2.5F, 5.0F, 7.0F, 5.0F),
			PartPose.offset(3.0F, 17.0F, 0.0F));
		return LayerDefinition.create(mesh, 128, 64);
	}
}

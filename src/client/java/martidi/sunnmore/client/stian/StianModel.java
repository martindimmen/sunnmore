package martidi.sunnmore.client.stian;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

import martidi.sunnmore.Sunnmore;

/**
 * Stian: player proportions, an oversized Bjørneunge always in his right hand, and a cigarette in
 * the corner of his mouth. Sits on his backside after a trip; raises his left hand to his mouth on
 * a røykepause. Units are pixels (16 = one block); y = 24 is the ground. Texture is 128x64:
 * head (0,0), hair (32,0), cigarette (64,0), body (0,16), can (52,16), arms (0,32), legs (16,32).
 */
public class StianModel extends HumanoidModel<StianRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(Sunnmore.id("stian"), "main");

	/** How far the whole body drops when he sits on the ground. */
	private static final float SIT_DROP = 10.0F;
	private static final float SIT_LEG_PITCH = -1.41F;
	private static final float SMOKE_ARM_PITCH = -2.1F;
	private static final float SMOKE_ARM_YAW = 0.55F;

	public StianModel(ModelPart root) {
		super(root);
	}

	@Override
	public void setupAnim(StianRenderState state) {
		super.setupAnim(state);
		if (state.smoking) {
			this.leftArm.xRot = SMOKE_ARM_PITCH;
			this.leftArm.yRot = SMOKE_ARM_YAW;
			this.leftArm.zRot = 0.0F;
		}
		if (state.sitting) {
			this.head.y += SIT_DROP;
			this.body.y += SIT_DROP;
			this.rightArm.y += SIT_DROP;
			this.leftArm.y += SIT_DROP;
			this.rightLeg.y += SIT_DROP;
			this.leftLeg.y += SIT_DROP;
			this.rightLeg.xRot = SIT_LEG_PITCH;
			this.rightLeg.yRot = 0.3F;
			this.leftLeg.xRot = SIT_LEG_PITCH;
			this.leftLeg.yRot = -0.3F;
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition head = root.addOrReplaceChild("head",
			CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
			PartPose.ZERO);
		head.addOrReplaceChild("hat",
			CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)),
			PartPose.ZERO);
		// The cigarette sticks out of the right corner of his mouth; its front end glows.
		head.addOrReplaceChild("cigarette",
			CubeListBuilder.create().texOffs(64, 0).addBox(-2.5F, -2.5F, -7.0F, 1.0F, 1.0F, 3.0F),
			PartPose.ZERO);
		root.addOrReplaceChild("body",
			CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F),
			PartPose.ZERO);
		PartDefinition rightArm = root.addOrReplaceChild("right_arm",
			CubeListBuilder.create().texOffs(0, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
			PartPose.offset(-5.0F, 2.0F, 0.0F));
		// The oversized can, held in front of the hand.
		rightArm.addOrReplaceChild("can",
			CubeListBuilder.create().texOffs(52, 16).addBox(-2.5F, 7.5F, -4.0F, 3.0F, 4.0F, 3.0F),
			PartPose.ZERO);
		root.addOrReplaceChild("left_arm",
			CubeListBuilder.create().texOffs(0, 32).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
			PartPose.offset(5.0F, 2.0F, 0.0F));
		root.addOrReplaceChild("right_leg",
			CubeListBuilder.create().texOffs(16, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
			PartPose.offset(-1.9F, 12.0F, 0.0F));
		root.addOrReplaceChild("left_leg",
			CubeListBuilder.create().texOffs(16, 32).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
			PartPose.offset(1.9F, 12.0F, 0.0F));
		return LayerDefinition.create(mesh, 128, 64);
	}
}

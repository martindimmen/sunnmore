package martidi.sunnmore.client.bard;

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
 * Bård: player proportions plus the caricature, a properly big butt (a child of the body, so it
 * moves with him), and a phone in his right hand that only shows while he checks the weather.
 * Units are pixels (16 = one block); y = 24 is the ground. Texture is 128x64:
 * head (0,0), hair (32,0), body (0,16), butt (24,16) and (24,26), phone (52,16), arms (0,32), legs (16,32).
 */
public class BardModel extends HumanoidModel<BardRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(Sunnmore.id("bard"), "main");

	/** Right arm raised in front of him, holding the phone up. */
	private static final float PHONE_ARM_PITCH = -1.25F;
	private static final float PHONE_ARM_YAW = -0.35F;
	/** Head bent down to read the screen. */
	private static final float PHONE_HEAD_PITCH = 0.45F;

	private final ModelPart phone;

	public BardModel(ModelPart root) {
		super(root);
		this.phone = this.rightArm.getChild("phone");
	}

	@Override
	public void setupAnim(BardRenderState state) {
		super.setupAnim(state);
		this.phone.visible = state.onPhone;
		if (state.onPhone) {
			this.rightArm.xRot = PHONE_ARM_PITCH;
			this.rightArm.yRot = PHONE_ARM_YAW;
			this.rightArm.zRot = 0.0F;
			this.head.xRot = PHONE_HEAD_PITCH;
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition head = root.addOrReplaceChild("head",
			CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
			PartPose.ZERO);
		// Blonde hair, a layer just outside the head.
		head.addOrReplaceChild("hat",
			CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)),
			PartPose.ZERO);
		PartDefinition body = root.addOrReplaceChild("body",
			CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F),
			PartPose.ZERO);
		// The butt: a wide block sticking out behind the hips, with a smaller one behind it to round it off.
		body.addOrReplaceChild("butt",
			CubeListBuilder.create()
				.texOffs(24, 16).addBox(-4.5F, 7.5F, 1.0F, 9.0F, 5.0F, 4.0F)
				.texOffs(24, 26).addBox(-3.5F, 8.5F, 5.0F, 7.0F, 3.0F, 1.0F),
			PartPose.ZERO);
		PartDefinition rightArm = root.addOrReplaceChild("right_arm",
			CubeListBuilder.create().texOffs(0, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
			PartPose.offset(-5.0F, 2.0F, 0.0F));
		// The phone sits in the palm, screen facing him when the arm is raised.
		rightArm.addOrReplaceChild("phone",
			CubeListBuilder.create().texOffs(52, 16).addBox(-2.0F, 8.0F, -3.0F, 2.0F, 3.0F, 1.0F),
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

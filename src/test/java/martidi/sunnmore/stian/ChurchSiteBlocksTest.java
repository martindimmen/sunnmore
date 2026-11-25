package martidi.sunnmore.stian;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.SharedConstants;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import martidi.sunnmore.stian.ChurchBlueprint.Placement;

/** Real block states from the vanilla registry: what a tipsy mistake does, and what counts as free space. */
class ChurchSiteBlocksTest {
	@BeforeAll
	static void bootstrap() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	private static BlockState state(String s) throws CommandSyntaxException {
		return BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, s, false).blockState();
	}

	@Test
	void mistakesNeverCrashAndKeepTheBlock() throws IOException, CommandSyntaxException {
		for (Placement p : ChurchBlueprintTest.church().buildOrder()) {
			BlockState right = state(p.block());
			// Spread-out seeds: small consecutive seeds give the same first coin flip.
			for (long seed = 1; seed <= 32; seed++) {
				BlockState wrong = ChurchSite.wrongly(right, RandomSource.create(seed * 0x9E3779B97F4A7C15L));
				assertEquals(right.getBlock(), wrong.getBlock(), "a mistake changed the block: " + p);
			}
		}
	}

	@Test
	void doorMistakeIsTheHinge() throws CommandSyntaxException {
		BlockState door = state("minecraft:birch_door[facing=south,half=lower,hinge=left,open=false]");
		assertEquals("right", ChurchSite.wrongly(door, RandomSource.create(1)).getValue(net.minecraft.world.level.block.DoorBlock.HINGE).getSerializedName());
	}

	@Test
	void freeSpaceIsAirPlantsAndSnowOnly() {
		assertTrue(ChurchSite.isSoftOrEmpty(Blocks.AIR.defaultBlockState()));
		assertTrue(ChurchSite.isSoftOrEmpty(Blocks.SHORT_GRASS.defaultBlockState()));
		assertTrue(ChurchSite.isSoftOrEmpty(Blocks.POPPY.defaultBlockState()));
		assertTrue(ChurchSite.isSoftOrEmpty(Blocks.SNOW.defaultBlockState()));
		// Never trees, fluids or anything a player built.
		assertFalse(ChurchSite.isSoftOrEmpty(Blocks.CHERRY_LEAVES.defaultBlockState()));
		assertFalse(ChurchSite.isSoftOrEmpty(Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState()));
		assertFalse(ChurchSite.isSoftOrEmpty(Blocks.WATER.defaultBlockState()));
		assertFalse(ChurchSite.isSoftOrEmpty(Blocks.LAVA.defaultBlockState()));
		assertFalse(ChurchSite.isSoftOrEmpty(Blocks.SEAGRASS.defaultBlockState()));
		assertFalse(ChurchSite.isSoftOrEmpty(Blocks.CHEST.defaultBlockState()));
		assertFalse(ChurchSite.isSoftOrEmpty(Blocks.STONE.defaultBlockState()));
	}
}

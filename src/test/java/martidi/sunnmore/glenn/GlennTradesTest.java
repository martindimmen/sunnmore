package martidi.sunnmore.glenn;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Items;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Items only, no ItemStacks: item components are not bound in a unit test, so stacks can't be made. */
class GlennTradesTest {
	@BeforeAll
	static void bootstrap() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	void discountOnlyOnSugar() {
		assertTrue(GlennTrades.discountApplies(Items.SUGAR));
		// The Wago deal is paid in glass and iron: a Stamkunde discount must never make it cheaper.
		assertFalse(GlennTrades.discountApplies(Items.GLASS));
		assertFalse(GlennTrades.discountApplies(Items.IRON_INGOT));
	}
}

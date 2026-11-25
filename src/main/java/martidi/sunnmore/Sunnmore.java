package martidi.sunnmore;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import martidi.sunnmore.bard.BardEntities;
import martidi.sunnmore.glenn.GlennEntities;
import martidi.sunnmore.glenn.GlennItems;
import martidi.sunnmore.glenn.GlennLoyalty;
import martidi.sunnmore.jinith.JinithEntities;
import martidi.sunnmore.visits.FriendVisits;
import martidi.sunnmore.visits.LureBlocks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sunnmore implements ModInitializer {
	public static final String MOD_ID = "sunnmore";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		GlennEntities.register();
		GlennItems.register();
		GlennLoyalty.register();
		JinithEntities.register();
		BardEntities.register();
		LureBlocks.register();
		FriendVisits.register();
		LOGGER.info("Sunnmore loaded");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}

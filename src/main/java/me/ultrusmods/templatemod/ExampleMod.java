package me.ultrusmods.templatemod;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExampleMod implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("templatemod");

	@Override
	public void onInitialize() {
		LOGGER.info("Template Mod has loaded!");
	}
}

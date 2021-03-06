package net.blay09.mods.excompressum.config;

import net.minecraftforge.common.config.Configuration;

public class ExCompressumConfig {

	public static boolean skipAutoSieveSkins;
	public static boolean showRegistryWarnings;
	public static boolean disableParticles;

	public static boolean enableWoodChippings;

	public static void preInit(Configuration config) {
		skipAutoSieveSkins = config.getBoolean("Skip Auto Sieve Skins", "client", false, "Set this to true to disable the loading of auto sieve skins from a remote server.");
		disableParticles = config.getBoolean("Disable Particles", "client", false, "If you're playing on a potato, setting this to true will disable particles from the Sieves and Auto Hammers from Ex Compressum.");
		showRegistryWarnings = config.getBoolean("Show Registry Warnings", "client", false, "Set this to true if you're a modpack dev to see Ex Compressum registry warnings in chat. Errors will always display.");

		enableWoodChippings = config.getBoolean("Wood Chippings", "items", true, "If set to true, wood can be hammered into wood chippings, which can be composted into dirt.");

		ProcessingConfig.load(config);
		CompressedMobsConfig.load(config);
		ToolsConfig.load(config);
		BaitConfig.load(config);
	}

	public static void init(Configuration config) {
		BlockConfig.postLoad(config);
		ItemConfig.postLoad(config);
	}
}

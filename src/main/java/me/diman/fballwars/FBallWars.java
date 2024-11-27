package me.diman.fballwars;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.diman.fballwars.game.FBallWarsConfig;
import me.diman.fballwars.game.FBallWarsWaiting;
import xyz.nucleoid.plasmid.api.game.GameType;

public class FBallWars implements ModInitializer {
	public static final String MOD_ID = "fballwars";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final GameType<FBallWarsConfig> TYPE = GameType.register(Identifier.of("fballwars", "fballwars"), FBallWarsConfig.CODEC, FBallWarsWaiting::open);

	@Override
	public void onInitialize() {
		LOGGER.info("Running sheep wars mod!");
	}
}
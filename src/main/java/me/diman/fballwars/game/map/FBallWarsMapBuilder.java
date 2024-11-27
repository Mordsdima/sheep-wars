package me.diman.fballwars.game.map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.world.biome.BiomeKeys;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.map_templates.MapTemplateMetadata;
import xyz.nucleoid.map_templates.MapTemplateSerializer;
import xyz.nucleoid.map_templates.TemplateRegion;
import xyz.nucleoid.plasmid.api.game.GameOpenException;

import java.io.IOException;
import java.util.List;

import me.diman.fballwars.FBallWars;
import me.diman.fballwars.game.FBallWarsConfig;

public class FBallWarsMapBuilder {
    private final FBallWarsConfig.MapConfig config;

    public FBallWarsMapBuilder(FBallWarsConfig.MapConfig config) {
        this.config = config;
    }

    public FBallWarsMap create(MinecraftServer server) throws GameOpenException {
        try {
            MapTemplate template = MapTemplateSerializer.loadFromResource(server, this.config.id());
            MapTemplateMetadata metadata = template.getMetadata();

            List<BlockBounds> blueSpawns = getSpawnsByName("blue_point", metadata);
            List<BlockBounds> redSpawns = getSpawnsByName("red_point", metadata);
            List<BlockBounds> waitingPoint = getSpawnsByName("waiting_point", metadata);

            FBallWarsMap map = new FBallWarsMap(template, blueSpawns, redSpawns, waitingPoint.get(0), this.config.spawnAngle());
            template.setBiome(BiomeKeys.THE_VOID);

            return map;
        } catch (IOException e) {
            throw new GameOpenException(Text.literal("Failed to load template"), e);
        }
    }

    private static List<BlockBounds> getSpawnsByName(String name, MapTemplateMetadata metadata) {
        List<BlockBounds> spawns = metadata.getRegions(name).sorted((a, b) -> {
            return getPriority(b) - getPriority(a);
        }).map(TemplateRegion::getBounds).toList();

        if (spawns.isEmpty()) {
            FBallWars.LOGGER.error("No spawn(s) is defined on the map! The game will not work.");
            throw new GameOpenException(Text.literal("no spawn defined"));
        } else {
            return spawns;
        }
    }

    private static int getPriority(TemplateRegion region) {
        return region == null || region.getData() == null ? 1 : region.getData().getInt("Priority");
    }
}

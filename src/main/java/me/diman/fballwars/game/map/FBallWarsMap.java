package me.diman.fballwars.game.map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.api.game.world.generator.TemplateChunkGenerator;

import java.util.List;

public class FBallWarsMap {
    private final MapTemplate template;
    public final List<BlockBounds> blueSpawns;
    public final List<BlockBounds> redSpawns;
    public final BlockBounds spectatorSpawn;
    public final int spawnAngle;
    public final BlockBounds bounds;

    public FBallWarsMap(MapTemplate template, List<BlockBounds> blueSpawns, List<BlockBounds> redSpawns, BlockBounds spectatorSpawn, int spawnAngle) {
        this.template = template;
        this.blueSpawns = blueSpawns;
        this.redSpawns = redSpawns;
        this.spectatorSpawn = spectatorSpawn;
        this.spawnAngle = spawnAngle;
        this.bounds = template.getBounds();
    }

    public ChunkGenerator asGenerator(MinecraftServer server) {
        return new TemplateChunkGenerator(server, this.template);
    }

    // blue

    public BlockBounds getBlueSpawn(int index) {
        return this.blueSpawns.get(index % this.blueSpawns.size());
    }

    public BlockBounds getBlueSpawn(Random random) {
        return Util.getRandom(this.blueSpawns, random);
    }

    // red

    public BlockBounds getRedSpawn(int index) {
        return this.redSpawns.get(index % this.blueSpawns.size());
    }

    public BlockBounds getRedSpawn(Random random) {
        return Util.getRandom(this.redSpawns, random);
    }

    // spectator

    public BlockBounds getSpectatorSpawn() {
        return this.spectatorSpawn;
    }
}

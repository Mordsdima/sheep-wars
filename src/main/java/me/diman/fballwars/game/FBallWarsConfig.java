package me.diman.fballwars.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.plasmid.api.game.common.config.WaitingLobbyConfig;

import net.minecraft.util.Identifier;

public record FBallWarsConfig(MapConfig map, WaitingLobbyConfig players) {
    public static final MapCodec<FBallWarsConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return instance.group(
            MapConfig.CODEC.fieldOf("map").forGetter(FBallWarsConfig::map),
            WaitingLobbyConfig.CODEC.fieldOf("players").forGetter(FBallWarsConfig::players)
        ).apply(instance, FBallWarsConfig::new);
    });

    public record MapConfig(Identifier id, int spawnAngle, long time) {
        public static final Codec<MapConfig> CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(
                    Identifier.CODEC.fieldOf("id").forGetter(MapConfig::id),
                    Codec.INT.fieldOf("spawn_angle").forGetter(MapConfig::spawnAngle),
                    Codec.LONG.optionalFieldOf("time", 6000L).forGetter(MapConfig::time)
            ).apply(instance, MapConfig::new);
        });
    }
}

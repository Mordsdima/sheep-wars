package me.diman.fballwars.game;

import me.diman.fballwars.game.map.FBallWarsMap;
import me.diman.fballwars.game.map.FBallWarsMapBuilder;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.api.game.*;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptor;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;
import xyz.nucleoid.plasmid.api.game.common.GameWaitingLobby;

public class FBallWarsWaiting {

    public ServerWorld world;
    public GameActivity gameActivity;
    public FBallWarsConfig config;
    public FBallWarsMap map;

    public static GameOpenProcedure open(GameOpenContext<FBallWarsConfig> context) {
        FBallWarsConfig config = context.config();

        FBallWarsMapBuilder gen = new FBallWarsMapBuilder(context.config().map());
        FBallWarsMap map = gen.create(context.server());

        // set up how the world that this minigame will take place in should be constructed
        RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
                .setGenerator(map.asGenerator(context.server()))
                .setTimeOfDay(6000);

        return context.openWithWorld(worldConfig, (activity, world) -> {
            var swgw = new FBallWarsWaiting();
            swgw.world = world;
            swgw.config = config;
            swgw.gameActivity = activity;
            swgw.map = map;
            GameWaitingLobby.addTo(activity, config.players());

            activity.listen(GamePlayerEvents.ACCEPT, swgw::acceptPlayer);
            activity.listen(GameActivityEvents.REQUEST_START, swgw::requestStart);
        });
    }

    private JoinAcceptorResult acceptPlayer(JoinAcceptor offer) {
        return offer.teleport(this.world, this.map.spectatorSpawn.center()).thenRunForEach(player -> {
            player.setYaw(this.config.map().spawnAngle());
            player.changeGameMode(GameMode.ADVENTURE);
            player.setVelocity(Vec3d.ZERO);
            player.fallDistance = 0.0f;
            player.setFireTicks(0);

            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.NIGHT_VISION,
                    -1,
                    1,
                    true,
                    false
            ));

            player.networkHandler.syncWithPlayerPosition();
            
            //this.resetPlayer(player, GameMode.ADVENTURE, null);
        });
    }

    private GameResult requestStart() {
        FBallWarsActive.open(this.world, this.gameActivity.getGameSpace(), this.map, this.config);
        return GameResult.ok();
    }
}

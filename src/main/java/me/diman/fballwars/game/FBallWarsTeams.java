package me.diman.fballwars.game;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.api.game.GameSpace;

public class FBallWarsTeams {
    
    public FBallWarsTeam redTeam = new FBallWarsTeam();
    public FBallWarsTeam blueTeam = new FBallWarsTeam();

    public FBallWarsTeams(GameSpace space) {
        var teamDistribution = divideNumber(space.getPlayers().size());
        var players = space.getPlayers().participants();
        int redTeamLimit = teamDistribution[0];
        int blueTeamLimit = teamDistribution[1];

        for (ServerPlayerEntity player : players) {
            if (redTeam.players.size() < redTeamLimit) {
                redTeam.players.add(player);
            } else if (blueTeam.players.size() < blueTeamLimit) {
                blueTeam.players.add(player);
            }
        }
    }

    public static int[] divideNumber(int num) {
        Random random = new Random();
        // Decide whether the larger number goes first or second
        boolean largerFirst = random.nextBoolean();

        int half = num / 2; // Integer division
        int num1 = largerFirst ? half + (num % 2) : half; // Add the remainder if needed
        int num2 = num - num1; // Ensure their sum equals the original number

        return new int[]{num1, num2};
    }

    public class FBallWarsTeam {
        public ArrayList<ServerPlayerEntity> players = new ArrayList<>();
    }
}

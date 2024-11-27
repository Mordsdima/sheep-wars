package me.diman.fballwars.game;

import java.util.Set;

import me.diman.fballwars.game.map.FBallWarsMap;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import xyz.nucleoid.plasmid.api.game.GameCloseReason;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.item.ItemUseEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class FBallWarsActive {

    enum WhoWinned {
        NO_ONE,
        BLUE_TEAM,
        RED_TEAM
    }

    public FBallWarsTeams teams;
    public ServerWorld world;
    public FBallWarsMap map;
    public GameSpace gameSpace;
    private long closeTime = -1;
    private long ticks = 0;

    public FBallWarsActive(ServerWorld world, GameSpace space, FBallWarsMap map, FBallWarsConfig config) {
        this.teams = new FBallWarsTeams(space);
        this.world = world;
        this.map = map;
        this.gameSpace = space;
    }

    public static void open(ServerWorld world, GameSpace space, FBallWarsMap map, FBallWarsConfig config) {
        space.setActivity(activity -> {
            var swga = new FBallWarsActive(world, activity.getGameSpace(), map, config);
            
            // Законы физики..
            activity.deny(GameRuleType.CRAFTING);
            activity.deny(GameRuleType.PORTALS);
            activity.allow(GameRuleType.PVP);
            activity.deny(GameRuleType.HUNGER);
            activity.deny(GameRuleType.SATURATED_REGENERATION);
            activity.deny(GameRuleType.FALL_DAMAGE);
            activity.allow(GameRuleType.INTERACTION);
            activity.allow(GameRuleType.BLOCK_DROPS);
            activity.deny(GameRuleType.THROW_ITEMS);
            activity.deny(GameRuleType.UNSTABLE_TNT);

            activity.listen(GameActivityEvents.ENABLE, swga::onOpen);
            activity.listen(PlayerDeathEvent.EVENT, swga::playerDeath); // SUICIDE SQUAD
            activity.listen(GameActivityEvents.TICK, swga::tick);
            activity.listen(ItemUseEvent.EVENT, swga::itemUse);
        });
    }

    private ActionResult itemUse(ServerPlayerEntity player, Hand hand) {
        if(player.isHolding(Items.FIRE_CHARGE)) {
            Vec3d vec3d = player.getRotationVec(1.0F);
            FireballEntity fireballEntity = new FireballEntity(world, player, vec3d, 0); 
            fireballEntity.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, 1.5F, 1.0F);
            fireballEntity.setPosition(player.getX() /*+ vec3d.x * 4*/, player.getBodyY(0.5) /*+ 0.5*/, player.getZ());
            world.spawnEntity(fireballEntity);
            if (!player.getAbilities().creativeMode) {
                player.getStackInHand(hand).decrement(1);
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    private void onOpen() {
        for(ServerPlayerEntity spe : this.teams.redTeam.players) {
            spe.changeGameMode(GameMode.SURVIVAL);
            preparePlayer(spe);
            var target = this.map.getRedSpawn(0).center();
            spe.teleport(target.x, target.y, target.z, false);
        }

        for(ServerPlayerEntity spe : this.teams.blueTeam.players) {
            spe.changeGameMode(GameMode.SURVIVAL);
            preparePlayer(spe);
            var target = this.map.getBlueSpawn(0).center();
            spe.teleport(target.x, target.y, target.z, false);
        }
    }

    private void preparePlayer(ServerPlayerEntity player) {
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
    }

    private EventResult playerDeath(ServerPlayerEntity player, DamageSource source) {
        player.teleport(this.world, this.map.spectatorSpawn.center().x, this.map.spectatorSpawn.center().y, this.map.spectatorSpawn.center().z, Set.of(), 0, 0, false);
        player.changeGameMode(GameMode.SPECTATOR);
        preparePlayer(player);
        
        for(ServerPlayerEntity spe : this.world.getPlayers()) {
            spe.sendMessage(Text.of(new StringBuilder().append("💀 ").append(player.getNameForScoreboard()).toString()));
        }

        return EventResult.DENY;
    }

    private void tick() {
        long time = this.world.getTime();

        if (this.closeTime > 0) {
            this.tickClosing(this.gameSpace, time);
            return;
        }

        var whoWinned = this.whoWinned();
        if(whoWinned != WhoWinned.NO_ONE) {
            this.closeTime = time + 20 * 5;
        }

        ticks += 1;
        
        if(ticks % 100 == 0) {
            this.gameSpace.getPlayers().forEach(player -> {
                player.giveItemStack(new ItemStack(Items.WHITE_WOOL));
                player.giveItemStack(new ItemStack(Items.FIRE_CHARGE));
            });
        }
    }

    private void tickClosing(GameSpace gameSpace, long time) {
        if (time >= this.closeTime) {
            gameSpace.close(GameCloseReason.FINISHED);
        }
    }

    private WhoWinned whoWinned() {
        return WhoWinned.NO_ONE;
        /* 
        if(this.teams.redTeam.players.stream().filter(a -> a.isSpectator()).toList().size() == 0) {
            return WhoWinned.BLUE_TEAM;
        }

        if(this.teams.blueTeam.players.stream().filter(a -> a.isSpectator()).toList().size() == 0) {
            return WhoWinned.RED_TEAM;
        }

        return WhoWinned.NO_ONE;*/
    }
}

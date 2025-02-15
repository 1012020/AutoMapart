package me.bebeli555.automapart.utils;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalBlock;
import net.minecraft.util.math.BlockPos;

public class BaritoneUtils extends Utils {
	/**
	 * Makes baritone path to the given goal
	 */
	public static void goTo(BlockPos pos) {
		getBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(pos.getX(), pos.getY(), pos.getZ()));
	}

	/**
	 * Cancels the current baritone task forcefully
	 */
	public static void forceCancel() {
		getBaritone().getPathingBehavior().forceCancel();
	}

	/**
	 * Checks if baritone is currently pathing
	 */
	public static boolean isPathing() {
		return getBaritone().getPathingBehavior().isPathing();
	}

	/**
	 * Gets the baritone instance for main player
	 */
	public static IBaritone getBaritone() {
		return BaritoneAPI.getProvider().getPrimaryBaritone();
	}
}

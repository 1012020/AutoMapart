package me.bebeli555.automapart.utils;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class PlayerUtils extends Utils {
	/**
	 * Gets all the players the client knows except yourself.
	 */
	public static List<PlayerEntity> getAllWithoutClientPlayer() {
		return getAll().stream().filter(player -> player != mc.player).toList();
	}

	/**
	 * Gets all loaded players in the world
	 */
	public static List<PlayerEntity> getAll() {
		return new ArrayList<>(mc.world.getPlayers());
	}

	/**
	 * Gets ip for current server
	 * Or Singleplayer if in singleplayer
	 */
	public static String getServerIp() {
		try {
			return mc.getServer().getServerIp();
		} catch (NullPointerException e) {
			return "Singleplayer";
		}
	}

	/**
	 * Gets a Formatting color for the Player with Friend and Enemy coloring
	 * If not a friend or enemy then returns empty
	 */
	public static String getPlayerColor(PlayerEntity player) {
		return "";
	}

	/**
	 * Gets a color for the Player with friend and enemy coloring
	 */
	public static int getPlayerColor(PlayerEntity player, int defaultColor) {
		return defaultColor;
	}

	/**
	 * Makes the client player send the given chat message or command
	 */
	public static void sendChatMessage(String chatText) {
		if (chatText.startsWith("/")) {
			mc.player.networkHandler.sendChatCommand(chatText.substring(1));
		} else {
			mc.player.networkHandler.sendChatMessage(chatText);
		}
	}

	/**
	 * Sends a right click to interact with the block or entity the player is looking at
	 */
	public static void rightClick() {
		mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, (BlockHitResult)mc.crosshairTarget);
		mc.player.swingHand(Hand.MAIN_HAND);
	}

	/**
	 * Finds the first valid player name from the given chat message
	 */
	public static PlayerListEntry getPlayerFromChatMessage(String message) {
		PlayerListEntry closest = null;
		for (PlayerListEntry entry : Utils.mc.getNetworkHandler().getListedPlayerListEntries()) {
			int index = message.indexOf(entry.getProfile().getName());
			if (index != -1) {
				if (closest == null || index < message.indexOf(closest.getProfile().getName())) {
					closest = entry;
				}
			}
		}
		return closest;
	}

	/**
	 * Checks if the player is inputting forward, back, left or right, and also sneak and jump if boolean is true
	 */
	public static boolean isInputting(boolean sneakAndJump) {
		if (sneakAndJump && (mc.options.jumpKey.isPressed() || mc.options.sneakKey.isPressed())) {
			return true;
		}
		return mc.options.forwardKey.isPressed() || mc.options.backKey.isPressed() || mc.options.leftKey.isPressed() || mc.options.rightKey.isPressed();
	}

	/**
	 * Gets normalized movement angle for the given yaw and pitch based on the client inputs
	 * Put pitch to -1 to ignore it and only calculate yaw
	 */
	public static Vec3d getMovement(double yaw, double pitch, double forwardMultiplier, double upMultiplier, boolean isJumpDown, boolean isSneakDown) {
		Vec3d movement = new Vec3d(0, 0, 0);

		if (mc.options.rightKey.isPressed()) movement = movement.add(0, 0, 1);
		if (mc.options.leftKey.isPressed()) movement = movement.add(0, 0, -1);
		if (mc.options.forwardKey.isPressed()) movement = movement.add(1, 0, 0);
		if (mc.options.backKey.isPressed()) movement = movement.add(-1, 0, 0);
		if (isJumpDown) movement = movement.add(0, 1, 0);
		if (isSneakDown) movement = movement.add(0, -1, 0);

		double directions = Math.abs(movement.x) + Math.abs(movement.y) + Math.abs(movement.z);
		if (directions == 2) directions = 1.415;
		if (directions == 3) directions = 1.732;
		movement = new Vec3d(movement.x / directions, movement.y / directions, movement.z / directions);

		if (pitch != -1) movement = movement.rotateZ((float)Math.toRadians(pitch));
		movement = new Vec3d(movement.x * forwardMultiplier, movement.y * upMultiplier, movement.z * forwardMultiplier);
		movement = movement.rotateY(-(float)Math.toRadians(yaw + 90));

		return movement;
	}

	/**
	 * Simulates a block attack (left-click) at the given block position.
	 * This method will break a block (for example, if a carpet is the wrong type)
	 * so that it can later be replaced with the correct one.
	 */
	public static void attackBlock(BlockPos pos) {
		// Attack the block at the given position using an upward direction (adjust if needed)
		mc.interactionManager.attackBlock(pos, Direction.UP);
		// Swing the main hand to show the animation
		mc.player.swingHand(Hand.MAIN_HAND);
	}
}

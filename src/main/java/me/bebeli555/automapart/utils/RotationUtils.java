package me.bebeli555.automapart.utils;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationUtils extends Utils {
	/**
	 * Rotates the player instantly towards the given vec which is a position ingame
	 */
	public static void rotateTo(Vec3d vec, boolean sendPacket) {
		float[] rotations = getRotations(vec);
		mc.player.setYaw(rotations[0]);
		mc.player.setPitch(rotations[1]);

		if (sendPacket) {
			mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround()));
		}
	}

	/**
	 * Rotates players yaw and pitch to the given vec, doesn't send packet
	 */
	public static void rotateTo(Vec3d vec) {
		rotateTo(vec, false);
	}

	/**
	 * Rotates to the best facing of the block position
	 */
	public static void rotateTo(BlockPos pos, double yAdd, Direction setDirection) {
		Direction closestDirection = Direction.UP;
		boolean directionSet = false;

		for (Direction direction : Direction.values()) {
			if (BlockUtils.distance(mc.player.getBlockPos(), pos.offset(direction)) < BlockUtils.distance(mc.player.getBlockPos(), pos.offset(closestDirection)) && mc.world.getBlockState(pos.offset(direction)).isAir()) {
				closestDirection = direction;
				directionSet = true;
			}
		}

		if (!directionSet) {
			for (Direction direction : Direction.values()) {
				if (BlockUtils.distance(mc.player.getBlockPos(), pos.offset(direction)) < BlockUtils.distance(mc.player.getBlockPos(), pos.offset(closestDirection)) && mc.world.getBlockState(pos.offset(direction)).isOpaque()) {
					closestDirection = direction;
				}
			}
		}

		if (setDirection != null) {
			closestDirection = setDirection;
		}

		if (closestDirection == Direction.SOUTH || closestDirection == Direction.WEST || closestDirection == Direction.NORTH || closestDirection == Direction.EAST) {
			rotateTo(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.25 + yAdd, pos.getZ() + 0.5));
		} else if (closestDirection == Direction.UP) {
			rotateTo(new Vec3d(pos.getX() + 0.5, pos.getY() + 1 + yAdd, pos.getZ() + 0.5));
		} else if (closestDirection == Direction.DOWN) {
			rotateTo(new Vec3d(pos.getX() + 0.5, pos.getY() + yAdd, pos.getZ() + 0.5));
		}
	}

	public static void rotateTo(BlockPos pos, double yAdd) {
		rotateTo(pos, yAdd, null);
	}

	public static void rotateTo(BlockPos pos) {
		rotateTo(pos, 0);
	}

	/**
	 * Calculates rotations from the vec position which is a position in the 3d game
	 */
	public static float[] getRotations(Vec3d vec) {
		Vec3d eyesPos = new Vec3d(mc.player.getPos().x, mc.player.getPos().y + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getPos().z);
		double diffX = vec.x - eyesPos.x;
		double diffY = vec.y - eyesPos.y;
		double diffZ = vec.z - eyesPos.z;
		double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
		float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
		float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

		return new float[]{mc.player.getYaw() + MathHelper.wrapDegrees(yaw - mc.player.getYaw()), mc.player.getPitch() + MathHelper.wrapDegrees(pitch - mc.player.getPitch())};
	}
}

package com.minecraftabnormals.neapolitan.common.entity.goals;

import java.util.EnumSet;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.minecraftabnormals.neapolitan.core.other.NeapolitanTags;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class AvoidBlockGoal<T extends LivingEntity> extends Goal {
	protected final CreatureEntity entity;
	private final double farSpeed;
	private final double nearSpeed;
	protected BlockPos avoidTarget;
	protected final int avoidDistance;
	protected Path path;
	protected final PathNavigator navigation;
	protected final Predicate<LivingEntity> avoidTargetSelector;
	protected final Predicate<LivingEntity> field_203784_k;

	public AvoidBlockGoal(CreatureEntity entityIn, int avoidDistanceIn, double farSpeedIn, double nearSpeedIn) {
		this(entityIn, (p_200828_0_) -> {
			return true;
		}, avoidDistanceIn, farSpeedIn, nearSpeedIn, EntityPredicates.CAN_AI_TARGET::test);
	}

	public AvoidBlockGoal(CreatureEntity entityIn, Predicate<LivingEntity> targetPredicate, int distance, double nearSpeedIn, double farSpeedIn, Predicate<LivingEntity> p_i48859_9_) {
		this.entity = entityIn;
		this.avoidTargetSelector = targetPredicate;
		this.avoidDistance = distance;
		this.farSpeed = nearSpeedIn;
		this.nearSpeed = farSpeedIn;
		this.field_203784_k = p_i48859_9_;
		this.navigation = entityIn.getNavigator();
		this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	public AvoidBlockGoal(CreatureEntity entityIn, int distance, double nearSpeedIn, double farSpeedIn, Predicate<LivingEntity> targetPredicate) {
		this(entityIn, (p_203782_0_) -> {
			return true;
		}, distance, nearSpeedIn, farSpeedIn, targetPredicate);
	}

	public boolean shouldExecute() {
		if (NeapolitanTags.Blocks.CREEPER_REPELLENTS.getAllElements().isEmpty()) {
			return false;
		} else {
			AxisAlignedBB aabb = entity.getBoundingBox().grow(6.0F, 4.0F, 6.0F);
			Stream<BlockPos> blocks = BlockPos.getAllInBox(new BlockPos(aabb.minX, aabb.minY, aabb.minZ), new BlockPos(aabb.maxX, aabb.maxY, aabb.maxZ));

			blocks.forEach(blockpos -> {
				double d0 = Double.MAX_VALUE;
				double d1 = this.entity.getDistanceSq(blockpos.getX(), blockpos.getY(), blockpos.getZ());
				if (entity.world.getBlockState(blockpos).getBlock().isIn(NeapolitanTags.Blocks.CREEPER_REPELLENTS) && d1 < d0) {
					d0 = d1;
					this.avoidTarget = blockpos;
				}
			});

			if (this.avoidTarget == null) {
				return false;
			} else {
				Vector3d vec3d = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.entity, this.avoidDistance, 7, new Vector3d(this.avoidTarget.getX(), this.avoidTarget.getY(), this.avoidTarget.getZ()));
				if (vec3d == null) {
					return false;
				} else if (this.avoidTarget.distanceSq(vec3d.x, vec3d.y, vec3d.z, false) < this.avoidTarget.distanceSq(this.entity.getPosX(), this.entity.getPosY(), this.entity.getPosZ(), false)) {
					return false;
				} else {
					this.path = this.navigation.getPathToPos(vec3d.x, vec3d.y, vec3d.z, 0);
					return this.path != null;
				}
			}
		}
	}

	public boolean shouldContinueExecuting() {
		return !this.navigation.noPath();
	}

	public void startExecuting() {
		this.navigation.setPath(this.path, this.farSpeed);
	}

	public void resetTask() {
		this.avoidTarget = null;
	}

	public void tick() {
		if (this.entity.getDistanceSq(this.avoidTarget.getX(), this.avoidTarget.getY(), this.avoidTarget.getZ()) < 49.0D) {
			this.entity.getNavigator().setSpeed(this.nearSpeed);
		} else {
			this.entity.getNavigator().setSpeed(this.farSpeed);
		}

	}
}
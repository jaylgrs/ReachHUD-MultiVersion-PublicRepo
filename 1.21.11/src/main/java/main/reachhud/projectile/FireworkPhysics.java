package main.reachhud.projectile;

import java.util.Optional;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class FireworkPhysics {

    private static final int MAX_SIMULATION_TICKS = 200;
    private static final double MAX_DISTANCE = 128.0;
    private static final double HORIZONTAL_ACCELERATION = 1.15;
    private static final double VERTICAL_ACCELERATION = 0.04;
    private static final double PROJECTILE_HALF_SIZE = 0.125;

    private FireworkPhysics() {
    }

    public static TrajectoryResult simulate(
            Vec3 startPosition,
            Vec3 initialVelocity,
            AABB targetBox
    ) {
        if (startPosition == null
                || initialVelocity == null
                || targetBox == null) {
            return TrajectoryResult.miss();
        }

        Vec3 position = startPosition;
        Vec3 velocity = initialVelocity;
        double travelledDistance = 0.0;

        for (int tick = 0; tick < MAX_SIMULATION_TICKS; tick++) {
            Vec3 nextPosition = position.add(velocity);
            double segmentDistance = position.distanceTo(nextPosition);

            Optional<Vec3> hitPosition = getIntersection(
                    position,
                    nextPosition,
                    targetBox
            );

            if (hitPosition.isPresent()) {
                double hitDistance = travelledDistance
                        + position.distanceTo(hitPosition.get());

                return TrajectoryResult.hit(
                        hitPosition.get(),
                        hitDistance
                );
            }

            travelledDistance += segmentDistance;

            if (travelledDistance >= MAX_DISTANCE) {
                break;
            }

            position = nextPosition;

            velocity = new Vec3(
                    velocity.x * HORIZONTAL_ACCELERATION,
                    velocity.y + VERTICAL_ACCELERATION,
                    velocity.z * HORIZONTAL_ACCELERATION
            );
        }

        return TrajectoryResult.miss();
    }

    private static Optional<Vec3> getIntersection(
            Vec3 start,
            Vec3 end,
            AABB targetBox
    ) {
        AABB expandedTarget = targetBox.inflate(PROJECTILE_HALF_SIZE);
        return expandedTarget.clip(start, end);
    }

    public record TrajectoryResult(
            boolean hit,
            Vec3 hitPosition,
            double distance
    ) {

        public static TrajectoryResult hit(
                Vec3 hitPosition,
                double distance
        ) {
            return new TrajectoryResult(
                    true,
                    hitPosition,
                    distance
            );
        }

        public static TrajectoryResult miss() {
            return new TrajectoryResult(
                    false,
                    Vec3.ZERO,
                    -1.0
            );
        }
    }
}
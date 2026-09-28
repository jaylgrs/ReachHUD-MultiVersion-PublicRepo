package main.reachhud.projectile;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ProjectilePhysics {

    private static final double MAX_DISTANCE = 64.0;
    private static final double ARROW_GRAVITY = 0.05;
    private static final double AIR_INERTIA = 0.99;
    private static final double PROJECTILE_HALF_SIZE = 0.125;

    private ProjectilePhysics() {
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

        for (int tick = 0; tick < 200; tick++) {
            Vec3 nextPosition = position.add(velocity);

            if (segmentIntersectsTarget(
                    position,
                    nextPosition,
                    targetBox
            )) {
                double hitDistance =
                        travelledDistance
                                + position.distanceTo(nextPosition);

                return TrajectoryResult.hit(
                        nextPosition,
                        hitDistance
                );
            }

            travelledDistance +=
                    position.distanceTo(nextPosition);

            if (travelledDistance >= MAX_DISTANCE) {
                break;
            }

            position = nextPosition;

            velocity = new Vec3(
                    velocity.x * AIR_INERTIA,
                    velocity.y * AIR_INERTIA
                            - ARROW_GRAVITY,
                    velocity.z * AIR_INERTIA
            );
        }

        return TrajectoryResult.miss();
    }

    private static boolean segmentIntersectsTarget(
            Vec3 start,
            Vec3 end,
            AABB targetBox
    ) {
        AABB expandedTarget =
                targetBox.inflate(
                        PROJECTILE_HALF_SIZE
                );

        return expandedTarget
                .clip(start, end)
                .isPresent();
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
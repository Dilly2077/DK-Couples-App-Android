package com.dk.together.pets.engine

import kotlin.math.abs
import kotlin.math.hypot

data class RoomMovementBounds(
    val minX: Double = 0.10,
    val maxX: Double = 0.90,
    val minY: Double = 0.55,
    val maxY: Double = 0.88
) {
    init {
        require(minX in 0.0..1.0 && maxX in 0.0..1.0 && minX < maxX)
        require(minY in 0.0..1.0 && maxY in 0.0..1.0 && minY < maxY)
    }
}

data class MovementTuning(
    val normalizedUnitsPerSecond: Double = 0.12,
    val minimumMoveDurationMs: Long = 900L,
    val maximumMoveDurationMs: Long = 7_000L,
    val minimumDistance: Double = 0.08
) {
    init {
        require(normalizedUnitsPerSecond > 0.0)
        require(minimumMoveDurationMs > 0)
        require(maximumMoveDurationMs >= minimumMoveDurationMs)
        require(minimumDistance >= 0.0)
    }
}

data class MovementPlan(
    val from: NormalizedPosition,
    val to: NormalizedPosition,
    val facing: FacingDirection,
    val durationMs: Long,
    val behaviourDuringMove: PetBehaviour = PetBehaviour.WALKING,
    val behaviourAfterMove: PetBehaviour = PetBehaviour.IDLE
)

class PetMovementEngine(
    private val tuning: MovementTuning = MovementTuning(),
    private val roomBounds: Map<PetRoom, RoomMovementBounds> = PetRoom.entries.associateWith {
        RoomMovementBounds()
    }
) {
    fun planNextMove(pet: PetInstance, random: RandomSource): MovementPlan {
        val bounds = roomBounds.getValue(pet.room)
        var target = randomPosition(bounds, random)

        // One retry prevents many tiny near-static moves while staying deterministic.
        if (distance(pet.position, target) < tuning.minimumDistance) {
            target = randomPosition(bounds, random)
        }

        val distance = distance(pet.position, target)
        val rawDurationMs = ((distance / tuning.normalizedUnitsPerSecond) * 1000.0).toLong()
        val duration = rawDurationMs.coerceIn(
            tuning.minimumMoveDurationMs,
            tuning.maximumMoveDurationMs
        )

        val facing = when {
            target.x < pet.position.x -> FacingDirection.LEFT
            target.x > pet.position.x -> FacingDirection.RIGHT
            else -> pet.facing
        }

        return MovementPlan(
            from = pet.position,
            to = target,
            facing = facing,
            durationMs = duration
        )
    }

    fun applyArrival(pet: PetInstance, plan: MovementPlan): PetInstance = pet.copy(
        position = plan.to,
        facing = plan.facing,
        behaviour = plan.behaviourAfterMove
    )

    private fun randomPosition(bounds: RoomMovementBounds, random: RandomSource): NormalizedPosition {
        val x = bounds.minX + random.nextUnitDouble() * (bounds.maxX - bounds.minX)
        val y = bounds.minY + random.nextUnitDouble() * (bounds.maxY - bounds.minY)
        return NormalizedPosition(x, y)
    }

    private fun distance(a: NormalizedPosition, b: NormalizedPosition): Double =
        hypot(abs(b.x - a.x), abs(b.y - a.y))
}

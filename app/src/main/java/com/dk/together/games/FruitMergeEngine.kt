package com.dk.together.games

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class FruitBody(
    val id: Long,
    val level: Int,
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var ageSeconds: Float = 0f,
)

data class FruitMergeSnapshot(
    val bodies: List<FruitBody>,
    val nextLevel: Int,
    val score: Int,
    val gameOver: Boolean,
)

class FruitMergeEngine(seed: Int = (System.nanoTime() and 0x7fffffff).toInt()) {
    private val random = Random(seed)
    private val bodies = mutableListOf<FruitBody>()
    private var nextId = 1L

    var nextLevel: Int = randomStartLevel()
        private set
    var score: Int = 0
        private set
    var gameOver: Boolean = false
        private set

    fun snapshot(): FruitMergeSnapshot = FruitMergeSnapshot(
        bodies = bodies.map { it.copy() },
        nextLevel = nextLevel,
        score = score,
        gameOver = gameOver,
    )

    fun drop(normalizedX: Float): Boolean {
        if (gameOver) return false
        val level = nextLevel
        val radius = radius(level)
        val x = normalizedX.coerceIn(radius + WALL_PADDING, 1f - radius - WALL_PADDING)
        bodies += FruitBody(
            id = nextId++,
            level = level,
            x = x,
            y = DROP_Y + radius,
            vy = 0.015f,
        )
        nextLevel = randomStartLevel()
        return true
    }

    fun step(dtSeconds: Float): Int {
        if (gameOver) return 0
        val dt = dtSeconds.coerceIn(0.001f, 0.033f)

        bodies.forEach { body ->
            body.ageSeconds += dt
            body.vy += GRAVITY * dt
            body.vx *= HORIZONTAL_DAMPING
            body.vy *= AIR_DAMPING
            body.x += body.vx * dt
            body.y += body.vy * dt

            val r = radius(body.level)
            if (body.x - r < WALL_PADDING) {
                body.x = WALL_PADDING + r
                body.vx = abs(body.vx) * WALL_BOUNCE
            } else if (body.x + r > 1f - WALL_PADDING) {
                body.x = 1f - WALL_PADDING - r
                body.vx = -abs(body.vx) * WALL_BOUNCE
            }

            if (body.y + r > FLOOR_Y) {
                body.y = FLOOR_Y - r
                body.vy = -abs(body.vy) * FLOOR_BOUNCE
                if (abs(body.vy) < 0.025f) body.vy = 0f
            }
        }

        var merges = 0
        val consumed = mutableSetOf<Long>()
        val spawned = mutableListOf<FruitBody>()

        for (i in bodies.indices) {
            val a = bodies[i]
            if (a.id in consumed) continue
            for (j in i + 1 until bodies.size) {
                val b = bodies[j]
                if (b.id in consumed) continue
                val dx = b.x - a.x
                val dy = b.y - a.y
                val distance = hypot(dx.toDouble(), dy.toDouble()).toFloat()
                val minDistance = radius(a.level) + radius(b.level)
                if (distance >= minDistance || minDistance <= 0f) continue

                if (a.level == b.level && a.level < MAX_LEVEL) {
                    consumed += a.id
                    consumed += b.id
                    val mergedLevel = a.level + 1
                    spawned += FruitBody(
                        id = nextId++,
                        level = mergedLevel,
                        x = ((a.x + b.x) / 2f).coerceIn(radius(mergedLevel), 1f - radius(mergedLevel)),
                        y = ((a.y + b.y) / 2f).coerceAtMost(FLOOR_Y - radius(mergedLevel)),
                        vx = (a.vx + b.vx) * 0.35f,
                        vy = min(a.vy, b.vy) - 0.055f,
                    )
                    score += scoreFor(mergedLevel)
                    merges++
                    break
                } else {
                    resolveCollision(a, b, dx, dy, distance, minDistance)
                }
            }
        }

        if (consumed.isNotEmpty()) {
            bodies.removeAll { it.id in consumed }
            bodies += spawned
        }

        gameOver = bodies.any { body ->
            val r = radius(body.level)
            body.ageSeconds > GAME_OVER_GRACE_SECONDS &&
                body.y - r < DANGER_LINE &&
                abs(body.vy) < 0.08f
        }

        return merges
    }

    internal fun debugSetBodies(value: List<FruitBody>) {
        bodies.clear()
        bodies += value.map { it.copy() }
        nextId = (bodies.maxOfOrNull { it.id } ?: 0L) + 1L
        score = 0
        gameOver = false
    }

    private fun resolveCollision(
        a: FruitBody,
        b: FruitBody,
        dx: Float,
        dy: Float,
        distance: Float,
        minDistance: Float,
    ) {
        val safeDistance = max(distance, 0.0001f)
        val nx = dx / safeDistance
        val ny = dy / safeDistance
        val overlap = minDistance - safeDistance
        val push = overlap * 0.5f

        a.x -= nx * push
        a.y -= ny * push
        b.x += nx * push
        b.y += ny * push

        val relativeNormalVelocity = (b.vx - a.vx) * nx + (b.vy - a.vy) * ny
        if (relativeNormalVelocity < 0f) {
            val impulse = -(1f + COLLISION_BOUNCE) * relativeNormalVelocity * 0.5f
            a.vx -= impulse * nx
            a.vy -= impulse * ny
            b.vx += impulse * nx
            b.vy += impulse * ny
        }
    }

    private fun randomStartLevel(): Int = random.nextInt(0, 4)

    companion object {
        const val MAX_LEVEL = 10
        const val DANGER_LINE = 0.155f
        private const val DROP_Y = 0.035f
        private const val FLOOR_Y = 0.985f
        private const val WALL_PADDING = 0.012f
        private const val GRAVITY = 0.72f
        private const val AIR_DAMPING = 0.998f
        private const val HORIZONTAL_DAMPING = 0.995f
        private const val FLOOR_BOUNCE = 0.22f
        private const val WALL_BOUNCE = 0.42f
        private const val COLLISION_BOUNCE = 0.12f
        private const val GAME_OVER_GRACE_SECONDS = 1.15f

        val fruitNames = listOf(
            "Cherry", "Strawberry", "Grape", "Mandarin", "Orange",
            "Apple", "Pear", "Peach", "Pineapple", "Melon", "Watermelon",
        )

        fun radius(level: Int): Float = when (level.coerceIn(0, MAX_LEVEL)) {
            0 -> 0.030f
            1 -> 0.037f
            2 -> 0.045f
            3 -> 0.054f
            4 -> 0.064f
            5 -> 0.076f
            6 -> 0.089f
            7 -> 0.103f
            8 -> 0.118f
            9 -> 0.136f
            else -> 0.153f
        }

        fun scoreFor(level: Int): Int = when (level.coerceIn(0, MAX_LEVEL)) {
            0 -> 1
            1 -> 3
            2 -> 6
            3 -> 10
            4 -> 15
            5 -> 21
            6 -> 28
            7 -> 36
            8 -> 45
            9 -> 55
            else -> 75
        }
    }
}

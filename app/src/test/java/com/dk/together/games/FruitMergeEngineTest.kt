package com.dk.together.games

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FruitMergeEngineTest {
    @Test
    fun identicalTouchingFruitMergeAndScore() {
        val engine = FruitMergeEngine(seed = 1)
        engine.debugSetBodies(
            listOf(
                FruitBody(id = 1, level = 0, x = 0.49f, y = 0.7f),
                FruitBody(id = 2, level = 0, x = 0.53f, y = 0.7f),
            )
        )

        val merges = engine.step(0.016f)
        val snapshot = engine.snapshot()

        assertEquals(1, merges)
        assertEquals(1, snapshot.bodies.size)
        assertEquals(1, snapshot.bodies.single().level)
        assertEquals(FruitMergeEngine.scoreFor(1), snapshot.score)
    }

    @Test
    fun droppedFruitAlwaysStartsWithinBoard() {
        val engine = FruitMergeEngine(seed = 3)
        assertTrue(engine.drop(-2f))
        assertTrue(engine.drop(4f))

        engine.snapshot().bodies.forEach { body ->
            val radius = FruitMergeEngine.radius(body.level)
            assertTrue(body.x >= radius)
            assertTrue(body.x <= 1f - radius)
        }
    }
}

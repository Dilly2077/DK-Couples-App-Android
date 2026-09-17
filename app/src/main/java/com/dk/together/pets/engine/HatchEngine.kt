package com.dk.together.pets.engine

import kotlin.math.max
import kotlin.math.min

enum class HatchStage {
    PRISTINE,
    WOBBLE,
    SMALL_CRACK,
    MEDIUM_CRACK,
    HEAVY_CRACK,
    SPLIT_SHELL,
    HATCH_FLASH,
    READY_TO_CLAIM,
    CLAIMED
}

data class HatchSession(
    val id: String,
    val species: PetSpecies,
    val startedAtEpochMs: Long,
    val endsAtEpochMs: Long,
    val claimedAtEpochMs: Long? = null
) {
    init {
        require(endsAtEpochMs > startedAtEpochMs) { "Hatch end time must be after start time" }
        if (claimedAtEpochMs != null) {
            require(claimedAtEpochMs >= startedAtEpochMs) { "Claim time cannot be before hatch start" }
        }
    }
}

data class HatchSnapshot(
    val sessionId: String,
    val species: PetSpecies,
    val progress: Double,
    val stage: HatchStage,
    val remainingMs: Long,
    val complete: Boolean,
    val claimed: Boolean
)

fun interface HatchDurationProvider {
    fun durationMsFor(species: PetSpecies): Long
}

/**
 * Temporary duration policy. Species-specific timings can be assigned later without changing engine logic.
 */
class ConfigurableHatchDurationProvider(
    private val defaultDurationMs: Long = 4L * 60L * 60L * 1000L,
    private val overrides: Map<PetSpecies, Long> = emptyMap()
) : HatchDurationProvider {
    init {
        require(defaultDurationMs > 0L) { "default hatch duration must be positive" }
        require(overrides.values.all { it > 0L }) { "all hatch duration overrides must be positive" }
    }

    override fun durationMsFor(species: PetSpecies): Long = overrides[species] ?: defaultDurationMs
}

class HatchEngine(
    private val durationProvider: HatchDurationProvider = ConfigurableHatchDurationProvider()
) {
    fun createSession(
        id: String,
        species: PetSpecies,
        nowEpochMs: Long
    ): HatchSession {
        val duration = durationProvider.durationMsFor(species)
        return HatchSession(
            id = id,
            species = species,
            startedAtEpochMs = nowEpochMs,
            endsAtEpochMs = nowEpochMs + duration
        )
    }

    fun snapshot(session: HatchSession, nowEpochMs: Long): HatchSnapshot {
        if (session.claimedAtEpochMs != null) {
            return HatchSnapshot(
                sessionId = session.id,
                species = session.species,
                progress = 1.0,
                stage = HatchStage.CLAIMED,
                remainingMs = 0L,
                complete = true,
                claimed = true
            )
        }

        val total = (session.endsAtEpochMs - session.startedAtEpochMs).toDouble()
        val elapsed = (nowEpochMs - session.startedAtEpochMs).toDouble()
        val progress = min(1.0, max(0.0, elapsed / total))
        val complete = nowEpochMs >= session.endsAtEpochMs

        return HatchSnapshot(
            sessionId = session.id,
            species = session.species,
            progress = progress,
            stage = stageFor(progress, complete),
            remainingMs = max(0L, session.endsAtEpochMs - nowEpochMs),
            complete = complete,
            claimed = false
        )
    }

    fun claim(session: HatchSession, nowEpochMs: Long): HatchSession {
        require(session.claimedAtEpochMs == null) { "Hatch session is already claimed" }
        require(nowEpochMs >= session.endsAtEpochMs) { "Hatch session is not complete yet" }
        return session.copy(claimedAtEpochMs = nowEpochMs)
    }

    private fun stageFor(progress: Double, complete: Boolean): HatchStage {
        if (complete) return HatchStage.READY_TO_CLAIM
        return when {
            progress < 0.30 -> HatchStage.PRISTINE
            progress < 0.50 -> HatchStage.WOBBLE
            progress < 0.65 -> HatchStage.SMALL_CRACK
            progress < 0.78 -> HatchStage.MEDIUM_CRACK
            progress < 0.88 -> HatchStage.HEAVY_CRACK
            progress < 0.96 -> HatchStage.SPLIT_SHELL
            else -> HatchStage.HATCH_FLASH
        }
    }
}

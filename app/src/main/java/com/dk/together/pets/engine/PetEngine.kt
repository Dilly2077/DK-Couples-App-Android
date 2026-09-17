package com.dk.together.pets.engine

import java.util.UUID

fun interface IdSource {
    fun nextId(prefix: String): String
}

class UuidIdSource : IdSource {
    override fun nextId(prefix: String): String = "$prefix-${UUID.randomUUID()}"
}

class PetEngine(
    private val repository: PetEngineRepository,
    private val random: RandomSource = KotlinRandomSource(),
    private val idSource: IdSource = UuidIdSource(),
    private val hatchSelector: HatchSelector = HatchSelector(),
    private val hatchEngine: HatchEngine = HatchEngine(),
    private val needsEngine: PetNeedsEngine = PetNeedsEngine(),
    private val movementEngine: PetMovementEngine = PetMovementEngine()
) {
    fun startHatch(nowEpochMs: Long): HatchSnapshot {
        require(repository.loadActiveHatch() == null) { "An egg is already incubating" }

        val selected = hatchSelector.select(repository.loadHistory(), random)
        val session = hatchEngine.createSession(
            id = idSource.nextId("hatch"),
            species = selected.species,
            nowEpochMs = nowEpochMs
        )
        repository.saveActiveHatch(session)
        return hatchEngine.snapshot(session, nowEpochMs)
    }

    fun activeHatch(nowEpochMs: Long): HatchSnapshot? =
        repository.loadActiveHatch()?.let { hatchEngine.snapshot(it, nowEpochMs) }

    fun claimCompletedHatch(nowEpochMs: Long): PetInstance {
        val session = requireNotNull(repository.loadActiveHatch()) { "No active hatch session" }
        val snapshot = hatchEngine.snapshot(session, nowEpochMs)
        require(snapshot.complete) { "Egg is not ready to hatch" }
        require(!snapshot.claimed) { "Egg has already been claimed" }

        hatchEngine.claim(session, nowEpochMs)

        val pet = PetInstance(
            id = idSource.nextId("pet"),
            species = session.species,
            hatchedAtEpochMs = nowEpochMs,
            lastNeedsUpdateEpochMs = nowEpochMs
        )
        repository.savePet(pet)
        repository.saveHistory(
            hatchSelector.historyAfterClaim(repository.loadHistory(), session.species)
        )
        repository.saveActiveHatch(null)
        return pet
    }

    fun pets(nowEpochMs: Long): List<PetInstance> = repository.loadPets().map { pet ->
        val advanced = applyNeeds(pet, nowEpochMs)
        if (advanced != pet) repository.savePet(advanced)
        advanced
    }

    fun pet(id: String, nowEpochMs: Long): PetInstance? {
        val pet = repository.findPet(id) ?: return null
        val advanced = applyNeeds(pet, nowEpochMs)
        if (advanced != pet) repository.savePet(advanced)
        return advanced
    }

    fun visibleNeeds(id: String, nowEpochMs: Long): PetNeedsSnapshot? =
        pet(id, nowEpochMs)?.let(needsEngine::snapshot)

    fun planMovement(id: String, nowEpochMs: Long): MovementPlan {
        val pet = requireNotNull(pet(id, nowEpochMs)) { "Unknown pet id: $id" }
        return movementEngine.planNextMove(pet, random)
    }

    fun finishMovement(id: String, plan: MovementPlan, nowEpochMs: Long): PetInstance {
        val pet = requireNotNull(pet(id, nowEpochMs)) { "Unknown pet id: $id" }
        val arrived = movementEngine.applyArrival(pet, plan)
        repository.savePet(arrived)
        return arrived
    }

    fun moveToRoom(
        id: String,
        room: PetRoom,
        position: NormalizedPosition = NormalizedPosition(0.5, 0.72),
        nowEpochMs: Long
    ): PetInstance {
        val pet = requireNotNull(pet(id, nowEpochMs)) { "Unknown pet id: $id" }
        val moved = pet.copy(
            room = room,
            position = position,
            behaviour = PetBehaviour.IDLE
        )
        repository.savePet(moved)
        return moved
    }

    fun feed(id: String, nowEpochMs: Long): PetInstance {
        val pet = requireNotNull(repository.findPet(id)) { "Unknown pet id: $id" }
        return needsEngine.feed(pet, nowEpochMs).also(repository::savePet)
    }

    fun clean(id: String, nowEpochMs: Long): PetInstance {
        val pet = requireNotNull(repository.findPet(id)) { "Unknown pet id: $id" }
        return needsEngine.clean(pet, nowEpochMs).also(repository::savePet)
    }

    fun hatchHistory(): HatchHistory = repository.loadHistory()

    private fun applyNeeds(pet: PetInstance, nowEpochMs: Long): PetInstance {
        val advanced = needsEngine.advance(pet, nowEpochMs)
        val needs = needsEngine.snapshot(advanced)
        return if (needs.suggestedBehaviour != advanced.behaviour && advanced.behaviour != PetBehaviour.SLEEPING) {
            advanced.copy(behaviour = needs.suggestedBehaviour)
        } else {
            advanced
        }
    }
}

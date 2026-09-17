package com.dk.together.pets.engine

/**
 * Persistence boundary for the pet engine.
 *
 * The engine deliberately does not depend on Room/DataStore yet. The Android update can provide
 * a persistent implementation later without changing hatch, needs, or movement rules.
 */
interface PetEngineRepository {
    fun loadHistory(): HatchHistory
    fun saveHistory(history: HatchHistory)

    fun loadActiveHatch(): HatchSession?
    fun saveActiveHatch(session: HatchSession?)

    fun loadPets(): List<PetInstance>
    fun savePet(pet: PetInstance)
    fun findPet(id: String): PetInstance?
}

class InMemoryPetEngineRepository : PetEngineRepository {
    private var history: HatchHistory = HatchHistory()
    private var activeHatch: HatchSession? = null
    private val pets = linkedMapOf<String, PetInstance>()

    override fun loadHistory(): HatchHistory = history

    override fun saveHistory(history: HatchHistory) {
        this.history = history
    }

    override fun loadActiveHatch(): HatchSession? = activeHatch

    override fun saveActiveHatch(session: HatchSession?) {
        activeHatch = session
    }

    override fun loadPets(): List<PetInstance> = pets.values.toList()

    override fun savePet(pet: PetInstance) {
        pets[pet.id] = pet
    }

    override fun findPet(id: String): PetInstance? = pets[id]
}

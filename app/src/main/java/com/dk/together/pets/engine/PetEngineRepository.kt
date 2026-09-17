package com.dk.together.pets.engine

/** Persistence boundary for the pet engine. */
interface PetEngineRepository {
    fun loadHistory(): HatchHistory
    fun saveHistory(history: HatchHistory)

    fun loadActiveHatch(): HatchSession?
    fun saveActiveHatch(session: HatchSession?)

    fun loadPets(): List<PetInstance>
    fun savePet(pet: PetInstance)
    fun findPet(id: String): PetInstance?

    /** True when this exact care interaction has already mutated a pet. */
    fun hasCareEvent(interactionId: String): Boolean

    /**
     * Atomically persists a pet mutation and records its stable care interaction id. Returns false
     * when that interaction was already applied, making feed/clean/play retries safe.
     */
    fun savePetForCare(pet: PetInstance, interactionId: String, careType: String): Boolean
}

class InMemoryPetEngineRepository : PetEngineRepository {
    private var history: HatchHistory = HatchHistory()
    private var activeHatch: HatchSession? = null
    private val pets = linkedMapOf<String, PetInstance>()
    private val careEvents = linkedMapOf<String, Pair<String, String>>()

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

    override fun hasCareEvent(interactionId: String): Boolean = careEvents.containsKey(interactionId)

    override fun savePetForCare(pet: PetInstance, interactionId: String, careType: String): Boolean {
        require(interactionId.isNotBlank())
        require(careType.isNotBlank())
        if (careEvents.containsKey(interactionId)) return false
        pets[pet.id] = pet
        careEvents[interactionId] = pet.id to careType
        return true
    }
}

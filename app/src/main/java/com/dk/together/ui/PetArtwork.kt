package com.dk.together.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.dk.together.pets.engine.PetSpecies
import java.util.zip.ZipInputStream

enum class PetEnvironmentArtwork {
    PLAYROOM,
    KITCHEN,
    GARDEN,
    SPA,
}

enum class PetVisualState {
    IDLE,
    HUNGRY,
    DIRTY,
    HAPPY,
    CARRIED,
    SEATED,
    EATING,
    BATHING,
    PLAYING,
}

private const val RUNTIME_PACK = "pets/packs/evelune_pet_assets_v03_runtime.zip"
private const val BASE_PACK = "pets/packs/Evelune-pet-assets-v0.1.zip"
private const val EXPANSION_PACK = "pets/packs/Evelune-pet-expansion-v0.1.zip"

private data class PackedPetAsset(
    val zipAssetPath: String,
    val entryName: String,
)

private fun loadBitmapFromZip(context: Context, asset: PackedPetAsset): Bitmap? = runCatching {
    context.assets.open(asset.zipAssetPath).use { input ->
        ZipInputStream(input.buffered()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (!entry.isDirectory && entry.name == asset.entryName) {
                    return@runCatching BitmapFactory.decodeStream(zip)
                }
                zip.closeEntry()
            }
        }
    }
    null
}.getOrNull()

@Composable
private fun PackedArtwork(
    candidates: List<PackedPetAsset>,
    modifier: Modifier,
    contentScale: ContentScale,
): Boolean {
    val context = LocalContext.current
    val bitmap = remember(candidates) {
        candidates.firstNotNullOfOrNull { loadBitmapFromZip(context, it) }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = modifier,
            contentScale = contentScale,
        )
        return true
    }
    return false
}

@Composable
internal fun PackedRuntimeArtwork(
    fileName: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
): Boolean = PackedArtwork(
    candidates = listOf(PackedPetAsset(RUNTIME_PACK, fileName)),
    modifier = modifier,
    contentScale = contentScale,
)

@Composable
fun ApprovedEnvironmentArtwork(
    environment: PetEnvironmentArtwork,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val entry = when (environment) {
        PetEnvironmentArtwork.PLAYROOM -> "environment_playroom.webp"
        PetEnvironmentArtwork.KITCHEN -> "environment_kitchen.webp"
        PetEnvironmentArtwork.GARDEN -> "environment_garden.webp"
        PetEnvironmentArtwork.SPA -> "environment_spa_hot_tub.webp"
    }

    val loaded = PackedRuntimeArtwork(entry, modifier, contentScale)
    if (!loaded) {
        Canvas(modifier) {
            drawRect(
                when (environment) {
                    PetEnvironmentArtwork.PLAYROOM -> Color(0xFFF1E6FA)
                    PetEnvironmentArtwork.KITCHEN -> Color(0xFFFFEAF1)
                    PetEnvironmentArtwork.GARDEN -> Color(0xFFDDECCF)
                    PetEnvironmentArtwork.SPA -> Color(0xFFE5E4F7)
                }
            )
        }
    }
}

@Composable
fun StorybookPetAvatar(
    species: PetSpecies,
    state: PetVisualState = PetVisualState.IDLE,
    modifier: Modifier = Modifier,
) {
    val loaded = PackedArtwork(
        candidates = petAssetCandidates(species, state),
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )

    if (!loaded) {
        FallbackPetAvatar(species, state, modifier)
    }
}

private fun petAssetCandidates(species: PetSpecies, state: PetVisualState): List<PackedPetAsset> {
    val candidates = mutableListOf<PackedPetAsset>()

    if (species == PetSpecies.PUPPY || species == PetSpecies.KITTEN) {
        runtimeCareEntry(species, state)?.let { candidates += PackedPetAsset(RUNTIME_PACK, it) }
        candidates += PackedPetAsset(BASE_PACK, basePetEntry(species, state))
    } else {
        candidates += PackedPetAsset(EXPANSION_PACK, expansionPetEntry(species, state))
    }

    return candidates
}

private fun runtimeCareEntry(species: PetSpecies, state: PetVisualState): String? {
    val prefix = when (species) {
        PetSpecies.PUPPY -> "mocha"
        PetSpecies.KITTEN -> "lumi"
        else -> return null
    }

    // The recovered Lumi playing frame is a tiny broken export, so use her full happy frame instead.
    if (species == PetSpecies.KITTEN && state == PetVisualState.PLAYING) return null

    val suffix = when (state) {
        PetVisualState.CARRIED -> "carried"
        PetVisualState.SEATED -> "seated"
        PetVisualState.EATING -> "eating"
        PetVisualState.BATHING -> "bathing"
        PetVisualState.PLAYING -> "playing"
        else -> return null
    }
    return "${prefix}_${suffix}.png"
}

private fun basePetEntry(species: PetSpecies, state: PetVisualState): String {
    val prefix = if (species == PetSpecies.PUPPY) "mocha" else "lumi"
    val frame = when (state) {
        PetVisualState.HUNGRY -> "hungry"
        PetVisualState.DIRTY -> "dirty"
        PetVisualState.HAPPY, PetVisualState.EATING, PetVisualState.PLAYING, PetVisualState.BATHING -> "happy"
        PetVisualState.CARRIED, PetVisualState.SEATED, PetVisualState.IDLE -> "idle_front"
    }
    return "evelune_pet_assets_v0.1/characters/$prefix/${prefix}_${frame}.png"
}

private fun expansionPetEntry(species: PetSpecies, state: PetVisualState): String {
    val slug = when (species) {
        PetSpecies.BUNNY -> "bunny"
        PetSpecies.DUCKLING -> "duckling"
        PetSpecies.HEDGEHOG -> "hedgehog"
        PetSpecies.FERRET -> "ferret"
        PetSpecies.OTTER -> "otter"
        PetSpecies.FOX -> "fox"
        PetSpecies.RED_PANDA -> "red_panda"
        PetSpecies.AXOLOTL -> "axolotl"
        PetSpecies.TIGER -> "tiger"
        PetSpecies.DRAGON -> "dragon"
        PetSpecies.PUPPY -> "bunny"
        PetSpecies.KITTEN -> "bunny"
    }
    val frame = when (state) {
        PetVisualState.HUNGRY -> "hungry"
        PetVisualState.DIRTY -> "dirty"
        PetVisualState.HAPPY, PetVisualState.EATING, PetVisualState.PLAYING, PetVisualState.BATHING -> "happy"
        PetVisualState.CARRIED, PetVisualState.SEATED, PetVisualState.IDLE -> "idle_front"
    }
    return "Evelune-pet-expansion-v0.1/$slug/frames/${slug}_${frame}.png"
}

@Composable
private fun FallbackPetAvatar(
    species: PetSpecies,
    state: PetVisualState,
    modifier: Modifier,
) {
    Canvas(modifier) {
        val accent = when (species) {
            PetSpecies.PUPPY -> Color(0xFFC98E67)
            PetSpecies.KITTEN -> Color(0xFFD8B8A9)
            PetSpecies.BUNNY -> Color(0xFFE7C5D0)
            PetSpecies.DUCKLING -> Color(0xFFF0C860)
            PetSpecies.HEDGEHOG -> Color(0xFF9E6F59)
            PetSpecies.FERRET -> Color(0xFF9F806A)
            PetSpecies.OTTER -> Color(0xFF8D644F)
            PetSpecies.FOX -> Color(0xFFF2A06D)
            PetSpecies.RED_PANDA -> Color(0xFFC96F57)
            PetSpecies.AXOLOTL -> Color(0xFFE28FB5)
            PetSpecies.TIGER -> Color(0xFFF2B36D)
            PetSpecies.DRAGON -> Color(0xFF8B79B9)
        }
        val body = when (state) {
            PetVisualState.DIRTY -> Color(0xFFE1D5C7)
            PetVisualState.HAPPY, PetVisualState.PLAYING -> Color(0xFFFFF7EE)
            else -> Color(0xFFFFF3E8)
        }
        drawOval(Color(0xFF6D5262).copy(alpha = .10f), Offset(size.width * .18f, size.height * .84f), Size(size.width * .64f, size.height * .10f))
        drawOval(body, Offset(size.width * .24f, size.height * .40f), Size(size.width * .52f, size.height * .48f))
        drawCircle(body, size.minDimension * .25f, Offset(size.width * .50f, size.height * .36f))
        drawCircle(accent, size.minDimension * .055f, Offset(size.width * .39f, size.height * .35f))
        drawCircle(accent, size.minDimension * .055f, Offset(size.width * .61f, size.height * .35f))
    }
}

@Composable
fun PetEggArtwork(modifier: Modifier = Modifier) {
    val loaded = PackedArtwork(
        candidates = listOf(
            PackedPetAsset(BASE_PACK, "evelune_pet_assets_v0.1/egg/egg_stage_01_pristine.png")
        ),
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )

    if (!loaded) {
        Canvas(modifier) {
            drawOval(Color(0xFFFFF7EC), Offset(size.width * .24f, size.height * .08f), Size(size.width * .52f, size.height * .72f))
            drawCircle(Color(0xFFF5B3C6), size.width * .045f, Offset(size.width * .40f, size.height * .32f))
            drawCircle(Color(0xFFD7C4EE), size.width * .035f, Offset(size.width * .61f, size.height * .43f))
        }
    }
}

@Composable
fun PetCareArtwork(kind: String, modifier: Modifier = Modifier) {
    val fileName = when (kind.lowercase()) {
        "feed" -> "food_kibble.png"
        "clean" -> "cleaning_sponge.png"
        else -> "toy_ball.png"
    }
    val loaded = PackedRuntimeArtwork(fileName, modifier, ContentScale.Fit)
    if (!loaded) {
        Canvas(modifier) {
            val color = when (kind.lowercase()) {
                "feed" -> Color(0xFFC98B8E)
                "clean" -> Color(0xFFFFD78B)
                else -> Color(0xFF9BD6C8)
            }
            drawCircle(color, size.minDimension * .32f, Offset(size.width * .5f, size.height * .5f))
        }
    }
}

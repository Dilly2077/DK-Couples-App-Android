package com.dk.together.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dk.together.pets.engine.AndroidPetEngineRepository
import com.dk.together.pets.engine.HatchSnapshot
import com.dk.together.pets.engine.PetEngine
import com.dk.together.pets.engine.PetSpecies
import com.dk.together.ui.theme.EveluneBackground
import com.dk.together.ui.theme.EveluneCard
import com.dk.together.ui.theme.EveluneInk
import com.dk.together.ui.theme.EveluneMuted
import com.dk.together.ui.theme.EveluneRose
import com.dk.together.ui.theme.EveluneRoseDeep
import com.dk.together.ui.theme.EveluneRosePale
import kotlinx.coroutines.delay
import kotlin.math.ceil

private enum class PetPage { WORLD, MEADOWS, HATCH, KITCHEN, GARDEN_CLEAN, PLAYROOM, SHOP }

@Composable
fun PetSectionScreen(modifier: Modifier = Modifier) {
    var page by remember { mutableStateOf(PetPage.WORLD) }
    when (page) {
        PetPage.WORLD -> PetWorldScreen(
            modifier,
            onExplore = { page = PetPage.MEADOWS },
            onHatch = { page = PetPage.HATCH },
            onKitchen = { page = PetPage.KITCHEN },
            onClean = { page = PetPage.GARDEN_CLEAN },
            onPlay = { page = PetPage.PLAYROOM },
            onShop = { page = PetPage.SHOP },
        )
        PetPage.MEADOWS -> MeadowExploreScreen(
            modifier,
            onBack = { page = PetPage.WORLD },
            onHatch = { page = PetPage.HATCH },
            onKitchen = { page = PetPage.KITCHEN },
            onGarden = { page = PetPage.GARDEN_CLEAN },
            onPlayroom = { page = PetPage.PLAYROOM },
            onShop = { page = PetPage.SHOP },
        )
        PetPage.HATCH -> HatchEggScreen({ page = PetPage.WORLD }, modifier)
        PetPage.KITCHEN -> PersistentKitchenFeedingScreen({ page = PetPage.WORLD }, modifier)
        PetPage.GARDEN_CLEAN -> PersistentGardenCleaningScreen({ page = PetPage.WORLD }, modifier)
        PetPage.PLAYROOM -> PersistentPlayroomScreen({ page = PetPage.WORLD }, modifier)
        PetPage.SHOP -> PersistentPetShopScreen({ page = PetPage.WORLD }, modifier)
    }
}

@Composable
private fun PetWorldScreen(
    modifier: Modifier,
    onExplore: () -> Unit,
    onHatch: () -> Unit,
    onKitchen: () -> Unit,
    onClean: () -> Unit,
    onPlay: () -> Unit,
    onShop: () -> Unit,
) {
    Box(modifier.fillMaxSize().background(EveluneBackground)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(18.dp, 16.dp, 18.dp, 30.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { PetWorldHero() }
            item { PetProgressionOverviewCard(onShop = onShop) }
            item { HatchShortcut(onHatch) }
            item { MeadowWorldCard(onExplore) }
            item { CareActions(onKitchen, onClean, onPlay) }
            item { PetFooterBanner() }
        }
    }
}

@Composable
private fun PetWorldHero() {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Text("Pet World", color = EveluneRoseDeep, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 38.sp)
            Surface(shape = CircleShape, color = EveluneRosePale) {
                Box(Modifier.size(38.dp), contentAlignment = Alignment.Center) {
                    StorybookPetAvatar(PetSpecies.PUPPY, PetVisualState.HAPPY, Modifier.size(34.dp))
                }
            }
        }
        Text("Explore, play and discover together. ♥", color = EveluneMuted, fontSize = 16.sp)
        Text("A brighter world\nfor your little family.", color = EveluneInk, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 31.sp, modifier = Modifier.padding(top = 12.dp))
        Text("Care. Play. Grow together.", color = EveluneMuted, fontSize = 15.sp)
    }
}

@Composable
private fun HatchShortcut(onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), color = Color(0xFFF7E9FA), shape = RoundedCornerShape(25.dp), shadowElevation = 2.dp) {
        Row(Modifier.padding(16.dp, 13.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = CircleShape, color = Color(0xFFFFF8FB)) { Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) { PetEggArtwork(Modifier.size(44.dp)) } }
            Column(Modifier.weight(1f)) {
                Text("Hatch a new egg", color = EveluneInk, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                Text("Start or check your shared egg.", color = EveluneMuted, fontSize = 13.sp)
            }
            Text("›", color = EveluneRoseDeep, fontSize = 31.sp)
        }
    }
}

@Composable
private fun MeadowWorldCard(onExplore: () -> Unit) {
    Surface(onClick = onExplore, modifier = Modifier.fillMaxWidth(), color = Color(0xFFFFFCFF), shape = RoundedCornerShape(28.dp), shadowElevation = 3.dp) {
        Column {
            Row(Modifier.fillMaxWidth().padding(16.dp, 13.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = Color(0xFFF0E5FA)) { Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) { StorybookPetAvatar(PetSpecies.BUNNY, PetVisualState.HAPPY, Modifier.size(37.dp)) } }
                Column(Modifier.weight(1f).padding(start = 10.dp)) {
                    Text("Evelune Meadows", color = EveluneInk, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                    Text("A safe, cozy world for your pets.", color = EveluneMuted, fontSize = 13.sp)
                }
                Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFFF4E8FA)) {
                    Text("Explore  ›", color = EveluneRoseDeep, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(13.dp, 9.dp))
                }
            }
            Box(Modifier.fillMaxWidth().height(365.dp).clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))) {
                ApprovedEnvironmentArtwork(PetEnvironmentArtwork.GARDEN, Modifier.fillMaxSize())
                PetPreview("Hungry", "Mocha", "Lv. 3", PetSpecies.PUPPY, PetVisualState.HUNGRY, Modifier.align(Alignment.BottomStart).padding(start = 36.dp, bottom = 38.dp))
                PetPreview("Bath?", "Lumi", "Lv. 2", PetSpecies.KITTEN, PetVisualState.DIRTY, Modifier.align(Alignment.BottomEnd).padding(end = 42.dp, bottom = 38.dp))
            }
        }
    }
}

@Composable
private fun PetPreview(need: String, name: String, level: String, species: PetSpecies, state: PetVisualState, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = .96f), shadowElevation = 2.dp) { Text(need, color = EveluneInk, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(12.dp, 7.dp)) }
        StorybookPetAvatar(species, state, Modifier.size(104.dp))
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = .94f)) {
            Column(Modifier.padding(14.dp, 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(name, color = EveluneInk, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(level, color = EveluneMuted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun CareActions(onFeed: () -> Unit, onClean: () -> Unit, onPlay: () -> Unit) {
    Surface(Modifier.fillMaxWidth(), color = EveluneCard, shape = RoundedCornerShape(27.dp), shadowElevation = 2.dp) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Give them some care ♥", color = EveluneInk, fontFamily = FontFamily.Serif, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Their faces and thought bubbles tell you what they need.", color = EveluneMuted, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CareAction("feed", "Feed", "Kitchen", Modifier.weight(1f), onFeed)
                CareAction("clean", "Clean", "Garden spa", Modifier.weight(1f), onClean)
                CareAction("play", "Play", "Playroom", Modifier.weight(1f), onPlay)
            }
        }
    }
}

@Composable
private fun CareAction(kind: String, title: String, subtitle: String, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier, color = Color(0xFFF7EDF9), shape = RoundedCornerShape(21.dp)) {
        Column(Modifier.padding(12.dp)) {
            PetCareArtwork(kind, Modifier.size(38.dp))
            Text(title, color = EveluneInk, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = EveluneMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun PetFooterBanner() {
    Surface(Modifier.fillMaxWidth(), color = Color(0xFFE8D8F5), shape = RoundedCornerShape(24.dp)) {
        Row(Modifier.padding(17.dp, 15.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("☾", color = EveluneRoseDeep, fontSize = 30.sp)
            Text("  Caring today builds brighter tomorrows.", color = EveluneInk, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Text("♥", color = EveluneRose, fontSize = 22.sp)
        }
    }
}

@Composable
private fun MeadowExploreScreen(
    modifier: Modifier,
    onBack: () -> Unit,
    onHatch: () -> Unit,
    onKitchen: () -> Unit,
    onGarden: () -> Unit,
    onPlayroom: () -> Unit,
    onShop: () -> Unit,
) {
    Box(modifier.fillMaxSize().background(EveluneBackground)) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { PetScreenHeader("Evelune Meadows", "Choose somewhere to go.", onBack) }
            item {
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), color = Color.White, shadowElevation = 2.dp) {
                    Box(Modifier.fillMaxWidth().height(225.dp)) {
                        ApprovedEnvironmentArtwork(PetEnvironmentArtwork.GARDEN, Modifier.fillMaxSize())
                        Surface(modifier = Modifier.align(Alignment.BottomStart).padding(14.dp), shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = .90f)) {
                            Text("Your shared world", color = EveluneInk, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(14.dp, 9.dp))
                        }
                    }
                }
            }
            item { DestinationCard("Kitchen", "Feed a pet and use your food inventory.", PetEnvironmentArtwork.KITCHEN, onKitchen) }
            item { DestinationCard("Garden Spa", "Drag a pet into the hot tub and scrub them clean.", PetEnvironmentArtwork.SPA, onGarden) }
            item { DestinationCard("Playroom", "Take a pet to a toy and complete a play activity.", PetEnvironmentArtwork.PLAYROOM, onPlayroom) }
            item { SimpleActionCard("Hatchery", "Start or check an egg incubation.", onHatch) }
            item { SimpleActionCard("Pet Shop", "Spend Pet Coins on food and supplies.", onShop) }
        }
    }
}

@Composable
private fun DestinationCard(title: String, subtitle: String, art: PetEnvironmentArtwork, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(25.dp), color = Color.White, shadowElevation = 2.dp) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(112.dp).clip(RoundedCornerShape(20.dp))) { ApprovedEnvironmentArtwork(art, Modifier.fillMaxSize()) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = EveluneInk, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 21.sp)
                Text(subtitle, color = EveluneMuted, fontSize = 12.sp, lineHeight = 16.sp)
                Text("Open  ›", color = EveluneRoseDeep, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun SimpleActionCard(title: String, subtitle: String, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), color = EveluneCard, shape = RoundedCornerShape(24.dp), shadowElevation = 1.dp) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = EveluneInk, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(subtitle, color = EveluneMuted, fontSize = 12.sp)
            }
            Text("›", color = EveluneRoseDeep, fontSize = 30.sp)
        }
    }
}

@Composable
private fun HatchEggScreen(onBack: () -> Unit, modifier: Modifier) {
    val context = LocalContext.current
    val engine = remember { PetEngine(AndroidPetEngineRepository(context)) }
    var snapshot by remember { mutableStateOf(engine.activeHatch(System.currentTimeMillis())) }
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(snapshot?.sessionId) {
        while (true) {
            snapshot = engine.activeHatch(System.currentTimeMillis())
            delay(1_000)
        }
    }

    Box(modifier.fillMaxSize().background(EveluneBackground)) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp, 12.dp, 18.dp, 30.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { PetScreenHeader("Hatchery", "A shared companion for both of you. ♥", onBack) }
            item {
                Surface(Modifier.fillMaxWidth(), color = Color(0xFFFFFBFF), shape = RoundedCornerShape(30.dp), shadowElevation = 2.dp) {
                    Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        PetEggArtwork(Modifier.size(190.dp))
                        if (snapshot == null) {
                            Text("No egg is incubating", color = EveluneInk, fontFamily = FontFamily.Serif, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                            Text("Start an egg and the pet species will be selected by the hatch system.", color = EveluneMuted, fontSize = 13.sp, textAlign = TextAlign.Center)
                            Surface(onClick = {
                                runCatching { engine.startHatch(System.currentTimeMillis()) }
                                    .onSuccess { snapshot = it; message = "Incubation started." }
                                    .onFailure { message = it.message ?: "Could not start incubation." }
                            }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = EveluneRoseDeep) {
                                Text("Start incubation", color = Color.White, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 14.dp))
                            }
                        } else {
                            ActiveHatchContent(requireNotNull(snapshot)) {
                                runCatching { engine.claimCompletedHatch(System.currentTimeMillis()) }
                                    .onSuccess { pet -> message = "${pet.species.prettyName()} hatched successfully ♥"; snapshot = null }
                                    .onFailure { message = it.message ?: "The egg is not ready yet." }
                            }
                        }
                    }
                }
            }
            message?.let { text -> item { Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = EveluneRosePale) { Text(text, color = EveluneRoseDeep, modifier = Modifier.padding(14.dp), fontSize = 13.sp) } } }
        }
    }
}

@Composable
private fun ActiveHatchContent(snapshot: HatchSnapshot, onClaim: () -> Unit) {
    val fraction = snapshot.progress.toFloat().coerceIn(0f, 1f)
    val mins = ceil(snapshot.remainingMs / 60_000.0).toInt().coerceAtLeast(0)
    Text(snapshot.species.prettyName(), color = EveluneInk, fontFamily = FontFamily.Serif, fontSize = 25.sp, fontWeight = FontWeight.Bold)
    Text(if (snapshot.complete) "Ready to hatch!" else "${snapshot.stage.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }} • about $mins min remaining", color = EveluneMuted, fontSize = 13.sp, textAlign = TextAlign.Center)
    Box(Modifier.fillMaxWidth().height(12.dp).background(Color(0xFFEDE2F4), RoundedCornerShape(20.dp))) { Box(Modifier.fillMaxWidth(fraction).height(12.dp).background(EveluneRose, RoundedCornerShape(20.dp))) }
    Text("${(snapshot.progress * 100).toInt()}%", color = EveluneRoseDeep, fontWeight = FontWeight.Bold)
    if (snapshot.complete) Surface(onClick = onClaim, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = EveluneRoseDeep) { Text("Hatch now", color = Color.White, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 14.dp)) }
}

private fun PetSpecies.prettyName(): String = name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }

@Composable
private fun PetScreenHeader(title: String, subtitle: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
        Surface(onClick = onBack, shape = CircleShape, color = Color.White.copy(alpha = .88f)) { Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = EveluneRoseDeep) } }
        Column(Modifier.weight(1f)) {
            Text(title, color = EveluneRoseDeep, fontFamily = FontFamily.Serif, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = EveluneMuted, fontSize = 12.sp)
        }
    }
}

@Composable
fun EveluneMoreHub(onExplore: () -> Unit, onUs: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().background(EveluneBackground)) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { Column { Text("More", color = EveluneRoseDeep, fontFamily = FontFamily.Serif, fontSize = 38.sp, fontWeight = FontWeight.Bold); Text("Activities, profile and settings.", color = EveluneMuted, fontSize = 15.sp) } }
            item { MoreHubCard(Icons.Filled.Search, "Explore activities", "Questions, cards and games", onExplore) }
            item { MoreHubCard(Icons.Filled.Settings, "Us & settings", "Partner names, testing side and app settings", onUs) }
        }
    }
}

@Composable
private fun MoreHubCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), color = EveluneCard, shape = RoundedCornerShape(25.dp), shadowElevation = 2.dp) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
            Surface(shape = CircleShape, color = EveluneRosePale) { Box(Modifier.size(50.dp), contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = EveluneRoseDeep) } }
            Column(Modifier.weight(1f)) { Text(title, color = EveluneInk, fontSize = 18.sp, fontWeight = FontWeight.SemiBold); Text(subtitle, color = EveluneMuted, fontSize = 12.sp) }
            Text("›", color = EveluneRoseDeep, fontSize = 31.sp)
        }
    }
}

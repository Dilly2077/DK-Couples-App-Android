package com.dk.together.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dk.together.pets.engine.PetSpecies
import com.dk.together.ui.theme.EveluneBackground
import com.dk.together.ui.theme.EveluneCard
import com.dk.together.ui.theme.EveluneInk
import com.dk.together.ui.theme.EveluneMuted
import com.dk.together.ui.theme.EveluneRose
import com.dk.together.ui.theme.EveluneRoseDeep
import com.dk.together.ui.theme.EveluneRosePale

private enum class PetPage { WORLD, HATCH, KITCHEN, GARDEN_CLEAN, PLAYROOM, SHOP }
private enum class PetKind { DOG_HUNGRY, CAT_DIRTY }

@Composable
fun PetSectionScreen(modifier: Modifier = Modifier) {
    var page by remember { mutableStateOf(PetPage.WORLD) }
    when (page) {
        PetPage.WORLD -> PetWorldScreen(
            modifier = modifier,
            onHatch = { page = PetPage.HATCH },
            onKitchen = { page = PetPage.KITCHEN },
            onClean = { page = PetPage.GARDEN_CLEAN },
            onPlay = { page = PetPage.PLAYROOM },
            onShop = { page = PetPage.SHOP },
        )
        PetPage.HATCH -> HatchEggScreen(
            modifier = modifier,
            onBack = { page = PetPage.WORLD },
        )
        PetPage.KITCHEN -> PersistentKitchenFeedingScreen(
            modifier = modifier,
            onBack = { page = PetPage.WORLD },
        )
        PetPage.GARDEN_CLEAN -> PersistentGardenCleaningScreen(
            modifier = modifier,
            onBack = { page = PetPage.WORLD },
        )
        PetPage.PLAYROOM -> PersistentPlayroomScreen(
            modifier = modifier,
            onBack = { page = PetPage.WORLD },
        )
        PetPage.SHOP -> PersistentPetShopScreen(
            modifier = modifier,
            onBack = { page = PetPage.WORLD },
        )
    }
}

@Composable
private fun PetWorldScreen(
    onHatch: () -> Unit,
    onKitchen: () -> Unit,
    onClean: () -> Unit,
    onPlay: () -> Unit,
    onShop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize().background(EveluneBackground)) {
        PetBackdrop()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { PetWorldHero() }
            item { PetProgressionOverviewCard(onShop = onShop) }
            item { HatchShortcut(onHatch) }
            item { MeadowWorldCard() }
            item { CareActions(onFeed = onKitchen, onClean = onClean, onPlay = onPlay) }
            item { PetFooterBanner() }
        }
    }
}

@Composable
private fun PetWorldHero() {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Text(
                "Pet World",
                color = EveluneRoseDeep,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 38.sp,
                lineHeight = 40.sp,
            )
            Surface(shape = CircleShape, color = EveluneRosePale) {
                Box(Modifier.size(38.dp), contentAlignment = Alignment.Center) {
                    StorybookPetAvatar(PetSpecies.PUPPY, PetVisualState.HAPPY, Modifier.size(34.dp))
                }
            }
        }
        Text("Explore, play and discover together. ♥", color = EveluneMuted, fontSize = 16.sp)
        Text(
            "A brighter world\nfor your little family.",
            color = EveluneInk,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 31.sp,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text("Care. Play. Grow together.", color = EveluneMuted, fontSize = 15.sp)
    }
}

@Composable
private fun HatchShortcut(onHatch: () -> Unit) {
    Surface(
        onClick = onHatch,
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF7E9FA),
        shape = RoundedCornerShape(25.dp),
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(shape = CircleShape, color = Color(0xFFFFF8FB)) {
                Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) {
                    PetEggArtwork(Modifier.size(44.dp))
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Hatch a new egg", color = EveluneInk, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                Text("Choose an egg and follow its hatching stages.", color = EveluneMuted, fontSize = 13.sp)
            }
            Text("›", color = EveluneRoseDeep, fontSize = 31.sp)
        }
    }
}

@Composable
private fun MeadowWorldCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFFFCFF),
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 3.dp,
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(shape = CircleShape, color = Color(0xFFF0E5FA)) {
                    Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                        StorybookPetAvatar(PetSpecies.BUNNY, PetVisualState.HAPPY, Modifier.size(37.dp))
                    }
                }
                Column(Modifier.weight(1f).padding(start = 10.dp)) {
                    Text("Evelune Meadows", color = EveluneInk, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                    Text("A safe, cozy world for your pets.", color = EveluneMuted, fontSize = 13.sp)
                }
                Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFFF4E8FA)) {
                    Text("Explore  ›", color = EveluneRoseDeep, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp))
                }
            }
            Box(
                modifier = Modifier.fillMaxWidth().height(365.dp).clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)),
            ) {
                MeadowCanvas(Modifier.fillMaxSize())

                Column(
                    modifier = Modifier.align(Alignment.BottomStart).padding(start = 40.dp, bottom = 44.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    NeedBubble("Hungry")
                    PetSprite(PetKind.DOG_HUNGRY)
                    PetName("Mocha", "Lv. 3")
                }

                Column(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 46.dp, bottom = 44.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    NeedBubble("Bath?")
                    PetSprite(PetKind.CAT_DIRTY)
                    PetName("Lumi", "Lv. 2")
                }
            }
        }
    }
}

@Composable
private fun NeedBubble(text: String) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = .96f),
        shadowElevation = 2.dp,
    ) {
        Text(text, color = EveluneInk, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp))
    }
}

@Composable
private fun PetName(name: String, level: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = .94f)) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(name, color = EveluneInk, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(level, color = EveluneMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun PetSprite(kind: PetKind) {
    when (kind) {
        PetKind.DOG_HUNGRY -> StorybookPetAvatar(
            species = PetSpecies.PUPPY,
            state = PetVisualState.HUNGRY,
            modifier = Modifier.size(104.dp),
        )
        PetKind.CAT_DIRTY -> StorybookPetAvatar(
            species = PetSpecies.KITTEN,
            state = PetVisualState.DIRTY,
            modifier = Modifier.size(104.dp),
        )
    }
}

@Composable
private fun MeadowCanvas(modifier: Modifier = Modifier) {
    ApprovedEnvironmentArtwork(
        environment = PetEnvironmentArtwork.GARDEN,
        modifier = modifier,
    )
}

@Composable
private fun CareActions(onFeed: () -> Unit, onClean: () -> Unit, onPlay: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = EveluneCard,
        shape = RoundedCornerShape(27.dp),
        shadowElevation = 2.dp,
    ) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Give them some care ♥", color = EveluneInk, fontFamily = FontFamily.Serif, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Their faces and thought bubbles tell you what they need.", color = EveluneMuted, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CareAction("feed", "Feed", "Take them to the Kitchen", Modifier.weight(1f), onClick = onFeed)
                CareAction("clean", "Clean", "Take them to the Garden", Modifier.weight(1f), onClick = onClean)
                CareAction("play", "Play", "Take them to the Playroom", Modifier.weight(1f), onClick = onPlay)
            }
        }
    }
}

@Composable
private fun CareAction(
    kind: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = modifier,
        color = Color(0xFFF7EDF9),
        shape = RoundedCornerShape(21.dp),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            PetCareArtwork(kind, Modifier.size(38.dp))
            Text(title, color = EveluneInk, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = EveluneMuted, fontSize = 11.sp, lineHeight = 14.sp)
        }
    }
}

@Composable
private fun PetFooterBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFE8D8F5),
        shape = RoundedCornerShape(24.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 17.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("☾", color = EveluneRoseDeep, fontSize = 30.sp)
            Text("  Caring today builds brighter tomorrows.", color = EveluneInk, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Text("♥", color = EveluneRose, fontSize = 22.sp)
        }
    }
}

@Composable
private fun HatchEggScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize().background(EveluneBackground)) {
        PetBackdrop()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(onClick = onBack, shape = CircleShape, color = Color.White.copy(alpha = .76f)) {
                        Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = EveluneRoseDeep)
                        }
                    }
                    Column {
                        Text("Hatch Egg", color = EveluneRoseDeep, fontFamily = FontFamily.Serif, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                        Text("A shared companion for both of you. ♥", color = EveluneMuted, fontSize = 14.sp)
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Hatch your shared\ncompanion ♥", color = EveluneInk, fontFamily = FontFamily.Serif, fontSize = 30.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold)
                    Text("A little love today, a brighter tomorrow.", color = EveluneMuted, fontSize = 15.sp)
                }
            }
            item { EggArtwork() }
            item { HatchStatusCard() }
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFFFBFF),
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 1.dp,
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(shape = CircleShape, color = EveluneRosePale) {
                            Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) { Text("♥", color = EveluneRoseDeep, fontSize = 22.sp) }
                        }
                        Column(Modifier.weight(1f)) {
                            Text("This pet belongs to both of you", color = EveluneInk, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                            Text("Species-specific hatch timers will be added when pet types are finalised.", color = EveluneMuted, fontSize = 12.sp, lineHeight = 16.sp)
                        }
                    }
                }
            }
            item {
                Surface(onClick = onBack, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), color = EveluneRoseDeep) {
                    Text("Back to Pet World", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.padding(vertical = 15.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun EggArtwork() {
    Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.size(270.dp),
            shape = CircleShape,
            color = Color(0xFFF6CFF0).copy(alpha = .30f),
        ) {
            Box(contentAlignment = Alignment.Center) {
                PetEggArtwork(Modifier.size(210.dp))
            }
        }
    }
}

@Composable
private fun HatchStatusCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFFFCFF),
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 2.dp,
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            Text("Getting ready to hatch...", color = EveluneInk, fontFamily = FontFamily.Serif, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Your little companion can’t wait to meet you both.", color = EveluneMuted, fontSize = 13.sp)
            Box(Modifier.fillMaxWidth().height(12.dp).background(Color(0xFFEDE2F4), RoundedCornerShape(20.dp))) {
                Box(Modifier.fillMaxWidth(.68f).height(12.dp).background(EveluneRose, RoundedCornerShape(20.dp)))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HatchStage("✓", "Egg chosen", "Ready", Modifier.weight(1f), true)
                HatchStage("•••", "Incubating", "Current", Modifier.weight(1f), true)
                HatchStage("○", "Ready to hatch", "Later", Modifier.weight(1f), false)
            }
        }
    }
}

@Composable
private fun HatchStage(icon: String, title: String, subtitle: String, modifier: Modifier, active: Boolean) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Surface(shape = CircleShape, color = if (active) EveluneRosePale else Color(0xFFF2EEF4)) {
            Box(Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                Text(icon, color = if (active) EveluneRoseDeep else EveluneMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text(title, color = EveluneInk, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, lineHeight = 15.sp)
        Text(subtitle, color = EveluneMuted, fontSize = 10.sp)
    }
}

@Composable
private fun PetBackdrop() {
    Canvas(Modifier.fillMaxSize()) {
        drawCircle(Color(0xFFF0D6F5).copy(alpha = .55f), radius = size.width * .65f, center = Offset(size.width * .96f, size.height * .03f))
        drawCircle(Color(0xFFFFE6EF).copy(alpha = .55f), radius = size.width * .44f, center = Offset(size.width * .05f, size.height * .45f))
    }
}

@Composable
fun EveluneMoreHub(
    onExplore: () -> Unit,
    onUs: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize().background(EveluneBackground)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("More", color = EveluneRoseDeep, fontFamily = FontFamily.Serif, fontSize = 38.sp, fontWeight = FontWeight.Bold)
                    Text("Activities, profile and settings.", color = EveluneMuted, fontSize = 15.sp)
                }
            }
            item { MoreHubCard(Icons.Filled.Search, "Explore activities", "Questions, cards and games", onExplore) }
            item { MoreHubCard(Icons.Filled.Settings, "Us & settings", "Partner names, testing side and app settings", onUs) }
        }
    }
}

@Composable
private fun MoreHubCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), color = EveluneCard, shape = RoundedCornerShape(25.dp), shadowElevation = 2.dp) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
            Surface(shape = CircleShape, color = EveluneRosePale) {
                Box(Modifier.size(50.dp), contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = EveluneRoseDeep) }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, color = EveluneInk, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = EveluneMuted, fontSize = 12.sp)
            }
            Text("›", color = EveluneRoseDeep, fontSize = 31.sp)
        }
    }
}

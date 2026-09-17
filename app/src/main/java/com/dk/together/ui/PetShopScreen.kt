package com.dk.together.ui

import android.content.Context
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dk.together.pets.engine.AndroidPetEngineRepository
import com.dk.together.pets.engine.PetEngine
import com.dk.together.pets.feeding.AndroidFeedingRepository
import com.dk.together.pets.feeding.FoodId
import com.dk.together.pets.shop.PetShopEngine
import com.dk.together.pets.shop.PetShopItem
import com.dk.together.pets.shop.PurchaseDecision
import com.dk.together.rewards.AndroidRewardRepository
import com.dk.together.rewards.LevelProgress
import com.dk.together.rewards.PetBondProgress
import com.dk.together.rewards.RewardBalance
import com.dk.together.rewards.RewardEngine
import com.dk.together.ui.theme.EveluneBackground
import com.dk.together.ui.theme.EveluneCard
import com.dk.together.ui.theme.EveluneInk
import com.dk.together.ui.theme.EveluneMuted
import com.dk.together.ui.theme.EveluneRose
import com.dk.together.ui.theme.EveluneRoseDeep
import com.dk.together.ui.theme.EveluneRosePale
import java.util.UUID

@Composable
fun PersistentPetShopScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val rewardEngine = remember { RewardEngine(AndroidRewardRepository(context)) }
    val feedingRepository = remember { AndroidFeedingRepository(context) }
    val shop = remember { PetShopEngine(rewardEngine, feedingRepository) }
    val petEngine = remember { PetEngine(AndroidPetEngineRepository(context)) }

    var balance by remember { mutableStateOf(RewardBalance()) }
    var globalProgress by remember { mutableStateOf(rewardEngine.globalProgress()) }
    var petProgress by remember { mutableStateOf<PetBondProgress?>(null) }
    var inventory by remember { mutableStateOf<Map<FoodId, Int>>(emptyMap()) }
    var message by remember { mutableStateOf("Pet Coins are earned by meaningful care and activities.") }

    fun refresh() {
        balance = rewardEngine.balance()
        globalProgress = rewardEngine.globalProgress()
        val pet = petEngine.pets(System.currentTimeMillis()).firstOrNull()
        petProgress = pet?.let { rewardEngine.petProgress(it.id) }
        inventory = shop.inventory()
    }

    LaunchedEffect(Unit) {
        shop.ensureInventoryInitialized()
        refresh()
    }

    Box(modifier.fillMaxSize().background(EveluneBackground)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { PetShopHeader(onBack = onBack) }
            item {
                ProgressionPanel(
                    balance = balance,
                    globalProgress = globalProgress,
                    petProgress = petProgress,
                    compact = false,
                )
            }
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFFF9FC),
                ) {
                    Text(
                        message,
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 12.dp),
                        color = EveluneMuted,
                        fontSize = 12.sp,
                    )
                }
            }
            items(shop.catalog(), key = { it.food.id.name }) { item ->
                PetShopItemCard(
                    item = item,
                    owned = inventory[item.food.id] ?: 0,
                    currentCoins = balance.petCoins,
                    onBuy = {
                        val result = shop.purchase(
                            foodId = item.food.id,
                            purchaseId = UUID.randomUUID().toString(),
                            nowEpochMs = System.currentTimeMillis(),
                        )
                        message = when (result.decision) {
                            PurchaseDecision.PURCHASED ->
                                "Added ${result.item.food.displayName} to the Kitchen inventory."
                            PurchaseDecision.ALREADY_PURCHASED ->
                                "That purchase was already completed. Nothing was charged twice."
                            PurchaseDecision.INSUFFICIENT_COINS ->
                                "Not enough Pet Coins yet. Care for your pets or complete Evelune activities to earn more."
                        }
                        refresh()
                    },
                )
            }
        }
    }
}

@Composable
fun PetProgressionOverviewCard(
    onShop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val rewardEngine = remember { RewardEngine(AndroidRewardRepository(context)) }
    val petEngine = remember { PetEngine(AndroidPetEngineRepository(context)) }
    val balance = rewardEngine.balance()
    val global = rewardEngine.globalProgress()
    val pet = petEngine.pets(System.currentTimeMillis()).firstOrNull()
    val bond = pet?.let { rewardEngine.petProgress(it.id) }
    val seen = remember { context.getSharedPreferences("evelune_progression_seen", Context.MODE_PRIVATE) }
    var globalLevelsGained by remember { mutableStateOf(0) }
    var petLevelsGained by remember { mutableStateOf(0) }

    LaunchedEffect(global.level, pet?.id, bond?.progress?.level) {
        val previousGlobal = seen.getInt("global_level", global.level)
        globalLevelsGained = (global.level - previousGlobal).coerceAtLeast(0)
        seen.edit().putInt("global_level", global.level).apply()

        if (pet != null && bond != null) {
            val key = "pet_level_${pet.id}"
            val previousPet = seen.getInt(key, bond.progress.level)
            petLevelsGained = (bond.progress.level - previousPet).coerceAtLeast(0)
            seen.edit().putInt(key, bond.progress.level).apply()
        } else {
            petLevelsGained = 0
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RewardLevelUpNotice(
            globalLevelsGained = globalLevelsGained,
            petLevelsGained = petLevelsGained,
        )
        Surface(
            onClick = onShop,
            modifier = Modifier.fillMaxWidth(),
            color = EveluneCard,
            shape = RoundedCornerShape(26.dp),
            shadowElevation = 2.dp,
        ) {
            Column(
                modifier = Modifier.padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Your Evelune journey", color = EveluneInk, fontFamily = FontFamily.Serif, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        Text("XP, pet bond and your shared Pet Coin wallet", color = EveluneMuted, fontSize = 11.sp)
                    }
                    Surface(shape = RoundedCornerShape(18.dp), color = EveluneRosePale) {
                        Text("Shop  ›", color = EveluneRoseDeep, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                    }
                }
                ProgressionPanel(balance, global, bond, compact = true)
            }
        }
    }
}

@Composable
private fun PetShopHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(onClick = onBack, shape = CircleShape, color = Color.White.copy(alpha = .88f)) {
            Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = EveluneRoseDeep)
            }
        }
        Column {
            Text("Pet Shop", color = EveluneRoseDeep, fontFamily = FontFamily.Serif, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text("Spend Pet Coins on food for your shared companions.", color = EveluneMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ProgressionPanel(
    balance: RewardBalance,
    globalProgress: LevelProgress,
    petProgress: PetBondProgress?,
    compact: Boolean,
) {
    val content: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatPill("✦", "Evelune", "Lv. ${globalProgress.level}", Modifier.weight(1f))
                StatPill("●", "Pet Coins", balance.petCoins.toString(), Modifier.weight(1f))
                StatPill("♥", "Pet Bond", petProgress?.let { "Lv. ${it.progress.level}" } ?: "—", Modifier.weight(1f))
            }
            XpProgressRow("Evelune XP", globalProgress)
            if (!compact && petProgress != null) {
                XpProgressRow("Pet Bond XP", petProgress.progress)
            }
        }
    }

    if (compact) {
        content()
    } else {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = EveluneCard,
            shape = RoundedCornerShape(25.dp),
            shadowElevation = 2.dp,
        ) {
            Box(Modifier.padding(15.dp)) { content() }
        }
    }
}

@Composable
private fun StatPill(
    icon: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(18.dp), color = Color(0xFFF7EDF9)) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 9.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(icon, fontSize = 18.sp, color = EveluneRoseDeep)
            Text(value, color = EveluneInk, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(label, color = EveluneMuted, fontSize = 9.sp)
        }
    }
}

@Composable
private fun XpProgressRow(title: String, progress: LevelProgress) {
    val fraction = if (progress.xpForNextLevel <= 0) 1f else {
        (progress.xpIntoLevel.toFloat() / progress.xpForNextLevel.toFloat()).coerceIn(0f, 1f)
    }
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = EveluneInk, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text(
                if (progress.xpForNextLevel <= 0) "Max" else "${progress.xpIntoLevel}/${progress.xpForNextLevel}",
                color = EveluneMuted,
                fontSize = 10.sp,
            )
        }
        Box(Modifier.fillMaxWidth().height(8.dp).background(Color(0xFFEDE2F4), RoundedCornerShape(20.dp))) {
            Box(Modifier.fillMaxWidth(fraction).height(8.dp).background(EveluneRose, RoundedCornerShape(20.dp)))
        }
    }
}

@Composable
private fun PetShopItemCard(
    item: PetShopItem,
    owned: Int,
    currentCoins: Int,
    onBuy: () -> Unit,
) {
    val canAfford = currentCoins >= item.coinCost
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFFFCFF),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFF7EDF9)) {
                Box(Modifier.size(62.dp), contentAlignment = Alignment.Center) {
                    PetFoodArtwork(item.food.id, Modifier.size(50.dp))
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(item.food.displayName, color = EveluneInk, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Text("Owned: $owned  •  +${item.quantity} per purchase", color = EveluneMuted, fontSize = 10.sp)
                Text("${item.food.hungerReduction.toInt()} satiation", color = EveluneMuted, fontSize = 10.sp)
            }
            Surface(
                onClick = onBuy,
                shape = RoundedCornerShape(18.dp),
                color = if (canAfford) EveluneRoseDeep else Color(0xFFE9E1E9),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("●", color = if (canAfford) Color.White else EveluneMuted, fontSize = 12.sp)
                    Text(item.coinCost.toString(), color = if (canAfford) Color.White else EveluneMuted, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun RewardLevelUpNotice(
    globalLevelsGained: Int,
    petLevelsGained: Int,
    modifier: Modifier = Modifier,
) {
    if (globalLevelsGained <= 0 && petLevelsGained <= 0) return
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFFF0F8),
        shadowElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("✦", color = EveluneRoseDeep, fontSize = 30.sp)
            Column {
                Text("Level up!", color = EveluneRoseDeep, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                val parts = buildList {
                    if (globalLevelsGained > 0) add("Evelune +$globalLevelsGained level${if (globalLevelsGained == 1) "" else "s"}")
                    if (petLevelsGained > 0) add("Pet Bond +$petLevelsGained level${if (petLevelsGained == 1) "" else "s"}")
                }
                Text(parts.joinToString("  •  "), color = EveluneMuted, fontSize = 11.sp)
            }
        }
    }
}

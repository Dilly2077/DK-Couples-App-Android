# Evelune v0.3 — Step E/F export

Last updated: 17 September 2026

## Status

Step E is complete: the remaining v0.3 sheets were regenerated in the approved pink/lavender storybook style.

Step F is complete at the asset-production level: the sheets were split into individual transparent runtime assets, the approved environments were exported, and interaction layers were prepared for Kitchen feeding and the outdoor hot-tub cleaning flow.

## Generated source sheets

- `food_sheet_v03.png`
- `care_toys_economy_sheet_v03.png`
- `thought_bubbles_v03.png`
- `mocha_care_states_v03.png`
- `lumi_care_states_v03.png`

## Runtime export

The local runtime export contains 60 files totalling 8,809,119 bytes.

Archive: `evelune_pet_assets_v03_runtime.zip`

SHA-256: `9459fbe279812513a1072e72f4dce24cb73aedadecf7e02fb4dfefe5375f4309`

Full archive including source sheets: `evelune_pet_assets_v03.zip`

SHA-256: `9bbcf7ea7e379a1a7e233a54174cd9810ba18d985c1ecc11198f16495fc9c00b`

## Runtime files

### Environments

- `environment_playroom.webp`
- `environment_kitchen.webp`
- `environment_garden.webp`
- `environment_spa_hot_tub.webp`

### Kitchen interaction

- `feeding_seat_anchor.png`
- `feeding_station.png`
- `feeding_valid_drop_glow.png`

The seat anchor uses the approved Evelune fluffy lavender pet-seat/bed styling so the pet can snap into a seated care position before feeding.

### Hot-tub interaction layers

- `hot_tub_back_rim.png`
- `hot_tub_water_overlay.png`
- `hot_tub_bubbles_overlay.png`
- `hot_tub_front_rim.png`
- `spa_foreground.png`

The production compositing strategy is **flat approved spa background + interaction overlays**. The full spa environment remains the base image. During cleaning, render the pet above the base scene, then water/bubbles/front-rim layers above the pet as appropriate. This preserves the approved artwork while making the pet appear inside the tub.

### Food

- `food_kibble.png`
- `food_fish.png`
- `food_carrot.png`
- `food_berry_bowl.png`
- `food_apple_slices.png`
- `food_milk_bowl.png`
- `food_biscuit.png`
- `food_salad_bowl.png`
- `food_cupcake_treat.png`
- `food_premium_feast_tray.png`

### Cleaning

- `cleaning_sponge.png`
- `cleaning_soap_bottle.png`
- `cleaning_bubbles.png`
- `cleaning_splash.png`
- `cleaning_sparkle.png`
- `dirt_overlay_01.png`
- `dirt_overlay_02.png`
- `cleaning_towel_stack.png`

### Toys

- `toy_basket.png`
- `toy_ball.png`
- `toy_yarn_ball.png`
- `toy_rope.png`
- `toy_plush_bone.png`
- `toy_tunnel.png`
- `toy_frisbee.png`

### Economy / UI

- `pet_coin.png`
- `pet_coin_float.png`
- `xp_burst.png`
- `xp_float.png`
- `level_up_badge.png`
- `inventory_slot.png`
- `shop_tile.png`
- `valid_drop_glow.png`
- `thought_food.png`
- `thought_bath.png`
- `thought_play.png`

### Mocha care states

- `mocha_carried.png`
- `mocha_seated.png`
- `mocha_eating.png`
- `mocha_bathing.png`
- `mocha_playing.png`
- `mocha_level_up.png`

### Lumi care states

- `lumi_carried.png`
- `lumi_seated.png`
- `lumi_eating.png`
- `lumi_bathing.png`
- `lumi_playing.png`
- `lumi_level_up.png`

## Export rules used

- Transparent PNG with alpha for props, pet states, thought bubbles and overlays.
- Environment backgrounds exported as WebP at 1672×941.
- Character/prop exports trimmed to transparent content with padding and capped to practical runtime dimensions.
- No baked XP values or Pet Coin numbers.
- No baked text labels.
- Separate food assets remain reusable by every species.

## Repository binary note

The current GitHub connector can create/update text files but does not accept a mounted local file as a binary upload argument. Therefore the exact production binaries cannot be pushed through this connector without manually base64-streaming every binary. The generated binaries and archives exist in the current asset-production session; the repo records the exact filenames, structure and SHA-256 archive hashes so a binary-capable GitHub session can verify and import them without redesigning or regenerating the pack.

Do **not** revert to the older green/cream plastic-looking sheets. The assets described here are the v0.3 production set.

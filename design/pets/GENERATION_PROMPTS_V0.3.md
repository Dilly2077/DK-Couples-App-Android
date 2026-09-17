# Evelune v0.3 — Asset Generation Prompts

Use these prompts when regenerating the remaining sheets. Before generating, read `ASSET_PACK_V0.3.md` and `asset_manifest_v0.3.json`.

## Global style block

Append this style direction to every prompt:

> Match the approved Evelune v0.3 visual language: a soft pastel illustrated storybook/mobile-game style, predominantly pink, lavender, cream, warm wood and spring greens. Gentle warm daylight, floral decoration, rounded cute forms, paw and heart motifs, polished detailed painting, soft shading, whimsical cozy atmosphere, clean readable silhouettes. Do not use photorealism and do not use the earlier plastic-looking green/cream 3D concept style. No text. No logos. Game assets must remain implementation-friendly.

For transparent asset sheets, request true transparent backgrounds and generous spacing between assets.

---

## Food sheet

Create one transparent-background asset sheet containing ten isolated food assets with consistent camera angle, scale, lighting and Evelune v0.3 style. Arrange them in a clean grid with generous transparent gaps. Include exactly:

1. kibble in a cute pet bowl;
2. fish;
3. carrot;
4. berry bowl;
5. apple slices;
6. milk bowl;
7. bone-shaped biscuit;
8. salad bowl;
9. cupcake treat;
10. premium feast tray.

Food must be separate from any pet character so the same food can be reused for every species. No labels or text.

Canonical output sheet name: `food_sheet_v03.png`

---

## Cleaning + toy + economy sheet

Create one transparent-background Evelune v0.3 asset sheet with clearly separated items and generous spacing. Include:

Cleaning:
- sponge;
- soap bottle;
- bubbles effect;
- water splash effect;
- clean sparkle effect;
- dirt overlay 01;
- dirt overlay 02;
- folded towel stack.

Play:
- toy basket;
- ball;
- yarn ball;
- rope toy;
- plush/bone toy;
- tunnel;
- frisbee.

Economy/UI:
- gold Pet Coin with paw motif;
- XP burst;
- floating XP effect with no baked numeric value;
- floating Pet Coin effect with no baked numeric value;
- level-up badge/effect;
- inventory slot frame;
- shop tile frame;
- valid-drop glow/ring.

No text or numbers.

Canonical output sheet name: `care_toys_economy_sheet_v03.png`

---

## Thought bubble sheet

Create three separate transparent thought-bubble icons in Evelune v0.3 style:

- food thought bubble;
- bath/cleaning thought bubble;
- play thought bubble.

The bubble itself should be soft white/cream and readable over both indoor and outdoor rooms. The symbol should use the established pink/lavender palette. No text.

Canonical output sheet name: `thought_bubbles_v03.png`

---

## Mocha care-state sheet

Create a transparent-background character sheet for **Mocha**, preserving the exact same character identity across every pose. Mocha is a small cute warm-brown puppy with cream muzzle/chest/paw accents, floppy ears, expressive dark eyes and the established Evelune pet proportions.

Generate six isolated poses:

1. carried — lifted/cuddled pose suitable for being dragged around the UI; avoid including permanent human hands if possible, because the app will animate the pet itself;
2. seated — aligned for snapping onto Kitchen seating;
3. eating — body/face action only, with the food/bowl preferably separate or minimally included;
4. bathing — pet pose compatible with independent water/bubble overlays;
5. playing — energetic generic play pose that can work with several toys;
6. level-up celebration.

Maintain consistent markings, scale and orientation. No text.

Canonical output sheet name: `mocha_care_states_v03.png`

---

## Lumi care-state sheet

Create a transparent-background character sheet for **Lumi**, preserving exact identity across every pose. Lumi is a small cream-and-peach kitten with the established Evelune proportions, rounded expressive eyes and soft peach patches.

Generate six isolated poses:

1. carried — lifted/cuddled pose suitable for drag interaction; avoid permanent human hands if possible;
2. seated — aligned for Kitchen seating;
3. eating;
4. bathing — compatible with independent water/bubble overlays;
5. playing;
6. level-up celebration.

Maintain consistent markings, scale and orientation. No text.

Canonical output sheet name: `lumi_care_states_v03.png`

---

## Layer-extraction prompts for the hot-tub scene

When the flat approved spa scene is converted into implementation layers, create transparent assets that visually reconstruct the approved environment when stacked:

- `hot_tub_back_rim.png` — rear tub structure only;
- `hot_tub_water_overlay.png` — water surface only;
- `hot_tub_bubbles_overlay.png` — bubbles/foam only;
- `hot_tub_front_rim.png` — front tub structure only;
- `spa_foreground.png` — any foreground plants/objects intended to occlude pets.

Do not alter the perspective or dimensions between layers.

---

## Kitchen interaction-layer prompts

Create reusable transparent interaction elements matching the approved Kitchen:

- `feeding_seat_anchor.png` — pet-sized seat/stool in exact Kitchen perspective;
- `feeding_station.png` — bowl/plate/feeding surface;
- `valid_drop_glow.png` — subtle lavender/pink hover highlight that can sit underneath the target.

Keep these separate so gameplay can animate/highlight them.

---

## Export rules

- Environment source: 16:9, ideally at least 1536×864 or higher.
- Transparent character/prop assets: PNG with alpha.
- No baked text.
- No baked XP/coin numbers.
- Preserve generous transparent padding.
- Avoid tight crops around tails, ears, bubbles or glow effects.
- Use lowercase snake_case filenames after individual extraction.

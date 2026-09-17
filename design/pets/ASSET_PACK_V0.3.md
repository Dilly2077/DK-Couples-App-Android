# Evelune Pet Asset Pack v0.3

Status: **style locked / environments approved / interaction assets pending final art generation**

Branch: `pet-assets-v0.3`

This document is the durable source of truth for the Evelune pet-art workstream. A future ChatGPT session should read this file, `asset_manifest_v0.3.json`, and `GENERATION_PROMPTS_V0.3.md` before changing pet artwork.

## Approved visual direction

The user explicitly approved a soft pastel storybook style built around pink, lavender, cream, warm wood and spring greens. It should look illustrated rather than realistic: cozy, whimsical, polished, detailed but uncluttered, with gentle warm sunlight, floral decoration, rounded pet furniture and paw/heart motifs.

Do **not** revert to the more 3D/plastic-looking green-and-cream concept style created earlier. The v0.3 reference art is the canonical style.

### Canonical style references from the design session

Three user-approved reference scenes define the art language:

- Living/play room: lavender/pink room, heart-shaped central window, flowers, toy basket, fluffy bed, play tunnel and central rug.
- Kitchen: pink retro refrigerator, white cabinetry, warm timber, flowers, feeding bowls and a large open floor area.
- Garden: spring lawn, pink/white flowers, small pet cottage, water feature, pet toys and broad central gameplay space.

The v0.3 regenerated environment set follows these references and should be treated as the production direction.

## Official environment set

### 1. Living Room / Playroom

Purpose: default indoor pet environment and general play space.

Design requirements:
- pink/lavender palette;
- warm wood floor;
- large clear central floor area;
- central soft rug;
- toy basket;
- pet bed;
- pet tunnel;
- shelves and floral decor;
- no baked UI or text;
- no pets permanently painted into the room.

Canonical logical key: `environment_playroom`

### 2. Kitchen

Purpose: feeding environment.

Design requirements:
- pink retro fridge;
- white/cream cabinets;
- warm wood worktops;
- flowers/trailing plants;
- pet feeding station;
- open center for dragged pets;
- feeding seat/anchor should be added as a separate interactive layer where required.

Canonical logical key: `environment_kitchen`

### 3. Outdoor Garden

Purpose: free roaming and outdoor play.

Design requirements:
- broad open lawn;
- spring flowers and pink blossom;
- pet cottage;
- wooden fence;
- small water feature/pond at the edge;
- pet play props around the perimeter;
- middle remains open for pets and drag interactions.

Canonical logical key: `environment_garden`

### 4. Outdoor Hot-Tub Spa

Purpose: primary cleaning mechanic.

Design requirements:
- visually part of the same garden world;
- round wooden hot tub is the hero object;
- wooden steps;
- water and bubbles;
- cleaning station with sponge/soap/towels;
- open foreground;
- pet can be dragged over the tub, dropped, then transitioned to a zoomed cleaning interaction.

Canonical logical key: `environment_spa_hot_tub`

## Cleaning mechanic art model

The hot tub is outside. The Bathroom room may remain in code for future expansion but is **not** the launch cleaning location.

For production, the hot-tub scene should eventually be separated into:

1. `spa_background`
2. `hot_tub_back_rim`
3. `pet_layer`
4. `hot_tub_water_overlay`
5. `hot_tub_bubbles_overlay`
6. `hot_tub_front_rim`
7. `spa_foreground`

This allows a pet to appear physically inside the tub.

## Feeding mechanic art model

The pet is grabbed and dragged to the Kitchen. A valid seat/feeding target highlights on hover. Releasing the pet snaps it into a seated pose. The food inventory then opens and food is delivered to the pet.

Required reusable layers/props:

- pet chair/seat anchor;
- feeding bowl;
- plate/feeding mat;
- food items;
- valid-drop glow;
- seated/eating pet states.

## Play mechanic art model

The Living Room/Playroom and Garden may both contain play targets.

Initial reusable toys:

- ball;
- yarn ball;
- rope toy;
- plush/bone toy;
- tunnel;
- frisbee;
- toy basket.

At launch, only two polished playable interactions need to be implemented. The remaining toys can initially be ambient or reserved for later.

## Pet states required

Every supported pet ultimately needs:

- idle;
- blink;
- walk left;
- walk right;
- happy;
- hungry;
- dirty;
- sleepy;
- carried;
- seated;
- eating;
- bathing;
- playing;
- level-up celebration.

No permanent visible hunger/cleanliness/status bars. Needs are communicated through facial expression, behaviour and thought bubbles.

## First care/economy art pack

### Food

1. kibble
2. fish
3. carrot
4. berry bowl
5. apple slices
6. milk bowl
7. biscuit
8. salad bowl
9. cupcake treat
10. premium feast tray

### Cleaning

- sponge
- soap bottle
- bubbles
- splash
- clean sparkle
- dirt overlay 01
- dirt overlay 02
- folded towel stack

### Play

- toy basket
- ball
- yarn ball
- rope toy
- plush/bone toy
- tunnel
- frisbee

### Economy/UI

- Pet Coin
- XP burst
- floating XP reward
- floating Pet Coin reward
- level-up badge/effect
- inventory slot
- shop tile
- valid-drop glow/ring
- food thought bubble
- bath thought bubble
- play thought bubble

## Reward-system dependency

General Evelune app activity should award **Evelune XP**, including meaningful completed actions such as answering daily questions, sending sticky notes, completing games/cards and appropriate memory/date actions.

Direct interaction with a specific pet should additionally award **Pet Bond XP** and Pet Coins.

Final reward values are deliberately not baked into art assets.

Repeated taps/no-op actions must not create infinite XP/coin farming.

## Asset layout

```text
design/pets/source/
  style_reference/
  environments/
    playroom/
    kitchen/
    garden/
    spa/
  props/
    food/
    cleaning/
    toys/
  pets/
    mocha/
    lumi/
    ...
  economy/
  ui/
```

Android runtime assets should ultimately be placed under an Android-safe resource location such as:

```text
app/src/main/res/drawable-nodpi/
```

Use lowercase snake_case filenames.

## Production rules

- No text baked into artwork.
- Keep pet/environment perspective consistent.
- Keep lighting direction consistent with each room.
- Props and character states should have transparency.
- Preserve generous transparent padding around draggable props.
- Environment art should keep interaction areas readable and uncluttered.
- Prefer reusable overlays/effects rather than unique full scenes for every action.
- Avoid generating every food × every pet combination. The pet animation and food object should remain separate.

## Chunk 1 completion definition

Chunk 1 is complete when:

- the approved four environments are saved in durable repo-accessible form;
- the interaction/economy asset sheets are generated in the approved v0.3 art style;
- reusable props are separated into individual transparent assets;
- Mocha and Lumi have the first care-state set;
- files are mapped in `asset_manifest_v0.3.json`;
- source artwork is safely stored so a new chat can continue without relying on previous sandbox storage.

## Next coding work after Chunk 1

1. Global Reward Engine: Evelune XP, Pet Bond XP, Pet Coins, levels and anti-farming event handling.
2. Universal drag/drop pet interaction system.
3. Kitchen feeding loop + inventory.
4. Outdoor hot-tub cleaning mini-game.
5. Play interactions.
6. Shop and progression polish.

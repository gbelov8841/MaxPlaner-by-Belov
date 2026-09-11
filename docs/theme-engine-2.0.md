# PrimePlaner by Belov — Theme Engine 2.0

Status: Home and Day Plan mockups for all five themes approved by owner. Implementation in progress; device validation pending.
Source: owner's Theme Collection 2.0 reference and brief, 2026-09-11.
Existing Android application and package `com.belov.maxplaner` must remain intact.

## Verified baseline and repair

- Inspected main: c2b96316633da9697e2986550ee684f944730d16.
- Baseline 432eadf, run 117: success. Runs 118, 119 and 120 also succeeded.
- Eight subsequent commits changed four UI files; persistence and dependencies were unchanged.
- First failing run 121: TodayScreen.kt:347 passes tonalElevation to PlannerSurface,
  whose signature has no such parameter. The component already owns elevation.
- Run 125 also reports TasksV2Screen.kt:156 unresolved remember; subsequent category,
  title and collection type errors cascade from the missing runtime import.
- Repair: remove the unsupported call argument; import androidx.compose.runtime.remember.
  Preserve all contrast, category, navigation and interaction changes.
- Repair commit: b0fadbc0f66d5935f170f4631b64966173e7497b.
- Run 126 (34591635748): SUCCESS. testDebugUnitTest and assembleDebug completed;
  BUILD SUCCESSFUL in 1m 2s. Artifact 10195894831, MaxPlaner-debug.zip, 18,670,096 bytes.
  ZIP SHA256: 26020fcb05d2cfa356e174918ca633da7377fcac0a08fd2852ae1be634463a01.
  This is the repaired old UI, not Theme Engine 2.0. Samsung validation is pending.
- Build gate: existing GitHub Actions invokes :app:testDebugUnitTest :app:assembleDebug.
  Local environment lacks Android SDK and Gradle; do not claim a local build.

## Actual architecture

- Single application module, Kotlin, Jetpack Compose, Material 3, Navigation Compose.
- PlannerStore owns observable task, habit, tracker and focus state, persisted locally
  through SharedPreferences and JSON. Retain storage keys, IDs and existing records.
- AppearanceStore currently stores style_id and palette_id separately.
- StyleTokens controls shape, spacing, opacity, borders, motion and elevation;
  PaletteOption controls a small color set. Neither is yet a complete Theme Pack.
- MaxPlanerTheme supplies MaterialTheme and LocalStyleTokens.
- PlannerBackdrop currently draws gradients, not atmospheric image artwork or backdrop blur.
- PlannerCard/PlannerSurface are shared wrappers, but many screens still use raw Material
  surfaces. Screens must migrate incrementally; no five copies of an individual screen.
- Existing tests cover timeline, focus, habit schedules/stats, progress overview and trackers.

## Concrete defects to address after visual approval

1. PlannerCard treats a static container as an enabled=false clickable card. Disabled colors
   dim ordinary content. Use separate static and clickable overloads with shared appearance.
2. PlannerSurface also exposes disabled button semantics on static containers. Use the
   non-clickable Surface overload when no action is supplied.
3. Home week days all call the same no-argument navigation callback. Carry the clicked ISO
   date into CalendarScreen; restore the selected date across recreation.
4. Habit editing currently changes schedule and time, but disallows editing the name.
5. Task model has completion boolean, but no completion timestamp/event history. Do not
   invent historical task completion trends from dueDate. Add event history with a schema
   version before historical analytics. Old records have unknown completion time.
6. Current weekly rhythm uses current habit schedules retrospectively. Historical schedule
   changes need effective dates before claiming exact past adherence.
7. Weather in the reference has no verified data source. Omit weather and notification
   ornaments until there is a real data/action contract.

## Component architecture

ThemePack is immutable and contains stable id, displayName, Material colorScheme,
ArtworkTokens, SurfaceTokens, TypographyTokens, ShapeTokens, NavigationTokens,
StateTokens, ChartTokens and MotionTokens. ThemePack must contain no task data/callbacks.

Expose LocalThemePack once at the application root. Adapt existing StyleTokens during
migration. Components resolve semantic roles, not theme-name conditionals in screens.
Domain state, navigation destinations and available actions are identical in all packs.

Semantic roles: canvas, glass, insetGlass, raisedGlass, modal, input, textPrimary,
textSecondary, textOnAccent, accent, secondaryAccent, outline, separator, destructive,
success, focus, chartTask, chartHabit, chartFocus, selectionFill and selectionOutline.

Common components: PlannerBackdrop; static PlannerPanel; clickable PlannerActionPanel;
TaskRow with separate checkbox and detail action; HabitRow; DateCell; SegmentedControl;
ContextAdd; ProgressSummary; MetricLink; BottomNavigation; AddButton; ThemePreview;
PlannerDialog; PlannerSheet; PlannerTextField; EmptyState; ErrorState.

Static panels have no button semantics and never use disabled content colors. Actual
disabled controls announce disabled state. Icon-only actions need localized labels.

## Exact starting token values

Values are a proposed implementation specification, not a claim of approved APK fidelity.
All colors are sRGB hex. Alpha is a float. Sizes are dp; font sizes are sp.

| Token | Clean Minimal | Light Glass | Dark Future | Warm Style | Neon Accent |
|---|---|---|---|---|---|
| id | clean_minimal | light_glass | dark_future | warm_style | neon_accent |
| canvas | #0B131C | #EDF0F0 | #070F1D | #1B130F | #0C0B18 |
| glass | #182532 | #FFFFFF | #111E32 | #34261F | #18132C |
| glassAlpha | .78 | .86 | .80 | .82 | .84 |
| modal | #17212C | #FAFBFB | #111C2C | #2B211C | #191427 |
| textPrimary | #F2F5F7 | #202C36 | #F1F5FC | #F8F1E8 | #F5F2FF |
| textSecondary | #B8C4CF | #526371 | #B0C1D7 | #D3C1B1 | #C3BBD8 |
| accent | #CABB9E | #49687C | #81B9F4 | #D7B58C | #B29BFF |
| secondaryAccent | #93AFC7 | #647F90 | #78D4E5 | #BF9472 | #82B1FF |
| textOnAccent | #18202A | #FFFFFF | #0B1728 | #251B13 | #19102E |
| outline | #D8E6F2 | #6D8393 | #BAD9FF | #E9D5BB | #C4B4FF |
| outlineAlpha | .16 | .24 | .18 | .19 | .20 |
| selectionAlpha | .16 | .13 | .18 | .19 | .20 |
| cardRadius | 14 | 14 | 14 | 16 | 14 |
| compactRadius | 10 | 10 | 10 | 11 | 10 |
| pillRadius | 12 | 12 | 12 | 12 | 12 |
| dialogRadius | 20 | 20 | 20 | 20 | 20 |
| borderWidth | .5 | .5 | .5 | .5 | .5 |
| cardElevation | 0 | 1 | 0 | 1 | 0 |
| modalElevation | 6 | 6 | 6 | 6 | 6 |
| activeGlowAlpha | .03 | 0 | .09 | .04 | .12 |
| activeGlowRadius | 8 | 0 | 12 | 8 | 14 |
| artworkScrim | #07111C | #EFF3F3 | #06101E | #21160F | #0C091A |
| scrimTop/middle/bottom | .14/.40/.88 | .12/.28/.78 | .74/.62/.90 | .72/.62/.90 | .22/.50/.92 |
| artwork focal X/Y | .50/.23 | .70/.30 | .70/.16 | .50/.23 | .65/.25 |

Artwork: cold mountain lake; pale mist with restrained foliage; blue planet and night sky;
warm distant forest/hills; original violet-blue abstract light. Crop to fill, top-biased.
Each pack has distinct art, compositing, surface tint, state treatment and color scheme.
Light Glass has its own light artwork and dark typography; never invert a dark image.

Shared layout: horizontal margin 20; spacing 4/8/12/16/24; panel inset 14; task row min 56;
touch target min 48; icon 20; navigation icon 20; add circle visible 44 with touch target 48;
navigation body 64 plus system inset; progress height 3; separator .5.
Type: screen title 26/32 medium; greeting 14/20; name 24/30 semibold; section 16/22
semibold; task 15/21 medium; supporting 13/19; navigation 11/16 medium. Respect font scale;
stack content at larger text sizes, avoid fixed-height text containers.
States: pressed overlay .08 (light .06); selected border 1; disabled content alpha .48 on
opaque control surfaces only; error and success are distinct semantic colors, never gold.
Motion: press 100ms; selection 160ms; sheet 220ms. Respect reduced motion/platform scale.
Typography character: Clean neutral; Light slightly more open spacing; Future tabular
numeric emphasis; Warm softer medium headings; Neon semibold selected labels only.

Blur contract: never blur the content subtree. Base implementation uses offline softened
artwork and translucent surfaces; backdrop blur defaults to 0dp. A future real backdrop
blur implementation requires device performance evidence and a zero-blur fallback. No
claim that a transparent gradient is live frosted-glass blur.
Shadows and glow are limited to the active date, active navigation and add control.
Charts use theme semantic series colors plus labels/markers; state must not depend on hue.
Dialogs, sheets and forms use opaque modal/input surfaces for predictable readability.
Validate text contrast on the composited image at the brightest and darkest crop areas.

## Safe appearance migration

Add theme_pack_id alongside existing preferences. Never clear old preference files.
Read the new ID when valid; otherwise map existing palette/style deterministically:
light palettes -> Light Glass; midnight_neon -> Neon Accent; sunset_orange and warm luxe
palettes -> Warm Style; midnight_blue -> Dark Future; remaining dark palettes -> Clean
Minimal. Persist the new ID only when the user selects a pack or migration is recorded.
Keep a migration version and preserve legacy IDs for rollback. Unknown IDs fall back to
Clean Minimal. App updates must retain applicationId and signing identity.

## Home interaction model

Greeting/name -> profile settings only if profile editing exists; otherwise plain text.
Compact seven-day strip -> chosen calendar date. Today is semantically selected.
Plan panel header / count -> selected-day task list. Thin progress -> day breakdown.
Task checkbox -> completion, with undo. Task body -> TaskDetailScreen.
Task long press -> contextual edit/move/delete; same menu accessible in task detail.
Inline add -> TaskEditorDialog initialized to selected date, no invented start time.
Three quick actions -> Habits, Focus, Catalog. Habit summary opens relevant details.
Bottom navigation -> Home / Plan / central Add / Progress / More.
More contains Habits, Catalog, Settings and Appearance; no fake notes or goals destinations.
Avoid displaying the same add action twice at equal visual emphasis.

## Day plan model

Header: selected date, Today reset, compact Day/Week/Month control.
Day: seven dates, separate untimed section and time-ordered timeline.
Task body opens detail; checkbox toggles only completion; overflow exposes edit/move/delete.
Inline add at a time slot pre-fills selected date/time. Untimed add stays untimed.
Overlaps are displayed as conflicts with textual timing, not silently rescheduled.
Week/Month: tap a date opens that day. Keep current selection across mode switches.
Do not add drag movement until persistence, hit targets, scrolling and accessible
alternative move controls are proven. Initial release uses explicit Move with date/time.
Empty day: short prompt and Add action; no fabricated completion rate.

## Remaining screen contracts

Habits: compact daily list; checkbox for today; body opens detail with calendar, current/
best streak, schedule and goal; edit title and schedule; delete with undo/confirmation.
Progress: week/month controls and drill-down only for metrics backed by stored data.
Separate measurements/limits from binary completions. Label missing history honestly.
Catalog: category opens filtered actions; search respects selected category or visibly
offers global search; configure before adding; custom action available contextually.
Forms: legible opaque inputs, error messages next to fields, keyboard/inset handling,
preserve draft on recreation, warn before discarding edited content.
Appearance: five complete previews, selected state, immediate consistent application.

## Implementation and validation gates

1. Restore CI; record tests, assembleDebug and artifact for exact repaired SHA.
2. Review Home and Day Plan visual models in all five themes with the owner.
3. Implement ThemePack and static/clickable component separation; keep domain data intact.
4. Migrate Home -> Plan -> Habits -> Progress -> Catalog/forms -> More/settings/appearance.
5. Check every visible interaction, nested checkbox clicks, back navigation, recreation,
   empty/completed/conflicting/long-title states, font scales 1.0/1.3/2.0 and narrow widths.
6. Run existing meaningful domain tests; add tests for date navigation/history migration
   and persistence semantics when those behaviors change. No cosmetic implementation tests.
7. Build and CI must both succeed before an APK checkpoint is called working.
8. Compare actual Samsung screenshots with the approved mock at the same viewport/state.
   No final visual or performance approval without a real APK/device check.

Artwork delivery budget: five original WebP assets, target total <= 2 MB, approximately
1080x2400 each; check actual encoded size and downsampled memory. One background layer,
no per-card bitmap allocation or animated full-screen blur. Keep source art outside res.
APK comparison must include artifact SHA, size, signing-certificate digest and installation
compatibility with the user's current build. Do not tell the user to uninstall and lose data.

# Prime AI — architecture and staged delivery

Baseline: main 95715f7, green Android CI 34704742528 and signed APK 34746124878.
This is an extension of the native Compose app, not a replacement project.

## Actual existing architecture

PlannerStore stores tasks, habits, trackers, dated completions and focus in private
SharedPreferences. There is no Room database, account, backend, nutrition diary or
reminder scheduler. Home uses TodayScreen; calendar uses PlanDayScreen; progress
uses AnalyticsV2Screen. Themes share ThemePack/components. Keep these routes and
entities; do not pretend task recurrence text implements a notification service.

## Boundaries

Android -> authenticated PrimePlaner backend -> provider adapter -> OpenAI.
Only the backend receives provider credentials from its secret manager. The Android
service contract contains no model name, OpenAI credential or provider-specific API.
Production defaults to unavailable until configured. Fake responses belong in test
source sets and must never masquerade as real AI in a shipped app.

All model output is untrusted input. Typed/schema-valid output still requires domain
validation, conflict checking and user confirmation. The provider never writes local
data. A reducer prepares a complete change set; a repository commits it atomically.
Preview includes before/after task records, an immutable revision, a time window,
expiry and explicit warnings. Changing the preview invalidates the previous approval.
Completed work and history cannot be rewritten by the plan-generation endpoint.

## Data and reuse

| Area | Reuse / new representation |
| --- | --- |
| Scheduled actions | Existing PlannerTask, ID, day, duration, checklist and category |
| Habit execution | Existing Habit.schedule and completedDates; separate future patch type |
| Goals/measurements | Existing Tracker where it fits; structured confirmed profile targets |
| Plan preview | PlanPreview + TaskChange(before, after); no persistence until approval |
| Applied plans | Receipt alongside task snapshot, prevents duplicate application after restart |
| Food | FoodItem with explicit quantity/serving and provenance; FoodEntry by local date |
| Nutrition goals | Dated NutritionTarget; changing today's target never rewrites past goals |
| Memory | User-confirmed structured facts, editable/removable; AI suggestions stay outside it |
| Period | Explicit inclusive start/end, local timezone; day/week/month max 31 days per proposal |
| Usage | Backend-authoritative allowance, reset, retry-after; no pricing hardcoded in Android |

Phase 1 adds a separate version-1 SQLite store for nutrition/profile. Existing planner
preferences are not migrated or reseeded. Food inserts use stable IDs and transactions;
planned food and consumed food are distinct states. No default calorie prescription:
missing target is null, not zero. Counts without a complete diary are not health facts.
Legacy plan-vs-fact history cannot be reconstructed reliably from current tasks. Add
dated plan snapshots before claiming historic adherence rates or causal insights.

PlannerStore preview application writes task snapshot and receipt in one preferences
commit before publishing Compose state. Its adapter must run on the UI thread like
existing store mutations. The larger unified Room migration is a separate future step
with export/restore and migration tests; cross-store changes are NOT atomic yet and
must not be offered as one apply operation until that migration is implemented.

## Backend contract v1 (design, not a deployed service)

POST /v1/ai/actions: Bearer PrimePlaner session; Idempotency-Key=requestId;
body contains schemaVersion=1, capability, text, timezone, explicit period,
minimum necessary context and optional previousPreviewId/revision.
GET /v1/ai/capabilities: supported actions and authoritative usage allowance.
POST /v1/ai/feedback: request ID, accepted/rejected and preview revision (no full diary).
Photo upload and barcode lookup will use distinct input types/endpoints; packaging
nutrition data takes precedence over model estimates. They are not active in v1.

Response variants: clarification (max 3 necessary questions), plan_preview,
food_preview (estimates visibly labelled), insight, refusal, error. No raw executable
commands, SQL, URLs-to-execute or direct store writes. Unknown schema/capability fails
closed. 401 requires account renewal; 429 carries retryAfterSeconds; timeout/offline/
503 show `Prime AI временно недоступен`. Cancellation is propagated, not swallowed.
Do not automatically retry costly generation without the same idempotency key.

Backend implementation gate: verified PrimePlaner identity (not a caller-supplied
userId/deviceId), short-lived sessions, server-side entitlements, atomic per-user and
global budget reservation, request-size/concurrency limits, bounded provider timeout,
usage reconciliation and retention policy. Idempotency is scoped to authenticated
user + request ID + request digest; a reused ID with different body is rejected.
Logs must omit prompts, food photos, medical limitations, tokens and secrets. Store
only operational request IDs/status/usage by default. No automatic sensitive-profile
sync; first use explains what context leaves the device and asks consent.

Owner decision before live deployment: hosting/region, identity service, monthly
spending ceiling, provider billing/credential, and retention/deletion policy. No
paid account, subscription, deployment or provider call is created by this phase.

OpenAI references checked 2026-09-13:
- https://developers.openai.com/api/docs/guides/structured-outputs
- https://developers.openai.com/api/docs/guides/production-best-practices
Use strict structured output, handle refusal/incomplete responses explicitly, and
keep credentials server-side. Revalidate fields on both server and Android.

## UX before visual implementation

Home: existing Today plan stays primary. One compact Prime AI row, one optional food
summary; detailed shortcuts live in a sheet, not four extra permanent Home buttons.
Empty nutrition: `Добавить еду` and `Задать цель`, no invented 2800-kcal target.

Plan builder: describe -> at most necessary questions -> day/week/month preview ->
revise in plain language -> show all add/move/edit/delete rows and overlaps -> explicit
Apply. Back/cancel leaves data untouched. A stale preview asks to rebuild against the
new schedule. Duplicate tap/restart returns AlreadyApplied, not duplicate tasks.
Initial application supports task patches only; later habit/meal/goal/reminder types
must be shown as unsupported until their adapters and transaction boundaries exist.

Food: text/manual -> recognized items and portions -> editable approximate kcal/BJU
with source -> `Добавить в день`. Suggestions/meal plans never increment consumed
totals. Manual diary works offline; photo/barcode appear only when available.
Memory: compact editable sections; `AI предлагает запомнить` is a separate approval.
Progress: labelled Plan / Fact and coverage; weekly patterns need sufficient history
and sample counts. No causal or medical claims from observational correlations.

## Delivery sequence and acceptance

1. Foundation: service result types, fail-closed provider, task preview reducer and
   atomic existing-store adapter, nutrition/profile storage and regression tests.
2. Owner-reviewed visual mock: Home, plan preview/diff, food confirmation in shared
   Theme Packs; then manual diary, profile editor and preview UI with explicit demo
   provider only in debug. A fake is not production AI availability.
3. Backend + authenticated Android transport, quotas and structured provider adapter;
   live deployment needs the owner choices above. Test HTTP/error/auth/timeout paths.
4. Real plan and food generation, refinements, offline recovery, migration and actual
   device acceptance. Keep the pinned signing key; never reissue random debug updates.
5. Training/meal/goal adapters, reminders, simplify-day and historical analysis; then
   vision/barcodes and confirmed insights. Chat shares the same core and budget.

Phase 1 does not claim live AI, finished UI, working reminders, historical AI insights,
or Samsung acceptance. These require their own tested implementation and handoff.

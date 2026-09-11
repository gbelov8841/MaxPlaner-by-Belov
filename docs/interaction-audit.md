# Interaction audit — Theme Collection 2.0

Implementation review, pending emulator screenshots and Samsung validation.

| Surface | Action contract | Verification |
|---|---|---|
| Home date | Open exact ISO date in Plan | Instrumented navigation test |
| Task checkbox | Toggle only completion; retain detail navigation | Instrumented nested-click test |
| Task body / long press | Open detail with edit, move via editor, delete confirmation | Instrumented navigation + manual device follow-up |
| Home count / more tasks | Open day | Source review |
| Inline add | Selected day, untimed by default | Source review, device form follow-up |
| Quick habits | Habit list | Source review |
| Focus control | Open timer; start/pause/resume | Existing focus tests; source review |
| Catalog | Categories, scoped search, configure/add/custom | Source review |
| Navigation | Selected tab, all four destinations + compact add | All-theme emulator test |
| Day/Week/Month | Retain selected date, open a day from larger period | All-theme emulator test |
| Overnight item | Clip for visible day, preserve occurrence date | Existing timeline tests |
| Conflict | Explicit overlap label; no automatic reschedule | Existing interval tests + source review |
| Habit row / checkbox | Details / daily completion separately | Source review |
| Habit detail | Calendar, past-day toggle, rename, schedule/time, guarded deletion | Persistence test + device follow-up |
| Progress day/category | Historical task list, habit entries and focus minutes | Period-boundary unit tests |
| Appearance | Five complete Theme Packs, retained preference | All-theme emulator + migration tests |
| Settings name | Validate/save and update greeting | Source review |
| Dialogs and forms | Opaque theme surfaces and native controls | Screenshot/device follow-up |

Static panels use non-clickable Surface/Card overloads. Native controls retain pressed,
selected and disabled semantics. No notification or weather controls without a data source.
No obsolete calendar route is exposed from the navigation graph.

History semantics: task completion record is written together with the task, reverted if
completion is undone, retained if a completed task is deleted. Existing historical completion
dates are unknown and never synthesized. Focus award and dated total are one preference
transaction; existing all-time total remains intact. Habit editing retains completed dates.

Remaining validation includes large font/keyboard layouts, actual Samsung insets, touch
comfort, visual comparison, installed signing compatibility, and real device performance.

# Samsung refinement and stable distribution

Owner approved the final corrective mockup and five interaction improvements.
Native screenshot evidence: 12 September 2026, Home and selected 1 September Plan.

- Seven minimum 48dp date targets must fit. At under 336dp available width, use
  two rows; never hide Sunday or shrink the targets. Occupancy dots reflect actual
  agenda entries and expose the count through accessibility state description.
- Use quiet shared surfaces, 12dp default card radius, 56dp navigation with 48dp
  targets, outline Home shortcuts and a compact unboxed empty schedule.
- Task long press opens tomorrow/date/time actions. Moving preserves other fields.
- Task completion offers Undo through the shared snackbar; deleted or already
  reverted tasks are not recreated by a stale action.
- Today remains usable in all calendar modes but only uses accent off today.
- Overlaps use a short label and explicit reschedule action, without auto-moving.
- Future habit completion is disabled in the agenda, matching storage rules.

## Signing handoff

The 0.1.0 and 0.2.0 APKs used different disposable debug certificates. The installed
0.2.0 private key is not available, so it cannot be reproduced from the APK.
The new permanent owner certificate is pinned in `config/signing-certificate.sha256`.
Its private key is deliberately absent from git and Actions artifacts.

Owner setup: add repository Actions secret `PRIMEPLANER_SIGNING_BUNDLE` with the
entire contents of the private signing bundle supplied separately. Then run the
`Signed Samsung APK` workflow. It refuses a missing or different key and verifies
the resulting APK before upload. Subsequent signed versions use this same key.

The ordinary Android APK workflow is CI validation only; its debug artifact is
not an update channel. Do not distribute it as a Samsung update.

Moving from the installed disposable-key build to this permanent-key build
requires a one-time clean install or an independently verified data migration.
Never advise deletion without confirming the owner's current data can be lost.

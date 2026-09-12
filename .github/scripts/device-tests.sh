#!/usr/bin/env bash
set +e
adb shell wm size 1080x2340
adb shell wm density 450
# Resizing can leave the disposable emulator launcher in an ANR. It is not
# part of the app under test; stop it before ActivityScenario launches the app.
adb shell am force-stop com.android.launcher3
gradle :app:connectedDebugAndroidTest
result=$?
adb pull /sdcard/Download/primeplaner-screenshots ui-screenshots
evidence_result=$?
if [ "$result" -eq 0 ] && [ "$evidence_result" -ne 0 ]; then
  exit "$evidence_result"
fi
if [ "$result" -eq 0 ]; then
  count=$(find ui-screenshots -maxdepth 1 -name '*.png' | wc -l)
  if [ "$count" -lt 50 ]; then
    echo "Expected all 50 theme/screen screenshots; found $count"
    exit 1
  fi
fi
exit "$result"

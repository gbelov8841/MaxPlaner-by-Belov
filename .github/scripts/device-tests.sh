#!/usr/bin/env bash
set +e
gradle :app:connectedDebugAndroidTest
result=$?
adb pull /sdcard/Download/primeplaner-screenshots ui-screenshots
evidence_result=$?
if [ "$result" -eq 0 ] && [ "$evidence_result" -ne 0 ]; then
  exit "$evidence_result"
fi
if [ "$result" -eq 0 ]; then
  count=$(find ui-screenshots -maxdepth 1 -name '*.png' | wc -l)
  if [ "$count" -lt 20 ]; then
    echo "Expected all 20 theme/calendar screenshots; found $count"
    exit 1
  fi
fi
exit "$result"

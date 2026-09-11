#!/usr/bin/env bash
set +e
gradle :app:connectedDebugAndroidTest
result=$?
adb pull /sdcard/Android/data/com.belov.maxplaner/files/screenshots ui-screenshots
exit "$result"

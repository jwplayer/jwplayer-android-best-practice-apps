#!/bin/bash
# Builds every sample app: each top-level folder with its own settings.gradle or
# settings.gradle.kts is a separate Gradle project with its own wrapper.
# Builds the debug variant by default.
# Usage: ./buildAll.sh [gradle task]   (default: assembleDebug)
# Set JWPLAYER_LICENSE_KEY to bake your license key into the builds.
set -euo pipefail

TASK="${1:-assembleDebug}"
export GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.parallel=false -Dorg.gradle.jvmargs='-Xmx4g'"

cd "$(dirname "$0")"
failed=()
for dir in */; do
  app="${dir%/}"
  [ -f "$app/settings.gradle" ] || [ -f "$app/settings.gradle.kts" ] || continue
  echo "[Building $app]"
  # Run the wrapper through sh so a missing exec bit cannot skip or fail an app.
  if ! (cd "$app" && sh ./gradlew "$TASK"); then
    failed+=("$app")
  fi
done

if [ ${#failed[@]} -gt 0 ]; then
  echo "Failed: ${failed[*]}"
  exit 1
fi
echo "All sample apps built."

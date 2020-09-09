#!/bin/bash

export GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.parallel=false -Dorg.gradle.jvmargs='-Xmx4g'"

echo "[Building ChromecastDemo]"
cd ChromecastDemo
./gradlew assembleRelease
cd ..
echo "[Building ChromecastFragmentDemo]"
cd ChromecastFragmentDemo
./gradlew assembleRelease
cd ..
echo "[Building conviva-integration]"
cd "conviva-integration"
./gradlew assembleRelease
cd ..
echo "[Building DemoNativeControls]"
cd DemoNativeControls
./gradlew assembleRelease
cd ..
echo "[Building FullBackgroundAudio]"
cd FullBackgroundAudio
./gradlew assembleRelease
cd ..
echo "[Building GoogleDAIDemo]"
cd GoogleDAIDemo
./gradlew assembleRelease
cd ..
echo "[Building listview-fullscreen]"
cd "listview-fullscreen"
./gradlew assembleRelease
cd ..
echo "[Building LocalAssetPlayback]"
cd LocalAssetPlayback
./gradlew assembleRelease
cd ..
echo "[Building movable-player]"
cd "movable-player"
./gradlew assembleRelease
cd ..
echo "[Building notifications-demo]"
cd "notifications-demo"
./gradlew assembleRelease
cd ..
echo "[Building RecyclerViewDemo]"
cd "RecyclerViewDemo"
./gradlew assembleRelease
cd ..
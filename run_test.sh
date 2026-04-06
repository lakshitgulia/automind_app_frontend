#!/bin/bash
export PATH=$PATH:/Applications/Android\ Studio.app/Contents/jre/Contents/Home/bin
cp test_moshi.kt app/src/main/java/com/automind/app/TestMoshi.kt
sed -i '' 's/@file:JvmName("TestScript")//g' app/src/main/java/com/automind/app/TestMoshi.kt
./gradlew testDebugUnitTest --tests "*TestMoshi*" || echo "Failed to run test directly, let me just compile it..."

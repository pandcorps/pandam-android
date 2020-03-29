echo Starting
export GAME_PACKAGE=$1
export GAME_NAME=$2
export PROJ=/c/Users/am202/workspace/pandam-android
export GAME_JAR=$PROJ/lib/$GAME_NAME.jar
export JDK_PATH="/c/Program Files/Java/jdk1.8.0_92/bin"
export SDK_PATH=/c/Users/am202/AppData/Local/Android/Sdk
export ANDROID_JAR=$SDK_PATH/platforms/android-27/android.jar
export SDK_BUILD=$SDK_PATH/build-tools/27.0.3
export JKS=/c/Personal/Games/Platform/release.nfo.jks

if [ -z "$GAME_NAME" ]; then
  echo Game must be specified
  exit 1
fi

cd $PROJ

echo Preparing environment for $GAME_NAME
META_FILES=(
  "AndroidManifest.xml"
  "res/values/strings.xml"
)
for META_FILE in ${META_FILES[@]}; do
  cp game/$GAME_PACKAGE/$META_FILE $META_FILE
done

$SDK_BUILD/aapt package -f -m -J $PROJ/src -M $PROJ/AndroidManifest.xml -S $PROJ/res -I $ANDROID_JAR
echo Running javac for Android-specific java files
"$JDK_PATH"/javac -d obj -cp "$GAME_JAR" -bootclasspath $ANDROID_JAR src/org/pandcorps/$GAME_PACKAGE/*.java src/org/pandcorps/pandam/android/*.java
if [[ $? != 0 ]]; then
    echo Error running javac
    exit 1
fi

echo Running dx
$SDK_BUILD/dx.bat --dex --output=$PROJ/bin/classes.dex $GAME_JAR $PROJ/obj
if [[ $? != 0 ]]; then
    echo Error running dx
    exit 1
fi

echo Creating unaligned APK
$SDK_BUILD/aapt package -f -m -F $PROJ/bin/$GAME_NAME.unaligned.apk -M $PROJ/AndroidManifest.xml -S $PROJ/res -I $ANDROID_JAR
if [[ $? != 0 ]]; then
    echo Error creating unaligned APK
    exit 1
fi

cp $PROJ/bin/classes.dex .
$SDK_BUILD/aapt add $PROJ/bin/$GAME_NAME.unaligned.apk classes.dex

if [ "$GAME_JAR" -nt "work/org" ]; then
  echo Removing previous jar contents
  rm -Rf work/org
  echo Extracting jar contents
  unzip $GAME_JAR -o -d work
fi

echo Adding jar contents to APK
cd work
for FILE in $(find org -type f | grep -v .class)
do
  $SDK_BUILD/aapt add $PROJ/bin/$GAME_NAME.unaligned.apk "$FILE"
done
cd ..

echo Aligning APK
$SDK_BUILD/zipalign -f 4 $PROJ/bin/$GAME_NAME.unaligned.apk $PROJ/bin/$GAME_NAME.apk
echo Signing APK
$SDK_BUILD/apksigner.bat sign --ks $JKS $PROJ/bin/$GAME_NAME.apk
echo Finished

echo Starting
export GAME_PACKAGE=$1
export GAME_NAME=$2
export PROJ=/c/Git/pandam-android
export GAME_JAR=$PROJ/lib/$GAME_NAME.jar
export JAVA_HOME="/c/Program Files/Java/jdk1.8.0_251"
export APKSIGNER_JAVA_HOME="/c/Program Files/Java/jdk-9.0.4"
export JDK_PATH="$JAVA_HOME/bin"
export SDK_PATH=/c/Users/am202/AppData/Local/Android/Sdk
export ANDROID_JAR=$SDK_PATH/platforms/android-30/android.jar
export SDK_BUILD=$SDK_PATH/build-tools/30.0.0
export JKS=/c/Personal/Games/Platform/release.nfo.jks

if [ -z "$GAME_NAME" ]; then
  echo Game must be specified
  exit 1
fi

cd $PROJ

DIRS=(
  "res"
  "res/values"
  "res/drawable-ldpi"
  "res/drawable-mdpi"
  "res/drawable-tvdpi"
  "res/drawable-hdpi"
  "res/drawable-xhdpi"
  "res/drawable-xxhdpi"
  "res/drawable-xxxhdpi"
  "obj"
)
for DIR in ${DIRS[@]}; do
  if [ ! -d "$DIR" ]; then
    mkdir "$DIR"
  fi
done

echo Preparing environment for $GAME_NAME
META_FILES=(
  "AndroidManifest.xml"
  "res/values/strings.xml"
  "res/drawable-ldpi/ic_launcher.png"
  "res/drawable-mdpi/ic_launcher.png"
  "res/drawable-tvdpi/ic_launcher.png"
  "res/drawable-hdpi/ic_launcher.png"
  "res/drawable-xhdpi/ic_launcher.png"
  "res/drawable-xxhdpi/ic_launcher.png"
  "res/drawable-xxxhdpi/ic_launcher.png"
)
for META_FILE in ${META_FILES[@]}; do
  cp game/$GAME_PACKAGE/$META_FILE $META_FILE
  if [[ $? != 0 ]]; then
    echo Error preparing $META_FILE
    exit 1
  fi
done

echo Running aapt package
$SDK_BUILD/aapt package -f -m -J $PROJ/src -M $PROJ/AndroidManifest.xml -S $PROJ/res -I $ANDROID_JAR
if [[ $? != 0 ]]; then
  echo Error running aapt package
  exit 1
fi

echo Running javac for Android-specific java files
"$JDK_PATH"/javac -d obj -cp "$GAME_JAR" -source 1.8 -target 1.8 -bootclasspath $ANDROID_JAR src/org/pandcorps/$GAME_PACKAGE/*.java src/org/pandcorps/pandam/android/*.java
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
  unzip -o $GAME_JAR -d work
fi

echo Adding jar contents to APK
cd work
find org -type f | grep -v .class | xargs $SDK_BUILD/aapt add $PROJ/bin/$GAME_NAME.unaligned.apk
cd ..

echo Aligning APK
$SDK_BUILD/zipalign -f 4 $PROJ/bin/$GAME_NAME.unaligned.apk $PROJ/bin/$GAME_NAME.apk

echo Signing APK
export JAVA_HOME="$APKSIGNER_JAVA_HOME"
while true; do
  $SDK_BUILD/apksigner.bat sign --ks $JKS $PROJ/bin/$GAME_NAME.apk
  if [[ $? == 0 ]]; then
    break
  fi
done

echo Finished

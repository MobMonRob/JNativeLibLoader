#!/bin/bash

cd "$(dirname "$BASH_SOURCE")"

./clear.sh

mkdir -p ./target/

gcc -shared -fPIC -x c -O3 -flto ./src/common/*.c ./src/unix/*.c -o ./target/libgluegen_rt.so -I./include/native -I./include/native/Unix -I./include/stub_includes/platform -I/usr/lib/jvm/default-java/include/linux -I/usr/lib/jvm/default-java/include -Wl,-rpath,'$ORIGIN/.'

#-LDataStreamSDK_1.10 -lViconDataStreamSDK_CPP

echo "gcc finished"

#Has to be: <Project Folder>/natives/<os-arch> !!
mkdir -p ../NativeLibLoader/natives/linux-amd64/
cp ./target/libgluegen_rt.so ../NativeLibLoader/natives/linux-amd64/

echo "NativeGenerator_Linux64 finished"


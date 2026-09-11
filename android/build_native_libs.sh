#!/usr/bin/env bash
set -e

NDK_DIR="/home/willygailo/Android/Sdk/ndk/27.0.12077973"
CMAKE_BIN="/usr/bin/cmake"
STRIP_BIN="${NDK_DIR}/toolchains/llvm/prebuilt/linux-x86_64/bin/llvm-strip"
SRC_DIR="/home/willygailo/Documents/Game-Launcher/android/app/src/main/cpp"
JNILIBS_DIR="/home/willygailo/Documents/Game-Launcher/android/app/src/main/jniLibs"

ABIS=("arm64-v8a" "armeabi-v7a" "x86_64")

for ABI in "${ABIS[@]}"; do
    echo "=== Building native library for ABI: ${ABI} ==="
    BUILD_DIR="/tmp/build_native_${ABI}"
    rm -rf "${BUILD_DIR}"
    mkdir -p "${BUILD_DIR}"

    "${CMAKE_BIN}" -B "${BUILD_DIR}" \
        -DCMAKE_TOOLCHAIN_FILE="${NDK_DIR}/build/cmake/android.toolchain.cmake" \
        -DANDROID_ABI="${ABI}" \
        -DANDROID_PLATFORM=android-33 \
        -DANDROID_STL=c++_static \
        -DCMAKE_BUILD_TYPE=Release \
        "${SRC_DIR}"

    "${CMAKE_BIN}" --build "${BUILD_DIR}" --config Release -j$(nproc)

    TARGET_DIR="${JNILIBS_DIR}/${ABI}"
    mkdir -p "${TARGET_DIR}"

    cp "${BUILD_DIR}/libgamebooster_native.so" "${TARGET_DIR}/libgamebooster_native.so"
    "${STRIP_BIN}" --strip-unneeded "${TARGET_DIR}/libgamebooster_native.so"

    echo "Successfully updated: ${TARGET_DIR}/libgamebooster_native.so"
    ls -lh "${TARGET_DIR}/libgamebooster_native.so"
done

echo "=== All ABIs compiled and updated successfully! ==="

cmake_minimum_required(VERSION 3.22.1)

project("gamebooster_native")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(
    gamebooster_native
    SHARED
    native_config_injector.cpp
)

target_compile_options(
    gamebooster_native
    PRIVATE
    -O3
    -fvisibility=hidden
    -ffunction-sections
    -fdata-sections
    -fno-rtti
    -fno-exceptions
    -fomit-frame-pointer
    -flto=thin
)

# Android 15 & 16 16KB page-size kernel compatibility alignment + gc-sections + LTO + strip
target_link_options(
    gamebooster_native
    PRIVATE
    "-Wl,-z,max-page-size=16384"
    "-Wl,--gc-sections"
    "-Wl,--strip-all"
    "-flto=thin"
)

find_library(
    log-lib
    log
)

target_link_libraries(
    gamebooster_native
    ${log-lib}
)

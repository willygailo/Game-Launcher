cmake_minimum_required(VERSION 3.22.1)

project("gamebooster_native")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(
    gamebooster_native
    SHARED
    config_common.cpp
    native_config_injector.cpp
    mlbb_injector.cpp
    pubgm_injector.cpp
    codm_injector.cpp
    other_games_injector.cpp
    universal_combat_injector.cpp
)

target_compile_options(
    gamebooster_native
    PRIVATE
    -O3
    -ffunction-sections
    -fdata-sections
)

# Android 15 & 16 16KB page-size kernel compatibility alignment + gc-sections
target_link_options(
    gamebooster_native
    PRIVATE
    "-Wl,-z,max-page-size=16384"
    "-Wl,--gc-sections"
)

find_library(
    log-lib
    log
)

target_link_libraries(
    gamebooster_native
    ${log-lib}
)

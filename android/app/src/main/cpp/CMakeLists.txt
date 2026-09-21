cmake_minimum_required(VERSION 3.22.1)

project("gamebooster_native")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(
    gamebooster_native
    SHARED
    config_common.cpp
    native_config_injector.cpp
    native_lua_engine.cpp
    mlbb_injector.cpp
    pubgm_injector.cpp
    codm_injector.cpp
    other_games_injector.cpp
    universal_combat_injector.cpp
    native_security_bypass.cpp
)

target_compile_options(
    gamebooster_native
    PRIVATE
    -O3
    -flto
    -ffunction-sections
    -fdata-sections
    -fvisibility=hidden
    -fvisibility-inlines-hidden
    -fno-exceptions
    -fno-rtti
    -fomit-frame-pointer
    # Fix N4: NDEBUG suppresses assert() in release; Wall+Wextra surface silent bugs
    -DNDEBUG
    -Wall
    -Wextra
    -Wno-unused-parameter
    -Wno-unused-variable
    -Wno-unused-but-set-variable
)

# Android 15 & 16 16KB page-size kernel compatibility alignment + gc-sections + LTO
target_link_options(
    gamebooster_native
    PRIVATE
    "-flto"
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

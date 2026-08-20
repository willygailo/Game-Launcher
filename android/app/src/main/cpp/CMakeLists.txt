cmake_minimum_required(VERSION 3.22.1)

project("gamebooster_native")

add_library(
    gamebooster_native
    SHARED
    native_config_injector.cpp
)

find_library(
    log-lib
    log
)

target_link_libraries(
    gamebooster_native
    ${log-lib}
)

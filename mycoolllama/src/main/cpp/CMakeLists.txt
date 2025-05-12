cmake_minimum_required(VERSION 3.22.1)

project(mcl-jni LANGUAGES CXX C)

set(LLAMA_DIR llama.cpp)

add_subdirectory(${LLAMA_DIR} build-llama)

add_library(${CMAKE_PROJECT_NAME} SHARED
        mcl-context.cpp
        mcl-jni.cpp
)

target_link_libraries(${CMAKE_PROJECT_NAME} PRIVATE
        llama
        common
        android
        log)

cmake_minimum_required(VERSION 3.22.1)

project(mcl-jni LANGUAGES CXX C)

add_subdirectory(llama.cpp build-llama)

add_library(${CMAKE_PROJECT_NAME} SHARED
        mcl-context.cpp
        mcl-jni.cpp)

target_link_libraries(${CMAKE_PROJECT_NAME} PRIVATE
        llama
        common
        android
        log)

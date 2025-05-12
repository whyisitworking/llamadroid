cmake_minimum_required(VERSION 3.22.1)

project(llama-adapter LANGUAGES CXX C)

set(LLAMA_DIR llama.cpp)

# Include llama.cpp as a subproject
add_subdirectory(${LLAMA_DIR} build-llama)

# Create your application library
add_library(${CMAKE_PROJECT_NAME} SHARED
        # List C/C++ source files with relative paths to this CMakeLists.txt.
        llama-bridge.cpp
        llama-session.cpp
        llama-completion.cpp
)

# Link against the llama libraries
target_link_libraries(${CMAKE_PROJECT_NAME} PRIVATE
        # List libraries link to the target library
        llama
        common
        android
        log)

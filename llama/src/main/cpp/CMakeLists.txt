cmake_minimum_required(VERSION 3.31.6)

project(mcl-jni LANGUAGES CXX C)

###############################################################################
#                     LLAMA-SPECIFIC OPTIONS FOR ANDROID                      #
###############################################################################

# ======================= CORE COMPONENTS ===========================
set(LLAMA_BUILD_COMMON ON CACHE BOOL "Build common components for basic functionality" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "Build server components" FORCE)

# ======================= NETWORK OPTIONS ===========================
set(LLAMA_CURL OFF CACHE BOOL "Enable CURL for network operations" FORCE)

# ======================= DISABLE UNUSED FEATURES ===================
set(LLAMA_SANITIZE_THREAD OFF CACHE BOOL "Enable thread sanitizer" FORCE)
set(LLAMA_SANITIZE_ADDRESS OFF CACHE BOOL "Enable address sanitizer" FORCE)
set(LLAMA_SANITIZE_UNDEFINED OFF CACHE BOOL "Enable undefined behavior sanitizer" FORCE)

###############################################################################
#                         GGML OPTIONS FOR ANDROID                            #
###############################################################################

# ======================= CORE CONFIGURATION ===========================
set(GGML_NATIVE OFF CACHE BOOL "Optimize for the host machine" FORCE)
set(GGML_STATIC OFF CACHE BOOL "Build static libraries" FORCE)
set(GGML_LTO ON CACHE BOOL "Enable Link Time Optimization" FORCE)
set(GGML_OPENMP ON CACHE BOOL "Enable OpenMP for multi-threading" FORCE)

# ======================= PLATFORM-SPECIFIC ==========================
set(GGML_CPU ON CACHE BOOL "Enable CPU backend" FORCE)
set(GGML_CPU_AARCH64 ON CACHE BOOL "Enable ARM64 specific optimizations" FORCE)

# ======================= GPU ACCELERATION ===========================
set(GGML_VULKAN OFF CACHE BOOL "Enable Vulkan GPU backend" FORCE)
set(GGML_VULKAN_USE_PREBUILT_SHADERS OFF CACHE BOOL "Use pre-built Vulkan shaders" FORCE)
set(GGML_VULKAN_VALIDATE OFF CACHE BOOL "Enable Vulkan validation" FORCE)

# ======================= DISABLED BACKENDS ==========================
set(GGML_ACCELERATE OFF CACHE BOOL "Enable Apple Accelerate framework" FORCE)
set(GGML_METAL OFF CACHE BOOL "Enable Metal GPU backend" FORCE)
set(GGML_CUDA OFF CACHE BOOL "Enable CUDA GPU backend" FORCE)
set(GGML_HIP OFF CACHE BOOL "Enable AMD HIP GPU backend" FORCE)
set(GGML_SYCL OFF CACHE BOOL "Enable SYCL backend" FORCE)
set(GGML_OPENCL OFF CACHE BOOL "Enable OpenCL backend" FORCE)
set(GGML_LLAMAFILE OFF CACHE BOOL "Enable llamafile packaging" FORCE)

# ======================= BUILD CONFIGURATION =======================
set(GGML_BUILD_TESTS OFF CACHE BOOL "Build tests" FORCE)
set(GGML_BUILD_EXAMPLES OFF CACHE BOOL "Build examples" FORCE)

set(CMAKE_BUILD_TYPE MinSizeRel CACHE STRING "Debug, Release, MinSizeRel" FORCE)

add_subdirectory(external/llama.cpp)

add_library(${CMAKE_PROJECT_NAME} SHARED
        mcl-context.cpp
        mcl-jni.cpp)

target_link_libraries(${CMAKE_PROJECT_NAME} PRIVATE
        llama
        common
        android
        log)
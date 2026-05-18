cmake_minimum_required(VERSION 3.22)
project(english-reader-jni CXX)

set(CMAKE_CXX_STANDARD 17)

# llama.cpp prebuilt
add_library(llama SHARED IMPORTED)
set_target_properties(llama PROPERTIES
    IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libllama.so
)

add_library(ggml SHARED IMPORTED)
set_target_properties(ggml PROPERTIES
    IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libggml.so
)

add_library(ggml-base SHARED IMPORTED)
set_target_properties(ggml-base PROPERTIES
    IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libggml-base.so
)

# MNN prebuilt
add_library(MNN SHARED IMPORTED)
set_target_properties(MNN PROPERTIES
    IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libMNN.so
)

add_library(MNN_Express SHARED IMPORTED)
set_target_properties(MNN_Express PROPERTIES
    IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libMNN_Express.so
)

add_library(diffusion SHARED IMPORTED)
set_target_properties(diffusion PROPERTIES
    IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libdiffusion.so
)

add_library(mnn-llm SHARED IMPORTED)
set_target_properties(mnn-llm PROPERTIES
    IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libllm.so
)

# llama.cpp headers
set(LLAMA_CPP_DIR "F:/solo/llama.cpp")
include_directories(
    ${LLAMA_CPP_DIR}/include
    ${LLAMA_CPP_DIR}/ggml/include
    ${CMAKE_SOURCE_DIR}/mnn-include
)

# JNI bridge for llama
add_library(llama-android SHARED llama-android.cpp)
target_compile_definitions(llama-android PRIVATE GGML_BACKEND_DL)
target_link_libraries(llama-android llama ggml ggml-base log)

# JNI bridge for diffusion
add_library(diffusion-android SHARED diffusion-jni.cpp)
target_link_libraries(diffusion-android diffusion MNN MNN_Express mnn-llm log)

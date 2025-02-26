#!/bin/bash

# Define common path variables
BUILD_DIR="./build/linux-x86_64-normal-server-release/hotspot/variant-server"
OBJS_DIR="$BUILD_DIR/libjvm/objs"
SUPPORT_DIR="$BUILD_DIR/support/adlc"
MAKE_HOTSPOT_DIR="./make/hotspot"
SRC_DIR="./src/hotspot/cpu/x86"

echo "01: Copying cpp and header files to objs and make/hotspot directories..."

# Copy adlc-related files in batches to the target directories
cp $SUPPORT_DIR/ad_x86.* $OBJS_DIR/
cp $SUPPORT_DIR/ad_x86.* $MAKE_HOTSPOT_DIR/
cp $SUPPORT_DIR/ad_x86_gen.cpp $MAKE_HOTSPOT_DIR/
cp $SUPPORT_DIR/ad_x86_expand.cpp $MAKE_HOTSPOT_DIR/
cp $SUPPORT_DIR/ad_x86_misc.cpp $MAKE_HOTSPOT_DIR/
cp $SUPPORT_DIR/ad_x86_clone.cpp $MAKE_HOTSPOT_DIR/
cp $SUPPORT_DIR/dfa_x86.cpp $MAKE_HOTSPOT_DIR/
cp $SUPPORT_DIR/ad_x86_pipeline.cpp $MAKE_HOTSPOT_DIR/
cp $SUPPORT_DIR/ad* $MAKE_HOTSPOT_DIR/

# Copy x86_64.ad files to multiple directories
cp $SRC_DIR/x86_64.ad $OBJS_DIR/
cp $SRC_DIR/x86_64.ad $OBJS_DIR/src/hotspot/cpu/x86/

# Recursively copy the src folder to the target directories
cp -r ./src $OBJS_DIR/
cp -r ./src $MAKE_HOTSPOT_DIR/

echo "02: Processing .cmdline files to copy additional cpp files..."

# Find all .cmdline files and process their contents
find $OBJS_DIR -name "*.cmdline" | while read cmd_file; do
    # Read each line of the .cmdline file
    while read -r line; do
        # Extract the possible cpp file path
        cpp_path=$(echo "$line" | rev | cut -d " " -f 1 | sed 's/ //g' | rev)

        # If the path is valid and not "true", copy the file to the target directory
        if [ "$cpp_path" != "true" ] && [ -f "$cpp_path" ]; then
            echo "Copying $cpp_path to $OBJS_DIR/"
            cp "$cpp_path" "$OBJS_DIR/"
        fi
    done < "$cmd_file"
done

rsync -av --exclude="build/linux-x86_64-normal-server-release/hotspot/variant-server/libjvm/objs/build" build/ build/linux-x86_64-normal-server-release/hotspot/variant-server/libjvm/objs/build/

echo "All tasks completed successfully."
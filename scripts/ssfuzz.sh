#!/bin/bash

# Parameter 1: Log file name
# Parameter 2: NUM, a string parameter
# Parameter 3: INDEX
LOG_FILE="$1"
NUM="$2"
INDEX="$3"

# Logging function
function log() {
    # Get the current time, add 8 hours, and format as yyyy-mm-dd HH:MM:SS
    local timestamp=$(date -d "+8 hour" "+%Y-%m-%d %H:%M:%S")
    # Append log information to the file
    echo "$timestamp - $@" >> "$LOG_FILE"
}

# Log the start time of the script execution
log "Script execution started"

# Execute the first command
lcov_cmd="lcov -d /root/ssfuzz/00JVMs/compiledSources/coverage/jdk11u-dev/build/linux-x86_64-normal-server-release/hotspot/variant-server/libjvm/objs --rc lcov_branch_coverage=1 --gcov-tool /usr/bin/gcov-7 -c -o /root/ssfuzz/workline/cover_code/mm${NUM}/output${NUM}group${INDEX}.info"
log "Executing command: $lcov_cmd"
eval "$lcov_cmd"
log "Command execution completed"

# Execute the second command
genhtml_cmd="genhtml --rc genhtml_branch_coverage=1 -o /root/ssfuzz/workline/cover_code/mm${NUM}/HtmlFile${INDEX} /root/shannonfuzz-python/fuzz/coverage/mm${NUM}/output${NUM}group${INDEX}.info >> /root/ssfuzz/workline/cover_code/mm${NUM}/all${INDEX}.txt"
log "Executing command: $genhtml_cmd"
eval "$genhtml_cmd"
log "Command execution completed"

# Log the end time of the script execution
log "Script execution ended"

# Uncomment the following lines if needed
# find /root/ssfuzz/00JVMs/compiledSources/jdk11u-dev -name "*.gcno" -o -name "*.gcda" -exec rm -f {} +

# git clone https://github.com/openjdk/jdk11u-dev.git
# bash configure --enable-native-coverage --disable-warnings-as-errors --with-boot-jdk=/root/ssfuzz/00JVMs/bootJDKs/jdk-11.0.1/
# make images
# rsync -av --exclude="build/linux-x86_64-normal-server-release/hotspot/variant-server/libjvm/objs/build" build/ build/linux-x86_64-normal-server-release/hotspot/variant-server/libjvm/objs/build/
# lcov --capture --no-external --rc lcov_branch_coverage=1 --gcov-tool /usr/bin/gcov-7 --directory /root/ssfuzz/00JVMs/compiledSources/jdk11u-dev/build --output-file coverage.info
# lcov --capture --gcov-tool /usr/bin/gcov-7 --directory . --output-file coverage.info
# lcov -d ./ --rc lcov_branch_coverage=1 --gcov-tool /usr/bin/gcov-7 -c --no-recursion -o /root/ssfuzz/00JVMs/compiledSources/jdk11u-dev/output.info
# genhtml --rc genhtml_branch_coverage=1 -o HtmlFile coverage.info

# Example Java HelloWorld program
# class HelloWorld {
#     // Your program begins with a call to main().
#     // Prints "Hello, World" to the terminal window.
#     public static void main(String[] args) {
#         System.out.println("Hello, World");
#     }
# }

# Copy files if needed
# cp ./build/linux-x86_64-normal-server-release/hotspot/variant-server/support/adlc/ad_x86.hpp .
# cp ./build/linux-x86_64-normal-server-release/hotspot/variant-server/support/adlc/ad_x86.cpp .
# cp ./build/linux-x86_64-normal-server-release/hotspot/variant-server/support/adlc/ad_x86_clone.cpp .
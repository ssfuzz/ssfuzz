#!/bin/bash

# Parameter 1: Log file name
# Parameter 2: NUM, a string type parameter
LOG_FILE="$1"
NUM="$2"
INDEX="$3"

# Log recording function
function log() {
    # Use the date command to get the current time, add 8 hours, and output in the format yyyy-mm-dd HH:MM:SS
    local timestamp=$(date -d "+8 hour" "+%Y-%m-%d %H:%M:%S")
    # Output log information to the file
    echo "$timestamp - $@" >> "$LOG_FILE"
}

# Record the start time of the script execution
log "Script starts executing"
# hotspot/variant-server/libjvm/objs
# Execute the first command
lcov_cmd="lcov -b /root/ssfuzz/00JVMs/compiledSources/jdk11u-dev -d /root/ssfuzz/00JVMs/compiledSources/jdk11u-dev/build/linux-x86_64-normal-server-release --rc lcov_branch_coverage=1 --gcov-tool /usr/bin/gcov-7 -c -o /root/ssfuzz/workline/cover_code/mm${NUM}/output${NUM}group${INDEX}.info"
log "Executing command: $lcov_cmd"
eval "$lcov_cmd"
log "Command execution completed"

# Execute the second command
genhtml_cmd="genhtml --rc genhtml_branch_coverage=1 -o /root/ssfuzz/workline/cover_code/mm${NUM}/HtmlFile${INDEX} /root/ssfuzz/workline/cover_code/mm${NUM}/output${NUM}group${INDEX}.info >> /root/ssfuzz/workline/cover_code/mm${NUM}/all${INDEX}.txt"
log "Executing command: $genhtml_cmd"
eval "$genhtml_cmd"
log "Command execution completed"

## Execute the third command
#lcov_cmd="lcov -d /root/jvm/openjdk11/jdk-11.0.14-ga -z"
#log "Executing command: $lcov_cmd"
#eval "$lcov_cmd"
#log "Command execution completed"

# Record the end time of the script execution
log "Script execution ended"
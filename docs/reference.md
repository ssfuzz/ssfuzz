# Reference

## JVMs 
> There is already a compiled jvm in the link, the following steps are for expansion and are not necessary to execute the steps

> The following differential test engines are available

OpenJDK(HotSpot), IBM-OpenJ9, Azul-Zulu JVM, Oracle-GraalVM, Tencent-Kona

### OpenJDK(HotSpot)

```
Source Code Download：
https://openjdk-sources.osci.io/openjdk8/
https://openjdk-sources.osci.io/openjdk11/

Engine Recent Updates View:
https://mail.openjdk.java.net/pipermail/jdk8u-dev/
https://mail.openjdk.java.net/pipermail/jdk-updates-dev/

Openjdk all updates log:
https://builds.shipilev.net/backports-monitor/

Java report submission system:
https://bugs.openjdk.java.net/secure/Dashboard.jspa

Installation commands: (use chmod +x . /configure to increase script access)
jdk8：
./configure (It may be necessary to install jdk as bootjdk with the command sudo apt-get install openjdk-8-jdk)
make all
 
Check the version:
./build/*/jdk/bin/java -version
jdk11:
First check if there is a bootjdk available, you need to download the N-1 or N-2 version of the jdk, add the path after --with-boot-jdk../configure --with-boot-jdk=/root/jvm/jdk-10
make images
OR make all
Check the version:
./build/*/images/jdk/bin/java -version

```

### IBM-OpenJ9

```
Source code download: “Get source code” step to update all repositories

https://github.com/eclipse-openj9/openj9
https://github.com/ibmruntimes/openj9-openjdk-jdk8
https://github.com/eclipse-openj9/openj9-omr

Installation Tutorial:
https://github.com/eclipse-openj9/openj9/tree/master/doc/build-instructions/Build_Instructions_V8.md
https://github.com/eclipse-openj9/openj9/blob/master/doc/build-instructions/Build_Instructions_V11.md

Installation Steps:
Get environment dependency information:
wget https://raw.githubusercontent.com/eclipse-openj9/openj9/master/buildenv/docker/mkdocker.sh
bash mkdocker.sh --tag=openj9 --dist=ubuntu --version=18.04 --print
Configuration dependencies:
sudo apt-get install -qq -y --no-install-recommends software-properties-common
sudo add-apt-repository ppa:ubuntu-toolchain-r/test
sudo apt-get install -qq -y --no-install-recommends ant ant-contrib autoconf build-essential ca-certificates cmake cpio curl file g++-7 gcc-7 gdb git libasound2-dev libcups2-dev libdwarf-dev libelf-dev libexpat1-dev libffi-dev libfontconfig  libfontconfig1-dev libfreetype6-dev libnuma-dev libssl-dev libx11-dev libxext-dev libxrandr-dev libxrender-dev libxt-dev libxtst-dev nasm openssh-client openssh-server perl pkg-config systemtap-sdt-dev wget xvfb zlib1g-dev
sudo rm -rf /var/lib/apt/lists/*
export CC=gcc-7 CXX=g++-7
Boot jdk configuration:
wget -O bootjdk8.tar.gz https://api.adoptopenjdk.net/v3/binary/latest/8/ga/linux/x64/jdk/openj9/normal/adoptopenjdk
tar -xzf bootjdk8.tar.gz
rm -f bootjdk8.tar.gz
mv $(ls | grep -i jdk8) bootjdk8

Get the source code (including the latest versions of the openj9-openjdk-jdk8, omr and openj9 repositories):
git clone https://github.com/ibmruntimes/openj9-openjdk-jdk8.git
cd openj9-openjdk-jdk8
bash get_source.sh
Compile and install:
bash configure --with-boot-jdk=/root/jvm/bootjdk8
make all
Check the version:
cd build/linux-x86_64-normal-server-release/images/j2re-image
./bin/java -version
The above output includes version information for each repository, for example:
OpenJ9   - 68d6fdb
OMR      - 7c3d3d7
OpenJDK  - 27f5b8f based on jdk8u152-b03
```

### Azul-Zulu JVM

```
Source code download:
https://www.azul.com/downloads/?version=java-8-lts&os=ubuntu&architecture=x86-64-bit&package=jdk
https://www.azul.com/downloads/?version=java-11-lts&os=ubuntu&architecture=x86-64-bit&package=jdk
Select the corresponding Java Version, Operating System, Architecture, and Java Package, and download the .tar.gz
format installation package.

```

### Oracle-GraalVM

```
Release Update Log:
https://www.graalvm.org/release-notes/version-roadmap/
https://www.graalvm.org/release-notes/21_3/
https://www.graalvm.org/release-notes/21_2/
Source download:
Official website: https://www.graalvm.org/downloads/
Github：
https://github.com/graalvm/graalvm-ce-builds/releases/tag/vm-21.3.0
https://github.com/graalvm/graalvm-ce-builds/releases/tag/vm-21.2.0
Select the linux version from the list of “Java 11 based”, “Java 17 based”, “Java 8 based” to download the installer.
```

### Tencent-Kona

```
Source code download:
https://github.com/Tencent/TencentKona-8/releases
https://github.com/Tencent/TencentKona-11/releases
Unzip directly
```


---------------

### jvm installation and coverage staking

1. `apt update && apt upgrade`
2. ```shell
    apt install -y autoconf libx11-dev libxext-dev libxrender-dev \
    libxtst-dev libxt-dev libasound2-dev libcups2-dev \
    libfontconfig1-dev unzip zip apt-utils autoconf build-essential file \
    libxrandr-dev lcov

3. Install jdk

    ```
    1. wget https://download.java.net/java/GA/jdk20.0.2/6e380f22cbe7469fa75fb448bd903d8e/9/GPL/openjdk-20.0.2_linux-x64_bin.tar.gz
    2. tar -zxvf openjdk-20.0.2_linux-x64_bin.tar.gz
    3. rm openjdk-20.0.2_linux-x64_bin.tar.gz  
    ```

    ```
    1. wget https://download.oracle.com/java/17/latest/jdk-17_linux-x64_bin.tar.gz
    2. tar -zxvf jdk-17_linux-x64_bin.tar.gz  
    3. rm jdk-17_linux-x64_bin.tar.gz
    ```

4. Compile the latest version of openjdk directly (cd /root/jvm/).
    ```
    1. git clone https://github.com/openjdk/jdk.git
    2. bash configure --with-boot-jdk=/root/bootjdk/jdk-20.0.2
    3. make images
    ```

5. Coverage statistics
    1. Instrumentation
        1. `git clone https://github.com/openjdk/jdk11u-dev.git`
        2. `bash configure --enable-native-coverage --disable-warnings-as-errors --with-boot-jdk=/root/bootjdk/jdk-11.0.19/`
        3. `make images`
    2. Generation of coverage-related documents
        1. `cd build/linux-x86_64-normal-server-release/hotspot/variant-server/libjvm/objs/`
        2. `lcov -b ./ -d ./ --rc lcov_branch_coverage=1 --gcov-tool /usr/bin/gcov-7 -c -o /root/jvm/jdk11u-dev/output.info`
           [//]: # (        lcov -b ./ -d ./ --rc lcov_branch_coverage=1 --gcov-tool /usr/bin/gcov-7 -c -o /root/jvm/coverage/jdk11u-dev-master/output.info)
        3. Need to copy the file
           The following copy scripts have been integrated into /root/buildjvm/scripts/coverage_copy.sh , if you execute them, you can directly copy them to the directory of the engine to be compiled, there is a build folder after the ls, and you can execute them directly after chmod
           +x can be executed directly after the privilege
       ```shell
           
       # #!/bin/bash
           
       echo "01 copy cpp file to objs ..."
           
       ls /root/ssfuzz/00JVMs/compiledSources/jdk11u-dev/build/linux-x86_64-server-release/hotspot/variant-server/libjvm/objs/*.cmdline | while read file
       do
       cat $file | while read line
       do
       cpppath=`echo $line | rev | cut -d " " -f 1 | sed 's/ //g' | rev`
       if [ "$cpppath" != "true" ]; then
       echo $cpppath
       cp $cpppath /root/ssfuzz/00JVMs/compiledSources/jdk11u-dev/build/linux-x86_64-server-release/hotspot/variant-server/libjvm/objs/
       fi
       done
       done
           
       ```   
       ```
          cp build/linux-x86_64-server-release/hotspot/variant-server/support/adlc/ad_x86.cpp build/linux-x86_64-server-release/hotspot/variant-server/libjvm/objs/
          cp build/linux-x86_64-server-release/hotspot/variant-server/support/adlc/ad_x86.hpp /root/buildjvm/jdk18/build/linux-x86_64-server-release/hotspot/variant-server/libjvm/objs/
          cp src/hotspot/cpu/x86/x86_64.ad /root/buildjvm/jdk18/build/linux-x86_64-server-release/hotspot/variant-server/libjvm/objs/
          cp src/hotspot/cpu/x86/x86_64.ad /root/buildjvm/jdk18/build/linux-x86_64-server-release/hotspot/variant-server/libjvm/objs/src/hotspot/cpu/x86/
          cp -r ./src /root/jvm/coverage/jdk11u-dev-master/build/linux-x86_64-normal-server-release/hotspot/variant-server/libjvm/objs/
          cp -r ./build ./build/linux-x86_64-server-release/hotspot/variant-server/libjvm/objs/
       ```
    4. With output.info, execute the  
       `genhtml --rc genhtml_branch_coverage=1 -o HtmlFile output.info`

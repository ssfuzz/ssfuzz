# This is the test harness for the JVM fuzzer. It takes in a list of test cases and runs them against the JVM fuzzer.

import yaml
import subprocess
import os

# Read the YAML configuration file
config_file = 'jvms.yaml'
with open(config_file, 'r') as file:
    jvms = yaml.safe_load(file)

# HelloWorld program
java_source = 'HelloWorld.java'
java_class = 'HelloWorld'

# Ensure HelloWorld.java exists
java_code = """
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, world!");
    }
}
"""
with open(java_source, 'w') as java_file:
    java_file.write(java_code)

for jvm in list(jvms['jvms'].keys()):
    jvm_java_path = jvms['jvms'][jvm]['java']['path']
    jvm_javac_path = jvms['jvms'][jvm]['javac']['path']
    print(f"\n{jvm}: Compiling and Running HelloWorld.java")

    # Compile the Java file
    compile_cmd = f"{jvm_javac_path} {java_source}"
    compilation_result = subprocess.run(compile_cmd.split(), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    if compilation_result.returncode != 0:
        print(f"Failed to compile with {jvm}. Error: {compilation_result.stderr.decode()}")
        continue
    else:
        print(f"Compilation successful with {jvm}")

    # Run the compiled class file
    run_cmd = f"{jvm_java_path} -cp . {java_class}"
    run_result = subprocess.run(run_cmd.split(), stdout=subprocess.PIPE)
    print(f"Output from {jvm}: {run_result.stdout.decode()}")

# Clean up the .class file
os.remove(f"{java_class}.class")
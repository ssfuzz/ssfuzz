import os
import subprocess
import yaml

# Set the directory path to search (relative path)
search_dir = './compiledSources'  # Relative path

# Path to the result file
yaml_file = 'jvms.yaml'

# Find all executable java and javac files
def find_java_and_javac(search_dir):
    jvm_data = []

    # Traverse each subdirectory in search_dir (i.e., folders for JVM implementation versions)
    for root, dirs, files in os.walk(search_dir):
        # Process only folders containing the 'bin' directory
        if "bin" in dirs:
            java_path = os.path.join(root, 'bin', 'java')
            javac_path = os.path.join(root, 'bin', 'javac')

            # If java and javac are found and executable
            if os.path.exists(java_path) and os.access(java_path, os.X_OK):
                if os.path.exists(javac_path) and os.access(javac_path, os.X_OK):
                    # Get the version
                    java_version = get_version(java_path)
                    jvm_data.append({
                        'java_path': os.path.abspath(java_path),
                        'javac_path': os.path.abspath(javac_path),
                        'java_version': java_version,
                    })

        # Process folders containing `build` or `images`, indicating they are post-compilation directories
        elif "images" in dirs or "build" in dirs:
            # For source code compilation, java and javac are in the images or build directory
            java_path = os.path.join(root, 'images', 'jdk', 'bin', 'java')  # Possible location
            javac_path = os.path.join(root, 'images', 'jdk', 'bin', 'javac')  # Possible location

            if os.path.exists(java_path) and os.access(java_path, os.X_OK):
                if os.path.exists(javac_path) and os.access(javac_path, os.X_OK):
                    # Get the version
                    java_version = get_version(java_path)
                    jvm_data.append({
                        'java_path': os.path.abspath(java_path),
                        'javac_path': os.path.abspath(javac_path),
                        'java_version': java_version,
                    })

    return jvm_data

# Get the version of java or javac
def get_version(executable_path):
    try:
        result = subprocess.run([executable_path, '-version'], capture_output=True, text=True)
        version_line = result.stderr.splitlines()[0]  # `stderr` contains version information
        return version_line
    except Exception as e:
        return "".join(str(e))

# Update the YAML file
def update_yaml(yaml_file, jvm_data):
    with open(yaml_file, 'r') as file:
        yaml_data = yaml.safe_load(file)
    yaml_data = {'jvms': {}}
    # Add new data under `jvms`
    for jvm in jvm_data:
        version_number = jvm['java_path'].split('/')[5]  # Extract version number information
        yaml_data['jvms'][version_number] = {
            'java': {'version': jvm['java_version'], 'path': jvm['java_path']},
            'javac': {'path': jvm['javac_path']}
        }

    # Write the updated content back to the YAML file
    with open(yaml_file, 'w') as file:
        yaml.dump(yaml_data, file, default_flow_style=False)

# Main execution flow
def main():
    # Find all executable java and javac files
    jvm_data = find_java_and_javac(search_dir)

    # Update the YAML file
    update_yaml(yaml_file, jvm_data)
    print(f"The JVM version information has been updated in the {yaml_file} file.")

# Run the main program
if __name__ == "__main__":
    main()
import json, os, sys
SCRIPT_ROOT = os.path.abspath(os.path.dirname(__file__))
PROJECT_ROOT = os.path.abspath(os.path.dirname(SCRIPT_ROOT))

def convert_json_to_properties(json_file, properties_file):
    """
    Read configuration information from a given JSON file and convert it to Properties format, writing it to another file.

    :param json_file: Path to the JSON configuration file
    :param properties_file: Path to the target Properties file
    """
    # Read the JSON file
    with open(json_file, 'r', encoding='utf-8') as f:
        config = json.load(f)

    # Open or create the Properties file for writing
    with open(properties_file, 'w', encoding='utf-8') as prop_f:
        for env, details in config.items():
            # Add a comment separator for each environment
            prop_f.write(f'\n# {env.upper()} Environment Configuration\n')

            # Iterate through all key-value pairs for the current environment and write to the Properties file
            for key, value in details.items():
                # Use the environment name as a prefix to avoid conflicts
                prop_f.write(f'{env}.{key}={value}\n')


if __name__ == '__main__':
    # Specify the input and output file paths
    json_file_path = os.path.join(PROJECT_ROOT, 'config.json')  # Replace with the actual path to your JSON file
    properties_file_path = os.path.join(PROJECT_ROOT, '01seedGenerate/GenerateTestcases/src/main/resources/db-config.properties')  # Replace with the actual path where you want to save the Properties file
    # Perform the conversion
    convert_json_to_properties(json_file_path, properties_file_path)
    print("Conversion completed!")
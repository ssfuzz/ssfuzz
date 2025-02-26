import os
import re

# Input file path
# name = "gpt-3.5-turbo"
# name = "doubao-lite-128k"
# name = "o1-mini"
# name = "o3-mini"
# name = "moonshot-v1-8k"
# name = "claude-3-5-haiku-20241022" 
# name = "claude-3-5-sonnet-20240620" 
# name = "deepseek-r1"
# name = "deepseek-v3"
# name = "gpt-4o"
# name = "gpt-3.5-turbo"
name = "llama-3.1-405b-instruct"

input_file = "llm/log/{}-result.txt".format(name)
output_dir = "llm/models/testcase/{}".format(name)

# Create output directory if it doesn't exist
os.makedirs(output_dir, exist_ok=True)

# Counter for the number of extracted test cases
test_case_count = 0

# Open the input file and read it line by line
with open(input_file, "r", encoding="utf-8") as file:
    lines = file.readlines()

# Regular expression patterns for matching Java code blocks and class names
java_code_pattern = re.compile(r"```java(.*?)```", re.DOTALL)
class_name_pattern = re.compile(r"public\s+class\s+(\w+)")

# Temporary storage for the current test case content
current_test_case = []
inside_test_case = False

# Iterate through each line to extract test cases
for line in lines:
    # Skip lines containing "--- Result ---" and the line following it
    if line.strip() == "--- Result ---":
        inside_test_case = False
        continue
    if line.strip().startswith("---") and line.strip().endswith("---"):
        inside_test_case = False
        continue

    # Detect the start of a test case
    if line.strip().startswith("```java"):
        inside_test_case = True
        current_test_case = []
        continue

    # Detect the end of a test case
    if line.strip().startswith("```") and inside_test_case:
        inside_test_case = False

        # Join the test case content into a complete code snippet
        test_case_code = "".join(current_test_case)

        # Extract the class name
        class_name_match = class_name_pattern.search(test_case_code)
        if class_name_match:
            test_case_count += 1
            class_name = class_name_match.group(1)
            test_case_name = f"{class_name}.java"
        else:
            # Use a default naming scheme if no class name is found
            test_case_count += 1
            test_case_name = f"MyJVMTest_{test_case_count}.java"

        # Save the file
        output_path = os.path.join(output_dir, test_case_name)

        with open(output_path, "w", encoding="utf-8") as output_file:
            output_file.write(test_case_code)

        print(f"Test case saved: {output_path}")
        continue

    # If inside a test case, record its content
    if inside_test_case:
        current_test_case.append(line)

print(f"A total of {test_case_count} test cases have been extracted and saved to the directory: {output_dir}")
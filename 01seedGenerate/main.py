import os
import subprocess
import sys
import time
from pathlib import Path
from functools import wraps
from step03_generate_seed import get_args, print_or_write
import threading

seedGenerate_path = str(Path(__file__).resolve().parent)
sys.path.append(seedGenerate_path)


def timeout_decorator(timeout_minutes):
    def decorator(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            should_stop = {"value": False}  # Timeout flag

            # Timeout thread
            def set_timeout_flag():
                print("Timeout flag has been triggered.")
                should_stop["value"] = True

            timer = threading.Timer(timeout_minutes * 60, set_timeout_flag)  # Start the timer
            timer.start()

            try:
                result = func(*args, should_stop, **kwargs)
            finally:
                timer.cancel()  # Ensure the timer is canceled after the function ends

            return result
        return wrapper
    return decorator


def run_jar(jar_path):
    command = ['java', '-jar', jar_path]
    process = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    stdout, stderr = process.communicate()
    return stdout, stderr


def step1_generate_function():
    # 1 Fine-tune model generation
    print("Generating functions.............")
    args = get_args(prompt="import java", num_return_sequences=100)
    print_or_write(args, 0, 10)


def step2_generate_seed():
    # 2 Concatenate methods into complete seeds
    grammar_path = os.path.join(seedGenerate_path,
                                "GenerateTestcases/target/seedGenerate-1.0-SNAPSHOT-shaded.jar")
    stdout, stderr = run_jar(grammar_path)
    return stdout, stderr
    # print(stdout.decode('utf-8'))


def step3_check_seed():
    # 3 Check the correctness of the seeds
    grammar_path = os.path.join(seedGenerate_path,
                                "GrammaCheck/target/GrammaCheck-1.0-SNAPSHOT-shaded.jar")
    stdout_grammar, stderr_grammar = run_jar(grammar_path)
    return stdout_grammar, stderr_grammar
    # print(stdout_grammar.decode('utf-8'))


@timeout_decorator(timeout_minutes=30)
def main(should_stop):
    while True:
        # Check the timeout flag
        if should_stop["value"]:
            print("Timeout flag has been triggered, exiting the main loop.")
            break

        # Execute step 1
        print("Executing step1_generate_function...")
        step1_generate_function()

        # Execute step 2
        print("Executing step2_generate_seed...")
        try:
            stdout, stderr = step2_generate_seed()
            print(f"Step 2 completed, output: {stdout}, error: {stderr}")
        except Exception as e:
            print(f"An error occurred in step 2: {e}")

        # Execute step 3
        print("Executing step3_check_seed...")
        try:
            stdout_grammar, stderr_grammar = step3_check_seed()
            print(f"Step 3 completed, grammar output: {stdout_grammar}, grammar error: {stderr_grammar}")
        except Exception as e:
            print(f"An error occurred in step 3: {e}")


if __name__ == '__main__':
    main()
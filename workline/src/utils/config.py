import os
import sys
import json
from pathlib import Path

BASE_DIR = str(Path(__file__).resolve().parent.parent.parent)

sys.path.append(BASE_DIR)


def load_config(file_path, environment):
    with open(file_path, 'r') as file:
        config = json.load(file)
    return config[environment]

if __name__ == '__main__':
    # Load development or test configuration
    db_config = load_config('config.json', 'test')  # 或 'test'
    print(f"Host: {db_config['host']}")
    print(f"Port: {db_config['port']}")
    print(f"User: {db_config['user']}")
    print(f"Password: {db_config['password']}")
    print(f"Database: {db_config['database']}")
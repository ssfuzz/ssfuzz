#!/bin/bash

# Check and create a virtual environment
if [ ! -d ".venv" ]; then
  python -m venv .venv
fi

# Activate the virtual environment
source .venv/bin/activate

# Prompt the user to press any key to continue
read -n 1 -s -r -p "Press any key to continue..."

# Print an empty line to improve readability
echo ""
echo "Executing conf2properties.py..."
python scripts/conf2properties.py

# Check the return value of the last command
if [ $? -eq 0 ]; then
  echo "conf2properties.py executed successfully."
else
  echo "Error occurred while executing conf2properties.py."
fi
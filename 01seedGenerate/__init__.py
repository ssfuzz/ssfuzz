import sys
from pathlib import Path

seedGenerate_path = str(Path(__file__).resolve().parent)
print(seedGenerate_path)
sys.path.append(seedGenerate_path)
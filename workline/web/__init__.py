from pathlib import Path
import sys

BASE_ROOT = str(Path(__file__).resolve().parent.parent.parent)
sys.path.append(BASE_ROOT)
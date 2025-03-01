import os
import chardet
def count_code_lines(directory):
    """
    统计指定目录下Python和Java代码的总行数
    """
    python_lines = 0
    java_lines = 0

    for root, dirs, files in os.walk(directory):
        for file in files:
            # 检查文件扩展名，判断是否为Python或Java文件
            if file.endswith('.py'):
                python_lines += count_file_lines(os.path.join(root, file))
            elif file.endswith('.java'):
                java_lines += count_file_lines(os.path.join(root, file))

    return python_lines, java_lines

def count_file_lines(file_path):
    """
    统计单个文件的代码行数（忽略空行）
    """
    line_count = 0
    try:
        # 检测文件编码
        with open(file_path, 'rb') as f:
            raw_data = f.read()
            encoding_info = chardet.detect(raw_data)
            encoding = encoding_info['encoding'] or 'utf-8'

        # 使用检测到的编码读取文件
        with open(file_path, 'r', encoding=encoding, errors='ignore') as f:
            for line in f:
                if line.strip():
                    line_count += 1
    except Exception as e:
        print(f"Error reading file {file_path}: {e}")
    return line_count

if __name__ == "__main__":
    import sys

    if len(sys.argv) != 2:
        print("Usage: python script.py <directory>")
        sys.exit(1)

    directory = sys.argv[1]

    # 检查目录是否存在
    if not os.path.isdir(directory):
        print(f"Error: Directory '{directory}' does not exist.")
        sys.exit(1)

    python_total, java_total = count_code_lines(directory)

    print(f"Python code total lines: {python_total}")
    print(f"Java code total lines: {java_total}")
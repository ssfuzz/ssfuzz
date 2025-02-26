import os

folder_path = 'data'  # Folder path


class ListToFile:
    def __init__(self, filename, delimiter=','):
        self.filename = filename
        self.delimiter = delimiter

    def save_list(self, data):
        with open(self.filename, 'w') as file:
            file.write(self.delimiter.join(map(str, data)))

    def read_list(self):
        with open(self.filename, 'r') as file:
            data = file.read().split(self.delimiter)
            return list(map(int, data))  # If the elements in the list are integers, convert them to int


# Get all files in the folder
files = os.listdir(folder_path)

# Extract added and removed code lines
added_lines = []
removed_lines = []

# Iterate through the list of files
for file_name in files:
    # Join the file path
    file_path = os.path.join(folder_path, file_name)

    # If it is a file and not a folder
    if os.path.isfile(file_path):
        # Open the file
        with open(file_path, 'r') as file:
            # Read the file content
            content = file.read()

            # Process the file content (here we just print it)
            print(f'Content of {file_name}:')
            print(content)
            print('--------------------------')

            for line in content.split('\n'):
                if line.startswith('+'):
                    added_lines.append(line.lstrip('+ '))
                elif line.startswith('-'):
                    removed_lines.append(line.lstrip('- '))
            print('--------------------------')
            print(added_lines)
            print('--------------------------')
            print(removed_lines)

# Create a ListToFile object, specifying the filename and delimiter (default is comma)
list_add_file = ListToFile('added_data.txt', '\n<split>\n')
list_add_file.save_list(added_lines)

# Save the list to a file
list_rm_file = ListToFile('removed_lines.txt', '\n<split>\n')
list_rm_file.save_list(removed_lines)
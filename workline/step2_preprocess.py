import os, sys, json
import time
import shutil

from pathlib import Path
current_file_path = Path(__file__).resolve()
sys.path.append(str(current_file_path.parent.parent))
ROOT_DIR = current_file_path.parent.parent.parent.parent
BASE_DIR = str(Path(__file__).resolve().parent.parent)
from workline.Counter import Counter
from workline.Energy import Energy
from src.studyMysql.Table_Operation import Table_Result, Table_Testcase

LIST1 = []

class TestcaseToDB():
    def __init__(self, dir) -> None:
        self.dir = dir

    def writeToDB(self, num):
        listTemp = [] 
        table_Testcase = Table_Testcase()
        # Get file list
        for root, dirs, files in os.walk(self.dir):
            print(self.dir)
            for file in files:
                if not file.endswith('.java'):
                    continue
                with open(self.dir + file, 'r') as f:
                    content = f.read()
                    listTemp.append(content)
        testcases_list_to_write = self.makeTestcasesListToWrite(listTemp, 0, 0, 0, num, 0, 0, 0, None)
        table_Testcase.insertManyDataToTableTestcase(testcases_list_to_write)

    def makeTestcasesListToWrite(self, all_testcases, SourceFun_id, SourceTestcase_id, Fuzzing_times,
                             Mutation_method, Mutation_times, Interesting_times, Probability, Remark) -> list:
        lis = []
        for testcase in all_testcases:
            Testcases_content = testcase
            item = [Testcases_content, SourceFun_id, SourceTestcase_id, Fuzzing_times, Mutation_method,
                    Mutation_times, Interesting_times, Probability, Remark]
            lis.append(item)
        return lis
    
class AddExports():
    def __init__(self, jdkdir, exportdir) -> None:
        self.jdkdir = jdkdir
        self.exportdir = exportdir
        self.delete_file()

    def delete_file(self):
        if os.path.exists(self.exportdir):
            os.remove(self.exportdir)
            print(f"{self.exportdir} initialized")
        else:
            print(f"{self.exportdir} already exists")

    def addExports(self):
        list2 = self.jdkdir.split('/')
        name = list2[len(list2) - 1]
        self.getAdd(name, self.jdkdir)
        list2 = LIST1[1:]
        end = "=ALL-UNNAMED"
        # javac
        start = "--add-exports=java.base/"
        with open(self.exportdir, "a+") as f:
            for l in list2:
                f.write(start + l + end + "\n")

    def getAdd(self, name, dir):
        for root, dirs, files in os.walk(dir):
            if name.__contains__("java.base"):
                continue
            if name.__contains__("META-INF"):
                continue
            if name.__contains__("%") or name.__contains__("bootstrap.linux") or name.__contains__("bootstrap.solaris"):
                continue
            if name.__contains__("8024061") or name.__contains__("8012933") or name.__contains__("4504153") \
                    or name.__contains__("6409194") or name.__contains__("6550798"):
                continue
            if name.__contains__("sun.misc"):
                continue
            if name.__contains__("sun.reflect"):
                continue
            if name.__contains__("sun.security.krb5.config.native") or name.__contains__("sun.text.IntHashtable.patch-src"):
                continue
            if len(dirs) == 0:
                if not LIST1.__contains__(name):
                    LIST1.append(name)
                break
            for d in dirs:
                if not LIST1.__contains__(name):
                    LIST1.append(name)
                self.getAdd(name + "." + d, dir + '/' + d)

    def watchAddExports(self):
        with open(self.exportdir, "r") as f:
            res = f.readlines()
            cmd = []
            for s in res:
                if s != '':
                    cmd.append(s[:-1])
                print(cmd)

class compareCoverage():
    def __init__(self) -> None:
        pass
    def get_file_info(self, lines, prefix):
        file_info = {}
        for i in range(0, len(lines), 2):
            line = lines[i].strip()
            if line.startswith('FilePath:'):
                file_path = str(lines[i+1].strip())
                file_path = file_path.replace(prefix, "")
                file_info[file_path] = {'Lines': [], 'Branches': []}
            elif line.startswith('Lines:'):
                lines_str = json.loads(lines[i+1].strip())
                file_info[file_path]['Lines'] = lines_str
            elif line.startswith('Branches:'):
                branches_str = lines[i+1].strip()
                if branches_str != '[]':
                    branches = branches_str[1:-1].split('),')
                    for b in branches:
                        branch_info = b.strip()[1:].split(',')
                        branch_line = int(branch_info[0])
                        if branch_info[1].endswith(')'):
                            branch_info[1] = branch_info[1][:-1]
                        branch_num = int(branch_info[1])
                        file_info[file_path]['Branches'].append((branch_line, branch_num))
        return file_info

    def get_new_coverage(self, test1f, test2f, prefix):
        with open(test1f, 'r') as f1:
            test1 = f1.readlines()
        with open(test2f, 'r') as f2:
            test2 = f2.readlines()
        test1_files = self.get_file_info(test1, prefix)
        test2_files = self.get_file_info(test2, prefix)
        resultF = []
        resultL = []
        resultB = []
        new_coverage = False
        for file_path, info2 in test2_files.items():
            if file_path not in test1_files:
                if len(info2['Lines']) == 0 and len(info2['Branches']) == 0:
                    continue
                resultF = [file_path]
                new_coverage = True
                break
            info1 = test1_files[file_path]
            if len(info2['Lines']) > 0 and info1['Lines'] != info2['Lines']:
                set1 = set(info1['Lines'])
                resultL = [x for x in info2['Lines'] if x not in set1]
                if len(resultL) > 0:
                    new_coverage = True
                    break
            if len(info2['Branches']) > 0 and info2['Branches'] != info1['Branches']:
                set1 = set(info1['Branches'])
                resultB = [x for x in info2['Branches'] if x not in set1]
                if len(resultB) > 0:
                    new_coverage = True

        if new_coverage:
            print("New Coverage!")
        else:
            print("Coverage Unchanged")
        return resultF, resultL, resultB

def copy_files_to_new_folder(source_folder, destination_folder):
    current_time = time.strftime("%Y%m%d-%H%M%S")
    new_folder_name = os.path.join(destination_folder, current_time)
    try:
        shutil.copytree(source_folder, new_folder_name)
        print(f"All files and folders from '{source_folder}' have been copied to '{new_folder_name}'.")
    except FileNotFoundError:
        print("Error: Source folder not found.")
    except FileExistsError:
        print(f"Error: Destination folder '{new_folder_name}' already exists.")

def run_testcaseToDB_main(dir):
    """
    If the specified directory has subdirectories, recursively find all subdirectories and call TestcaseToDB;
    If there are no subdirectories, directly call TestcaseToDB on the current directory.

    :param dir: Directory path to process
    """
    def find_all_folders(directory):
        """
        Find all subdirectory paths under the specified directory.

        :param directory: Root directory path
        :return: List of subdirectory paths
        """
        folder_paths = []
        for root, dirs, _ in os.walk(directory):
            for folder in dirs:
                folder_path = os.path.abspath(os.path.join(root, folder))
                folder_paths.append(folder_path)
        return folder_paths

    # Find all subdirectories
    folder_paths = find_all_folders(dir)

    if folder_paths:
        # If there are subdirectories, process each one
        for subdir in folder_paths:
            print(f"Processing subdirectory: {subdir}")
            testcasetodb = TestcaseToDB(subdir + '/')
            testcasetodb.writeToDB(0)
    else:
        # If there are no subdirectories, process the current directory
        print(f"No subdirectories, processing directory directly: {dir}")
        testcasetodb = TestcaseToDB(dir + '/')
        testcasetodb.writeToDB(0)
    
def run_addExports_main():
    jdkdir = "/root/ssfuzz/00JVMs/compiledSources/openjdk-11.0.25-ga.tar/jdk-11.0.25+9/test/jdk/sun"
    export_dir = "/root/ssfuzz/workline/data/addExports.txt"
    addexports = AddExports(jdkdir, export_dir)
    addexports.addExports()

def run_energyupdate_main():
    obj = Energy()
    test1f = os.path.join(BASE_DIR, "data/entropyData/analyse1.txt")
    c2 = obj.get_frequency(test1f)

    energy = obj.get_energy(c2)

    obj.val_dir = "/root/ssfuzz/feature/src/test/output/target_test"
    obj.Mutation_method = 1
    all_files = obj.read_mutators_fromDir()

    mutator_file = "/root/ssfuzz/feature/src/test/output/target_test/0/MyJVMTest_201363.txt"
    name, files_data = obj.read_mutator(mutator_file)
    files_data = {name: files_data}

    print("All Data: ", all_files, files_data, energy)

def run_compareCoverage_main():
    comparecoverage = compareCoverage()
    test1f = "/root/ssfuzz/fuzz/data/entropyData/analyse1.txt"
    test2f = "/root/ssfuzz/fuzz/data/entropyData/analyse2.txt"
    prefix = "/root/ssfuzz/fuzz/data/entropyData/HTMLFileJT3"
    newFile, newLines, newBranches = comparecoverage.get_new_coverage(test1f, test2f, prefix)
    print("newFile: ", newFile)
    print("newLines: ", newLines)
    print("newBranches: ", newBranches)

def find_max_folder_number(directory_path):
    max_number = 0
    for folder in os.listdir(directory_path):
        if os.path.isdir(os.path.join(directory_path, folder)) and folder.startswith("folder_"):
            try:
                number = int(folder.split("_")[1])
                max_number = max(max_number, number)
            except ValueError:
                pass
    return max_number


if __name__ == '__main__':
    # basePath = "/root/ssfuzz/parser/feature/src/test/output/task003/"
    # max_number = find_max_folder_number(basePath)
    dir = '/root/ssfuzz/workline/llm/models/testcase'
    run_testcaseToDB_main(dir)

    # source_folder = "/".join(dir.split("/")[:-1])
    # destination_folder = "/root/ssfuzz/parser/feature/src/test/output/task001"
    # copy_files_to_new_folder(source_folder, destination_folder)

    # Generate the latest configuration
    run_addExports_main()
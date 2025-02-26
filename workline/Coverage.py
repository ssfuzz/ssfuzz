import json
import pathlib
import subprocess
import time
import sys
import os
curPath = os.path.abspath(os.path.dirname(__file__))
rootPath = os.path.split(curPath)[0]
sys.path.append(rootPath)
# print(rootPath+"/src")
from concurrent.futures import ThreadPoolExecutor
from typing import List, Tuple
from src.studyMysql.Table_Operation import Table_Testcase
from workline.Counter import Counter
from workline.Energy import Energy
from workline.table_to_class.Table_Testcase import Testcase_Object
from lxml import html
from workline.harness_tools.harness_class_javac import Harness as H_javac

class Coverage:
    def __init__(self):
        self.testcase_object = None
        self.listTestcase = []
        self.want_testbed_location = "/root/jvm/openjdk11/jdk-11.0.14-ga/build/linux-x86_64-normal-server-release/images/jdk/bin/java"
        self.number = 100
        self.groupList = []
        self.cur_group_names = []
    def parse_coverage_info(self,file_path):
        with open(file_path) as f:
            content = f.read()

        lines_index = content.find("lines......")
        functions_index = content.find("functions..")
        branches_index = content.find("branches...")

        lines_str = content[lines_index: functions_index]
        functions_str = content[functions_index: branches_index]
        branches_str = content[branches_index:]

        lines_data = lines_str.split(" ")
        functions_data = functions_str.split(" ")
        branches_data = branches_str.split(" ")
        # print(lines_data)
        # print(functions_data)
        # print(branches_data)
        res = {}
        res["lines"] = [round(float(lines_data[1][:-1]) * 0.01, 3), int(lines_data[2].strip("(")), int(lines_data[4])]
        res["functions"] = [round(float(functions_data[1][:-1]) * 0.01, 3), int(functions_data[2].strip("(")),
                            int(functions_data[4])]
        res["branches"] = [round(float(branches_data[1][:-1]) * 0.01, 3), int(branches_data[2].strip("(")),
                           int(branches_data[4])]
        return res

    def parse_html_file(self,filepath):
        with open(filepath, 'r') as f:
            tree = html.fromstring(f.read())
            lines = []
            branches = []
            #pre check
            td_tags = tree.xpath("//td[@class='headerCovTableEntryLo']")
            if td_tags:
                all_zeros = all(td_tag.text.strip() == '0.0 %' for td_tag in td_tags)
                if all_zeros:
                    return filepath, lines, branches
            #real check
            tag = tree.xpath("//pre[@class='source']")
            pre_tag = tag[0].text if tag else None
            if pre_tag:
                a_tags = tag[0].xpath(".//a")
                for a_tag in a_tags:
                    l_tags = a_tag.xpath(".//span[@class='lineCov']")
                    if l_tags:
                        lines.append(int(a_tag.get('name')))
                    span_tags = a_tag.xpath(".//span[@class='branchCov']")
                    if span_tags:
                        for span_tag in span_tags:
                            which_branch = str(span_tag.get("title")).split(' ')[1]
                            branches.append((int(a_tag.get('name')), int(which_branch)))
            return filepath, lines, branches

    def parse_html_dir(self,root_dir: str) -> Tuple[List[str], List[str]]:
        html_files = []
        for root, dirs, files in os.walk(root_dir):
            for file in files:
                if file.endswith(('.cpp.gcov.html', '.hpp.gcov.html', '.h.gcov.html')):
                    html_files.append(os.path.join(root, file))
        # print(len(html_files))
        files = {}
        with ThreadPoolExecutor(max_workers=os.cpu_count()) as executor:
            results = executor.map(self.parse_html_file, html_files)
            for filepath, file_lines, file_branches in results:
                # print("*" * 10 + filepath + "*" * 10)
                # print(file_lines)
                # print(file_branches)
                if file_lines == '[]' and file_branches == '[]':
                    continue
                filepath = filepath.replace(root_dir, "")
                files[filepath] = {"lineCov": file_lines, "branchCov": file_branches}
        return files

    def analyseHtmls(self,root_dir, output_path):
        res = self.parse_html_dir(root_dir)
        for filepath in res.keys():
            with open(os.path.join(root_dir, output_path), 'a') as f:
                f.write(f"FilePath:\n{filepath}\n")
                f.write(f"Lines:\n{res[filepath]['lineCov']}\n")
                f.write(f"Branches:\n{res[filepath]['branchCov']}\n")

    def get_new_coverage(self,test1f, test2f,prefix1,prefix2):
        with open(test1f, 'r') as f1:
            test1 = f1.readlines()
        with open(test2f, 'r') as f2:
            test2 = f2.readlines()
        test1_files = self.get_file_info(test1, prefix1)
        test2_files = self.get_file_info(test2, prefix2)

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

    def get_file_info(self,lines, prefix):
        file_info = {}
        for i in range(0, len(lines), 2):
            line = lines[i].strip()
            if line.startswith('FilePath:'):
                file_path = str(lines[i + 1].strip())
                file_path = file_path.replace(prefix, "")
                file_info[file_path] = {'Lines': [], 'Branches': []}
            elif line.startswith('Lines:'):
                lines_str = json.loads(lines[i + 1].strip())
                file_info[file_path]['Lines'] = lines_str
            elif line.startswith('Branches:'):
                branches_str = lines[i + 1].strip()
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
    def getListTestcase(self,mm):
        table_Testcases = Table_Testcase()
        self.listTestcase = table_Testcases.selectAllfromTableTestcase()
        self.getGroupList()
        if len(self.listTestcase) == 0:
            print("no testcase exist.")

    def getGroupList(self):
        if len(self.listTestcase)>0:
            toIndex = self.number
            listSize = len(self.listTestcase)
            keyToken = 0
            for i in range(0, listSize, self.number):
                if i + self.number > listSize:
                    toIndex = listSize-i
                newList = self.listTestcase[i:i+toIndex]
                self.groupList.append(newList)
                keyToken+=1

    def javac_Testcases(self,group,mm):
        for testcase in group:
            self.javac_compile_single(testcase,mm)
        print("all testcase compiled finshed.")

    def run_Testcases(self,group,mm):
        for testcase in group:
            self.single_harness(testcase,mm)
        print("all testcase executed finshed.")

    def javac_compile_single(self,testcase,mm):
        testcase_object = Testcase_Object(testcase)
        testcase_object.single_engine_compile_coverage(self.want_testbed_location,mm)

    def single_harness(self,testcase,mm):
        testcase_object = Testcase_Object(testcase)
        counter = self.check(testcase_object.Testcase_context,mm)
        if counter == 1:
            testcase_object.single_engine_run_coverage(self.want_testbed_location,mm)

    def check(self, testcase,mm) -> int:
        counter  = 0
        code_files = "/root/ssfuzz/fuzz/shannon_v0/"+str(mm)+"/"
        harness = H_javac()
        harness.get_class_name(testcase)
        class_path = pathlib.Path(code_files + self.want_testbed_location.split("/")[3] + "/out/" + harness.name+".class")
        self.cur_group_names.append(harness.name)
        # print(class_path)
        if class_path.is_file() and os.stat(str(class_path)).st_size > 0:
            print("class file exists.classfile path is: ",class_path)
            counter=1
        else:
            print("class file doesn't exists.  ")
        return counter


if __name__ == '__main__':
    mm = 3  # NUM
    docpath = "/root/ssfuzz/fuzz/coverage/mm"+str(mm)+"/coverageAll.txt"

    obj1 = Coverage()
    obj2 = Energy()
    c = Counter()

    count_record = "/root/ssfuzz/fuzz/coverage/countHistory.txt"
    countHistoryDir = '/root/ssfuzz/fuzz/coverage/countHistoryDir'+str(mm)+'/'
    obj2.val_dir = "/root/ssfuzz/feature/src/test/output/target_test"
    obj2.Mutation_method = mm
    all_val_files_data = obj2.read_mutators_fromDir()[str(mm)]

    tempfile = "/root/ssfuzz/fuzz/coverage/temp.txt"

    obj1.number = 15 
    obj1.getListTestcase(mm)

    for index in range(0, len(obj1.groupList)):
        obj1.cur_group_names = []
        obj1.javac_Testcases(obj1.groupList[index],mm)
        obj1.run_Testcases(obj1.groupList[index],mm)
        script_directory = "/root/ssfuzz/fuzz/scripts"
        script_name = "time_coverage.sh"
        arg1_logfile = "/root/ssfuzz/fuzz/coverage/mm"+str(mm)+"/log.txt"
        arg2_mm = str(mm)
        arg3_index = str(index)
        subprocess.run(['bash', script_name, arg1_logfile, arg2_mm, arg3_index], cwd=script_directory)

        allFilepath = "/root/ssfuzz/fuzz/coverage/mm"+str(mm)+"/all"+str(index)+".txt"
        allCover = obj1.parse_coverage_info(allFilepath)
        with open(docpath, "a") as f:
            f.write("*"*20+str(mm)+"---"+str(index)+"*"*20+"\n")
            for k,v in allCover.items():
                f.write(k)
                f.write(str(v))
                f.write('\n')

        html_root_dir = '/root/ssfuzz/fuzz/coverage/mm'+str(mm)+'/HtmlFile'+str(index)
        analyse_output_path = '/root/ssfuzz/fuzz/coverage/mm'+str(mm)+'/analyse'+str(index)+'.txt'
        start_time = time.time()
        obj1.analyseHtmls(html_root_dir, analyse_output_path)
        print(f'analyse Htmls Spending time:{int(time.time() - start_time)}s')
        obj2.get_frequency(analyse_output_path)
        with open(analyse_output_path, 'r') as f1:
            analyse_con = f1.readlines()
        analyse_files = obj2.get_file_info(analyse_con)

        with open(count_record, "r") as f:
            record = f.read()
        if len(record) > 0:
            c.count_from_string(record)
        c.count(analyse_files)
        count_res = c.get_counts()
        energy = obj2.get_energy(count_res)
        obj2.write_energy(all_val_files_data,obj1.cur_group_names,energy)
        with open(tempfile,"a")as f:
            for k,v in allCover.items():
                f.write(k)
                f.write(str(v))
                f.write('\n')
            f.write(str(index)+"---"+str(energy))
        with open(countHistoryDir+str(index)+".txt", "w") as f:
            f.write(str(c.get_counts()))
    print("energy count finshed!")

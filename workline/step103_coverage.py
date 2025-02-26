import os, sys
import csv
import re
import time
import subprocess
import shutil
from multiprocessing.dummy import Pool as ThreadPool
from typing import List
import pymysql
from pathlib import Path
from workline.harness_tools.harness_class import ThreadLock, HarnessResult
from workline.harness_tools.harness_class_javac import Harness as H_javac
from workline.Counter import Counter
from src.studyMysql.Table_Operation import Table_Result, Table_Testcase
from workline.table_to_class.Table_Testcase import Testcase_Object

# Base directory
BASE_DIR = str(Path(__file__).resolve().parent.parent)
sys.path.append(BASE_DIR)

# JVM engine paths
ENGINES = ["/root/ssfuzz/00JVMs/compiledSources/coverage/jdk11u-dev/build/linux-x86_64-normal-server-release/jdk/bin/java"]
# /root/ssfuzz/00JVMs/compiledSources/jdk11u-dev/build/linux-x86_64-normal-server-release/hotspot/variant-server/libjvm/objs

class CoverageAnalyzer:
    def __init__(self, db_config: dict, coverage_dir: str, output_csv: str):
        self.db_config = db_config
        self.coverage_dir = coverage_dir
        self.output_csv = output_csv
        self.result_db = Table_Result()
        self.testcase_db = Table_Testcase()
        self.javac = H_javac()
        self.counter = Counter()
        self.create_csv_file()

    def create_csv_file(self):
        """ Create or append to a CSV file to store results """
        headers = [
            "line_coverage", "line_covered", "line_total",
            "function_coverage", "function_covered", "function_total",
            "branch_coverage", "branch_covered", "branch_total", "coverageCountTime"
        ]
        file_exists = os.path.exists(self.output_csv)
        with open(self.output_csv, 'a', encoding='utf8', newline='') as f:
            writer = csv.writer(f)
            if not file_exists:
                writer.writerow(headers)
                print(f"File {self.output_csv} created successfully, headers written.")
            else:
                print(f"File {self.output_csv} already exists, content will be appended.")

    def get_onetestcases_from_db(self, method_id: int) -> List[Testcase_Object]:
        """ Retrieve a single test case from the database """
        return self.testcase_db.selectOneFromTableTestcase(method_id)

    def get_testcases_from_db(self) -> List[Testcase_Object]:
        """ Retrieve test cases from the database """
        return self.testcase_db.selectAllNeedTestFromTestcase()

    def get_testcases_from_folder(self, folder_path: str) -> List[str]:
        """ Retrieve test cases from a folder """
        testcases = []
        for root, dirs, files in os.walk(folder_path):
            for file in files:
                if file.endswith(".java"):
                    testcases.append(os.path.join(root, file))
        return testcases

    def compile_testcase(self, testcase: Testcase_Object, method_id: int) -> bool:
        """ Compile a single test case """
        try:
            testcase_object = Testcase_Object(testcase)
            output = testcase_object.single_engine_compile_coverage(ENGINES[0], method_id)
            return True
        except Exception as e:
            print(f"Compilation error: {e}")
            return False

    def run_testcase(self, testcase: Testcase_Object, method_id: int) -> bool:
        """ Run a single test case """
        try:
            testcase_object = Testcase_Object(testcase)
            output = testcase_object.single_engine_run_coverage(ENGINES[0], method_id)
            return True
        except Exception as e:
            print(f"Execution error: {e}")
            return False

    def get_coverage(self):
        """ Generate coverage information and write it to a CSV file """
        coverage_cmd = (
            "cd /root/ssfuzz/00JVMs/compiledSources/jdk11u-dev/build/linux-x86_64-normal-server-release/hotspot/variant-server/libjvm/objs "
            "&& lcov --quiet -b ./ -d ./ --rc lcov_branch_coverage=1 --rc geninfo_gcov_all_blocks=0 --gcov-tool /usr/bin/gcov-7 -c -o output.info "
            "&& genhtml --rc genhtml_branch_coverage=1 --rc geninfo_gcov_all_blocks=0 -o ../HtmlFile output.info | tee result.txt"
        )
        # Record the start time of coverage generation
        start_time = time.time()
        os.system(coverage_cmd)
        # Record the end time of coverage generation
        end_time = time.time()
        duration = end_time - start_time
        self.parse_coverage("/root/ssfuzz/00JVMs/compiledSources/jdk11u-dev/build/linux-x86_64-normal-server-release/hotspot/variant-server/libjvm/objs/result.txt", time=duration)

    def parse_coverage(self, result_path: str, time=0):
        """ Parse coverage results and write them to a CSV file """
        with open(result_path, "r") as f:
            content = f.read()
        # Regular expression to match coverage information
        pattern = r"lines......: (\d+\.\d+)% \((\d+) of (\d+) lines\)\s+" \
                  r"functions..: (\d+\.\d+)% \((\d+) of (\d+) functions\)\s+" \
                  r"branches...: (\d+\.\d+)% \((\d+) of (\d+) branches\)"
        match = re.search(pattern, content)
        if match:
            # Extract matched coverage information
            line_coverage, line_covered, line_total = match.group(1), match.group(2), match.group(3)
            function_coverage, function_covered, function_total = match.group(4), match.group(5), match.group(6)
            branch_coverage, branch_covered, branch_total = match.group(7), match.group(8), match.group(9)

            # Write coverage information to the CSV file
            with open(self.output_csv, "a", newline="") as f:
                writer = csv.writer(f)
                writer.writerow([
                    line_coverage, line_covered, line_total,
                    function_coverage, function_covered, function_total,
                    branch_coverage, branch_covered, branch_total, time
                ])
            print(f"Coverage data written to {self.output_csv}")
        else:
            print(f"No coverage data found in {result_path}")

    def process_testcases(self, testcases: List[Testcase_Object], method_id: int):
        """
        Process test cases: compile, run, and get coverage.

        Parameters:
            testcases (List[Testcase_Object]): List of test case objects.
            method_id (int): Method ID of the test case.
        """
        if not testcases:
            print("No test cases found. Exiting.")
            return

        batch_size = 200  # Process coverage statistics every 200 test cases
        processed_count = 0  # Counter for processed test cases

        for testcase in testcases:
            try:
                # Compile test case
                compile_result = self.compile_testcase(testcase, method_id)
                if compile_result:
                    print(f"Compilation successful for {testcase}")
                else:
                    print(f"Compilation failed for {testcase}")
                    continue
                # Run test case
                run_result = self.run_testcase(testcase, method_id)
                if run_result:
                    print(f"Execution successful for {testcase}")
                else:
                    print(f"Execution failed for {testcase}")
                    continue
            except Exception as e:
                print(f"Error processing test case {testcase}: {e}")
                continue
            finally:
                # Update processed test case counter
                processed_count += 1
                # If the total number of test cases is not a multiple of batch_size, 
                # statistics are generated once at the end
                if processed_count % batch_size == 0:
                    # Get coverage information and write it to the CSV file
                    self.get_coverage()


    def run_from_db(self, method_id: int = None, single_testcase: bool = False):
        """
        Retrieve test cases from the database and process them.

        Parameters:
            method_id (int): Method ID of the test case. Required only when single_testcase is True.
            single_testcase (bool): Whether to query a single test case. Default is False.
        """
        if single_testcase:
            if method_id is None:
                raise ValueError("method_id is required when single_testcase is True")
            testcases = self.get_onetestcases_from_db(method_id)
        else:
            testcases = self.get_testcases_from_db()

        print(f"Total test cases retrieved: {len(testcases)}")
        self.process_testcases(testcases, method_id)

    # def run_from_folder(self, folder_path: str, method_id: int):
    #     """ Retrieve test cases from a folder and process them """
    #     testcases = self.get_testcases_from_folder(folder_path)
    #     self.process_testcases(testcases, method_id)


if __name__ == "__main__":
    db_config = {
        "host": "10.15.0.38",
        "port": 10348,
        "user": "root",
        "passwd": "root",
        "db": "ssfuzzTest",
        "charset": "utf8mb4"
    }
    coverage_dir = "/root/ssfuzz/workline/cover_code"
    output_csv = "/root/ssfuzz/workline/cover_code/ssfuzz.csv"

    analyzer = CoverageAnalyzer(db_config, coverage_dir, output_csv)

    # Retrieve test cases from the database
    # method_id = 15
    analyzer.run_from_db(single_testcase=False)

    # # Retrieve test cases from a folder
    # folder_path = "/root/ssfuzz/workline/cover_code/ssfuzz"
    # analyzer.run_from_folder(folder_path, method_id)
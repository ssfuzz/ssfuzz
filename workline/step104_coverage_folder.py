import os
import sys
import csv
import re
import time
import subprocess
import shutil
from multiprocessing.dummy import Pool as ThreadPool
from typing import List
import pymysql
from pathlib import Path
import logging

BASE_DIR = str(Path(__file__).resolve().parent.parent)
sys.path.append(BASE_DIR)
logging.info(f"BASE_DIR: {BASE_DIR}")

from workline.harness_tools.harness_class import ThreadLock, HarnessResult
from workline.harness_tools.harness_class_javac import Harness as H_javac
from workline.Counter import Counter
from src.studyMysql.Table_Operation import Table_Result, Table_Testcase
from workline.table_to_class.Table_Testcase import Testcase_Object

def setup_logging(file_name):
    logger = logging.getLogger()
    logger.setLevel(logging.INFO)

    # Remove previous handlers to avoid duplicate log entries
    for handler in logger.handlers[:]:
        logger.removeHandler(handler)

    # Create a new file handler
    file_handler = logging.FileHandler(file_name)
    file_handler.setLevel(logging.INFO)

    # Create a console handler
    console_handler = logging.StreamHandler()
    console_handler.setLevel(logging.INFO)

    # Create a formatter and bind it to the handlers
    formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
    file_handler.setFormatter(formatter)
    console_handler.setFormatter(formatter)

    # Add the handlers to the logger
    logger.addHandler(file_handler)
    logger.addHandler(console_handler)

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
        """Create or append content to the CSV file to store results"""
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
                logging.info(f"File {self.output_csv} created, headers written.")
            else:
                logging.info(f"File {self.output_csv} already exists, content will be appended.")

    def get_onetestcases_from_db(self, method_id: int) -> List[Testcase_Object]:
        """Retrieve test cases from the database"""
        return self.testcase_db.selectOneFromTableTestcase(method_id)

    def get_testcases_from_db(self) -> List[Testcase_Object]:
        """Retrieve test cases from the database"""
        return self.testcase_db.selectAllNeedTestFromTestcase()

    def get_testcases_from_folder(self, folder_path: str) -> List[tuple]:
        """
        Retrieve test cases from a folder.

        Parameters:
            folder_path (str): Path to the test case folder.

        Returns:
            List[tuple]: List containing test case file paths and file content.
        """
        testcases = []
        for root, dirs, files in os.walk(folder_path):
            for file in files:
                if file.endswith(".java"):
                    file_path = os.path.join(root, file)
                    with open(file_path, "r", encoding="utf-8") as f:
                        content = f.read()
                    testcases.append((file_path, content))
        return testcases

    def compile_testcase(self, testcase: Testcase_Object, method_id: int) -> bool:
        """Compile a single test case"""
        try:
            testcase_object = Testcase_Object(testcase)
            output = testcase_object.single_engine_compile_coverage(ENGINES[0], method_id)
            logging.info(f"Compilation successful for {testcase}")
            return True
        except Exception as e:
            logging.error(f"Compilation error: {e}")
            return False

    def run_testcase(self, testcase: Testcase_Object, method_id: int) -> bool:
        """Run a single test case"""
        try:
            testcase_object = Testcase_Object(testcase)
            output = testcase_object.single_engine_run_coverage(ENGINES[0], method_id)
            logging.info(f"Execution successful for {testcase}")
            return True
        except Exception as e:
            logging.error(f"Execution error: {e}")
            return False

    def get_coverage(self):
        """Retrieve coverage information and write to CSV file"""
        coverage_cmd = (
            "cd /root/coverage/OpenJdk11/jdk-11.0.14.1-ga/build/linux-x86_64-normal-server-release/hotspot/variant-server/libjvm/objs "
            "&& lcov -b ./ -d ./ --rc lcov_branch_coverage=1 --rc geninfo_gcov_all_blocks=0 --gcov-tool /usr/bin/gcov -c -o output.info "
            "&& genhtml --rc genhtml_branch_coverage=1 --rc geninfo_gcov_all_blocks=0 -o ../HtmlFile output.info|tee result.txt"
        )
        start_time = time.time()
        os.system(coverage_cmd)
        end_time = time.time()
        duration = end_time - start_time
        self.parse_coverage("/root/coverage/OpenJdk11/jdk-11.0.14.1-ga/build/linux-x86_64-normal-server-release/hotspot/variant-server/libjvm/objs/result.txt", time=duration)

    def parse_coverage(self, result_path: str, time=0):
        """Parse coverage results and write to CSV file"""
        with open(result_path, "r") as f:
            content = f.read()
        pattern = r"lines......: (\d+\.\d+)% \((\d+) of (\d+) lines\)\s+" \
                  r"functions..: (\d+\.\d+)% \((\d+) of (\d+) functions\)\s+" \
                  r"branches...: (\d+\.\d+)% \((\d+) of (\d+) branches\)"
        match = re.search(pattern, content)
        if match:
            line_coverage, line_covered, line_total = match.group(1), match.group(2), match.group(3)
            function_coverage, function_covered, function_total = match.group(4), match.group(5), match.group(6)
            branch_coverage, branch_covered, branch_total = match.group(7), match.group(8), match.group(9)

            with open(self.output_csv, "a", newline="") as f:
                writer = csv.writer(f)
                writer.writerow([
                    line_coverage, line_covered, line_total,
                    function_coverage, function_covered, function_total,
                    branch_coverage, branch_covered, branch_total, time
                ])
            logging.info(f"Coverage data written to {self.output_csv}")
        else:
            logging.warning(f"No coverage data found in {result_path}")

    def process_testcases(self, testcases: List[Testcase_Object], method_id: int):
        """
        Process test cases: compile, run, and retrieve coverage.

        Parameters:
            testcases (List[Testcase_Object]): List of test case objects.
            method_id (int): Method ID of the test case.
        """
        if not testcases:
            logging.warning("No test cases found. Exiting.")
            return

        batch_size = 200  # Calculate coverage every 200 test cases
        processed_count = 0  # Counter for processed test cases

        for testcase in testcases:
            try:
                compile_result = self.compile_testcase(testcase, method_id)
                if compile_result:
                    logging.info(f"Compilation successful for {testcase}")
                else:
                    logging.error(f"Compilation failed for {testcase}")
                    continue
                run_result = self.run_testcase(testcase, method_id)
                if run_result:
                    logging.info(f"Execution successful for {testcase}")
                else:
                    logging.error(f"Execution failed for {testcase}")
                    continue
            except Exception as e:
                logging.error(f"Error processing test case {testcase}: {e}")
                continue
            finally:
                processed_count += 1
                if processed_count % batch_size == 0:
                    self.get_coverage()

    def process_testcases_from_folder(self, testcases: List[tuple], folder_path):
        """
        Process test cases from a folder: compile, run, and retrieve coverage.

        Parameters:
            testcases (List[tuple]): List of test cases, each element is a tuple containing file path and content.
            method_id (int): Method ID of the test case.
        """
        if not testcases:
            logging.warning("No test cases found in folder. Exiting.")
            return

        class_path = ""

        batch_size = len(testcases)  # Calculate coverage every 200 test cases
        processed_count = 0  # Counter for processed test cases
        total_testcases = len(testcases)  # Total number of test cases
        compile_success_count = 0  # Counter for successful compilations

        if not folder_path:
            raise ValueError("Folder path is not specified.")
        output_dir = os.path.join(folder_path, "out")
        os.makedirs(output_dir, exist_ok=True)  # Create output directory

        for file_path, content in testcases:
            try:
                logging.info(f"Compiling test case {processed_count}: {file_path} to {output_dir}")
                compile_cmd = f"timeout -s9 60 {ENGINES[0]}c -d {output_dir} {file_path}"
                logging.info(f"compile cmd : {compile_cmd}")
                compile_result = subprocess.run(compile_cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
                if compile_result.returncode == 0:
                    logging.info(f"Compilation successful for {file_path}")
                    compile_success_count += 1
                else:
                    logging.error(f"Compilation failed for {file_path}: {compile_result.stderr.decode('utf-8')}")
                logging.info(f"Compiling test case: {file_path} to {output_dir}")

                logging.info(f"Running test case {processed_count}: {os.path.join(output_dir, file_path.split('/')[-1]).replace('.java', '')}")
                run_cmd = f"timeout -s9 60 {ENGINES[0]} -cp /root/coverage/OpenJdk11/jdk-11.0.14.1-ga/build/linux-x86_64-normal-server-release/jdk/lib:{output_dir}: {file_path.split('/')[-1].replace('.java', '')}"
                logging.info(f"run cmd : {run_cmd}")
                run_result = subprocess.run(run_cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
                logging.info(f"Execution successful for {file_path}:{run_result.stdout.decode('utf-8')}")
                if run_result.returncode != 0:
                    logging.error(f"Execution failed for {file_path}: {run_result.stderr.decode('utf-8')}")
                    continue
                logging.info(f"Execution successful for {file_path}")

            except Exception as e:
                logging.error(f"Error processing test case {file_path}: {e}")
                continue
            finally:
                processed_count += 1
                if processed_count % batch_size == 0:
                    self.get_coverage()
        grammar_correctness_rate = compile_success_count / total_testcases if total_testcases != 0 else 0
        logging.info(f"Grammar correctness rate (Compile success rate): {grammar_correctness_rate * 100:.2f}%")

    def run_from_db(self, method_id: int = None, single_testcase: bool = False):
        """
        Retrieve test cases from the database and process them.

        Parameters:
            method_id (int): Method ID of the test case. Only used when single_testcase is True.
            single_testcase (bool): Whether to query a single test case. Default is False.
        """
        if single_testcase:
            if method_id is None:
                raise ValueError("method_id is required when single_testcase is True")
            testcases = self.get_onetestcases_from_db(method_id)
        else:
            testcases = self.get_testcases_from_db()

        logging.info(f"Total test cases retrieved: {len(testcases)}")
        self.process_testcases(testcases, method_id)

    def run_from_folder(self, folder_path: str):
        """
        Retrieve test cases from a folder and process them.

        Parameters:
            folder_path (str): Path to the test case folder.
            method_id (int): Method ID of the test case.
        """
        testcases = self.get_testcases_from_folder(folder_path)
        logging.info(f"Total test cases retrieved from folder: {len(testcases)}")
        self.process_testcases_from_folder(testcases, folder_path)

if __name__ == "__main__":
    folder_names = ["grok-3"]
    for folder_name in folder_names:
        try:
            init_cmd = (
                "cd /root/coverage/OpenJdk11/jdk-11.0.14.1-ga/build/linux-x86_64-normal-server-release/hotspot/variant-server/libjvm/objs "
                "&& find . -name '*.gcda' | xargs -i rm -f {} "
            )
            start_time = time.time()
            os.system(init_cmd)

            ENGINES = ["/root/coverage/OpenJdk11/jdk-11.0.14.1-ga/build/linux-x86_64-normal-server-release/jdk/bin/java"]

            db_config = {
                "host": "10.15.0.38",
                "port": 10348,
                "user": "root",
                "passwd": "root",
                "db": "ssfuzzTest",
                "charset": "utf8mb4"
            }
            coverage_dir = "/root/ssfuzz/llm/cover_code"
            output_csv = "/root/ssfuzz/llm/cover_code/{}.csv".format(folder_name)

            setup_logging('coverage_analyzer_{}.log'.format(folder_name))

            analyzer = CoverageAnalyzer(db_config, coverage_dir, output_csv)

            folder_path = "/root/ssfuzz/llm/models1/testcase/{}".format(folder_name)
            analyzer.run_from_folder(folder_path)
        except Exception as e:
            logging.info(f"Error occurred: {e}")
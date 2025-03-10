import collections
import json
import math
import pathlib
import subprocess
import gc
import re
from threading import Thread
from typing import List
import os
import yaml
from workline import FUZZING_ROOT

from src.studyMysql.Table_Operation import Table_Testbed, Table_Result, Table_Suspicious_Result
from src.utils import labdate
from loguru import logger

configPath = os.path.normpath(os.path.join(FUZZING_ROOT,"config.yml"))

with open(configPath, 'r', encoding='utf-8') as f:
    config = yaml.load(f.read(), Loader=yaml.FullLoader)
# BASE_DIR = str(Path(__file__).resolve().parent.parent.parent)
listTxt = os.path.normpath(os.path.join(FUZZING_ROOT,"data/list.txt"))
code_files = os.path.relpath(os.path.join(FUZZING_ROOT,"data/code_files/"))
code_files_cov = os.path.relpath(os.path.join(FUZZING_ROOT,"data/code_files_cov/"))
addExportsPath = os.path.normpath(os.path.join(FUZZING_ROOT,"data/addopens.txt"))

Majority = collections.namedtuple('Majority', [
    'majority_outcome', 'outcome_majority_size',
    'majority_stdout', 'stdout_majority_size'
])

javac_classpath = ["-classpath",config['javac_classpath']]

# javac_classpath = ["-classpath", "/root/jvm/openjdk17/jdk-17.0.5+8/build/linux-x86_64-server-release/images/jdk/lib:"
#                                  "/root/jvm/openjdk11/jdk-11.0.14-ga/build/linux-x86_64-normal-server-release/images/jdk/lib:"
#                                  "/root/jvm/openj9-jdk11/openj9-openjdk-jdk11/build/linux-x86_64-normal-server-release/images/images/lib:"
#                                  "/root/jvm/openj9-jdk17/openj9-openjdk-jdk17/build/linux-x86_64-server-release/images/jdk/lib:"
#                                  "/root/jvm/zulu-jdk17/zulu17.38.21-ca-jdk17.0.5-linux_x64/lib:"
#                                  "/root/jvm/zulu-jdk11/zulu11.60.19-ca-jdk11.0.17-linux_x64/lib:"
#                                  "/root/jvm/graal-jdk17/graalvm-ce-java17-22.3.0/lib:"
#                                  "/root/jvm/graal-jdk11/graalvm-ce-java11-22.3.0/lib:"
#                                  "/JVMfuzzing/htm/java_code/HotspotOut:"
#                                  "/JVMfuzzing/htm/java_code/HtmOut:"
#                    ]
with open(listTxt, "r") as f:
    res = f.read()
javac_classpath[1] = javac_classpath[1] + res

addOpens = []
with open(addExportsPath, 'r') as f:
    addOpens = f.readlines()


class ThreadLock(Thread):
    def __init__(self, testbed_location, testcase_path, testbed_id, mm=0, time:str = "60"):
        super().__init__()
        self.testbed_id = testbed_id
        self.output = None
        self.testbed_location = testbed_location
        self.testcase_path = testcase_path
        self.returnInfo = None
        self.isCov = False
        self.mm = mm
        self.time = time

    def run(self):
        try:
            self.output = self.run_test_case(self.testbed_location, self.testcase_path, self.testbed_id)
        except BaseException as e:
            self.returnInfo = 1

    def run_test_case(self, testbed_location: str, testcase_path: pathlib.Path, testbed_id):
        if self.isCov == True:
            # code_files2 = "/tmp/comfort_jvm_cov/" + str(self.mm) + "/"
            code_files2 = os.path.join(code_files_cov, str(self.mm))
            # print(code_files2)
        else:
            code_files2 = code_files
        try:
            logger.info("start java...", testcase_path)
            if os.path.exists(str(testcase_path)):
                logger.info("class file exists.")
            else:
                logger.info("class file doesn't exist.")
            cmd = ["timeout", "-s9", self.time]
            for ob in testbed_location.split():
                cmd.append(ob)
            if "openj9" in testbed_location or "Openj9" in testbed_location:
                cmd.append("-XX:+CompactStrings")
                cmd.append("-Xnuma:none")
                cmd.append("-XX:-HandleSIGABRT")
                cmd.append("-Xjit:count=0")
                if "jdk8" in testbed_location:
                    cmd.append("-Djava.lang.string.substring.nocopy=true")
            for s in addOpens:
                if s != '':
                    cmd.append(s[:-1])
            cmd.append("-cp")
            jvm_name = testbed_location.split("/")[3]
            # print(code_files2)
            cmd.append(os.path.join(code_files2, jvm_name) + "/out:" + javac_classpath[1])
            classfile_path = testcase_path.name.replace(".class", "")
            # print(classfile_path)
            cmd.append(str(classfile_path))
            cmd.append("——illegal-access=permit")
            start_time = labdate.GetUtcMillisecondsNow()
            pro = subprocess.Popen(cmd, stdin=subprocess.PIPE, stdout=subprocess.PIPE,
                                   stderr=subprocess.PIPE, universal_newlines=True)
            logger.debug("java cmd:", cmd)
            stdout, stderr = pro.communicate()
            logger.info("java stdout:", stdout)
            logger.info("java stderr:", stderr)
        except Exception as e:
            logger.info("java failed: ", e)
        end_time = labdate.GetUtcMillisecondsNow()
        duration_ms = int(round(
            (end_time - start_time).total_seconds() * 1000))
        event_start_epoch_ms = labdate.MillisecondsTimestamp(start_time)
        output = Output(testbed_id=testbed_id, testbed_location=testbed_location, returncode=pro.returncode,
                        stdout=stdout,
                        stderr=stderr,
                        duration_ms=duration_ms, event_start_epoch_ms=event_start_epoch_ms)
        return output


class DifferentialTestResult:
    def __init__(self, function_id: int, testcase_id: int, error_type: str, testbed_id: int, testbed_location: str):
        self.function_id = function_id
        self.testcase_id = testcase_id
        self.error_type = error_type
        self.testbed_id = testbed_id
        self.testbed_location = testbed_location
        self.classify_result = None
        self.classify_id = None
        self.remark = None

    def serialize(self):
        return {"Differential Test Result": {"testcase_id": self.testcase_id,
                                             "error_type": self.error_type,
                                             "testbed_id": self.testbed_id,
                                             "function_id": self.function_id,
                                             "inconsistent_testbed": self.testbed_location,
                                             "classify_result": self.classify_result,
                                             "classify_id": self.classify_id
                                             }}

    def __str__(self):
        return json.dumps(self.serialize(), indent=4)

    def save_to_table_suspicious_Result(self):
        """
        Save the result to the database
        :return:
        """
        table_suspicious_Result = Table_Suspicious_Result()
        table_suspicious_Result.insertDataToTableSuspiciousResult(self.error_type, self.testcase_id,
                                                                  self.function_id,
                                                                  self.testbed_id,
                                                                  self.remark)


class Output:
    def __init__(self,
                 testbed_id: int,
                 testbed_location: str,
                 returncode: int,
                 stdout: str,
                 stderr: str,
                 duration_ms: int,
                 event_start_epoch_ms: int):
        self.testbed_id = testbed_id
        self.testbed_location = testbed_location
        self.returncode = returncode
        self.stdout = stdout
        self.stderr = stderr
        self.duration_ms = duration_ms
        self.event_start_epoch_ms = event_start_epoch_ms
        self.output_class = self.get_output_class()

    def get_output_class(self) -> str:
        """
        The order in which branches are judged cannot be reversed，
        because Whether the test case has a syntax error or not, chakraCore's returnCode is equal to 0
        """
        if self.returncode == -9 and self.duration_ms > 30 * 1000:
            return "timeout"
        elif self.returncode < 0:
            return "crash"
        elif self.returncode > 0 or not self.stderr == "":
            return "script_error"
        else:
            return "pass"

    def serialize(self):
        return {"testbed_id": self.testbed_id,
                "testbed_location": self.testbed_location,
                "returncode": self.returncode,
                "stdout": self.stdout,
                "stderr": self.stderr,
                "duration_ms": self.duration_ms,
                "event_start_epoch_ms": self.event_start_epoch_ms
                }

    def __str__(self):
        return json.dumps({"testbed_id": self.testbed_id,
                           "testbed_location": self.testbed_location,
                           "returncode": self.returncode,
                           "stdout": self.stdout,
                           "stderr": self.stderr,
                           "duration_ms": self.duration_ms,
                           "event_start_epoch_ms": self.event_start_epoch_ms},
                          indent=4)


class HarnessResult:
    """
    This is the result type of the differential test, as opposed to ResultClass,
    which is the type that holds the results of the execution at runtime.
    """

    def __init__(self, function_id: int, testcase_id: int, testcase_context: str):
        self.function_id = function_id
        self.testcase_id = testcase_id
        self.testcase_context = testcase_context
        self.outputs: list[Output] = []

    def __str__(self):
        return json.dumps({"Harness_Result": {"testcase_id": self.testcase_id,
                                              "testcase_context": self.testcase_context,
                                              "outputs": [e.serialize() for e in self.outputs]
                                              }
                           }, indent=4)

    def get_majority_output(self) -> Majority:
        """Majority vote on testcase outcomes and outputs."""
        majority_outcome, outcome_majority_size = collections.Counter([
            output.output_class for output in self.outputs
        ]).most_common(1)[0]
        majority_stdout, stdout_majority_size = collections.Counter([
            output.stdout for output in self.outputs
        ]).most_common(1)[0]
        return Majority(majority_outcome, outcome_majority_size,
                        majority_stdout, stdout_majority_size)

    def differential_test(self) -> List[DifferentialTestResult]:
        if self.outputs is None:
            return []
        ratio = 2 / 3
        majority = self.get_majority_output()
        # print(majority)
        testbed_num = len(self.outputs)
        # print(testbed_num)

        bugs_info = []
        for output in self.outputs:
            if output.output_class == "crash":
                bugs_info.append(
                    DifferentialTestResult(self.function_id, self.testcase_id, "crash", output.testbed_id,
                                           output.testbed_location))
                pass
            elif majority.majority_outcome != output.output_class and majority.outcome_majority_size >= math.ceil(
                    ratio * testbed_num):
                if majority.majority_outcome == "pass":
                    bugs_info.append(
                        DifferentialTestResult(self.function_id, self.testcase_id, "Most Java engines pass",
                                               output.testbed_id,
                                               output.testbed_location))
                elif majority.majority_outcome == "timeout":
                    # Usually, this is not a bug
                    pass
                elif majority.majority_outcome == "crash":
                    bugs_info.append(
                        DifferentialTestResult(self.function_id, self.testcase_id, "Most Java engines crash",
                                               output.testbed_id,
                                               output.testbed_location))
                elif majority.majority_outcome == "script_error":
                    bugs_info.append(
                        DifferentialTestResult(self.function_id, self.testcase_id,
                                               "Majority Java engines throw runtime error/exception",
                                               output.testbed_id,
                                               output.testbed_location))
            elif output.output_class == "pass" and majority.majority_outcome == output.output_class and \
                    output.stdout != majority.majority_stdout and \
                    majority.stdout_majority_size >= math.ceil(ratio * majority.outcome_majority_size):
                if majority.outcome_majority_size >= math.ceil(ratio * testbed_num):
                    bugs_info.append(
                        DifferentialTestResult(self.function_id, self.testcase_id, "Pass value *** run error",
                                               output.testbed_id,
                                               output.testbed_location))
        return bugs_info

    def save_to_table_result(self):
        """
        Save the result to the database.
        :return:
        """
        table_result = Table_Result()
        for output in self.outputs:
            table_result.insertDataToTableResult(self.testcase_id, output.testbed_id, output.returncode, output.stdout,
                                                 output.stderr, output.duration_ms, 0, 0,
                                                 None)


class Harness:
    @staticmethod
    def get_engines():
        table_testbed = Table_Testbed()
        testbed_list = table_testbed.selectAllIdAndLocateFromTableTestbed()
        return testbed_list

    def __init__(self):
        """
        initialize harness
        :param engines: engines to be test
        """
        self.engines = self.get_engines()
        self.name = ""

    # 取出第一个类的类名
    def get_class_name(self, Testcase_context):
        pattern = r"(public class )(MyJVMTest_[\d]*?)(?=(\s{)|{)"
        name = re.search(pattern, Testcase_context).group(2)
        self.name = name

    def run_testcase(self, function_id: int, testcase_id: int, testcase_context: str, time:str = "60") -> HarnessResult:
        """
        Execute test cases with multiple engines and return test results after execution of all engines.
        :param function_id: executed function Id
        :param testcase_id:  executed Testcases Id
        :param testcase_context: Testcases to be executed
        :return: test results
        """
        result = HarnessResult(function_id=function_id, testcase_id=testcase_id, testcase_context=testcase_context)
        result.outputs = self.multi_thread(time)
        # result.outputs = self.single_thread(testcase_path)
        # result.outputs = self.run_testcase_single_engine(testcase_path)
        return result

    def multi_thread(self,time:str="60") -> List[Output]:
        """
        Multithreading test execution test cases
        :param testcase_path: path of the test case
        :return: execution results of all engines
        """
        outputs = []
        threads_pool = []
        for engine in self.engines:
            testbed_id = engine[0]
            testbed_location = engine[1]
            jvm_name = testbed_location.split("/")[3]
            # print(jvm_name)
            new_file_loc = code_files + jvm_name + "/out/" + self.name + ".class"
            testcase_path = pathlib.Path(new_file_loc)
            tmp = ThreadLock(testbed_location=testbed_location, testcase_path=testcase_path, testbed_id=testbed_id,time=time)
            threads_pool.append(tmp)
            tmp.start()
        for thread in threads_pool:
            thread.join()
            if thread.returnInfo:
                gc.collect()
            elif thread.output is not None:
                outputs.append(thread.output)
        return outputs

    def run_testcase_single_engine(self, want_testbed_id, want_testbed_location=""):
        logger.info("use engine:", self.engines)
        outputs = []
        threads_pool = []
        for engine in self.engines:
            if engine[0] == want_testbed_id:
                testbed_id = engine[0]
                if want_testbed_location != "":
                    testbed_location = want_testbed_location
                else:
                    testbed_location = engine[1]
                jvm_name = testbed_location.split("/")[3]
                logger.info(jvm_name)
                new_file_loc = code_files + jvm_name + "/out/" + self.name + ".class"
                testcase_path = pathlib.Path(new_file_loc)
                tmp = ThreadLock(testbed_location=testbed_location, testcase_path=testcase_path, testbed_id=testbed_id)
                threads_pool.append(tmp)
                tmp.start()
        for thread in threads_pool:
            thread.join()
            if thread.returnInfo:
                gc.collect()
            elif thread.output is not None:
                outputs.append(thread.output)
        return outputs

    def run_testcase_single_engine2(self, want_testbed_location, mm):
        # print("use engine:", self.engines)
        outputs = []
        threads_pool = []
        # code_files2 = "/tmp/comfort_jvm_cov/" + str(mm) + "/"
        code_files2 = os.path.join(code_files_cov, str(mm))
        jvm_name = want_testbed_location.split("/")[3]
        new_file_loc = os.path.join(code_files2, jvm_name) + "/out/" + self.name + ".class"
        # print("new_file_loc: ",new_file_loc)
        testcase_path = pathlib.Path(new_file_loc)
        tmp = ThreadLock(testbed_location=want_testbed_location, testcase_path=testcase_path, testbed_id=-1, mm=mm)
        tmp.isCov = True
        threads_pool.append(tmp)
        tmp.start()
        for thread in threads_pool:
            thread.join()
            if thread.returnInfo:
                gc.collect()
            elif thread.output is not None:
                outputs.append(thread.output)
        testcase_path.unlink()
        return outputs
    
    def run_testcase_single_engine_cov(self, want_testbed_location, mm):
        # print("use engine:", self.engines)
        outputs = []
        threads_pool = []
        # code_files2 = os.path.join(code_files_cov, str(mm))
        # jvm_name = want_testbed_location.split("/")[3]
        code_files2_jvm = '/root/ssfuzz/fuzz/shannon_v1/jdk11u-dev'
        new_file_loc = code_files2_jvm + "/out/" + self.name + ".class"
        # print("new_file_loc: ",new_file_loc)
        testcase_path = pathlib.Path(new_file_loc)
        tmp = ThreadLock(testbed_location=want_testbed_location, testcase_path=testcase_path, testbed_id=-1, mm=mm)
        # tmp.isCov = True
        threads_pool.append(tmp)
        tmp.start()
        for thread in threads_pool:
            thread.join()
            if thread.returnInfo:
                gc.collect()
            elif thread.output is not None:
                outputs.append(thread.output)
        # testcase_path.unlink()
        return outputs

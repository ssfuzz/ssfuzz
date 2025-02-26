import subprocess
import sys, re
import tempfile
from loguru import logger
import sys
sys.path.append('/root/ssfuzz/workline')

from workline.src.studyMysql.Table_Operation import Table_Testcase, Table_Function
from workline.harness_tools.harness_class import Harness as H_java
from workline.harness_tools.harness_class_javac import Harness as H_javac


class Testcase_Object(object):
    def __init__(self, Testcase_item):
        self.Id = Testcase_item[0]
        self.Testcase_context: str = Testcase_item[1]
        self.SourceFun_id = Testcase_item[2]
        self.SourceTestcase_id = Testcase_item[3]
        self.Fuzzing_times = Testcase_item[4]
        self.Mutation_method = Testcase_item[5]
        self.Mutation_times = Testcase_item[6]
        self.Interesting_times = Testcase_item[7]
        self.Probability = Testcase_item[8]
        self.Remark = Testcase_item[9]

    def check_classfile(self) -> int:
        harness = H_javac()
        if self.Testcase_context != "":
            harness.get_class_name(self.Testcase_context)
            logger.info('*' * 25 + "start check class file..." + '*' * 25)
            counter = harness.check_classfile(harness.name)
            return counter
        else:
            logger.info(f"{self.Id} has no content.")

    def single_engine_compile(self, testbed_id, want_testbed_location=""):
        harness = H_javac()
        logger.info("Compiling the ", self.Id, "testcase...")
        if self.Testcase_context != "":
            harness.get_class_name(self.Testcase_context)
            name = harness.name
            compiled_result = harness.compile_single_engine(name, self.Testcase_context, testbed_id,
                                                            want_testbed_location)
            return compiled_result
        else:
            return None

    def single_engine_compile_coverage(self, want_testbed_location, mm):
        harness = H_javac()
        logger.info("*" * 25, "Compiling the {} testcase...".format(self.Id))
        if self.Testcase_context != "":
            harness.get_class_name(self.Testcase_context)
            name = harness.name
            compiled_result = harness.compile_single_engine2(name, self.Testcase_context, want_testbed_location, mm)
            return compiled_result
        else:
            return None

    def single_engine_run_coverage(self, want_testbed_location, mm):
        harness = H_java()
        logger.info("*" * 25, "Running the ", self.Id, "testcase...", "*" * 25)
        if self.Testcase_context != "":
            harness.get_class_name(self.Testcase_context)
            harness_result = harness.run_testcase_single_engine_cov(want_testbed_location, mm)
            # print(harness_result[0])
            return harness_result
        else:
            logger.info(f"{self.Id} has no content.")
            return None

    def single_engine_run(self, testbed_id, want_testbed_location=""):
        harness = H_java()
        logger.info("Running the ", self.Id, "testcase...")
        if self.Testcase_context != "":
            harness.get_class_name(self.Testcase_context)
            harness_result = harness.run_testcase_single_engine(testbed_id, want_testbed_location)
            return harness_result
        else:
            logger.info(f"{self.Id} has no content.")
            return None

    def engine_compile_testcase(self):
        harness = H_javac()
        logger.info("Compiling the {} testcase...".format(self.Id))
        if self.Testcase_context != "":
            harness.get_class_name(self.Testcase_context)
            name = harness.name
            compiled_result = harness.run_testcase(self.SourceFun_id, self.Id, self.Testcase_context)
            return compiled_result
        else:
            return None

    def engine_run_testcase(self, time: str = "60"):
        harness = H_java()
        logger.info('*' * 25 + "Running the ", self.Id, "testcase..." + '*' * 25)
        if self.Testcase_context != "":
            harness.get_class_name(self.Testcase_context)
            harness_result = harness.run_testcase(self.SourceFun_id, self.Id, self.Testcase_context, time=time)
            self.Fuzzing_times += 1
            return harness_result
        else:
            logger.info(f"{self.Id} has no content.")
            return None

    def add_interesting_times(self, interesting_number):
        self.Interesting_times += interesting_number

    def add_mutation_times(self, mutation_times):
        self.Mutation_times += mutation_times

    def mutation_method4(self):
        with tempfile.NamedTemporaryFile(delete=True) as tmpfile:
            temp_file_path = tmpfile.name
            # print(temp_file_name)  # /tmp/tmp73zl8gmn
            tmpfile.write(self.Testcase_context.encode())
            tmpfile.seek(0)
            # tmpTxt = tmpfile.read().decode()
            # print(tmpTxt)
            all_mutation_testcases = self.Testcase_Mutatiod4(temp_file_path)
            # print(len(result))
            # for item in result:
            #     print(item)
            #     print('-----------------------------------------------------')

        all_mutation_testcases_pass = self.jshint_check_testcases(all_mutation_testcases)

        table_testcase = Table_Testcase()

        testcases_list_to_write = self.make_all_mutation_testcases_passListToWrite(all_mutation_testcases_pass,
                                                                                   self.SourceFun_id,
                                                                                   self.Id, 0, 4, 0, 0, 0, None)

        table_testcase.insertManyDataToTableTestcase(testcases_list_to_write)

        return all_mutation_testcases_pass


    def make_all_mutation_testcases_passListToWrite(self, all_mutation_testcases_pass, SourceFun_id, SourceTestcase_id,
                                                    Fuzzing_times,
                                                    Mutation_method, Mutation_times, Interesting_times, Probability,
                                                    Remark) -> list:
        lis = []

        for testcase in all_mutation_testcases_pass:
            Testcases_content = testcase
            item = [Testcases_content, SourceFun_id, SourceTestcase_id, Fuzzing_times, Mutation_method,
                    Mutation_times, Interesting_times, Probability, Remark]
            lis.append(item)
        return lis

    pass

    def get_function_content(self):
        from workline.table_to_class.Table_Function import Function_Object
        table_function = Table_Function()
        function = table_function.selectOneFromTableFunction(self.SourceFun_id)
        function_object = Function_Object(function)
        return function_object

    def mutation_method1_2(self, sess, if_save_function):
        from workline.table_to_class.Table_Function import Function_Object
        function_object = self.get_function_content()
        all_functions_generated, all_functions_replaced_generated = function_object.gpt_mutation_1_2(sess,
                                                                                                     if_save_function)

        regex = r"function(.+\n)+}"
        all_functions_generated_testcases = set()
        all_functions_replaced_generated_testcases = set()

        for function in all_functions_generated:
            result = re.sub(regex, function, self.Testcase_context, 0, re.MULTILINE)
            all_functions_generated_testcases.add(result)

        for function in all_functions_replaced_generated:
            result = re.sub(regex, function, self.Testcase_context, 0, re.MULTILINE)
            all_functions_replaced_generated_testcases.add(result)

        all_functions_generated_testcases_pass = self.jshint_check_testcases(all_functions_generated_testcases)
        all_functions_replaced_generated_testcases_pass = self.jshint_check_testcases(
            all_functions_replaced_generated_testcases)
        table_testcase = Table_Testcase()

        all_functions_generated_testcases_pass_list_to_write = self.make_all_mutation_testcases_passListToWrite(
            all_functions_generated_testcases_pass,
            self.SourceFun_id,
            self.Id, 0, 1, 0, 0, 0, None)
        all_functions_replaced_generated_testcases_pass_list_to_write = self.make_all_mutation_testcases_passListToWrite(
            all_functions_replaced_generated_testcases_pass,
            self.SourceFun_id,
            self.Id, 0, 2, 0, 0, 0, None)

        table_testcase.insertManyDataToTableTestcase(all_functions_generated_testcases_pass_list_to_write)
        table_testcase.insertManyDataToTableTestcase(all_functions_replaced_generated_testcases_pass_list_to_write)

        return all_functions_generated_testcases_pass, all_functions_replaced_generated_testcases_pass

    def updateFuzzingTimesInterestintTimes(self):
        table_Testcases = Table_Testcase()
        table_Testcases.updateFuzzingTimesInterestintTimes(self.Fuzzing_times,
                                                           self.Interesting_times, self.Id)

    def failcompiler_interesting(self):
        self.Interesting_times = -1

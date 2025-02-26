# -*- coding: utf-8 -*-
from loguru import logger
import numpy as np
from scipy import linalg
import time, sys
# export LC_ALL=C
from multiprocessing.dummy import Pool as ThreadPool
from pathlib import Path
import os
logger.add('a.log')
BASE_DIR = str(Path(__file__).resolve().parent.parent)
sys.path.append(BASE_DIR)
from src.studyMysql.Table_Operation import Table_Testcase, Table_Result
from workline.table_to_class.Table_Testcase import Testcase_Object
from workline.Energy import Energy
from workline.Coverage import Coverage
from workline.harness_tools.harness_class import Harness as H_java

table_Testcases = Table_Testcase()

def muti_javac_compile(testcase, isInsert=True):
    testcase_object = Testcase_Object(testcase)
    logger.info('*' * 25 + f'compile {testcase_object.Id}' + '*' * 25)
    Compiled_result = testcase_object.engine_compile_testcase()

    if isInsert:

        if Compiled_result != None:

            Compiled_result.save_to_table_result()

            different_result_list = Compiled_result.differential_test()

            if not len(different_result_list):
                logger.info("nothing happened.")
            else:
                logger.info("{}error has been occured by java compiler.".format(len(different_result_list)))

                for interesting_test_result in different_result_list:

                    interesting_test_result.save_to_table_suspicious_Result()
            logger.info(f'Spending time:{int(time.time() - start_time)}s')
            return different_result_list
        else:
            logger.info('*' * 25 + f'{testcase_object.Id} has no content.' + '*' * 25)
    return None


def check(testcase):
    testcase_object = Testcase_Object(testcase)
    counter = testcase_object.check_classfile()
    logger.info(f'{testcase_object.Id} classfiles has been made: ' + str(counter))

    return counter


def muti_harness(testcase, isInsert=True, file_counter=-1):
    testcase_object = Testcase_Object(testcase)
    if file_counter == 0:
        print(f"{testcase_object.Id} no classfile found.")
        return
    logger.info('*' * 25 + f'running{testcase_object.Id}' + '*' * 25)
    start_time = time.time()

    harness_result = testcase_object.engine_run_testcase(time="60")
    if isInsert:
        if harness_result != None:
            try:
                harness_result.save_to_table_result()
            except:
                pass

            different_result_list = harness_result.differential_test()

            if not len(different_result_list):
                logger.info("nothing happened.")

            else:
                logger.info("{}error has been occured by JVM.".format(len(different_result_list)))

                testcase_object.add_interesting_times(1)

                for interesting_test_result in different_result_list:

                    interesting_test_result.save_to_table_suspicious_Result()

        logger.info(f'Spending time:{int(time.time() - start_time)}s')

        testcase_object.updateFuzzingTimesInterestintTimes()


def run_javac_demo(list_unfuzzing):
    print(list_unfuzzing[0])
    for testcase in list_unfuzzing:
        try:
            start_time = time.time()
            muti_javac_compile(testcase)
            end_time = time.time()
            all_time = end_time - start_time
            logger.info(f'take {int(end_time - start_time)}s second. ')
          

        except Exception as e:
            logger.warning("muti_javac_compile catch Exception:{}".format(e))
        print(testcase)
        file_counter = check(testcase)
        if file_counter == 0:
            logger.info("update insteresting times to -1")

            faile_compiler(testcase=testcase)
        elif file_counter != 19:
            logger.info("incorrect file count: ", file_counter)

        elif file_counter == 19:
            logger.info("incorrect file count: ", file_counter)
            muti_harness(testcase, file_counter)

def faile_compiler(testcase):
    testcase_object = Testcase_Object(testcase)
    testcase_object.failcompiler_interesting()
    testcase_object.updateFuzzingTimesInterestintTimes()


def get_coverage(testcase):
    harness = H_java()
    Testcase_id = testcase[0]
    Testcase_context = testcase[1]
    mm = 33
    want_testbed_location = '/root/jdk_cov/jdk11u-dev/build/linux-x86_64-normal-server-release/jdk/bin/java'

    if Testcase_context != "":

        harness.get_class_name(Testcase_context)
        harness_result = harness.run_testcase_single_engine_cov(want_testbed_location,mm)

    else:
        print(f"{Testcase_id} has no content.")

    os.system(
        "cd /root/jdk_cov/jdk11u-dev/build/linux-x86_64-normal-server-release/hotspot/variant-server/libjvm/objs&&lcov -b ./ -d ./ --rc lcov_branch_coverage=1 --gcov-tool /usr/bin/gcov-7 -c -o output.info&&genhtml --rc genhtml_branch_coverage=1 --rc geninfo_gcov_all_blocks=0 -o ../HtmlFile output.info|tee result.txt")

    delect_gcda = os.system(
        "cd /root/jdk_cov/jdk11u-dev/build/linux-x86_64-normal-server-release/hotspot/variant-server/libjvm/objs&&find . -name '*.gcda' | xargs -i rm -f {}")

    html_root_dir = '/root/jdk_cov/jdk11u-dev/build/linux-x86_64-normal-server-release/hotspot/variant-server/libjvm/HtmlFile'
    analyse_output_path = '/root/ssfuzz/workline/data/analyse/analyse.txt'
    cov_obj = Coverage()
    cov_obj.analyseHtmls(html_root_dir, analyse_output_path)

    os.system("rm -r "+html_root_dir)


def get_energy(ana_path):
    obj = Energy()

    test1f = ana_path

    c2 = obj.get_frequency(test1f)

    energy = obj.get_energy(c2)
    return energy


if __name__ == '__main__':
    list_unfuzzing = table_Testcases.selectAllNeedTestFromTestcase()
    start_time = time.time()
    run_javac_demo(list_unfuzzing)
    end_time = time.time()
    all_time = end_time - start_time
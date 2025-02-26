from workline.table_to_class.Table_Testcase import Testcase_Object


def harness_testcase(testcase):
    testcase_object = Testcase_Object(testcase)
    javac_result = testcase_object.engine_compile_testcase()
    harness_result = testcase_object.engine_run_testcase()
    different_result_list = harness_result.differential_test()
    return javac_result, harness_result, different_result_list

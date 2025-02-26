import itertools
import math
import re
import subprocess
import sys
import tempfile
import time

from src.studyMysql.Table_Operation import Table_Testcase, Table_Function
from src.utils.config import generate_model_dir, generate_model_name
from workline.assemble_tools.callable_processor import CallableProcessor


class Function_Object(object):

    def __init__(self, function_object):
        self.Id = function_object[0]
        self.Function_Content: str = function_object[1]
        self.SourceFun_Id = function_object[2]
        self.Mutation_Method = function_object[3]
        self.Remark = function_object[4]
        self.js_line_count = self.getJSfileSize(self.Function_Content, 10)
        self.var_line_count = self.count_var_lines(self.Function_Content)

    def __str__(self):
        return str(self.Function_Content)

    def count_var_lines(self, code: str):

        regex = r'function.*\n( {4}"use strict";\n)?( {4}var.*\n)*'

        matches = re.finditer(regex, code, re.MULTILINE)

        count = 0
        for matchNum, match in enumerate(matches, start=1):
            # print(match.group(0))

            for line in match.group(0).splitlines():
                count = count + 1
        return count

    def getJSfileSize(self, code: str, cut_max_line: int) -> int:
        lines_list = code.splitlines()
        return min(len(lines_list), cut_max_line)

    def makeFunctionListToWrite(self, all_functions, SourceFun_id, mutation_type, Remark) -> list:

        lis = []

        for function in all_functions:
            Function_content = function
            item = [Function_content, SourceFun_id, mutation_type, Remark]
            lis.append(item)
        return lis

        # Function_content, SourceFun_id, Mutation_method, Remark

    def gpt_mutation_1_2(self, sess, if_save_function):

        all_start_time = time.time()
        IfReplaceBlock = True

        all_functions_generated = set()
        all_functions_replaced_generated = set()

        for prefix_line in range(self.var_line_count, self.js_line_count):
            all_functions_replace_block = set()
            start_time = time.time()
            function_prefix = self.getPrefix(self.Function_Content, prefix_line)
            print("function_prefix:", function_prefix)
            try:
                all_functions = self.function_generate(function_prefix, sess)

            except:
                continue
                
            all_functions_pass = self.jshint_check_function(all_functions)

            if IfReplaceBlock:
                gpt_line_num = prefix_line
                orginal_block_list = self.analysis_js_block(self.Function_Content)

                for function in all_functions_pass:
                    gpt_block_list = self.analysis_js_block(function)

                    block_num = self.fineBlockIdx(orginal_block_list, gpt_block_list, gpt_line_num + 1)

                    if block_num != len(gpt_block_list):
                        try:
                            orginal_block_list_copy = orginal_block_list.copy()

                            orginal_block_list_copy[block_num - 1] = gpt_block_list[block_num - 1]

                            code_string = ''
                            for block in orginal_block_list_copy:
                                code_string = code_string + block
                            all_functions_replace_block.add(code_string)

                        except:
                            pass
            end_time = time.time()

            if if_save_function:
                function_list_to_write1 = self.makeFunctionListToWrite(all_functions=all_functions_pass,
                                                                       SourceFun_id=self.Id,
                                                                       mutation_type=1, Remark=None)
                function_list_to_write2 = self.makeFunctionListToWrite(all_functions=all_functions_replace_block,
                                                                       SourceFun_id=self.Id,
                                                                       mutation_type=2, Remark=None)
                self.write_to_Table_function(function_list_to_write1, function_list_to_write2)
            else:
                all_functions_generated = all_functions_generated.union(all_functions_pass)
                all_functions_replaced_generated = all_functions_replaced_generated.union(all_functions_replace_block)

        return all_functions_generated, all_functions_replaced_generated

    def write_to_Table_function(self, *lis):
        list_to_write = []

        for item in lis:
            list_to_write += item
        # print(list_to_write)

        table_Function = Table_Function()
        table_Function.insertManyDataToTableFunction(list_to_write)

    def function_generate(self, function_prefix, sess):
        import gpt_2_simple as gpt2
        generate_prefix_top2000 = "//2000Functions\n"

        generate_prefix = generate_prefix_top2000 + function_prefix
        print(generate_prefix)


        nsamples = 32
        batch_size = 16
        batches = int(math.ceil(nsamples / batch_size))

        all_functions = set()

        for idx in range(batches):
            try:
                texts = gpt2.generate(sess,
                                      model_dir=generate_model_dir,
                                      model_name=generate_model_name,
                                      nsamples=batch_size,
                                      batch_size=batch_size,
                                      prefix=generate_prefix,
                                      top_p=0,
                                      top_k=0,
                                      temperature=0.5,
                                      include_prefix=True,
                                      return_as_list=True,
                                      length=512
                                      )
                for text in texts:

                    functions = text.split(generate_prefix_top2000)[1:]
                    if len(functions) > 1:
                        all_functions.add(functions[0])
            except:
                continue

        return all_functions

    def getPrefix(self, code: str, cut_line: int):
        lines_list = code.splitlines(True)
        line_list_cut = lines_list[0:cut_line]
        function_cut = ''
        for i in line_list_cut:
            function_cut += i
        # print(function_cut)
        # print('--')
        return function_cut

    def fineBlockIdx(self, orginal_block_list, gpt_block_list, gpt_line_num):
        block_count = 0
        line_count = 0

        for idx, block in enumerate(gpt_block_list):
            line_count = len(block.splitlines()) + line_count

            # print(block, line_count)

            if gpt_line_num <= line_count:
                block_count = idx
                break

        return block_count + 1

    def analysis_js_block(self, code) -> list:
        block_list = []
        regex = r'(^ {4})([^} ].*)([^;]$)\n(.*\n)*? {4}}((\))?)(;?)$\n|^ {4}\w+.*;$\n|function.*\n|^}| {4}"use strict"\n;| {4}for.*{}\n'
        matches = re.finditer(regex, code, re.MULTILINE)
        for matchNum, match in enumerate(matches, start=1):
            block_list.append(match.group())
        return block_list

    def gpt_mutation_3(self, sess):
        start_time = time.time()

        Function_Content_line_list = self.Function_Content.splitlines(True)

        var_number = self.countVarNumber(Function_Content_line_list)
        if var_number:
            all_functions = self.function_generate(Function_Content_line_list[0], sess)

            varSet = []
            for function in all_functions:
                for line in function.splitlines():
                    value = self.getVar(line)
                    if value:
                        varSet.append(value)

                        if len(varSet) > 10:
                            varSet = varSet[:10]

            varSet = set(varSet)

            all_functions_replace_block_pass = set()

            if varSet:
                if var_number < 5:
                    code_list = self.ReplacevalueStatement(Function_Content_line_list, varSet, var_number)

                    all_functions_replace_block_pass = self.jshint_check_function(code_list)

                    function_list_to_write = self.makeFunctionListToWrite(
                        all_functions=all_functions_replace_block_pass,
                        SourceFun_id=self.Id,
                        mutation_type=3, Remark=None)

                    self.write_to_Table_function(function_list_to_write)

            end_time = time.time()
        else:
            print(" ")
    def ReplacevalueStatement(self, test_str_line_list, valset, var_number):
        replaceLineDictList = []

        valsetList = list(valset)

        iterTimes = var_number

        iterPlan = set()

        for c in itertools.permutations(valsetList, iterTimes):
            iterPlan.add(c)

        for c in itertools.combinations_with_replacement(valsetList, iterTimes):
            iterPlan.add(c)

        for item in iterPlan:
            item_iter = iter(item)

            regex = r'^ {4}var \S+ = \S+;\n'
            replaceLineDict = {}
            for old_value_statement_idx, line in enumerate(test_str_line_list):
                matches = re.search(regex, line, re.MULTILINE)
                if matches:
                    old_value_statement = matches.group()
                    valset_Choice = next(item_iter)
                    value = old_value_statement.split('= ')[1].split(';')[0]

                    new_value_statement = old_value_statement.replace(value, valset_Choice)

                    replaceLineDict[old_value_statement_idx] = new_value_statement

            replaceLineDictList.append(replaceLineDict)

        codeList = []
        if replaceLineDictList:
            for replaceLineDict in replaceLineDictList:
                test_str_line_list_copy = self.replaceLine(test_str_line_list, replaceLineDict)
                code_string = ''
                for block in test_str_line_list_copy:
                    code_string = code_string + block
                codeList.append(code_string)

        return codeList

    def getVar(self, line):
        regex = r'^ {4}var \S+ = \S+;'

        matches = re.search(regex, line, re.MULTILINE)
        if matches:
            old_value_statement = matches.group()
            value = old_value_statement.split('= ')[1].split(';')[0]
            return value

    def countVarNumber(self, test_str_line_list):

        regex = r'^ {4}var \S+ = \S+;\n'

        count = 0

        for old_value_statement_idx, line in enumerate(test_str_line_list):
            # matches = re.finditer(regex, line, re.MULTILINE)
            matches = re.search(regex, line, re.MULTILINE)
            if matches:
                count = count + 1
        return count

    def replaceLine(self, test_str_line_list, replaceLineDict):
        test_str_line_list_copy = test_str_line_list.copy()
        for old_value_statement_idx, new_value_statement in replaceLineDict.items():
            test_str_line_list_copy[old_value_statement_idx] = new_value_statement
        return test_str_line_list_copy

    def jshint_check_function(self, all_functions):
        start_time = time.time()
        all_functions_pass = set()
        for function in all_functions:
            with tempfile.NamedTemporaryFile(delete=True) as tmpfile:
                temp_file_path = tmpfile.name
                fine_code = 'var FuzzingFunc = ' + function + ';'
                tmpfile.write(fine_code.encode())
                tmpfile.seek(0)
                tmpTxt = tmpfile.read().decode()
                print("tmpTxt:", tmpTxt)
                result = self.cmd_jshint(temp_file_path)
                if result:
                    all_functions_pass.add(function)

        end_time = time.time()
        return all_functions_pass

    def jshint_check_testcases(self, all_testcases):
        
        start_time = time.time()
        all_testcases_pass = set()
        for testcase in all_testcases:
            testcase_no_print = testcase[:testcase.rfind('\n')]
            print("testcase_no_print:", testcase_no_print)

            with tempfile.NamedTemporaryFile(delete=True) as tmpfile:
                temp_file_path = tmpfile.name
                tmpfile.write(testcase_no_print.encode())
                tmpfile.seek(0)
                result = self.cmd_jshint(temp_file_path)
                print("result", result)
                if result:
                    all_testcases_pass.add(testcase)
        end_time = time.time()
        return all_testcases_pass

    def cmd_jshint(self, temp_file_path):
        cmd = ['timeout', '60s', 'jshint', '-c', '/root/ssfuzz/data/.jshintrc', temp_file_path]

        if sys.platform.startswith('win'): 
            p = subprocess.Popen(cmd, stdout=subprocess.PIPE, shell=True)
        else: 
            p = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        stdout, stderr = p.communicate()
        if stdout:
            print(stdout)
        if stderr:
            print("error")
            print(stderr)

        if stdout.__len__() > 0:
            jshint_flag = False
        else:  
            jshint_flag = True
        return jshint_flag

    def makeTestcasesListToWrite(self, all_testcases, SourceFun_id, SourceTestcase_id, Fuzzing_times,
                                 Mutation_method, Mutation_times, Interesting_times, Probability, Remark) -> list:

        lis = []

        for testcase in all_testcases:
            Testcases_content = testcase
            item = [Testcases_content, SourceFun_id, SourceTestcase_id, Fuzzing_times, Mutation_method,
                    Mutation_times, Interesting_times, Probability, Remark]
            lis.append(item)
        return lis

    def assemble_to_testcase(self):
        callable_processor = CallableProcessor()
        try:
            function_assemle_list = set()
            for i in range(10):
                function_assemle = callable_processor.get_self_calling(self.Function_Content)
                function_assemle_list.add(function_assemle)
            all_testcases_pass = self.jshint_check_testcases(function_assemle_list)
            testcases_list_to_write = self.makeTestcasesListToWrite(all_testcases_pass, self.Id, 0, 0, 0, 0, 0, 0, None)
            table_Testcase = Table_Testcase()
            table_Testcase.insertManyDataToTableTestcase(testcases_list_to_write)
        except:
            pass

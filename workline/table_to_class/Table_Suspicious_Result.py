from pprint import pprint

from workline.mysql_tools.Table_Operation import Table_Testcase, Table_Result, Table_Suspicious_Result
from workline.table_to_class.Table_Result import Result_Object

import yaml



class Suspicious_Result_Object(object):
    def __init__(self, Suspicious_Result_Item):
        self.Id = Suspicious_Result_Item[0]
        self.Error_type: str = Suspicious_Result_Item[1]
        self.Testcase_id = Suspicious_Result_Item[2]
        self.Function_id = Suspicious_Result_Item[3]
        self.Testbed_id = Suspicious_Result_Item[4]
        self.Remark = Suspicious_Result_Item[5]
        self.Is_filtered = Suspicious_Result_Item[6]
        self.ResultDict, self.Returncode = self.getResultListAndReturnCode()
        self.Code_content = self.getCodeContent()
        # print(self.Returncode)

    def getCodeContent(self):
        table_Testcase = Table_Testcase()
        testcase = table_Testcase.selectOneFromTableTestcase(self.Testcase_id)
        return testcase[1]

    def getResultListAndReturnCode(self):
        table_Result = Table_Result()
        Result_list = table_Result.selectTestcasesFromTableResult(self.Testcase_id)
        result_object_dict = {}
        returncode = ''
        for item in Result_list:
            result_object = Result_Object(item)
            returncode += str(result_object.Testbed_Id)
            returncode += f'({result_object.Returncode})'
            result_object_dict[result_object.Testbed_Id] = result_object
        return result_object_dict, returncode

    def extractYaml1(self):
        for returncode_type in filter_info:
            if returncode_type['returncode'] == self.Returncode:
                return returncode_type
        else:
            return None

    def extractYaml2(self, error_info_list):
        error_type_list = []
        for error_info_item in error_info_list:
            flag = True
            if f"'engine': {self.Testbed_id}" in str(error_info_item):
                stdoutList = error_info_item['Stdout']
                stderrList = error_info_item['Stderr']
                codeList = error_info_item['Code']
                if stdoutList is not None:
                    flag = self.compareInfo('Stdout', stdoutList)
                if stderrList is not None and flag:
                    flag = self.compareInfo('Stderr', stderrList)
                if codeList is not None and flag:
                    flag = self.compareInfo('Code', codeList)
                if flag:
                    error_type_list.append(error_info_item)
                else:
                    pass
            else:
                pass
        return error_type_list

    def analysis(self):
        Returncode_block = self.extractYaml1()
        if not Returncode_block:
            pass
        else:
            print('匹配到return code', self.Returncode)
            error_info_list = self.extractYaml2(Returncode_block['error_info'])
            if len(error_info_list) == 0:
                pass
            else:
                remark_id = ""
                for error_info_item in error_info_list:
                    remark_id += f"({str(error_info_item['remark-id'])})"
                table_suspicious_Result = Table_Suspicious_Result()
                try:
                    table_suspicious_Result.updateIs_filtered(self.Id, remark_id)
                except:
                    pass

    def judgeInfo(self, stdout: str, info: str):
        if info in stdout:
            return True
        else:
            return False

    def judgeInfoByRegex(self, stdout: str, info: str):
        import re

        regex = info

        test_str = stdout

        matches = re.finditer(regex, test_str, re.MULTILINE)

        for matchNum, match in enumerate(matches, start=1):
            return True

        return False
    def compareInfo(self, type, stdList):
        for std in stdList:
            if type == 'Stdout':
                if self.judgeInfoByRegex(self.ResultDict.get(std['engine']).Stdout, std['info']):
                    pass
                else:
                    return False

            if type == 'Stderr':
                if self.judgeInfoByRegex(self.ResultDict.get(std['engine']).Stderr, std['info']):
                    pass
                else:
                    return False
            if type == 'Code':
                if self.judgeInfoByRegex(self.Code_content, std['content']):
                    pass
                else:
                    return False
        return True


import os

from workline.src.studyMysql.db_operation_base import DataBaseHandle


class Table_Function(object):
    def __init__(self):
        self.__table = DataBaseHandle()

    def selectOneFromTableFunction(self, id):
        sql = 'select * from Table_Function where id=%s'
        prames = (id)
        return (self.__table.selectOne(sql, prames),)

    def selectSourceIdFromTableFunction(self, SourceFun_id):
        sql = f'select * from Table_Function where SourceFun_id={SourceFun_id}'
        return self.__table.selectall(sql)


    def selectFromTableFunctionForNumber(self, id, number):
        sql = 'select * from Table_Function where id=%s limit %s'
        prames = (id, number)
        return self.__table.selectmany(sql, prames)

    def selectAllFromTableFunction(self):
        sql = 'select * from Table_Function'
        return self.__table.selectall(sql)

    def insertDataToTableFunction(self, Function_Content, SourceFun_Id, Mutation_Method, Remark):
        sql = 'INSERT INTO Table_Function(Function_content,SourceFun_id,Mutation_method,Remark) values(%s,%s,%s,%s)'
        prames = (Function_Content, SourceFun_Id, Mutation_Method, Remark)
        return self.__table.insert(sql, prames)

    def insertManyDataToTableFunction(self, lis):
        sql = 'insert into Table_Function(Function_content,SourceFun_id,Mutation_method,Remark) values(%s,%s,%s,%s)'
        return self.__table.insertMany(sql, lis)

    def deleteFromTableFunction(self, id):
        sql = 'delete from Table_Function where id=%s'
        prames = (id,)
        return self.__table.delete(sql, prames)

    def deleteAllFromTableFunction(self):
        sql = 'delete from Table_Function'
        return self.__table.deleteAll(sql)

    def updateDataBaseHandle(self, id, Function_content):
        sql = 'update Table_Function set Function_content= %s where id = %s'
        prames = (Function_content, id)
        return self.__table.update(sql, prames)


class Table_Testcase(object):

    def __init__(self):
        self.__table = DataBaseHandle()

    def selectAllfromTableTestcase(self):
        sql = 'SELECT * FROM Table_Testcase WHERE id > 1000 and Table_Testcase.Interesting_times!=-1 ORDER BY id ASC;' # 这里id>1000我不理解
        return self.__table.selectall(sql)

    def selectTestCoverageTestcase(self, limit):
        sql = 'select * from Table_Testcase limit {}'.format(limit)
        return self.__table.selectall(sql)

    def selectOneFromTableTestcase(self, id):
        sql = 'select * from Table_Testcase where id=%s'
        prames = (id)
        return (self.__table.selectOne(sql, prames),)

    def selectOneContextFromTestcaseContent(self, id):
        sql = "select Testcase_context from Table_Testcase where id=%s"
        prames = (id)
        return self.__table.selectOne(sql, prames)

    def selectOneMutatorFromTestcaseContent(self, id):
        sql = "select MutatorIdAndTableList from Table_Testcase where id=%s"
        prames = (id)
        return self.__table.selectOne(sql, prames)

    def seleOneTestcaseContent(self, con):
        sql = "select Testcase_context from Table_Testcase where Testcase_context like \'" + con + "\' limit 1"
        return self.__table.selectall(sql)

    def selectMutationMethodFromTableTestcaseNotJavac(self, Mutation_method):
        sql = f'select * from Table_Testcase where Mutation_method={Mutation_method} ' \
              f'and id not in(select distinct(Testcase_id) from Table_javac_Result)'
        return self.__table.selectall(sql)

    def selectMutationMethodFromTableTestcase(self, Mutation_method):
        sql = f'select * from Table_Testcase where Mutation_method={Mutation_method}' \
              f'and id in (select distinct Testcase_id from Table_javac_Result where Returncode=0)'
        return self.__table.selectall(sql)

    def selectallFromTableTestcase2(self):
        sql = f'select * from Table_Testcase where ' \
              f'id in (select distinct Testcase_id from Table_javac_Result where Returncode =0)'
        return self.__table.selectall(sql)

    def select234FromTableTestcase(self):
        sql = f'select * from Table_Testcase where (Mutation_method =2 or Mutation_method =4 or Mutation_method =3) ' \
              f'and id in (select distinct Testcase_id from Table_javac_Result where Returncode =0)'
        return self.__table.selectall(sql)

    def select78FromTableTestcase(self):
        sql = f'select * from Table_Testcase where (Mutation_method =7 or Mutation_method =8) ' \
              f'and id in (select distinct Testcase_id from Table_javac_Result where Returncode =0)'
        return self.__table.selectall(sql)

    def select12FromTableTestcase(self):
        sql = f'select * from Table_Testcase where Mutation_method =12 ' \
              f'and id in (select distinct Testcase_id from Table_javac_Result where Returncode =0)'
        return self.__table.selectall(sql)

    def selectMutationMethodFromTableTestcase3(self, Mutation_method):
        sql = f'select * from Table_Testcase where Mutation_method={Mutation_method} ' \
              f'and id in (select distinct Testcase_id from Table_javac_Result where Returncode=0) and id not in ' \
              f'(select distinct Testcase_id from Table_Result)'
        return self.__table.selectall(sql)

    def selectMutationMethodFromTableTestcase2(self, Mutation_method, Fuzzing_times):
        sql = f'select * from Table_Testcase where Mutation_method={Mutation_method} and Fuzzing_times={Fuzzing_times}'
        return self.__table.selectall(sql)

    def selectIdFromTableTestcase(self, id):
        sql = f'select * from Table_Testcase where Id={id}'
        return self.__table.selectall(sql)

    def selectTestcaseIdFromTableJavacResult(self, Returncode):
        sql = f'select Testcase_id from Table_javac_Result where Returncode={Returncode}'
        return self.__table.selectall(sql)

    def selectTestcaseIdFromTable(self, Table_Name, Mutation_method):
        if Mutation_method == None:
            sql = f'select Testcase_id from {Table_Name}'
        elif Mutation_method != None and Table_Name == 'Table_Testcase':
            sql = f'select id from {Table_Name} where Mutation_method={Mutation_method}'
        return self.__table.selectall(sql)

    def selectInterestingTimeFromTableTestcase(self, Interesting_times):
        sql = f'select * from Table_Testcase where Interesting_times={Interesting_times}'
        return self.__table.selectall(sql)

    def selectFuzzingTimeFromTableTestcase(self, Fuzzing_times):
        sql = f'select * from Table_Testcase where Fuzzing_times={Fuzzing_times} and Interesting_times >= 0' #and Interesting_times >= 0;s
        return self.__table.selectall(sql)

    def selectFromTableTestcaseForNumber(self, id, number):
        sql = 'select * from Table_Testcase where id=%s limit %s'
        prames = (id, number)
        return self.__table.selectmany(sql, prames)
    
    def selectAllNeedTestFromTestcase(self):
        sql = '''
                        with DistinctResults as (
                    select distinct Testcase_id
                    from Table_javac_Result
                    where Returncode = 0
                )
                select T.*
                from Table_Testcase T
                         join DistinctResults R
                              on T.id = R.Testcase_id;'''
        return self.__table.selectall(sql)

    def selectLLMTestFromTestcase(self):
        sql = 'SELECT * FROM Table_Testcase WHERE id > 163182'
        return self.__table.selectall(sql)

    def selectAllFromTableTestcase(self):
        sql = 'select * from Table_Testcase'
        return self.__table.selectall(sql)

    def selectAllJavacR0FromTableTestcase(self):
        sql = 'select Testcase_context from Table_Testcase where id in (select Testcase_id from Table_javac_Result where Returncode = 0)'
        return self.__table.selectall(sql)

    def insertDataToTableTestcase(self, Testcase_context, SourceFun_id, SourceTestcase_id, Fuzzing_times,
                                  Mutation_method, Mutation_times, Interesting_times, Probability, Remark):
        sql = 'INSERT INTO Table_Testcase(Testcase_context, SourceFun_id, SourceTestcase_id, Fuzzing_times,Mutation_method ,Mutation_times,Interesting_times,Probability,Remark) values(%s,%s,%s,%s,%s,%s,%s,%s,%s)'
        prames = (Testcase_context, SourceFun_id, SourceTestcase_id, Fuzzing_times, Mutation_method, Mutation_times,
                  Interesting_times, Probability, Remark)
        return self.__table.insert(sql, prames)

    def insertManyDataToTableTestcase(self, lis):
        sql = 'INSERT INTO Table_Testcase(Testcase_context, SourceFun_id, SourceTestcase_id, Fuzzing_times,Mutation_method ,Mutation_times,Interesting_times,Probability,Remark) values(%s,%s,%s,%s,%s,%s,%s,%s,%s)'
        return self.__table.insertMany(sql, lis)

    def deleteFromTableTestcase(self, id):
        sql = 'delete from Table_Testcase where id=%s'
        prames = (id,)
        return self.__table.delete(sql, prames)

    def deleteAllFromTableTestcase(self):
        sql = 'delete from Table_Testcase'
        return self.__table.deleteAll(sql)

    def updateDataBaseHandle(self, id, Function_content):
        sql = 'update Table_Testcase set Testcase_context= %s where id = %s'
        prames = (Function_content, id)
        return self.__table.update(sql, prames)

    def updateFuzzingTimesInterestintTimes(self, Fuzzing_times, Interesting_times, id):
        sql = 'update Table_Testcase set Fuzzing_times= %s ,Interesting_times = %s where id = %s'
        prames = (Fuzzing_times, Interesting_times, id)
        return self.__table.update(sql, prames)

    def updateMutationTimes(self, MutationTimes, id):
        sql = 'update Table_Testcase set Mutation_times= %s where id = %s'
        prames = (MutationTimes, id)
        return self.__table.update(sql, prames)


class Table_Result(object):

    def __init__(self):
        self.__table = DataBaseHandle()

    def selectFromNotHaveInResult(self):
        sql = 'select distinct Table_javac_Result.Testcase_id from Table_javac_Result left join Table_Result on ' \
              'Table_javac_Result.Testcase_id=Table_Result.Testcase_id where Table_Result.Testcase_id is null and Table_javac_Result.Returncode=0;'
        return self.__table.selectall(sql)

    def selectAllReturncode0(self):
        sql = 'select distinct Testcase_id from Table_javac_Result where Returncode=0;'
        return self.__table.selectall(sql)

    def selectFromNotCompleteInResult(self):
        sql = 'select distinct Testcase_id from Table_javac_Result group by Testcase_id,Returncode having count(Testcase_id)<9;'
        return self.__table.selectall(sql)

    def selectOneFromTableResult(self, id):
        sql = 'select * from Table_Result where id=%s'
        params = (id)
        return self.__table.selectOne(sql, params)

    def selectAllFromTableSuspiciousResult(self, mm=0):
        if mm == 0:
            sql = 'select distinct Testcase_id from Table_Suspicious_Result'
            return self.__table.selectall(sql)
        else:
            sql = 'select distinct Testcase_id from Table_Suspicious_Result ' \
                  'where Testcase_id in (select id from Table_Testcase where Mutation_method=' + str(mm) + ')'
            return self.__table.selectall(sql)

    def selectOOMFromTableSuspiciousResult(self, mm=0):
        if mm == 0:
            sql = 'select distinct Testcase_id from Table_Suspicious_Result where Error_type="Majority Java engines throw runtime error/exception" ' \
                  'and (Testbed_id=3 or Testbed_id=4)'
            return self.__table.selectall(sql)
        else:
            sql = 'select distinct Testcase_id from Table_Suspicious_Result where Error_type="Majority Java engines throw runtime error/exception" ' \
                  'and (Testbed_id=3 or Testbed_id=4) and Testcase_id in (select id from Table_Testcase where Mutation_method=' + str(
                mm) + ')'
            return self.__table.selectall(sql)

    def selectCFSFromTableSuspiciousResult(self, mm):
        if mm == 0:
            sql = 'select distinct s.Testcase_id from Table_Suspicious_Result as s join Table_javac_Result as c on Testcase_id where s.Error_type="Most Java engines pass" ' \
                  'and (s.Testbed_id=3 or s.Testbed_id=4) and c.Stderr like "%error: cannot find symbol%"'
            return self.__table.selectall(sql)
        else:
            sql = 'select distinct s.Testcase_id from Table_Suspicious_Result as s join Table_javac_Result as c on s.Testcase_id=c.Testcase_id where s.Error_type="Most Java engines pass" ' \
                  'and (s.Testbed_id=3 or s.Testbed_id=4) and c.Stderr like "%error: cannot find symbol%" ' \
                  'and s.Testcase_id in (select id from Table_Testcase where Mutation_method=' + str(mm) + ')'
            return self.__table.selectall(sql)

    def selectCanFindClassFromTableSuspiciousResult(self, mm):
        if mm == 0:
            sql = 'select distinct s.Testcase_id from Table_Suspicious_Result as s join Table_Result as r on s.Testcase_id=r.Testcase_id where ' \
                  'r.Stderr like "%Error: Could not find or load main class MyJVMTest_%"'
            return self.__table.selectall(sql)
        else:
            sql = 'select distinct s.Testcase_id from Table_Suspicious_Result as s join Table_Result as r on s.Testcase_id=r.Testcase_id where ' \
                  'r.Stderr like "%Error: Could not find or load main class MyJVMTest_%"' \
                  'and s.Testcase_id in (select id from Table_Testcase where Mutation_method=' + str(mm) + ')'
            return self.__table.selectall(sql)

    def selectdivideZeroFromTableSuspiciousResult(self, mm):
        if mm == 0:
            sql = 'select distinct s.Testcase_id from Table_Suspicious_Result as s join Table_Result as r on s.Testcase_id=r.Testcase_id where ' \
                  '(s.Testbed_id=3 or s.Testbed_id=4) and r.Stdout like "%java.lang.ArithmeticException: divide by zero%"'
            return self.__table.selectall(sql)
        else:
            sql = 'select distinct s.Testcase_id from Table_Suspicious_Result as s join Table_Result as r on s.Testcase_id=r.Testcase_id where ' \
                  '(s.Testbed_id=3 or s.Testbed_id=4) and r.Stdout like "% by zero%"' \
                  'and s.Testcase_id in (select id from Table_Testcase where Mutation_method=' + str(mm) + ')'
            return self.__table.selectall(sql)

    def selectOOM2FromTableSuspiciousResult(self, mm):
        if mm == 0:
            sql = 'select distinct s.Testcase_id from Table_Suspicious_Result as s join Table_Result as r on s.Testcase_id=r.Testcase_id where ' \
                  '(s.Testbed_id=3 or s.Testbed_id=4) and r.Stderr like "%java.lang.OutOfMemoryError: Requested array size exceeds VM limit%"'
            return self.__table.selectall(sql)
        else:
            sql = 'select distinct s.Testcase_id from Table_Suspicious_Result as s join Table_Result as r on s.Testcase_id=r.Testcase_id where ' \
                  '(s.Testbed_id=3 or s.Testbed_id=4) and r.Stderr like "%java.lang.OutOfMemoryError: Requested array size exceeds VM limit%"' \
                  'and s.Testcase_id in (select id from Table_Testcase where Mutation_method=' + str(mm) + ')'
            return self.__table.selectall(sql)

    def selectAllFromTableResult(self):
        sql = 'select * from Table_Result'
        return self.__table.selectall(sql)

    def selectByTestcaseIDFromTableResult(self, id):
        sql = 'select * from Table_Result where Testcase_id=%s'
        params = (id)
        return self.__table.selectmany(sql, params)


    def insertDataToTablejavacResult(self, Testcase_Id, Testbed_Id, Returncode, Stdout, Stderr, duration_ms,
                                     seed_coverage,
                                     engine_coverage, Remark):
        sql = 'INSERT INTO Table_javac_Result(Testcase_Id, Testbed_Id, Returncode, Stdout,Stderr ,duration_ms,seed_coverage,engine_coverage,Remark) values(%s,%s,%s,%s,%s,%s,%s,%s,%s)'
        prames = (
            Testcase_Id, Testbed_Id, Returncode, Stdout, Stderr, duration_ms, seed_coverage, engine_coverage, Remark)
        return self.__table.insert(sql, prames)

    def insertDataToTableResult(self, Testcase_Id, Testbed_Id, Returncode, Stdout, Stderr, duration_ms, seed_coverage,
                                engine_coverage, Remark):
        sql = 'INSERT INTO Table_Result(Testcase_Id, Testbed_Id, Returncode, Stdout,Stderr ,duration_ms,seed_coverage,engine_coverage,Remark) values(%s,%s,%s,%s,%s,%s,%s,%s,%s)'
        prames = (
            Testcase_Id, Testbed_Id, Returncode, Stdout, Stderr, duration_ms, seed_coverage, engine_coverage, Remark)
        return self.__table.insert(sql, prames)

    def insertManyDataToTableResult(self, lis):
        sql = 'INSERT INTO Table_Result(Testcase_Id, Testbed_Id, Returncode, Stdout,Stderr ,duration_ms,seed_coverage,engine_coverage,Remark) values(%s,%s,%s,%s,%s,%s,%s,%s,%s)'
        return self.__table.insertMany(sql, lis)

    def deleteFromTableResult(self, id):
        sql = 'delete from Table_Result where id=%s'
        prames = (id,)
        return self.__table.delete(sql, prames)

    def deleteAllFromTableResult(self):
        sql = 'delete from Table_Result'
        return self.__table.deleteAll(sql)

    def updateDataBaseHandle(self, id, Function_content):
        sql = 'update Table_Result set Testcase_context= %s where id = %s'
        prames = (Function_content, id)
        return self.__table.update(sql, prames)


class Table_Testbed(object):

    def __init__(self):
        self.__table = DataBaseHandle()

    def selectAllFromTableTestbed(self):
        sql = 'select * from Table_Testbed'
        return self.__table.selectall(sql)

    def selectAllIdAndLocateFromTableTestbed(self):
        sql = 'select Id,Testbed_location from Table_Testbed'
        return self.__table.selectall(sql)


class Table_Suspicious_Result(object):

    def __init__(self):
        self.__table = DataBaseHandle()

    def insertDataToTableSuspiciousResult(self, Error_type, Testcase_id, Function_id, Testbed_id, Remark):
        sql = 'INSERT INTO Table_Suspicious_Result( Error_type, Testcase_id, Function_id, Testbed_id,  Remark) values(%s,%s,%s,%s,%s)'
        prames = (Error_type, Testcase_id, Function_id, Testbed_id, Remark)
        return self.__table.insert(sql, prames)

    def insertDataToTablejavacSuspiciousResult(self, Error_type, Testcase_id, Function_id, Testbed_id, Remark):
        sql = 'INSERT INTO Table_javac_Suspicious_Result( Error_type, Testcase_id, Function_id, Testbed_id,  Remark) values(%s,%s,%s,%s,%s)'
        prames = (Error_type, Testcase_id, Function_id, Testbed_id, Remark)
        return self.__table.insert(sql, prames)

    def selectErrorTypeFromTableFunction(self, ErrorType):
        sql = f"select * from Table_Suspicious_Result where Error_type={ErrorType} ORDER BY Testcase_id"
        return self.__table.selectall(sql)

    def selectIdFromTablejavacSuspiciousResult(self, id):
        sql = f'select * from Table_Suspicious_Result where Function_id={id}'
        return self.__table.selectall(sql)

    def selectIdFromTableFunction(self, id):
        sql = f'select * from Table_Suspicious_Result where Id={id}'
        return self.__table.selectall(sql)
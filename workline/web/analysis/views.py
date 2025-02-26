import json

from django.http import HttpResponse
from django.shortcuts import render
from django.views.generic import ListView
from django_tables2 import tables, SingleTableView, RequestConfig

from workline.harness_tools.harness_for_web import harness_testcase

from .models import Testcase, Result, Suspicious_Result, Testbed
from .tables import TestbedTable


def show_testcase(request):

    Testcase_id = request.GET.get('id')
    if not Testcase_id:
        Testcase_id = 175837
    all_testcase = Testcase.objects.filter(id=Testcase_id)
    # print("all_testcase: ", all_testcase)
    testcase_result = Result.objects.filter(Testcase_id=Testcase_id)
    # print("testcase_result: ", testcase_result)
    all_suspicious_result = Suspicious_Result.objects.filter(Testcase_id=Testcase_id)
    # print("all_suspicious_result: ", all_suspicious_result)
    remark = request.POST.get('remark')
    all_suspicious_result.update(Remark=remark)
    # print("remark: ", remark)
    return render(request, 'testcase.html', locals())


def harness(request):
    Testcase_id = request.GET.get('id')
    if not Testcase_id:
        Testcase_id = 162824

    all_testcase = Testcase.objects.filter(id=Testcase_id)
    print("all_testcase: ",all_testcase)
    testcase = [all_testcase.first().id,
                all_testcase.first().Testcase_context,
                all_testcase.first().SourceFun_id,
                all_testcase.first().SourceTestcase_id,
                all_testcase.first().Fuzzing_times,
                all_testcase.first().Mutation_method,
                all_testcase.first().Mutation_times,
                all_testcase.first().Interesting_times,
                all_testcase.first().Probability,
                all_testcase.first().Remark]

    Testcase_context = testcase[1]
    testbedRes = Testbed.objects.all()
    testbed_info = {engine.id: engine.Testbed_name for engine in testbedRes}
    return render(request, 'harness.html', locals())


def herness_ajax(request):
    herness_ajax = request.POST.get("herness_ajax")
    testcase_id = request.POST.get("Testcase_id")

    all_testcase = Testcase.objects.filter(id=testcase_id)

    testcase = [all_testcase.first().id,
                all_testcase.first().Testcase_context,
                all_testcase.first().SourceFun_id,
                all_testcase.first().SourceTestcase_id,
                all_testcase.first().Fuzzing_times,
                all_testcase.first().Mutation_method,
                all_testcase.first().Mutation_times,
                all_testcase.first().Interesting_times,
                all_testcase.first().Probability,
                all_testcase.first().Remark]
    # print(herness_ajax)

    testcase[1] = herness_ajax

    # print(testcase)

    javac_result, harness_result, different_result_list = harness_testcase(testcase)

    def obj_different_result_2_json(obj):
        return {
            'testcase_id': obj.testcase_id,
            "error_type": obj.error_type,
            "testbed_id": obj.testbed_id,
        }

    different_result_list = json.dumps(different_result_list, default=obj_different_result_2_json)

    # print(harness_result)
    # print(different_result_list)

    dic = {'javac_result': str(javac_result), 'harness_result': str(harness_result),
           'different_result_list': different_result_list}
    dic = json.dumps(dic)
    # print(dic)

    # return HttpResponse(harness_result, different_result_list)

    # return HttpResponse(harness_result)
    return HttpResponse(dic)


def remark_ajax(request):
    remark_ajax = request.POST.get("remark_ajax")
    testcase_id = request.POST.get("Testcase_id")
    all_suspicious_result = Suspicious_Result.objects.filter(Testcase_id=testcase_id)
    all_suspicious_result.update(Remark=remark_ajax)
    return HttpResponse("remark is ok")


def get_remark_ajax(request):
    testcase_id = request.POST.get("Testcase_id")

    all_suspicious_result = Suspicious_Result.objects.filter(Testcase_id=testcase_id)
    dic = {}
    if all_suspicious_result:
        now_remark: str = all_suspicious_result.first().Remark
        dic['hasRemark'] = True
        dic['remarkContent'] = now_remark
    else:
        dic['hasRemark'] = False
    dic = json.dumps(dic)
    # print(dic)
    return HttpResponse(dic)


class testbed(SingleTableView):
    model = Testbed
    table_class = TestbedTable
    template_name = 'testbed.html'

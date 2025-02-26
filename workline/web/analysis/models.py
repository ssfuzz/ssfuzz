from django.db import models

# Create your models here.
class Testbed(models.Model):
    Testbed_name = models.TextField('Engine Name', null=True)
    Testbed_version = models.TextField('Engine Version', null=True)
    Testbed_location = models.TextField('Engine Location', null=True)
    Remark = models.TextField('Notes', null=True)

    def __str__(self):
        return self.Testbed_name

    class Meta:
        db_table = "Table_Testbed"
        verbose_name = "Engine" 
        verbose_name_plural = verbose_name


class Function(models.Model):
    Function_content = models.TextField(null=True)
    SourceFun_id = models.IntegerField(null=True)
    Mutation_method = models.IntegerField(null=True)
    Mutation_times = models.IntegerField(null=True)
    Remark = models.TextField(null=True)

    class Meta:
        db_table = "Table_Function"


class Suspicious_Result(models.Model):
    Error_type = models.TextField(null=True)
    Testcase_id = models.IntegerField(null=True)
    Function_id = models.IntegerField(null=True)
    Testbed_id = models.IntegerField(null=True)
    Remark = models.TextField(null=True)

    class Meta:
        db_table = "Table_Suspicious_Result"


class Testcase(models.Model):
    Testcase_context = models.TextField(null=True)
    SourceFun_id = models.IntegerField(null=True)
    SourceTestcase_id = models.IntegerField(null=True)
    Fuzzing_times = models.IntegerField(null=True)
    Mutation_method = models.IntegerField(null=True)
    Mutation_times = models.IntegerField(null=True)
    Interesting_times = models.IntegerField(null=True)
    Probability = models.IntegerField(null=True)
    Remark = models.TextField(null=True)

    class Meta:
        db_table = "Table_Testcase"


class Result(models.Model):
    Testcase_id = models.IntegerField(null=True)
    Testbed_id = models.IntegerField(null=True)
    Returncode = models.IntegerField(null=True)
    Stdout = models.TextField(null=True)
    Stderr = models.TextField(null=True)
    Duration_ms = models.IntegerField(null=True)
    Seed_coverage = models.DecimalField(max_digits=5, decimal_places=3, null=True)
    Engine_coverage = models.DecimalField(max_digits=5, decimal_places=3, null=True)
    Remark = models.TextField(null=True)

    class Meta:
        db_table = "Table_Result"


class Javac_Result(models.Model):
    Testcase_id = models.IntegerField(null=True)
    Testbed_id = models.IntegerField(null=True)
    Returncode = models.IntegerField(null=True)
    Stdout = models.TextField(null=True)
    Stderr = models.TextField(null=True)
    Duration_ms = models.IntegerField(null=True)
    Seed_coverage = models.DecimalField(max_digits=5, decimal_places=3, null=True)
    Engine_coverage = models.DecimalField(max_digits=5, decimal_places=3, null=True)
    Remark = models.TextField(null=True)

    class Meta:
        db_table = "Table_javac_Result"


class Javac_Suspicious_Result(models.Model):
    Error_type = models.TextField(null=True)
    Testcase_id = models.IntegerField(null=True)
    Function_id = models.IntegerField(null=True)
    Testbed_id = models.IntegerField(null=True)
    Remark = models.TextField(null=True)

    class Meta:
        db_table = "Table_javac_Suspicious_Result"


class Expression(models.Model):
    id = models.AutoField(primary_key=True)
    content = models.TextField()
    type = models.CharField(max_length=100, null=True)
    varList = models.TextField(null=True)
    filepath = models.CharField(max_length=500, null=True)
    method_info = models.TextField()
    energy = models.FloatField(null=True)
    import_info = models.TextField(null=True)

    class Meta:
        db_table = "Expression"


class GeneVariable(models.Model):
    id = models.AutoField(primary_key=True)
    vari_name = models.CharField(max_length=1000, null=True)
    vari_type = models.CharField(max_length=1000, null=True)
    vari_value = models.TextField(null=True)

    class Meta:
        db_table = "gene_variable"


class RelaVariable(models.Model):
    id = models.AutoField(primary_key=True)
    rela_name = models.CharField(max_length=500)
    rela_table = models.CharField(max_length=100)
    vari_id = models.IntegerField(null=True)
    rela_type = models.CharField(max_length=500, null=True)

    class Meta:
        db_table = "rela_variable"


class Statement(models.Model):
    id = models.AutoField(primary_key=True)
    content = models.TextField()
    type = models.CharField(max_length=100, null=True)
    varList = models.TextField(null=True)
    filepath = models.CharField(max_length=500, null=True)
    energy = models.FloatField(null=True)
    import_info = models.TextField(null=True)

    class Meta:
        db_table = "Statement"


class Variables(models.Model):
    id = models.AutoField(primary_key=True)
    vari_name = models.CharField(max_length=1000, null=True)
    vari_type = models.CharField(max_length=1000, null=True)
    vari_value = models.TextField(null=True)
    vari_expr = models.TextField(null=True)
    vari_ori_file = models.CharField(max_length=500, null=True)
    import_info = models.TextField(null=True)
    package_Info = models.TextField(null=True)
    vari_otherInfo = models.TextField(null=True)
    varList = models.TextField(null=True)
    energy = models.FloatField(null=True)

    class Meta:
        db_table = "variables"


class APIInfo(models.Model):
    id = models.AutoField(primary_key=True)
    class_name = models.CharField(max_length=120)
    constructor_stmt = models.TextField()
    class_url = models.TextField()

    class Meta:
        db_table = "API_Info"


class ConstructorInfo(models.Model):
    class_name = models.TextField()
    class_description = models.TextField()
    constructor_stmt = models.TextField()
    package_name = models.TextField()

    class Meta:
        db_table = "Constructor_Info"


class MethodInfo(models.Model):
    id = models.AutoField(primary_key=True)
    class_name = models.TextField()
    method_name = models.TextField()
    param_info = models.TextField()
    boundary_value = models.TextField()
    return_type = models.TextField()
    throws_desc = models.TextField(null=True)
    import_stmt = models.TextField(null=True)
    class_page = models.TextField()

    class Meta:
        db_table = "Method_Info"


class QuickMethodInfo(models.Model):
    id = models.AutoField(primary_key=True)
    class_name = models.TextField()
    method_name = models.TextField()
    param_info = models.TextField()
    boundary_value = models.TextField()
    return_type = models.TextField()
    throws_desc = models.TextField(null=True)
    import_stmt = models.TextField(null=True)
    class_page = models.TextField()

    class Meta:
        db_table = "Quick_Method_Info"


class TableBinaryExprFalse(models.Model):
    id = models.AutoField(primary_key=True)
    binary_expr_content = models.TextField()
    variable_type_binary_expr = models.TextField()

    class Meta:
        db_table = "Table_BinaryExpr_False"


class TableBinaryExprTrue(models.Model):
    id = models.AutoField(primary_key=True)
    binary_expr_content = models.TextField()
    variable_type_binary_expr = models.TextField()

    class Meta:
        db_table = "Table_BinaryExpr_True"
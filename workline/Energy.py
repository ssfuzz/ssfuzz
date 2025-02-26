import json
import os, sys
import pymysql
from pathlib import Path
from Counter import Counter
from entropy import Entropy

BASE_DIR = str(Path(__file__).resolve().parent.parent)
sys.path.append(BASE_DIR)

class Energy:

    def __init__(self):
        self.Mutation_method = 1
        self.val_dir = ""
        self.conn = dict(host='127.0.0.1',
                         port=10348,
                         user='root',
                         passwd='mysql123',
                         db='test',
                         charset='utf8mb4')

    def get_file_info(self, lines):
        file_info = {}
        for i in range(0, len(lines), 2):
            line = lines[i].strip()
            if line.startswith('FilePath:'):
                file_path = str(lines[i + 1].strip())
                file_info[file_path] = {'Lines': [], 'Branches': []}
            elif line.startswith('Lines:'):
                lines_str = json.loads(lines[i + 1].strip())
                file_info[file_path]['Lines'] = lines_str
            elif line.startswith('Branches:'):
                branches_str = lines[i + 1].strip()
                if branches_str != '[]':
                    branches = branches_str[1:-1].split('),')
                    for b in branches:
                        branch_info = b.strip()[1:].split(',')
                        branch_line = int(branch_info[0])
                        if branch_info[1].endswith(')'):
                            branch_info[1] = branch_info[1][:-1]
                        branch_num = int(branch_info[1])
                        file_info[file_path]['Branches'].append((branch_line, branch_num))
        return file_info

    def get_frequency(self, analysef):
        with open(analysef, 'r') as f1:
            analyse1 = f1.readlines()
        analyse1_files = self.get_file_info(analyse1)
        c = Counter()
        c.count(analyse1_files)
        str1 = c.get_counts()
        c2 = Counter()
        c2.count_from_string(str(str1))
        return c2.get_counts()

    def get_energy(self, count_res):
        obj = Entropy()
        p = obj.max_likelihood_estimate3_2(count_res)
        energy = obj.entropy_MLE(p)
        return energy

    def read_mutator(self, filepath):
        filename = filepath.split("/")[-1].split(".")[0]
        data = {"Variables": [], "Statement": []}
        with open(filepath, "r") as f:
            for line in f:
                if "Variables" in line or "Statement" in line:
                    parts = line.strip()[1:-1].split(", ")
                    for i in range(0, len(parts), 2):
                        if parts[i] == "Variables":
                            data["Variables"].append(int(parts[i + 1]))
                        elif parts[i] == "Statement":
                            data["Statement"].append(int(parts[i + 1]))
        return filename, data

    # 3.1.3
    def get_files(self):
        txt_files = []
        for root, dirs, files in os.walk(self.val_dir):
            for file in files:
                if file.endswith(".txt"):
                    txt_files.append(os.path.join(root, file))
        return txt_files

    def read_mutators_fromDir(self):
        txt_files = self.get_files()
        files_data = {str(self.Mutation_method): {}}
        for f in txt_files:
            filename, data = self.read_mutator(f)
            files_data[str(self.Mutation_method)][filename] = data
        return files_data

    def write_energy(self, all_files, files_data, energy):
        # print(all_files)
        # print(files_data)
        for f in files_data:
            if f in all_files.keys():
                for name, DB_ids in all_files[f].items():
                    if name == 'Variables':
                        print('Variables', DB_ids)
                        if len(DB_ids) > 0:
                            for id1 in DB_ids:
                                print(energy, 'variables', id1)
                                self.updateEnergy(energy, 'variables', id1)
                    if name == 'Statement':
                        print('Statement: ', DB_ids)
                        if len(DB_ids) > 0:
                            for id2 in DB_ids:
                                print(energy, 'Statement', id2)
                                self.updateEnergy(energy, 'Statement', id2)

    def updateEnergy(self, energy, table, id):
        sql = 'update ' + table + ' set energy= %s where id = %s'
        prames = (energy, id)
        return self.update(sql, prames)

    def update(self, sql, prames):
        conn = pymysql.connect(**self.conn)
        cur = conn.cursor()
        recount = cur.execute(sql, prames)
        conn.commit()
        cur.close()
        conn.close()
        return recount


if __name__ == '__main__':
    obj = Energy()
    test1f = os.path.join(BASE_DIR, "data/entropyData/analyse1.txt")
    prefix = os.path.join(BASE_DIR, "data/entropyData/HtmlFile3")
    c2 = obj.get_frequency(test1f, prefix)

    energy = obj.get_energy(c2)

    obj.val_dir = "/root/ssfuzz/feature/src/test/output/target_test"
    obj.Mutation_method = 1
    all_files = obj.read_mutators_fromDir()


    mutator_file = "/root/shannonfuzz-getfeature/feature/src/test/output/target_test/0/MyJVMTest_201363.txt"
    name,files_data = obj.read_mutator(mutator_file)
    files_data = {name:files_data}

    obj.write_energy(all_files["1"],files_data , energy)

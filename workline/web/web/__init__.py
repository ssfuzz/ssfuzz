import pymysql
import os, json, sys
pymysql.install_as_MySQLdb()
import sys
sys.path.append("../../")
from workline.web import BASE_ROOT

def load_config(environment,base_root=BASE_ROOT):
    file_path = os.path.normpath(os.path.join(base_root, 'config.json'))
    with open(file_path, 'r') as file:
        config = json.load(file)
    return config[environment]

if __name__ == '__main__':
    db_config = load_config('test')  # 或 'test'
    # print(f"Host: {db_config['host']}")
    # print(f"Port: {db_config['port']}")
    # print(f"User: {db_config['user']}")
    # print(f"Password: {db_config['password']}")
    # print(f"Database: {db_config['database']}")
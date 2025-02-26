import threading
import pymysql
import json

# Function to load SQL configuration
def load_sql_config(env="test", config_file="config.json"):
    """
    Load SQL configuration for the specified environment from the configuration file and return a formatted dictionary.

    :param env: Environment name (e.g., "development" or "test")
    :param config_file: Path to the configuration file
    :return: SQL configuration dictionary
    """
    try:
        # Open and load the configuration file
        with open(config_file, "r", encoding="utf-8") as file:
            config_data = json.load(file)

        # Check if the specified environment exists
        if env not in config_data:
            raise KeyError(f"Environment '{env}' does not exist in the configuration file.")

        # Get the configuration for the specified environment
        env_config = config_data[env]

        # Construct the sql_config dictionary
        sql_config = {
            "host": env_config.get("host", "127.0.0.1"),
            "port": int(env_config.get("port", 3306)),  # Convert to integer
            "user": env_config.get("user", "root"),
            "passwd": env_config.get("password", "root"),  # Map `password` to `passwd`
            "db": env_config.get("database", "test"),
            "charset": "utf8mb4"  # Default character set
        }
        return sql_config
    except FileNotFoundError:
        print(f"Configuration file '{config_file}' not found.")
        return None
    except json.JSONDecodeError:
        print(f"Configuration file '{config_file}' is malformed. Please check the JSON format.")
        return None
    except KeyError as e:
        print(e)
        return None

# Specify environment and configuration file path
environment = "test"  # Can be changed to "test" or other environments
config_path = "/root/ssfuzz/config.json"  # Path to the configuration file

# Load SQL configuration
sql_config = load_sql_config(env=environment, config_file=config_path)


class DataBaseHandle(object):

    def __init__(self):
        self.conn = sql_config
        self.mutex = threading.Lock()

    def createTable(self, sql):
        conn = pymysql.connect(**self.conn)
        cur = conn.cursor()
        cur.execute(sql)
        cur.close()
        conn.close()

    def selectOne(self, sql, params):
        conn = pymysql.connect(**self.conn)
        cur = conn.cursor()
        self.mutex.acquire()
        cur.execute(sql, params)
        self.mutex.release()
        data = cur.fetchone()
        cur.close()
        conn.close()
        return data

    def selectall(self, sql):
        conn = pymysql.connect(**self.conn)
        cur = conn.cursor()
        self.mutex.acquire()
        cur.execute(sql)
        self.mutex.release()
        data = cur.fetchall()
        cur.close()
        conn.close()
        return data

    def selectmany(self, sql, params):
        conn = pymysql.connect(**self.conn)
        cur = conn.cursor()
        self.mutex.acquire()
        cur.execute(sql, params)
        self.mutex.release()
        data = cur.fetchall()
        cur.close()
        conn.close()
        return data

    def insert(self, sql, params):
        conn = pymysql.connect(**self.conn)
        cur = conn.cursor()
        recount = cur.execute(sql, params)
        conn.commit()
        cur.close()
        conn.close()
        return recount

    def insertMany(self, sql, lis):
        conn = pymysql.connect(**self.conn)
        cur = conn.cursor()
        recount = cur.executemany(sql, lis)
        conn.commit()
        cur.close()
        conn.close()
        return recount

    def delete(self, sql, params):
        conn = pymysql.connect(**self.conn)
        cur = conn.cursor()
        recount = cur.execute(sql, params)
        conn.commit()
        cur.close()
        conn.close()
        return recount

    def deleteAll(self, sql):
        conn = pymysql.connect(**self.conn)
        cur = conn.cursor()
        recount = cur.execute(sql)
        conn.commit()
        cur.close()
        conn.close()
        return recount

    def update(self, sql, params):
        conn = pymysql.connect(**self.conn)
        cur = conn.cursor()
        recount = cur.execute(sql, params)
        conn.commit()
        cur.close()
        conn.close()
        return recount
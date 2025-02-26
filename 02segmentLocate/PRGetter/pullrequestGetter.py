import random
import requests
import sqlite3
import json, time, tqdm
import urllib3

urllib3.disable_warnings()


class PullsSaver:
    def __init__(self):
        self.dbName = "db/pullsData.db"
        # Create connection
        self.conn = sqlite3.connect(self.dbName)
        # Create cursor
        self.cursor = self.conn.cursor()

        self.table_name = 'my_table'

    def test(self):
        conn = sqlite3.connect(self.dbName)  # Connect to a database named test.db; create it if it doesn't exist
        print('Database opened successfully')

    def createTable(self, table_name):
        try:
            self.cursor.execute(f"SELECT url FROM {table_name}")
        except:
            pass

        result = self.cursor.fetchone()
        create_table_query = f'''
        CREATE TABLE IF NOT EXISTS {table_name} (
            url TEXT, 
            id TEXT PRIMARY KEY, 
            node_id TEXT, 
            html_url TEXT,
            diff_url TEXT,
            patch_url TEXT, issue_url TEXT,
            number TEXT,
            state TEXT,
            locked TEXT,
            title TEXT,
            user TEXT,
            body TEXT,
            created_at TEXT,
            updated_at TEXT,
            closed_at TEXT,
            merged_at TEXT,
            merge_commit_sha TEXT,
            assignee TEXT,
            assignees TEXT,
            requested_reviewers TEXT,
            requested_teams TEXT,
            labels TEXT,
            milestone TEXT,
            draft TEXT,
            commits_url TEXT,
            review_comments_url TEXT,
            review_comment_url TEXT,
            comments_url TEXT,
            statuses_url TEXT,
            head TEXT,
            base TEXT,
            _links TEXT,
            author_association TEXT,
            auto_merge TEXT,
            active_lock_reason TEXT
        )
        '''
        if result is None:
            self.cursor.execute(create_table_query)
            print(f"Table '{table_name}' created successfully.")
        else:
            print(f"Table '{table_name}' already exists.")

    def insertTable(self, table_name, dicts):
        columns = ','.join(list(dicts[0].keys()))
        values = ','.join(['?' for _ in dicts[0].values()])
        dicts = [{key: str(value) for key, value in data.items()} for data in dicts]
        dicts = [tuple(x.values()) for x in dicts]
        self.cursor.executemany(f"INSERT INTO {table_name} ({columns}) VALUES ({values})", dicts)
        self.conn.commit()

    def searchNumber(self, table_name):
        # SQL query to select the 'number' column
        sql_query = f"SELECT number FROM {table_name}"
        # Execute the query
        self.cursor.execute(sql_query)
        # Fetch the results
        results = self.cursor.fetchall()
        return results


class PullsGetter:
    def __init__(self):
        # Replace with your GitHub token and repository information
        self.token = "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"
        self.repository = "openjdk/jdk"

        # Build the API request URL
        self.url = f"https://api.github.com/repos/{self.repository}/pulls"
        self.params = {
            "state": "all",
            "per_page": 100,  # Number of results per page
        }
        self.headers = {
            "Authorization": f"Bearer {self.token}",
        }

    def pullsgetter(self, start_page=1, pages=1):
        page = start_page
        pull_requests = []
        # Make API requests in a loop to fetch all pull requests
        flag = 0
        while True:
            self.params["page"] = page
            response = requests.get(self.url, params=self.params, headers=self.headers)
            page_requests = response.json()
            if not page_requests:
                break  # No more pull requests
            pull_requests.extend(page_requests)
            page += 1
            flag += 1
            if flag >= pages:
                break
        return pull_requests


def process_diff_strings(string_with_signs, string_without_signs):
    lines_with_signs = string_with_signs.split("\n")  # Split the string with signs by lines
    filtered_lines = []  # Save processed lines

    for line_with_sign in lines_with_signs:
        if line_with_sign.startswith('-'):
            string_without_signs.remove(line_with_sign[1:].strip())
        elif line_with_sign.startswith('+'):
            filtered_lines.append(line_with_sign[1:])
        else:
            filtered_lines.append(line_with_sign)

    return '\n'.join(filtered_lines), string_without_signs


def load_completed_ids():
    try:
        with open("completed_ids.txt", "r") as file:
            completed_ids = set(file.read().splitlines())
    except FileNotFoundError:
        completed_ids = set()
    return completed_ids


def save_completed_id(completed_id):
    with open("completed_ids.txt", "a") as file:
        file.write(str(completed_id) + "\n")


user_agent_list = [
    "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36",
    "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36",
    "Mozilla/5.0 (Windows NT 10.0; …) Gecko/20100101 Firefox/61.0",
    "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36",
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.62 Safari/537.36",
    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36",
    "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0)",
    "Mozilla/5.0 (Macintosh; U; PPC Mac OS X 10.5; en-US; rv:1.9.2.15) Gecko/20110303 Firefox/3.6.15",
]

if __name__ == '__main__':
    table_name = "my_table_pages100"
    # Fetch pull requests
    pullsGetter = PullsGetter()
    # pull_requests = pullsGetter.pullsgetter(start_page=1, pages=100)

    # Save pull request information
    pullsSaver = PullsSaver()
    # pullsSaver.test()
    # pullsSaver.createTable(table_name)
    # pullsSaver.insertTable(table_name, pull_requests)

    # Query information from the database
    pull_requests = pullsSaver.searchNumber(table_name)
    requests.adapters.DEFAULT_RETRIES = 5
    proxies = {
        "http": "http://10.15.22.20:7890",
        "https": "http://10.15.22.20:7890"
    }
    # Get the code content of the number ID
    # Load completed IDs
    completed_ids = load_completed_ids()
    # Remove duplicate IDs
    unique_ids_list = list(set([x[0] for x in pull_requests]) - completed_ids)

    # Iterate through each pull request
    for pull_number in unique_ids_list:
        files_url = f"https://api.github.com/repos/{pullsGetter.repository}/pulls/{pull_number}/files"
        headers = {"Authorization": f"Bearer {pullsGetter.token}",
                   'referer': files_url.encode("utf-8").decode("latin1"), }
        headers['User-Agent'] = random.choice(user_agent_list)
        headers['Connection'] = 'close'
        print("files_url: ", files_url)
        try:
            files_response = requests.get(files_url, headers=headers, verify=False, proxies=proxies)
            files_api = files_response.json()
            # Iterate through each file
            for file_index in range(len(files_api)):
                print("files_api: ", files_api[file_index])
                time.sleep(5)
                raw_url = files_api[file_index]["raw_url"]
                try:
                    patch = files_api[file_index]["patch"]
                except KeyError:
                    patch = ""
                file_name = files_api[file_index]["filename"]
                new_content = requests.get(raw_url, headers=headers, verify=False, proxies=proxies)
                print("raw_url: ", raw_url)
                print("new_content: ", new_content.text)
                print("patch: ", patch)
                # Open the file; create it if it doesn't exist
                try:
                    savefile = open(
                        "data/getFeature/{}-{}-{}".format(pull_number, files_api[file_index]["filename"].split("/")[-2],
                                                          files_api[file_index]["filename"].split("/")[-1], ), "w")
                    # Write the string to the file
                    savefile.write(new_content.text)
                    savefile_patch = open(
                        "data/detectFeature/patch-{}-{}-{}".format(pull_number,
                                                                   files_api[file_index]["filename"].split("/")[-2],
                                                                   files_api[file_index]["filename"].split("/")[-1], ),
                        "w")

                    savefile_patch.write(patch)
                except Exception as e:
                    print(e)
                print("Saved id=", files_api[file_index]["filename"])
                # Close the file
                savefile.close()
                savefile_patch.close()
            save_completed_id(pull_number)
        except requests.exceptions.RequestException as e:
            print(f"An error occurred: {e}")
            pass
        except UnicodeEncodeError as e:
            print(f"UnicodeEncodeError: {e}")
            pass  # Continue execution after handling the exception
        # break
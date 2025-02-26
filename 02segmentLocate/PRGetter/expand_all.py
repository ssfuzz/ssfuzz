import re

from selenium import webdriver
from selenium.common.exceptions import TimeoutException
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait


class SeleniumCrawler(object):
    def __init__(self):
        self.driver = webdriver.Chrome()  # Use Chrome browser; you can also choose other browsers
        self.wait = WebDriverWait(self.driver, timeout=10)

    def crawl(self, url):
        # Initialize WebDriver
        self.driver.get(url)  # Replace with your GitHub repository URL

        # Locate elements using XPath
        expand_buttons = self.driver.find_elements(By.XPATH, "//button[contains(@aria-label, 'Expand all')]")
        for button in expand_buttons:
            button.click()

        try:
            # Wait for all files to expand
            self.wait.until(EC.visibility_of_all_elements_located((By.CLASS_NAME, "blob-expanded")))
        except TimeoutException as t:
            print(t)

        # Get file content
        files = self.driver.find_elements(By.XPATH,
                                          "//div[contains(@class, 'file') and contains(@class, 'js-file') and contains(@class, 'js-details-container')]")
        files = [file.text for file in files]

        # Close the browser
        self.driver.quit()
        return files

    def clean_text(self, string):
        def remove_diff_lines(string):
            pattern = r'^@@.*@@$'
            return re.sub(pattern, '', string, flags=re.MULTILINE)

        def remove_empty_lines(string):
            lines = string.split("\n")
            filtered_lines = [line for line in lines if line.strip()]
            result = '\n'.join(filtered_lines)
            return result

        texts = ["Expand Down", "Expand Up"]
        lines = string.split("\n")
        filtered_lines = []
        for line in lines:
            if all(text not in line for text in texts) or line.strip() == '':
                filtered_lines.append(line)

        # result = '\n'.join(filtered_lines)
        # print("result_filtered:  ", result)
        pattern = r"([\w/.]+)$"
        filename = filtered_lines[2]
        result = "\n".join(filtered_lines[3:])
        result = remove_diff_lines(result)
        result = remove_empty_lines(result)
        return filename, result


if __name__ == '__main__':
    crawler = SeleniumCrawler()
    files = crawler.crawl('https://github.com/openjdk/jdk/pull/17479/files?diff=unified&w=0')
    for file in files:
        filename, clean_file = crawler.clean_text(file)
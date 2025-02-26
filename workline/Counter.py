class Counter:
    def __init__(self):
        self.counts = {}
        self.total_Branches = 198397
        self.total_Lines = 361494
    def count(self, data):
        for filepath, content in data.items():
            lines = content["Lines"]
            branches = content["Branches"]

            for line in lines:
                self._increment_count(filepath, "Lines", line)

            for branch in branches:
                self._increment_count(filepath, "Branches", branch)

    def _increment_count(self, filepath, key, value):
        if filepath not in self.counts:
            self.counts[filepath] = {}

        if key not in self.counts[filepath]:
            self.counts[filepath][key] = {}

        if value not in self.counts[filepath][key]:
            self.counts[filepath][key][value] = 0

        self.counts[filepath][key][value] += 1

    def count_from_string(self, counts_str):
        counts = eval(counts_str)
        for filepath, content in counts.items():
            if filepath not in self.counts:
                self.counts[filepath] = {}

            for key, value_counts in content.items():
                if key not in self.counts[filepath]:
                    self.counts[filepath][key] = {}

                for value, count in value_counts.items():
                    if value not in self.counts[filepath][key]:
                        self.counts[filepath][key][value] = 0

                    self.counts[filepath][key][value] += count

    def get_counts(self):
        return self.counts

    def total_Coverage(self,counts_str):
        sumLines = 0
        sumBranches = 0
        self.count_from_string(counts_str)
        for k, v in self.counts.items():
            for k2, v2 in v.items():
                if k2 == 'Branches':
                    sumBranches = sumBranches + len(v2)
                elif k2 == 'Lines':
                    sumLines = sumLines + len(v2)
        return float(sumLines/self.total_Lines),float(sumBranches/self.total_Branches)
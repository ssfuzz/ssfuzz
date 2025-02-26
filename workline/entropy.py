import math, sys

import numpy as np
from matplotlib import pyplot as plt
from scipy.stats import multinomial


class Entropy:

    def entropy_MLE(self, p):
        H = 0
        for i in range(len(p)):
            if p[i] > 0:
                H += -p[i] * math.log2(p[i])
        return H

  
    def max_likelihood_estimate3(self, mapF):
        file_type = len(mapF)
        p = [0] * file_type
        Y = [0] * file_type
        index = 0
        sumAll = 0
        for filepath, value in mapF.items():
            sum = 0
            if len(mapF[filepath]['Lines']) > 0:
                for i in mapF[filepath]['Lines'].values():
                    sum += i
            if len(mapF[filepath]['Branches']) > 0:
                for i in mapF[filepath]['Branches'].values():
                    sum += i
            Y[index] += sum
            sumAll += sum
            index += 1
        # print(Y)
        # p[i] = Y[i]/file_types_size
        for i in range(0, file_type):
            if Y[i] != 0:
                p[i] = Y[i] / float(sumAll)
        return p

    def max_likelihood_estimate3_2(self, mapF):
        sumAll = 0
        setLines = {}
        for filepath in mapF.keys():
            if mapF[filepath].keys().__contains__('Lines') and len(mapF[filepath]['Lines']) > 0:
                for k, v in mapF[filepath]['Lines'].items():
                    if setLines.keys().__contains__(filepath + str(k)):
                        setLines[filepath + str(k)] += v
                    else:
                        setLines[filepath + str(k)] = v
                    sumAll += v
        for filepath in mapF.keys():
            if mapF[filepath].keys().__contains__('Branches') and len(mapF[filepath]['Branches']) > 0:
                for k, v in mapF[filepath]['Branches'].items():
                    key = filepath + str(k[0])
                    if setLines.keys().__contains__(key):
                        setLines[key] += v
                    else:
                        setLines[key] = v
                    sumAll += v
        p = [0] * len(setLines)
        index = 0
        # print(sumAll)
        for k in setLines.keys():
            p[index] = setLines[k] / float(sumAll)
            index += 1
        return p

    # (15)
    def max_likelihood_estimate4(self, mapF, size):
        p = []

        return p

    def max_likelihood_estimate2(self, X):
        p = [0] * len(X)
        for i in range(0, len(X)):
            p[i] += 1
        print(p)
        for i in range(len(X)):
            p[i] /= float(len(X))
        return p

    def max_likelihood_estimate1(self, all_values, type):
        p = [0] * type
        for x in all_values:
            p[x] += 1
        for i in range(type):
            p[i] /= float(len(all_values))
        return p


if __name__ == '__main__':
    obj = Entropy()

    all_values3 = {"fp1": {'Lines': {240: 1, 242: 1, 250: 1},
                           'Branches': {(241, 2): 1, (242, 3): 2}
                           },
                   "fp2": {'Lines': {240: 1, 243: 1, 256: 1},
                           'Branches': {(242, 1): 1, (242, 0): 1, (240, 2): 1}
                           }
                   }
    all_values4 = {"fp1": {'Lines': {240: 1, 242: 1, 250: 1},
                           'Branches': {(241, 1): 1, (242, 3): 1}
                           },
                   "fp2": {'Lines': {240: 1, 243: 1, 256: 1},
                           'Branches': {(242, 1): 1, (242, 0): 1, (240, 2): 1}
                           },
                   "fp3": {'Lines': {240: 1, 243: 1, 256: 1},
                           'Branches': {(242, 1): 1, (242, 0): 1, (240, 2): 1}
                           }
                   }
    print("*" * 20, "3_1")
    p3 = obj.max_likelihood_estimate3(all_values3)
    print("p3:", p3)
    e3 = obj.entropy_MLE(p3)
    print("e3:", e3)

    p4 = obj.max_likelihood_estimate3(all_values4)
    print("p4:", p4)
    e4 = obj.entropy_MLE(p4)
    print("e4:", e4)

    print("*" * 20, "3_2")
    p3_2 = obj.max_likelihood_estimate3_2(all_values3)
    print("p3_2:", p3_2)
    e3_2 = obj.entropy_MLE(p3_2)
    print("e3_2", e3_2)

    p4_2 = obj.max_likelihood_estimate3_2(all_values4)
    print("p4_2:", p4_2)
    e4_2 = obj.entropy_MLE(p4_2)
    print("e4_2", e4_2)

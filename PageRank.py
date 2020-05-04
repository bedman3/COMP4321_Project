#!/usr/bin/env python
# coding: utf-8

import json
from scipy.sparse import csr_matrix, lil_matrix
import numpy as np

print("start page rank algorithm now")
pagerank_raw = json.load(open('pagerank_raw.json'))

max_doc = pagerank_raw['maxDoc']
pagerank_dict = pagerank_raw['pageRankDict']
pagerank_it = pagerank_raw['pageRankIteration']
d = 0.85

lil_mat = lil_matrix((max_doc, max_doc), dtype=np.double)
for key in pagerank_dict:
    value_list = np.array(pagerank_dict[key])
    sum_value = value_list.shape[0]

    if sum_value > 0:
        key_value = int(key)
        value_num = 1 / sum_value
        for value in value_list:
            lil_mat[key_value, value] = value_num

csr_mat = lil_mat.tocsr()


pr_vector = csr_matrix(np.ones((1, max_doc)))
damping_vec = csr_matrix(np.ones((1, max_doc)) * (1 - d))

for i in range(pagerank_it):
    pr_vector = pr_vector.multiply(d).dot(csr_mat) + (damping_vec)

json.dump({'result': list(pr_vector.toarray()[0])}, open('pagerank_results.json', 'w+'))
print('finished page rank computation')

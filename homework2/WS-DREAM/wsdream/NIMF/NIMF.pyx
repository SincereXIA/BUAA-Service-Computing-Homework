#########################################################
# Author: Jamie Zhu <jimzhu@GitHub>
# License: MIT
# Last updated: 2016/5/4
#########################################################

import time
import numpy as np
from numpy import linalg as LA
cimport numpy as np # import C-API


#########################################################
# Make declarations on functions from cpp file
#
cdef extern from "c_NIMF.h":
    void NIMF(double *removedData, int numUser, int numService, int dim, 
        double lmda, double alpha, int maxIter, double etaInit, int topK,
        double *Udata, double *Sdata, double *predData)
#########################################################


#########################################################
# Function to perform the prediction algorithm
# Wrap up the C++ implementation
#
def predict(removedMatrix, para):  
    cdef int numService = removedMatrix.shape[1] 
    cdef int numUser = removedMatrix.shape[0] 
    cdef int dim = para['dimension']
    cdef double lmda = para['lambda']
    cdef int maxIter = para['maxIter']
    cdef double etaInit = para['etaInit']
    cdef int topK = para['topK']
    cdef double alpha = para['alpha']

    # initialization
    cdef np.ndarray[double, ndim=2, mode='c'] U = np.random.rand(numUser, dim)        
    cdef np.ndarray[double, ndim=2, mode='c'] S = np.random.rand(numService, dim)
    cdef np.ndarray[double, ndim=2, mode='c'] predMatrix = \
        np.zeros((numUser, numService), dtype=np.float64)

    # Wrap up c_NIMF.cpp
    NIMF(<double *> (<np.ndarray[double, ndim=2, mode='c']> removedMatrix).data,
        numUser,
        numService,
        dim,
        lmda,
        alpha,
        maxIter,
        etaInit,
        topK,
        <double *> U.data,
        <double *> S.data,
        <double *> predMatrix.data
        )
   
    return predMatrix
#########################################################


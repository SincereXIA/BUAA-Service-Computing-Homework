########################################################
# NTF.pyx: a python wrapper
# Author: Yuwen Xiong, Jamie Zhu <jimzhu@GitHub>
# Created: 2014/7/9
# Last updated: 2016/4/27
########################################################

import time
import numpy as np
cimport numpy as np # import C-API
from libcpp cimport bool


#########################################################
# Make declarations on functions from cpp file
#
cdef extern from "c_NTF.h":
    void NTF(double *removedData, double *predData, int numUser, 
        int numService, int numTimeSlice, int dim, double lmda, 
        int maxIter, bool debugMode, double *Udata, double *Sdata, 
        double *Tdata)
#########################################################


#########################################################
# Function to perform the prediction algorithm
# Wrap up the C++ implementation
#
def predict(removedTensor, para):
    cdef int numService = removedTensor.shape[1] 
    cdef int numUser = removedTensor.shape[0]
    cdef int numTimeSlice = removedTensor.shape[2]
    cdef int dim = para['dimension']
    cdef double lmda = para['lambda']
    cdef int maxIter = para['maxIter']
    cdef bool debugMode = para['debugMode']

    # initialization
    cdef np.ndarray[double, ndim=2, mode='c'] U = np.random.rand(numUser, dim)        
    cdef np.ndarray[double, ndim=2, mode='c'] S = np.random.rand(numService, dim)
    cdef np.ndarray[double, ndim=2, mode='c'] T = np.random.rand(numTimeSlice, dim)
    cdef np.ndarray[double, ndim=3, mode='c'] predTensor =\
        np.zeros((numUser, numService, numTimeSlice))
    
	
    # wrap up c_NTF.cpp
    NTF(<double *> (<np.ndarray[double, ndim=3, mode='c']> removedTensor).data,
        <double *> predTensor.data,
        numUser,
        numService,
        numTimeSlice,
        dim,
        lmda,
        maxIter,
        debugMode,
        <double *> U.data,
        <double *> S.data,
        <double *> T.data
        )

    return predTensor
#########################################################  

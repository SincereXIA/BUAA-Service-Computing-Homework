########################################################
# evallib.py: common functions for evaluator.py
# Author: Jamie Zhu <jimzhu@GitHub>
# Created: 2015/8/17
# Last updated: 2015/8/30
########################################################

import numpy as np 
from numpy import linalg as LA
import os, sys, time
from commons.utils import logger
import pickle as pickle
import random


#======================================================#
# Function to compute the evaluation metrics
#======================================================#
def evaluate(testMatrix, recoveredMatrix, para):
    (testVecX, testVecY) = np.where(testMatrix > 0)
    testVec = testMatrix[testVecX, testVecY]
    estiVec = recoveredMatrix[testVecX, testVecY]
    evalResult = errMetric(testVec, estiVec, para['metrics'])
    return evalResult


#======================================================#
# Function to remove the entries of data matrix
# Return the trainMatrix and testMatrix
#======================================================#
def removeEntries(matrix, density, seedId):
    (vecX, vecY) = np.where(matrix > 0)
    vecXY = np.c_[vecX, vecY]
    numRecords = vecX.size
    numAll = matrix.size
    random.seed(seedId)
    randomSequence = list(range(0, numRecords))
    # print('removeEntries')
    random.shuffle(randomSequence) # one random sequence per round
    numTrain = int(numAll * density)
    # by default, we set the remaining QoS records as testing data                     
    numTest = numRecords - numTrain
    trainXY = vecXY[randomSequence[0 : numTrain], :]
    testXY = vecXY[randomSequence[- numTest :], :]
    trainMatrix = np.zeros(matrix.shape)
    trainMatrix[trainXY[:, 0], trainXY[:, 1]] = matrix[trainXY[:, 0], trainXY[:, 1]]
    testMatrix = np.zeros(matrix.shape)
    testMatrix[testXY[:, 0], testXY[:, 1]] = matrix[testXY[:, 0], testXY[:, 1]]

    # ignore invalid testing data
    idxX = (np.sum(trainMatrix, axis=1) == 0)
    testMatrix[idxX, :] = 0
    idxY = (np.sum(trainMatrix, axis=0) == 0)
    testMatrix[:, idxY] = 0    
    return trainMatrix, testMatrix


#======================================================#
# Function to compute the evaluation metrics
#======================================================#
def errMetric(realVec, estiVec, metrics):
    result = []
    absError = np.abs(estiVec - realVec) 
    mae = np.average(absError)
    for metric in metrics:
        if 'MAE' == metric:
            result = np.append(result, mae)
        if 'NMAE' == metric:
            nmae = mae / (np.sum(realVec) / absError.shape)
            result = np.append(result, nmae)
        if 'RMSE' == metric:
            rmse = LA.norm(absError) / np.sqrt(absError.shape)
            result = np.append(result, rmse)
        if 'MRE' == metric or 'NPRE' == metric:
            relativeError = absError / realVec
            if 'MRE' == metric:
                mre = np.percentile(relativeError, 50)
                result = np.append(result, mre)
            if 'NPRE' == metric:
                npre = np.percentile(relativeError, 90)
                result = np.append(result, npre)
    return result


#======================================================#
# Dump the raw result into tmp file
#======================================================#
def dumpresult(outFile, result):
    try:
        with open(outFile, 'wb') as fid:
                pickle.dump(result, fid)
    except Exception:
        logger.error('Dump file failed: ' + outFile)
        logger.error(e)
        sys.exit()


#======================================================#
# Process the raw result files 
#======================================================#
def summarizeResult(para):
    path = '%s%s_%s_result'%(para['outPath'], para['dataName'], para['dataType'])
    evalResults = np.zeros((len(para['density']), para['rounds'], len(para['metrics']))) 
    timeResults = np.zeros((len(para['density']), para['rounds']))   

    k = 0
    for den in para['density']:
        for rnd in range(para['rounds']):
            inputfile = path + '_%.2f_round%02d.tmp'%(den, rnd + 1)
            with open(inputfile, 'rb') as fid:
                data = pickle.load(fid)
            os.remove(inputfile)
            (evalResults[k, rnd, :], timeResults[k, rnd]) = data
        k += 1
    saveSummaryResult(path, evalResults, timeResults, para)  


#======================================================#
# Save the summary evaluation results into file
#======================================================#
def saveSummaryResult(outfile, result, timeinfo, para):
    fileID = open(outfile + '.txt', 'w')
    print ('Average result: [%s]'%outfile)
    print ('Metrics:', para['metrics'] )
    fileID.write('======== Results summary ========\n')
    fileID.write('Metrics:    ')
    for metric in para['metrics']:
        fileID.write('|   %s  '%metric)
    fileID.write('\n')
    fileID.write('[Average]\n')
    k = 0
    for den in para['density']:
        fileID.write('density=%.2f: '%den)
        avgResult = np.average(result[k, :, :], axis=0)
        np.savetxt(fileID, np.matrix(avgResult), fmt='%.4f', delimiter='  ')
        print ('density=%.2f: '%den, avgResult)
        k += 1
    fileID.write('\n[Standard deviation (std)]\n')
    k = 0
    for den in para['density']:
        fileID.write('density=%.2f: '%den)
        np.savetxt(fileID, np.matrix(np.std(result[k, :, :], axis=0)), fmt='%.4f', delimiter='  ')
        k += 1

    fileID.write('\n======== Detailed results ========\n')
    k = 0
    for den in para['density']:
        fileID.write('[density=%.2f, %2d rounds]\n'%(den, para['rounds']))
        np.savetxt(fileID, np.matrix(result[k, :, :]), fmt='%.4f', delimiter='  ')
        fileID.write('\n')
        k += 1
    fileID.close()

    if para['saveTimeInfo']:
        fileID = open(outfile + '_time.txt', 'w')
        fileID.write('Average running time (second):\n')
        k = 0
        for den in para['density']:
            fileID.write('density=%.2f: '%den)
            np.savetxt(fileID, np.matrix(np.average(timeinfo[k, :])), fmt='%.4f', delimiter='  ')  
            k += 1
        fileID.close()

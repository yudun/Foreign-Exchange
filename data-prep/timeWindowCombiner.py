#!/usr/bin/env python
# -*- coding: utf-8 -*-
# @FileName: timeWindowCombiner.py
# @Author: Name:Shimin Wang; andrewID:shiminw
# @Email: wyudun@gmail.com
# @Date:   2015-09-16 00:01:48
# @Last Modified time: 2015-09-27 23:41:53
#
# @Description: Combining aligned into a single file where
# each record shows the directiobality for different currency.
# The result format for the data matrix will be:
# TIME	AUDJPY	AUDNZD	AUDUSD	CADJPY	CHFJPY	EURCHF	EURGBP	EURJPY	GBPJPY	GBPUSD	NZDUSD	USDCAD	USDCHF	USDJPY	EURUSD
# where 1 indicates rising and 0 indicates dropping.
# The last column -- EURUSD -- is the label field.

import datetime
import os
from os import listdir
from os.path import isfile, join

# We don't need features listed in this list
REMOVE_FEATURE = []
# We choose the directionality of the bid of EURUSD as the LABEL
LABEL = "EURUSD-2015-01.csv"
# How many minutes in the future we want to predict for
PREDICT_INTO = 10

# Define input and out put directory
INPUT_DATA_DIR = "../../lab1/alignedData/"
OUTPUT_DATA_DIR = "../../lab2/"

# 1 minute
ONE_MINUTE = datetime.timedelta(minutes=1) 

####################### helper function #######################
## Peek the next line of file f without advancing the file iterator
def peekLine(f):
    pos = f.tell()
    line = f.readline()
    f.seek(pos)
    return line

## Advance the file iterator ti the line with destTime stamp
## and return the directionality
def fileLineAdvanceTo(inf, destTime):
	while True:
		line = inf.readline().split("\t")
		if datetime.datetime.strptime(line[0], "%Y%m%d %H:%M") == destTime:
			return line[1].strip('\n')

## close all files in the file handle list
def closeAllFile(fHandleList):
	for f in fHandleList:
		f.close()

## combine all files in the fileNameList in to a data matrix
def timeWindowCombiner(fileNameList, outfilename):
	fHandleList = [open(join(INPUT_DATA_DIR, f), "r") for f in fileNameList]
	outf = open(join(OUTPUT_DATA_DIR, outfilename), "w")

	# get the first line of the first file
	line = peekLine(fHandleList[0])
	if not line:
		closeAllFile(fHandleList)
		return

	# get the maximal time stamp among all the first timestamp in each files
	# use this as the start timestamp in our result file
	maxTimeString = line.split("\t")[0]
	maxTime = datetime.datetime.strptime(maxTimeString, "%Y%m%d %H:%M")
	for i in range(1, len(fHandleList)):
		thisTimeString = peekLine(fHandleList[i]).split("\t")[0]
		thisTime = datetime.datetime.strptime(thisTimeString, "%Y%m%d %H:%M")
		if thisTime > maxTime:
			maxTime = thisTime

	# get all the directionality data for our first timeStamp
	firstLine = []
	for i in range(len(fHandleList)):
		f = fHandleList[i]
		if i == len(fHandleList)-1:
			firstLine.append(fileLineAdvanceTo(f, maxTime + PREDICT_INTO * ONE_MINUTE))
		else:
			firstLine.append(fileLineAdvanceTo(f, maxTime))

	# write the first line of our final maxtrix to the result file
	outf.write(maxTime.strftime("%Y%m%d %H:%M")+"\t" + "\t".join(firstLine) + "\n")

	# Process the rest lines for all files until whichever reaches its end
	noFileEnd = True
	while noFileEnd:
		directionalityList = []
		
		count = 0
		for f in fHandleList:
			s = f.readline()
			if not s:
				noFileEnd = False
				break		
			else:
				directionalityList.append(s.split("\t")[1].strip('\n'))

			if count == 0:
				featureTime = s
			count += 1
		
		if noFileEnd:
			outf.write(featureTime.split("\t")[0]+"\t" + "\t".join(directionalityList) + "\n")

	closeAllFile(fHandleList)
	outf.close()


####################### begin our generating for the data matrix #######################
filesList = [ f for f in listdir(INPUT_DATA_DIR)
	if f.endswith("csv") and isfile(join(INPUT_DATA_DIR,f)) ]

# move the LABEL to the end of fileList
filesList.remove(LABEL)
filesList.append(LABEL)

# remove unnecessary features
for filename in REMOVE_FEATURE:
	filesList.remove(filename)

timeWindowCombiner(filesList, "result.txt")

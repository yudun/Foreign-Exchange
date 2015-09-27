#!/usr/bin/env python
# -*- coding: utf-8 -*-
# @FileName: smallSampleGenerator.py
# @Author: Name:Shimin Wang; andrewID:shiminw
# @Email: wyudun@gmail.com
# @Date:   2015-09-16 00:00:06
# @Last Modified time: 2015-09-27 11:24:31
#
# @Description: pick at most MAX_LINE of data from 
# a file and save it to a specific dir.
# Format for the financial data is:
#		 NAME,TIME,BID,ASK

import os
from os import listdir
from os.path import isfile, join

# MAX number of lines should be reserved in result files
MAX_LINE = 100

# Define input and out put directory
INPUT_DATA_DIR = "../../lab1/sampleData/"
OUTPUT_DATA_DIR = "../../lab1/smallSampleData/"

filesList = [ f for f in listdir(INPUT_DATA_DIR) 
	if f.endswith("csv") and isfile(join(INPUT_DATA_DIR,f)) ]

if not os.path.exists(os.path.dirname(OUTPUT_DATA_DIR)):
    os.makedirs(os.path.dirname(OUTPUT_DATA_DIR))

# Begin shortenning each original data file 
for fname in filesList:
	f = open(join(INPUT_DATA_DIR,fname), "r")
	outf = open(join(OUTPUT_DATA_DIR, fname), "w")

	for i in range(MAX_LINE):
		s = f.readline()
		outf.write(s)

	f.close()
	outf.close()

print filesList, "\n\n These files has been shortened," \
"please check the result in "+OUTPUT_DATA_DIR


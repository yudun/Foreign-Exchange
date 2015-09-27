#!/usr/bin/env python
# -*- coding: utf-8 -*-
# @FileName: timeWindowGenerator.py
# @Author: Name:Shimin Wang; andrewID:shiminw
# @Email: wyudun@gmail.com
# @Date:   2015-09-16 01:32:54
# @Last Modified time: 2015-09-27 11:24:36
#
# @Description: pre-process the data so that 
# it align within a TIME_WINDOW_SIZE minutes window
# we choose the "closed" price within that minute to 
# calculate the directionality of bid.

import datetime
import os
from os import listdir
from os.path import isfile, join

# size of the time window (in minutes)
TIME_WINDOW_SIZE = 1
TIME_STEP = datetime.timedelta(minutes=TIME_WINDOW_SIZE) 

# Define input and out put directory
INPUT_DATA_DIR = "../../lab1/sampleData/"
OUTPUT_DATA_DIR = "../../lab1/alignedData/"


def timeWindowGenerator(filename):
	if not os.path.exists(os.path.dirname(OUTPUT_DATA_DIR)):
   		os.makedirs(os.path.dirname(OUTPUT_DATA_DIR))

   	inf = open(join(INPUT_DATA_DIR, filename), "r")
   	outf = open(join(OUTPUT_DATA_DIR, filename), "w")

	
	# Read the first line
   	lastString = inf.readline()
   	if not lastString:
   		return
	sArray = lastString.split(",")
   	# We will keep updating these 2 var during the processing
   	lastBid = float(sArray[2])
   	lastTime = datetime.datetime.strptime(sArray[1], "%Y%m%d %H:%M:%S.%f") \
   					.replace(microsecond=0).replace(second=0)

   	while True:
   		# Read next line from input file and extract the bid and time
   		thisString = inf.readline()
   		if not thisString:
   			break
   		sArray = thisString.split(",")
   		thisBid = float(sArray[2])
   		thisTime = datetime.datetime.strptime(sArray[1], "%Y%m%d %H:%M:%S.%f") \
   					.replace(microsecond=0).replace(second=0)

   		# check current 
   		if thisTime == lastTime:
   			pass
   		else:
   			diff = (thisTime-lastTime).seconds/60
			# calculate the direction for bid: 1 for rising, 0 for dropping
   			direction = (thisBid >= lastBid and 1 or 0)
   			for i in range(0, diff, TIME_WINDOW_SIZE):
   				outf.write(lastTime.strftime("%Y%m%d %H:%M")+"\t"+str(direction)+"\n")
   				lastTime += TIME_STEP

   		# update the lastBid and lastTime
   		lastBid = thisBid
   		lastTime = thisTime

   	# Don't forget to write the last minute into the result
   	outf.write(thisTime.strftime("%Y%m%d %H:%M")+"\t"+str(direction)+"\n")

   	inf.close()
   	outf.close()


# begin processing our unaligned data files
filesList = [ f for f in listdir(INPUT_DATA_DIR) 
	if f.endswith("csv") and isfile(join(INPUT_DATA_DIR,f)) ]

for fname in filesList:
   timeWindowGenerator(fname)
   print fname, " processed done."
   



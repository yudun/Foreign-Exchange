There are 3 files in total for processing the original Financial to a data matrix.

They should be executed one by one in this order to get the correct result:
1. smallSampleGenerator.py
2. timeWindowGenerator.py
3. timeWindowCombiner.py


The first program generates small sample data, which is a small subset
of the original data, to help me test my other 2 files.

The second one help align each input raw data file to a time window and calculate the
directionality of bid price for each currency. I also make some interpolation here
whenever a necessary timestamp is missed

The third file combine all the aligned data together to generate a data matrix,
where we use the directionality of all currency except for EURUSD as features (14 features in total), 
and we use EURUSD’s directionality 10 minutes later as the label.

Format:
TIME	AUDJPY	AUDNZD	AUDUSD	CADJPY	CHFJPY	EURCHF	EURGBP	EURJPY	GBPJPY	GBPUSD	NZDUSD	USDCAD	USDCHF	USDJPY	EURUSD(label)

Sample of the result matrix looks like following:

20150101 21:43	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0
20150101 21:44	0	0	0	1	1	0	1	0	1	1	1	0	0	1	1
20150101 21:45	0	0	0	0	0	0	1	1	1	1	1	0	0	1	1
20150101 21:46	0	0	1	0	1	1	1	0	1	1	1	0	1	1	0
20150101 21:47	0	0	1	0	0	1	1	0	1	1	1	0	1	1	0
20150101 21:48	0	1	1	0	0	1	1	0	1	1	1	0	1	1	0
20150101 21:49	0	1	1	0	0	1	1	0	1	1	1	0	1	1	0
20150101 21:50	0	1	0	0	0	1	1	0	1	1	1	0	1	1	0
.
.
.
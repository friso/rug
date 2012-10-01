import numpy as np
import urllib
import re

movies = map(lambda x: x.strip(), file('movies.txt').readlines())

year = 2012
month = 9
day = 1
hour = 8
minute = 0
second = 0
milli = 0

monthLengths = [-1, 31, 29 if (year % 4 == 0) else 28, 31, 30, 31, 30, 31, 30, 31, 30, 31, 30]

pattern = re.compile(r'(.*?)[,(:].*')

for month in range(7,10):
	for day in range(1, monthLengths[month] + 1):
		for hour in range(6, 15):
			for minute in range(0, 60):
				for second in range(0, 60):
					term = np.random.zipf(1.15)
					milli = 0.5
					if term < len(movies):
						search = pattern.match(movies[term]).groups()[0]
						print '127.0.0.1\t%4d-%02d-%02dT%02d:%02d:%02d.%03dZ\tGET /search?q=%s HTTP/1.1\t200\t%s' % (
						year,
						month,
						day,
						hour,
						minute,
						second,
						int(milli * 1000), urllib.quote_plus(search),
						search)

#!/bin/bash
time while read line; do curl -s -XPOST http://localhost:9200/test/movie/ -d '{ "title":"'"$line"'" }' > /dev/null; done < movies.txt

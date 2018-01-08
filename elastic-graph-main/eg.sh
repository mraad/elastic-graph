#!/usr/bin/env bash
java\
 -Djava.awt.headless=true\
 -jar target/elastic-graph-main-0.3-jar-with-dependencies.jar\
 --el 0.00015\
 --mu 0.00015\
 --robustDist 0.2\
 --maxNodes 30\
 --inputPath data/tree23.data\
 $*

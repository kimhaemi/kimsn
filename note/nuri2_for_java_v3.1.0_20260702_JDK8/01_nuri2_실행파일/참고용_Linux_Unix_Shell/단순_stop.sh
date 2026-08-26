#!/bin/bash
#####################
# Nuri2 ÆÄÀÏ¸í
NURI2_NAMES=nuri2.jar
kill $(ps -ef | grep $NURI2_NAMES | grep -v grep | awk '{print $2}')
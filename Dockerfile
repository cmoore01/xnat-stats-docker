FROM ubuntu:14.04
MAINTAINER Charlie Moore <moore.c@wustl.edu>

RUN apt-get install -qy texlive-full
RUN apt-get install -qy gnuplot
RUN apt-get install -qy python3-requests

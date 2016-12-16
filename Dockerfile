FROM openjdk:8
MAINTAINER Charlie Moore <moore.c@wustl.edu>
ENV GROOVY_VERSION 2.4.7
ENV GROOVY_HOME /usr/local/groovy
ENV PATH $PATH:$GROOVY_HOME/bin

RUN apt-get update

RUN apt-get install -qy texlive-full
RUN apt-get install -qy gnuplot

RUN wget -O groovy.zip "https://dl.bintray.com/groovy/maven/apache-groovy-binary-${GROOVY_VERSION}.zip" && \
    unzip groovy.zip && \
    rm groovy.zip && \
    mv "groovy-${GROOVY_VERSION}" $GROOVY_HOME

COPY *.groovy /tmp/

RUN cd /tmp && groovy GraphStats.groovy -u dummy -p dummy -s dummy -j dummy -d
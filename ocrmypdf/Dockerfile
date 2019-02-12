# Use an official Python runtime as a parent image
FROM alpine:3.9

# Set the working directory 
WORKDIR /usr/local/bin
COPY docker-build.sh .
ARG	POPPLER_VERSION=0.73.0
ARG	POPPLER_DATA_VERSION=0.4.9
COPY docker-build.sh .
COPY main.py requirements.txt /app/

RUN	docker-build.sh ${POPPLER_VERSION} ${POPPLER_DATA_VERSION}

# Set the working directory 
WORKDIR /app

# Install any needed packages specified in requirements.txt
#RUN apk add --update python3 && pip3 install --trusted-host pypi.python.org -r requirements.txt

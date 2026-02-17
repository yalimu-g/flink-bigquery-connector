#!/bin/bash

# Original content
export HADOOP_CLASSPATH=$(hadoop classpath)

# Install Flink 2.1.0
FLINK_VERSION="2.1.0"
SCALA_VERSION="2.12"
FLINK_ARCHIVE="flink-${FLINK_VERSION}-bin-scala_${SCALA_VERSION}.tgz"

# Download only if not already present
if [ ! -f "${FLINK_ARCHIVE}" ]; then
  wget "https://archive.apache.org/dist/flink/flink-${FLINK_VERSION}/${FLINK_ARCHIVE}"
fi

# Extract only if directory doesn't exist
if [ ! -d "flink-${FLINK_VERSION}" ]; then
  tar -xzf "${FLINK_ARCHIVE}"
fi

export FLINK_HOME="/$(pwd)/flink-${FLINK_VERSION}"
export PATH="${FLINK_HOME}/bin:${PATH}"

echo "Flink ${FLINK_VERSION} installation complete."
flink --version

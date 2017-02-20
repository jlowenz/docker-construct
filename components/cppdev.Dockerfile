# update and get version control 
run DEBIAN_FRONTEND=noninteractive apt-get -y update && apt-get -y upgrade && \
    apt-get -y install \
    apt-utils \
    build-essential \
    cmake \
    git \
    hashalot \
    mercurial \
    pkg-config \
    python \
    python-dev \
    wget

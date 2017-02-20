# build boost 1.63
arg BOOST_VER=1.63.0
arg BOOST_UVER=1_63_0
arg BOOST_SOURCE=https://sourceforge.net/projects/boost/files/boost/${BOOST_VER}/boost_${BOOST_UVER}.tar.bz2
arg BOOST_FNAME=boost_1_63_0

workdir /tmp
add boost_hash ./
shell ["/bin/bash", "-c"]
run wget ${BOOST_SOURCE} && \
    sha256sum -c boost_hash && \
    tar xjf ${BOOST_FNAME}.tar.bz2 && \
    pushd ${BOOST_FNAME} && \
    ./bootstrap.sh --prefix=/usr/local && \
    ./b2 -j $(nproc) install && \
    popd && \
    rm -rf ${BOOST_FNAME} ${BOOST_FNAME}.tar.bz2

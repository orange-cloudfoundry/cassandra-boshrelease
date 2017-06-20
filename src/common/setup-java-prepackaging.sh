JAVA_VERSION=jdk1.8.0_102
JAVA_TAR_BALL=openjdk.tar.gz

cd ${BUILD_DIR}

tar zxfv ${BUILD_DIR}/openjdk/${JAVA_TAR_BALL}

export JAVA_HOME=${BUILD_DIR}/${JAVA_VERSION}
echo $JAVA_HOME



cleanup_java() {
  rm -rf ${JAVA_HOME}
  rm -rf ${BUILD_DIR}/openjdk
  rm -rf ${BUILD_DIR}/target
  rm -rf ${BUILD_DIR}/common
}

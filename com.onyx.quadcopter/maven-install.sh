#!/bin/bash
set -e
#########################
# Maven install script. #
#########################
# v 1.0 #
#########

#############
# Constants #
#############
BUILD_NUM="3.3.9"
ARCHIVE_NAME="apache-maven-${BUILD_NUM}-bin.zip"
DIR_NAME="apache-maven-${BUILD_NUM}"
INSTALL_DIR="/usr/local/apache-maven"
MVN_OPTS="-Xms256m -Xmx512m"
MVN_PROFILE="/etc/profile.d/mvn.sh"
MVN_DOWNLOAD="http://mirror.csclub.uwaterloo.ca/apache/maven/maven-${BUILD_NUM:0:1}/${BUILD_NUM}/binaries/${ARCHIVE_NAME}"
CWD=`/bin/pwd`
# Download maven to the current directory
download_mvn() {
    /usr/bin/wget $MVN_DOWNLOAD
}

# Unzip the archive and move to directory.
unzip() {
    echo "Unzipping archive..."
    /usr/bin/unzip $CWD/$ARCHIVE_NAME
}

# Create maven profile in /etc/profile.d/
make_profile() {
    sudo touch $MVN_PROFILE
    echo "#!/bin/bash" | sudo tee --append $MVN_PROFILE > /dev/null
    echo "Creating M2_HOME environment variable."
    echo "export M2_HOME=${INSTALL_DIR}/${DIR_NAME}" | sudo tee --append $MVN_PROFILE > /dev/null
    echo "Creating M2 environment variable."
    echo "export M2=\$M2_HOME/bin" | sudo tee --append $MVN_PROFILE > /dev/null
    echo "Creating MAVEN_OPTS environment variable."
    echo "export MAVEN_OPTS=\"${MVN_OPTS}\"" | sudo tee --append $MVN_PROFILE > /dev/null
    echo "Updating PATH environment variable."
    echo "export PATH=\$M2:\$PATH" | sudo tee --append $MVN_PROFILE > /dev/null
}

# Copy maven files to install dir
copy_files() {

    if [ ! -e $INSTALL_DIR/$DIR_NAME ]
      then
        echo "Creating directory ${INSTALL_DIR}/${DIR_NAME}."
        sudo /bin/mkdir -p $INSTALL_DIR/$DIR_NAME
    fi
    echo "Copying files from ${DIR_NAME} to ${INSTALL_DIR}."
    cd $DIR_NAME
    sudo /bin/cp -R $CWD/* $INSTALL_DIR/$DIR_NAME
}

# Update user environment
update_env() {
    source /etc/profile
}

clean() {
    rm $CWD/$ARCHIVE_NAME
    rm -rf $CWD/$DIR_NAME
}
# Application entry point.
main() {
    download_mvn
    unzip
    copy_files
    make_profile
    update_env
    clean
}

main

#!/bin/sh

sudo cp /opt/onyx/systemd/onyx-* /lib/systemd/system/
for i in /opt/onyx/systemd/onyx-*;
do
    FILENAME=`basename $i`
    sudo ln -s /lib/systemd/system/$FILENAME  /etc/systemd/system/$FILENAME
done
sudo chmod +x /opt/onyx/onyx-*

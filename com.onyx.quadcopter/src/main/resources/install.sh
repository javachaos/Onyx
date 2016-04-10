#!/bin/sh

sudo cp ./systemd/onyx-* /lib/systemd/system/
for i in ./systemd/onyx-*;
do
    FILENAME=`basename $i`
    sudo ln -s /lib/systemd/system/$FILENAME  /etc/systemd/system/$FILENAME
done

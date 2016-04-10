#!/bin/bash
CONFIGFILE="/etc/uv4l/uv4l-uvc.conf"
/usr/bin/uv4l --auto-video_nr --sched-rr --mem-lock --driver uvc  --device-id 045e:0761 --driver-config-file=$CONFIGFILE --server-option=--editable-config-file=$CONFIGFILE
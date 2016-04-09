#!/bin/bash

cvlc v4l2:///dev/video0 --sout '#transcode{vcodec=theo,vb=256}:standard{access=http,mux=ogg,dst=:1234}'

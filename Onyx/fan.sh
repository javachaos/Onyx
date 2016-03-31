#!/bin/sh

PIN=17
EXPT_DIR=`realpath /sys/class/gpio/export`
GPIO_DIR=`realpath /sys/class/gpio/gpio$PIN`
FAN_GPIO="$GPIO_DIR/value"
DIR_GPIO="$GPIO_DIR/direction"

CUTOFF_TEMP=37
START_TEMP=38
ON=1
OFF=0
FAN_LOGFILE=/opt/onyx/Onyx/fan.log

#512MB
MAX_LOG_SIZE=512000000
SLEEP_TIME=5

main() {

    if [ ! -e "$FAN_GPIO" ]
    then
        echo $PIN>$EXPT_DIR
        echo "out">$DIR_GPIO
    fi

    while [ "1" = "1" ]
    do
        temp=`/opt/vc/bin/vcgencmd measure_temp | cut -c6,7`
        echo "Temperature: $temp at: "$(date) | tee -a $FAN_LOGFILE
        if [ $temp -ge $START_TEMP ]
        then
            echo "Fan Started: "$(date) | tee -a $FAN_LOGFILE
            echo $ON>$FAN_GPIO
        elif [ $temp -le $CUTOFF_TEMP ]
        then
            echo "Fan Stopped: "$(date) | tee -a $FAN_LOGFILE
            echo $OFF>$FAN_GPIO
        fi
        sleep $SLEEP_TIME
        s=$( stat -c %s $FAN_LOGFILE)
        if [ $s -ge $MAX_LOG_SIZE ]
        then
            echo "Clearing log."
            rm $FAN_LOGFILE
            touch $FAN_LOGFILE
        fi
    done

}

main

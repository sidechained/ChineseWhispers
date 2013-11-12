#! /bin/sh
# /etc/init.d/rebDevBootScript.sh

### BEGIN INIT INFO
# Provides: rebDevBoot
# Required-Start: 
# Required-Stop: 
# Should-Start: 
# Should-Stop: 
# Default-Start: 2 3 4 5
# Default-Stop: 0 1 6
# Short-Description: Start and stop for rebellious devices project
# Description: RebDev
### END INIT INFO

# The following part always gets executed.
# log output to file:
exec > /tmp/rebDevBootScriptLog.txt 2>&1
echo "rebDev boot script log:"
echo $(date)

# The following part carries out specific functions depending on arguments.
case "$1" in
  start)
	echo "BASH: starting up..."
	echo "BASH: adding temp sensor device tree overlay..."
	chmod ugo+w /sys/devices/bone_capemgr.8/slots
	echo BB-W1:00A0 > /sys/devices/bone_capemgr.8/slots
	echo "BASH: killing jack"
	killall -9 jackd
	sleep 2
	echo "BASH: waiting for jack to start"
	jackd -R -d alsa -d hw:1,0 &
	sleep 4
	echo "BASH: running sclang script..."
	sclang /home/debian/RebDev/RDDevice-script.scd
	sleep 2
	sudo python /home/debian/RebDev/RDDevice-script.py
	echo "BASH: done, should never get here!"
    ;;
  stop)
	echo "stopping"
    ;;
  *)
	echo "Usage: /etc/init.d/bootJackAndSCLang.sh {start|stop}"
	exit 1
    ;;
esac

exit 0

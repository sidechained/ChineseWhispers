#! /bin/sh
# /etc/init.d/cwMegaphoneBoot.sh

### BEGIN INIT INFO
# Provides: cwMegaphoneBoot
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
exec > /tmp/cwMegaphoneBoot.txt 2>&1
echo "ChineseWhispers boot script log:"
echo $(date)

# The following part carries out specific functions depending on arguments.
case "$1" in
  start)
	echo "BASH: starting up..."
	echo "BASH: running sclang script..."
	sclang "/home/debian/ChineseWhispers/megaphoneStartup.scd" 0 # TODO: MEGAPHONE_NUM = $1
	sleep 2
	sudo python /home/debian/ChineseWhispers/megaphoneControl.py
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

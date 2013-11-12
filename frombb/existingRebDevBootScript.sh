#! /bin/sh
# /etc/init.d/rebDevBootScript.sh

### BEGIN INIT INFO
# Provides: rebDevBoot
# Required-Start: $all
# Required-Stop: $all
# Should-Start: 
# Should-Stop: 
# Default-Start: 2 3 4 5
# Default-Stop: 0 1 6
# Short-Description: Start and stop for rebellious devices project
# Description: RebDev
### END INIT INFO

# The following part always gets executed.
PATH=/sbin:/usr/sbin:/bin:/usr/bin:/usr/local/bin
# log output to file:
exec > /tmp/rebDevBootScriptLog.txt 2>&1
echo "rebDev boot script log:"
echo $(date)

# The following part carries out specific functions depending on arguments.
case "$1" in
  start)
        (python /home/debian/RebDev/RDDevice-script.py &)
        /bin/su - debian -c "sh /home/debian/RebDev/RDDevice-startup.sh"
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

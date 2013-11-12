#!/bin/bash
# megaphone software update bash script
# TODO: put script in separate file
# TODO: specify megaphone number, change IP and megaphone number in megaphoneStartup.scd to that number
# DONE: specify IP by argument
# TODO: copy startup script in init.d


USERNAME=debian
HOSTS=$@ # take hosts from command line (string separated by spaces)

for HOSTNAME in ${HOSTS} ; do
# 0. ssh into remote host and 
    ssh -l ${USERNAME} ${HOSTNAME} "
# 1. remove existing files/dirs;
rm -r /home/debian/ChineseWhispers/;
rm -r /home/debian/.local/share/SuperCollider/Extensions/ChineseWhispers;
rm -r /home/debian/.local/share/SuperCollider/Extensions/Utopia;
# 2. create new directories;
mkdir /home/debian/ChineseWhispers/;
mkdir /home/debian/ChineseWhispers/code;
mkdir /home/debian/.local/share/SuperCollider/Extensions/ChineseWhispers;
mkdir /home/debian/.local/share/SuperCollider/Extensions/Utopia
"
# 3. copy files using scp [scp source_file_name username@destination_host:destination_folder]
    scp /Users/grahambooth/Desktop/Megaphone/github/beagleboard/megaphone/megaphoneStartup.scd ${USERNAME}@${HOSTNAME}:/home/debian/ChineseWhispers/code;
    scp /Users/grahambooth/Desktop/Megaphone/github/beagleboard/megaphone/megaphoneControl.py ${USERNAME}@${HOSTNAME}:/home/debian/ChineseWhispers/code;
    scp /Users/grahambooth/Desktop/Megaphone/github/cwClasses/cwMegaphone.sc ${USERNAME}@${HOSTNAME}:/home/debian/.local/share/SuperCollider/Extensions/ChineseWhispers;
    scp /Users/grahambooth/Desktop/Utopia/github/Utopia/classes/NMLAddressing.sc ${USERNAME}@${HOSTNAME}:/home/debian/.local/share/SuperCollider/Extensions/Utopia;
    scp /Users/grahambooth/Desktop/Utopia/github/Utopia/classes/NMLRelays.sc ${USERNAME}@${HOSTNAME}:/home/debian/.local/share/SuperCollider/Extensions/Utopia;
    scp /Users/grahambooth/Desktop/Utopia/github/Utopia/classes/NMLUtopian.sc ${USERNAME}@${HOSTNAME}:/home/debian/.local/share/SuperCollider/Extensions/Utopia;
# scp /Users/grahambooth/Desktop/Megaphone/github/beagleboard/cwMegaphoneBoot.sh debian@192.168.2.1:/etc/init.d;
# update-rc.d /etc/init.d/cwMegaphoneBoot.sh defaults
done

# presumes ssh keys are set up, if not on mac:
## generate key:
# ssh-keygen -b 1024 -t dsa
## copy onto remote beagleboard:
# scp ~/.ssh/id_dsa.pub debian@192.168.2.16:/home/debian/.ssh/
## on beagleboard, add newly copied key to authorised_keys
# cat id_dsa.pub >> authorized_keys



* Chinese Whispers

- simulation and actual classes
- an exploration/simulation of megaphone rotation and recording/playback from a central 'sound player'
- three classes at the moment: Megaphone, SoundPlayer, and MegaGUI

* Big Fat To Do List

- added many items from Crewe Day 2 (10th November)
DONE: remapped volume and position parameters in python (4 as the starting pos, 13 is the ending position, 8.5 is the midway position)
DONE: adjust calibration forwards a little (4.5 to 13.5), some servos grind at the current zero point 
DONE: pushed position remap 0.5 forwards (4.5 to 13)
TODO: implement online checks in CWRemoteMegaphone (before being able to play, set position, etc)
DONE: fixed problem that initial volume is never sent
TODO: modify set position code so that a speed can be specified (go to this position in this amount of time)
TODO: rewrite python script to initialise pins on startup, not on first call (initial positions)
TODO: python echo (to diagnose problems)
TODO: fix megaphone offset in gui
TODO: keep volume level when re-playing. take value from gui?

TODO: implement startup script for megaphones (when code is solid)

- TODO: rename classes - local and remote depends on perspective - better to call Megaphone and MegaphoneInterface
- TODO: 'isPlaying' should be implemented at the AbstractMegaphone level, but how to implement it on the real megaphone?
- TODO: shouldn't be able to relinquish control if a sound is playing
- TODO: fix fact that when joining, peers come online twice (causes two servers to be booted)
- TODO: apply changes to Registrar/Registrant that were applied to DecentralisedNode
- TODO: GUI doesn't pick up megaphones or sound sources that already exist
-- tested with NMLDecentralisedNode and it works fine
- TODO: addrbook misses some megaphones or sound sources (even though their color changes)
-- actually two peers have the same node id, and replace each other
-- registering slowly solves the problem
- TODO: adapt NMLDecentralisedNode as megaphone doesn't need to listen for responses from others (what do we do about this, as peers need to listen for themselves to check they are online)
- TODO: Utopian: what is the state of play, does it work?
- TODO: finalise collaborative GUI, think of ways to control this as a score
- TODO: finalise GUI in general (new format - in a line)
- TODO: spread sound in stereo field based on megaphone index
- TODO: understand the degree of spacing that will be required beween megaphones
- DONE: if recording doesn't fill the buffer, only loop around the filled portion when playing

* Software (finalising class structure)

- order of events:
-- megaphones and sound sources will boot and announce themselves
-- they do not need to know about each other or the laptops
-- laptops will come online and see these resources
-- each sound source and megaphone will have a specific number (hardcode on beagleboards)
-- need to represent on GUI which megaphones are online

- megaphones need to:
-- announce themselves
-- communicate with python script to actuate megaphone
-- don't need to see others
-- don't need an scserver
- sound sources need to:
-- announce themselves
-- playback sound files
-- don't need to see others
-- need an sc server
- laptops need to:
-- announce themselves (for benefit of other laptops)
-- need to see all others (laptops, megaphones, sound sources)
-- don't need an SC server
* all devices will run Utopian, using a decentralised network

classes:

-laptops will run:
sclang:
	CWGUI					
	Utopian
	NMLDecentralisedNode

-beagle board will run:
sclang:
	CWMegaphone	
	Utopian
	NMLDecentralisedNode
python:
	megaphone_control.py

-beagle board will run:
sclang:
	CWSoundSource
	Utopian
	NMLDecentralisedNode
scserver:

* Hardware:

- (put notes here)

* Technical/Setup Issues:

9 slots - but only have 8 port switch
do we have 7 beagles?
simplification: why don't the end megaphones playback sound?

* Simulation Notes:

Megaphone Movement:
- currently emulates an 180 degree servo
- megaphones turn at a fixed speed until they reach desired angle
- if interrupted, they start a new trajectory from the current point

Megaphone Sound Recording/Playback:
- synths which emulates the sound functions of a physical megaphone, with ability to:
- record from it's mic
- playback what it has recorded
- proximity to

How the Megaphones Work
- recording overrides playback
- playback does NOT override live input (worrying!)
- ten second buffer (approx)
- light goes off when recording stops
- emulate button behaviour, or not?

Spacing Decisions
- There are four main factors at play:
- 1. megaphone length
- 2. spacing between megaphones
- 3. room size
- 4. servo possible angle of rotation
- the megaphone length is the defining factor for everything, as the other two can be varied

* Parameter Sharing Between Megaphones

// two approaches...

// 1. dataspace approach (trying this first)
// - each megaphone has a dataspace
// - the dataspace contains takeControl and control data
// - to takeControl or control, put a key in the dataspace
// - a dataspace dependency checks for keys and updates Megaphone instance variable accordingly

// 2. objectspace approach
// - one objectspace that contains all megaphones would seem to make sense
// - but this would seem to suit higher level container, not one objectspace per megaphone, not ideal
// - each time any parameters of the megaphone change, we add it (this) to the objectspace
// - on the dependency side, when a new object is received, it overwrites the existing one in the megaphone array
// - the trouble with this is the local updates would be done before the remote ones: any way to avoid this? maybe the megaphone is duplicated, modified, put in the space, and only is updated locally when it comes out as a dependency...hmmm

* Managing shared access to sound sources:
// -laptops run a gui
// -option 1:
// -the gui allows or denies control of the resources
// -the availability or not of the resources is shared between the laptops
// -the resources themselves i.e. NetworkedSourceSource don't know who is in control of them
// -but the remote versions do i.e. RemoteSoundSource
// -SharedRemoteSoundSource
// think of a simple case where a sound source is controlled by laptop 1, laptop 2 or no-one

* Research file created during week in Crewe:

// TODO: names of soundfiles, menu or buttons
// TODO: controlled by colors
// TODO: why does sound source stop responding?!?
// TODO: when 

Jonas Hummel c/o Martin Blain,
MMU Cheshire Campus, Crewe Green Road
CW1 5DU Crewe, Cheshire	

*13 ss
*11 m
*15 m
-14 m
*16 m

// 

update-rc.d blah defaults

update-rc.d -f  blah remove

// get command line args in supercollider

http://new-supercollider-mailing-lists-forums-use-these.2681727.n2.nabble.com/input-arguments-in-command-line-sclang-td7588602.html

"killall python".unixCmd;
"sudo python /home/debian/ChineseWhispers/code/megaphoneControl.py".unixCmd;

// getting back a port

lsof -i :5955	
kill -9 PID

// copy utopian off sound source

/home/debian/.local/share/SuperCollider/Extensions

c = SharedServer(\test, NetAddr("127.0.0.1", 51000), clientID: 0);

NMLUtopian(topology: \decentralised, hasServer: true, seesServers: false, sharesSynthDefs: false, verbose: false);

a = CWLocalSoundSource(0, "/home/debian/ChineseWhispers/soundfiles");


jackd -R -d alsa -d hw:1,0 &
sc3> a = CWLocalSoundSource(0, "/home/debian/ChineseWhispers/soundfiles")

	echo "BASH: killing jack"
	killall -9 jackd
	sleep 2
	echo "BASH: waiting for jack to start"
	jackd -R -d alsa -d hw:1,0 &
	sleep 4

WARNING: me not yet in address book
ERROR: Message 'addr' not understood.

booting local server...
SharedServer 2 audio buses: 128 control buses 4096
SharedServer 2 buffers: 1024
booting 10002
connecting to existing local server - not yet implemented
JackPosixProcessSync::LockedTimedWait error usec = 5000000 err = Connection timed out


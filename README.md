* Chinese Whispers

- 
- simulation and actual classes
- an exploration/simulation of megaphone rotation and recording/playback from a central 'sound player'
- three classes at the moment: Megaphone, SoundPlayer, and MegaGUI

To Do List:
// first, check the networked megaphone and sound source work individually, get them working with utopian

// managing shared access to sound sources

// laptops run a gui
// option 1:
// the gui allows or denies control of the resources
// the availability or not of the resources is shared between the laptops
// the resources themselves i.e. NetworkedSourceSource don't know who is in control of them
// but the remote versions do i.e. RemoteSoundSource
// SharedRemoteSoundSource

// think of a simple case where a sound source is controlled by laptop 1, laptop 2 or no-one
// controlled by

// 

// revisit GUI classes

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


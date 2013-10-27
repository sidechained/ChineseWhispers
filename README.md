MEGAPHONE CHOREOGRAPHY V2:
- an exploration/simulation of megaphone rotation and recording/playback from a central 'sound player'
- three classes at the moment: Megaphone, SoundPlayer, and MegaGUI

TODO: spread sound in stereo field based on megaphone index
TODO: adjust
TODO: GUI buttons
TODO: understand the degree of spacing that will be required beween megaphones
DONE: if recording doesn't fill the buffer, only loop around the filled portion when playing

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
There are four main factors at play:
1. megaphone length
2. spacing between megaphones
3. room size
4. servo possible angle of rotation
- the megaphone length is the defining factor for everything, as the other two can be varied


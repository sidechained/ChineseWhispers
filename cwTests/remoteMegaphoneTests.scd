n = NMLDecentralisedNode(doWhenMeAdded: { c = CWRemoteMegaphone(0, n) });
c.isOnline
c.startPlaying(1) // not quite working
c.stopPlaying
c.setPlayVolume(0.01);

c.setPosition(0)
c.setPosition(90)
c.setPosition(180)
c.faceForward
c.faceLeft
c.faceRight
c.startRecording
c.stopRecording

(
// first Tdef written to remote control megaphone volume
Tdef(\x, {
	inf.do{
		c.setPlayVolume(rrand(2, 100));
		0.25.wait;
		c.setPlayVolume(0);
		0.25.wait;
	}
}).play
)

Tdef(\x, { c.stopPlaying; c.setPlayVolume(100);}) // Tdefs must be stopped and volume reset (otherwise risk stopping the Tdef whilst volume is up)
Tdef(\x).isPlaying // ...and the band plays on!


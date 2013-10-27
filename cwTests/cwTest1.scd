Server.killAll

(
var noOfMegaphones, angleSegment, megaphones, soundPlayer;
noOfMegaphones = 5;
angleSegment = 2pi/noOfMegaphones;
z = noOfMegaphones.collect{arg i;
	Megaphone.new(s, angleSegment * i);
};
p = SoundSource.new(s, "/Users/grahambooth/Desktop/Megaphone/megaphonesoundschosen/");
defer { MegaGUI.new(z, p); }
)

// There are private methods in SuperCollider. They are just not enforced, but conventional. You just write "// private implementation" in front of them and call them prSomething.

(
// SCENARIO A: turn a megaphone in, record from soundSource,
m = z.choose;
fork {
	m.faceOut;
	m.waitUntilTurned;
	p.startPlaying(p.buffers.choose);
	m.startRecording;
	1.wait;
	p.stopPlaying;
	m.stopRecording;
	m.faceIn;
	m.waitUntilTurned;
	m.startPlaying;
	1.wait;
	m.stopPlaying;
}
)

(
// SCENARIO A: turn a megaphone in, record from soundSource,
m = z.choose;
fork {
	m.faceOut;
	m.waitUntilTurned;
	p.startPlaying(p.buffers.choose);
	m.startRecording;
	1.wait;
	p.stopPlaying;
	m.stopRecording;
	m.faceIn;
	m.waitUntilTurned;
	m.startPlaying;
	1.wait;
	m.stopPlaying;
}
)

m.index

(
// SCENARIO C: record from next
// choose two adjacent
k = (0..z.size-1).choose;
l = k + 1 % (z.size);
m = z[k];
n = z[l];
fork {
	m.faceNext;
	n.faceNext;
	m.waitUntilTurned;
	n.waitUntilTurned;
	m.startPlaying;
	n.startRecording;
	1.wait;
	m.stopRecording;
	n.stopPlaying;
	n.startPlaying;
}
)

// simple tests: one gui, one megaphone, one soundsource
(
p = SoundSource.new(s, "/Users/grahambooth/Desktop/Megaphone/megaphonesoundschosen/");
m = Megaphone.new(s, 0);
n = Megaphone.new(s, 2pi/2);
g = MegaGUI.new([m,n], p);
)

// control the sound source:
p.startPlaying(p.buffers.choose);
p.stopPlaying;
p.isPlaying;

// control first megaphone:
m.startRecording;
m.stopRecording;
m.startPlaying;
m.stopPlaying;
m.faceIn;
m.faceOut;
m.faceNext;

// control second megaphone:
n.startRecording;
n.stopRecording;
n.startPlaying;
n.stopPlaying;
n.faceIn;
n.faceOut;
n.faceNext;



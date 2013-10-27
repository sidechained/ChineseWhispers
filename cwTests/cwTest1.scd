// test file

Server.killAll // in case of emergency

(
var noOfMegaphones, angleSegment, megaphones, soundPlayer;
noOfMegaphones = 5;
angleSegment = 2pi/noOfMegaphones;
z = noOfMegaphones.collect{arg i;
	CWMegaphone.new(s, angleSegment * i);
};
// adjust the folder below to your own sounds folder:
p = CWSoundSource.new(s, "/Users/grahambooth/Desktop/Megaphone/megaphonesoundschosen/");
CWGUI.new(z, p);
)

// simple tests with one gui, two megaphones, one soundsource
(
p = CWSoundSource.new(s, "/Users/grahambooth/Desktop/Megaphone/megaphonesoundschosen/");
m = CWMegaphone.new(s, 0);
n = CWMegaphone.new(s, 2pi/2);
g = CWGUI.new([m,n], p);
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

// OTHER METHODS:
// .turnTo(angle) method can be used to set a specific position
// .waitUntilTurned can be used within a fork or routine to pause until a maneuver has been completed

(
// SCENARIO A: face out, record, face in, play
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

// SCENARIO B: to do

(
// SCENARIO C: record from next megaphone
k = (0..z.size-1).choose; // choose a megaphone index
l = k + 1 % (z.size); // choose an adjacent megaphone (wrap if necessary)
m = z[k]; n = z[l];
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


// NEXT:
// - explore the possibilities of a single soundfile (need to choose one)
// - try making interesting higher level Routines that control multiple megaphones
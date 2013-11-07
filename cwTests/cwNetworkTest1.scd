OSCDataSpace
r[0].takeControl;
r[0].takeControlOfPlayback;
c.takeControlOfVolume;
c.relinquishControl;
c.relinquishControlOfPlayback;
c.relinquishControlOfVolume;
c.playBuffer(0, 0.5); // buffer num, initial volume
c.setVolume(0.8);
c.stopBuffer;

// core issue of 'me', for me:
// a peer cannot bind responders to a particular address and port until it knows an appropriate address to bind to
// the only way to know this is to receive the address externally
// we could send a test message, to get me straight away, so we don't have to worry about this
// have loopback IP first, then correct
// this would mean that - would be setup on this address
// using the IP is okay, but the port should still be based on the client id
// and the client id is not known until the peer has registered

// my approach right now:
// set up a dependency to see when I first come online, use that as a trigger for the rest of the setup process

check LocalNetAddr

myNode.addrBook.addDependant({arg addrBook, what, who;
	if (who.id == myNode.myId) {
		"I came online".postln;
		this.initDataSpace;
	};
})

a = NMLDecentralisedNode()
a.goOffline

(
m = [];
Routine({
	5.collect({arg i;
		var n;
		n = CWNetworkedMegaphone(i+1, simulated: true); // don't start from 0
		m = m.add(n);
		2.0.rand.wait;
	})
}).play
)

m

(
var sfp;
sfp = "/Users/grahambooth/Desktop/Megaphone/china_soundfiles/china_announcements_edited/_chosen/";
z = [];
Routine({
	 2.collect({arg i;
		var n;
		n = CWSharedRemoteSoundSource(i+1, simulated: true, pathToSoundFiles: sfp); // don't start from 0
		z = z.add(n);
		3.wait;
	})
}).play
)

s.boot
z[0].node.kill
g = CWGUI()
g.node.addrBook.peers.printAll;"";

// node tests:
l = NMLDecentralisedNode()
m = NMLDecentralisedNode()
n = NMLDecentralisedNode(verbose: true)

// testing do when me added, from ground up
l = NMLDecentralisedNode(doWhenMeAdded: {l.addrBook.peers.postln});
u = NMLUtopian(hasServer: true, doWhenMeAdded: {u.node.addrBook.peers.postln});

Server.killAll
// checking functionality of networked sound source (how to get it to trigger sounds):
(
n = CWLocalSoundSource(1, true, pathToSoundFiles: "/Users/grahambooth/Desktop/Megaphone/china_soundfiles/china_announcements_edited/_chosen/");
)
(
// need to wait here;
l = NMLDecentralisedNode(doWhenMeAdded: {c = CWSharedRemoteSoundSource(1, l)});
)

c.node.me.id
// control tests (use dataspace checks to see if received):
c.takeControl;
c.takeControlOfPlayback;
c.takeControlOfVolume;
c.relinquishControl;
c.relinquishControlOfPlayback;
c.relinquishControlOfVolume;
// sound tests:
c.playBuffer(0, 0.5); // buffer num, initial volume
c.setVolume(0.8);
c.stopBuffer;
// dataspace checks:
c.dataspace.keys
c.dataspace.values
n.dataspace.keys
// peer checks:
c.node.addrBook.peers // should be two peers
n.utopian.node.addrBook.peers // should be two peers
// laptop data spaces:
c.sharedLaptopDataSpace.keys

CWSoundSource(1, true, pathToSoundFiles: "/Users/grahambooth/Desktop/Megaphone/china_soundfiles/china_announcements_edited/_chosen/")

// First test of a networked megaphone class
// - relies on NML classes
// - and will require a hack of OSCDataSpaces, I suppose (already done?)
// - time to get the new NML classes on github!

Server.killAll // in case of emergency

// first let's create a decentralisedNode
// it will autoregister, and doesn't need a name

s.boot;

(
	var noOfMegaphones, gui;
	noOfMegaphones = 5;
	~megaphones = noOfMegaphones.collect{arg index;
		CWNetworkedMegaphone.new(index, noOfMegaphones, simulated: false);
	};
)

MegaphoneControlAllGUI(~megaphones);

// how to manage colors?
addrBook.peer;
(n.addrBook.ids.asArray - n.addrBook.ids.minItem).collect{arg color};

// how to hook GUI

// text checks:
~megaphones[0].dataSpace.keys

~megaphones[0].takeControlOfPosition;"";
~megaphones[0].setPosition(2pi/4);
~megaphones[0].relinquishControlOfPosition;"";
~megaphones[0].positionControlledBy;

~megaphones[0].takeControlOfRecording;"";
~megaphones[0].setRecordState(true);
~megaphones[0].relinquishControlOfRecording;"";
~megaphones[0].recordingControlledBy;

~megaphones[0].takeControlOfPlayback;"";
~megaphones[0].setPlaybackState(true);
~megaphones[0].relinquishControlOfPlayback;"";
~megaphones[0].positionControlledBy;

~megaphones[0].takeControlOfVolume;"";
~megaphones[0].setVolume(0.5);
~megaphones[0].relinquishControlOfVolume;"";
~megaphones[0].volumeControlledBy;

// all
~megaphones[0].takeControl;"";
~megaphones[0].relinquish;"";
)

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
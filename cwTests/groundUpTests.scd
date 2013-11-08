// * Testing of each class from the ground up

// 1. simulated megaphone test (local)
// comments:
// - interfacing with abstract class works
// - playback node frees itself after 1 second or so regardless of how long it recorded for
// - TODO: prevent playing back whilst recording
s.boot
m = CWSimulatedMegaphone(s);
m.startRecording;
m.stopRecording;
m.startPlaying;
m.stopPlaying;
m.setPlayVolume(3);

// 2. real megaphone test (local)
// can't test actual functionality, but interfacing with abstract class works
m = CWRealMegaphone();
m.startRecording;
m.stopRecording;
m.startPlaying;
m.stopPlaying;
m.setPlayVolume(3);

// 3. simulated megaphone test (networked)
// a. set up local megaphone:
l = CWLocalMegaphone(0, simulated: true);
// b. set up remote megaphone (remote interface to local megaphone)
d = NMLDecentralisedNode(doWhenMeAdded: {\done.postln; m = CWRemoteMegaphone(0, d) });
// we interact with the local megaphone
m.startRecording;
m.stopRecording;
m.startPlaying(1);
m.stopPlaying;
m.setPlayVolume(0.5);
m.isOnline; // we can check to see if the local megaphone is online
// kill the CWLocalMegaphone and check again
m.isRecording;
m.isPlaying;


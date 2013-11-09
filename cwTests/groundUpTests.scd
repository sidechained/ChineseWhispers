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
// we interact with the local megaphone by calling the same methods as we would if it were local
m.startRecording;
m.stopRecording;
m.startPlaying(1);
m.stopPlaying;
m.setPlayVolume(0.5);
// we can check if the local megaphone is online, is recording or is playing
m.isOnline; // would be useful here to kill the CWLocalMegaphone and check again
m.isRecording;
m.isPlaying;

// 4. sound source test (local)
s.boot;
t = CWSoundSource("/Users/grahambooth/Desktop/Megaphone/china_soundfiles/china_announcements_edited/_chosen/", s);
t.startPlaying(0, 1);
t.stopPlaying;
t.setPlayVolume(0.2);

// 5. sound source test (networked)
// a. set up local soundsource:
// - server will be passed in by utopian
l = CWLocalSoundSource(0, "/Users/grahambooth/Desktop/Megaphone/china_soundfiles/china_announcements_edited/_chosen/");
// b. set up remote sound source (remote interface to local sound source)
d = NMLDecentralisedNode(doWhenMeAdded: {\done.postln; t = CWRemoteSoundSource(0, d) });
t.startPlaying(0, 1);
t.stopPlaying;
t.setPlayVolume(0.2);
// we can check if the local sound source is online, is recording or is playing
t.isOnline; // would be useful here to kill the CWLocalMegaphone and check again
t.isPlaying;
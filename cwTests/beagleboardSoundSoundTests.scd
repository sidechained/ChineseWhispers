// beagleboard sound source test (local)

(
s.waitForBoot({
	t = CWSoundSource("~/ChineseWhispers/soundfiles", s);
	s.sync;
	Routine({
		inf.do{
			\go.postln;
			t.startPlaying(1, 1);
			1.rand.wait;
			t.setPlayVolume(0.2);
			3.rand.wait;
			t.stopPlaying;
			2.rand.wait;
		}
	}).play
})
)

// Test results:
// 1: dies after 15 minutes
// 2: also dies after 15 minutes
// 3: same
// now testing with different (larger) soundfile
// still dies after 15 minutes

// sample rate is not same as sound file sample rate (try with)
// a soundfile is missing

JackEngine::XRun: client = SuperCollider was not finished, state = Running
JackAudioDriver::ProcessGraphAsyncMaster: Process error

// bb loads soundfiles okay
//

// 5. sound source test (networked)
// a. set up local soundsource:
// - server will be passed in by utopian
l = CWLocalSoundSource(0, "/Users/grahambooth/Desktop/Megaphone/china_soundfiles/china_announcements_edited/_chosen/");
// b. set up remote sound source (remote interface to local sound source)
d = NMLDecentralisedNode(doWhenMeAdded: {\done.postln; t = CWRemoteSoundSource(0, d) });
t.isOnline;
t.startPlaying(0, 1);
t.startPlaying(2, 1);
t.startPlaying(3, 1);
t.startPlaying(4, 1);
t.startPlaying(9, 1);
t.startPlaying(10, 1);
t.stopPlaying;
t.setPlayVolume(0.1);
t.setPlayVolume(1);
// we can check if the local sound source is online, is recording or is playing
t.isOnline; // would be useful here to kill the CWLocalMegaphone and check again
t.isPlaying;

Platform.userExtensionDir

/home/debian/.local/share/SuperCollider/Extensions
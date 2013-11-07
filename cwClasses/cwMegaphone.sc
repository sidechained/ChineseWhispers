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

//		if (server.serverRunning) {
//			this.doWhenServerRunning;
//		}
//		{
//			fork {
//				inform("booting server");
//				server.boot;
//				server.bootSync;
//				this.doWhenServerRunning;
//			};
//		};

/*		if (server.serverRunning) {
buffer = Buffer.alloc(server, server.sampleRate * 10); // allocate a ten second buffer
} {
warn("server not running, cannot create megaphone buffer");
};*/

CWNetworkedMegaphone : CWMegaphone {

	// networked megaphone
	// - will act as a Decentralised Node on the network
	// - will announce itself under its name + index number (hardcoded)

	var <node, name, <gui, <dataSpace;
	var <controlledBy, <positionControlledBy, <recordingControlledBy, <playbackControlledBy, <volumeControlledBy;

	*new {arg index, simulated = false;
		^super.new(index, simulated).initNetworkedMegaphone;
	}

	initNetworkedMegaphone {
		node = NMLDecentralisedNode(peerStartingPort: 4000);
		node.doWhenMeAddedFunc_({this.initCallBack}); // call back when 'me' exists
	}

	initCallBack {
		name = 'megaphone' ++ index.asSymbol;
		inform("registering with name: " ++ name);
		node.register(name);
		this.initDataSpace;
	}

	initDataSpace{
		inform("initialising data space for: " ++ name);
		dataSpace = OSCDataSpace(node.addrBook, node.me, name);
		dataSpace.addDependant({arg dataSpace, val, key, value;
			this.updateState(key, value);
		});
	}



	takeControl {
		this.takeControlOfPosition(());
		this.takeControlOfRecording(());
		this.takeControlOfPlayback(());
		this.takeControlOfVolume(());
		dataSpace.put(\controlledBy, node.me.id);
	}

	relinquishControl {
		this.relinquishControlOfPosition(());
		this.relinquishControlOfRecording(());
		this.relinquishControlOfPlayback(());
		this.relinquishControlOfVolume(());
		dataSpace.put(\controlledBy, \reset); // cannot use nil as it gets converted to a 0 over network
	}

	takeControlOfPosition {
		dataSpace.put(\positionControlledBy, node.me.id);
	}

	takeControlOfRecording {
		dataSpace.put(\recordingControlledBy, node.me.id);
	}

	takeControlOfPlayback {
		dataSpace.put(\playbackControlledBy, node.me.id);
	}

	takeControlOfVolume {
		dataSpace.put(\volumeControlledBy, node.me.id);
	}

	relinquishControlOfPosition {
		dataSpace.put(\positionControlledBy, \reset);
	}

	relinquishControlOfRecording {
		dataSpace.put(\recordingControlledBy, \reset);
	}

	relinquishControlOfPlayback {
		dataSpace.put(\playbackControlledBy, \reset);
	}

	relinquishControlOfVolume {
		dataSpace.put(\volumeControlledBy, \reset);
	}

	setPosition {arg position;
		if (positionControlledBy == node.me.id) {
			dataSpace.put(\setPosition, position);
		} {
			warn("you are not in control of this megaphone");
		};
	}

	setRecordState {arg recordState;
		if (recordingControlledBy == node.me.id) {
			dataSpace.put(\setRecordState, recordState);
		} {
			warn("you are not in control of this megaphone");
		};
	}

	setPlaybackState {arg playbackState;
		if (playbackControlledBy == node.me.id) {
			dataSpace.put(\setPlaybackState, playbackState);
		} {
			warn("you are not in control of this megaphone");
		};
	}

	setVolume {arg volume;
		if (volumeControlledBy == node.me.id) {
			dataSpace.put(\setVolume, volume);
		} {
			warn("you are not in control of this megaphone");
		};
	}

	updateState {arg key, value;
		case
		// shared control:
		{
			(key == \controlledBy) ||
			(key == \positionControlledBy) ||
			(key == \recordingControlledBy) ||
			(key == \playbackControlledBy) ||
			(key == \volumeControlledBy)
		} {
			defer {
				gui.control(key, value);
			}
		}
		// sound functions:
		{key == \setPosition } {
			// how about abstract positions...faceNext, etc?
			this.setPosition(value);
		}
		{key == \faceIn } {
			this.setPosition(0); // TODO: replace with real world value
		}
		{key == \faceOut } {
			this.setPosition(2pi/2); // TODO: replace with real world value
		}
		{key == \faceNext } {
			this.setPosition(2pi/4); // TODO: replace with real world value
		}
		{key == \setPlaybackState } {
			var isPlaying;
			isPlaying = value;
			if (isPlaying) {
				this.startPlaying;
			} {
				this.stopPlaying;
			}
		}
		{key == \setRecordState } {
			var isRecording;
			isRecording = value;
			if (isRecording) {
				this.startRecording;
			} {
				this.stopRecording;
			}
		}
		{key == \setVolume } {
			this.setPlayVolume(value);
		}
	}

}

CWMegaphone {

	// TODO: recording volume shoulwd be set constantly, as can record whilst the megaphone is moving

	var index, simulated, startingAngle, <currentAngle = 0, turnSpeed;
	var <buffer, bufDur, recSynth, recStartTime, playSynth, turner, <isTurning = false, pythonAddr;

	// angle 0 = facing out
	// angle pi = facing in

	*new {arg index, simulated = false;
		^super.newCopyArgs(index, simulated).initMegaphone;
	}

	initMegaphone {
		// server boot stuff should be in here, if the server isn't booted the instance of megaphone shouldn't be created at all
		var angleSegment;
		angleSegment = 2pi/5.rand; // temp for now
		startingAngle = angleSegment * index;
		currentAngle = startingAngle;
		turnSpeed = 2pi / 100;
		// use default server here
		if (simulated) {
			buffer = Buffer.alloc(Server.default, Server.default.sampleRate * 10); // allocate a ten second buffer
			this.initSynthDefs;
			inform("running simulated megaphone")
		} {
			pythonAddr = NetAddr("127.0.0.1", 10000);
			inform("running real megaphone")
		};
	}

	initSynthDefs {

		// TODO: could check to see if these exist first before adding:

		SynthDef(\megaphoneRecorder, {arg buffer, recAmp = 1;
			var in;
			in = In.ar(0);
			in = in * recAmp;
			RecordBuf.ar(in, buffer, loop: 0, doneAction: 2); // stop once buffer is full
		}).add;

		SynthDef(\megaphonePlayer, {arg buffer, amp = 1, loopDur = 10;
			// playback buffer contents, loop endlessly
			// dur should be set to the used length of the buffer
			// (which may or may not = its total length)
			// ...it will resets the buffer to the start
			var looper, out;
			looper = Impulse.ar(1/loopDur);
			out = PlayBuf.ar(1, buffer, trigger: looper, loop: 1);
			Out.ar(0, out);
		}).add;

	}

	isInUse {
		^(this.isRecording || this.isPlaying  || isTurning );
	}

	isRecording {
		if (recSynth.isNil) { ^false } { ^recSynth.isPlaying };
	}

	isPlaying {
		if (playSynth.isNil) { ^false } { ^playSynth.isPlaying };
	}

	startRecording {
		if (simulated.not) {
			this.startRealRecording;
		}
		{
			this.startSimulatedRecording;
		}
	}

	startRealRecording {
		pythonAddr.sendMsg('/megaphone/record', 1); // HIGH
	}

	startSimulatedRecording {
		if (this.isRecording.not) {
			var recAmp;
			recAmp = (currentAngle - startingAngle).linlin(0, 2pi/2, 1, 0.2); // recording volume determined by position
			recSynth = Synth(\megaphoneRecorder, [\buffer, buffer, \recAmp, recAmp], addAction: \addToTail);
			recStartTime = Main.elapsedTime;
			// add to tail to ensure we record after playback (order of operations)
			NodeWatcher.register(recSynth);
		}
		{
			warn("megaphone already recording")
		}
	}

	stopRecording {
		if (simulated.not) {
			this.stopRealRecording;
		}
		{
			this.stopSimulatedRecording;
		}
	}

	stopRealRecording {
		pythonAddr.sendMsg('/megaphone/record', 0); // LOW
	}

	stopSimulatedRecording {
		if (this.isRecording) {
			bufDur = Main.elapsedTime - recStartTime;
			recSynth.free;
		} {
			warn("megaphone recording already stopped")
		};
	}

	startPlaying {arg amplitude;
		if (simulated.not) {
			this.startRealPlaying(amplitude);
		}
		{
			this.startSimulatedPlaying(amplitude);
		}
	}

	startRealPlaying {
		pythonAddr.sendMsg('/megaphone/play', 1); // HIGH
	}

	startSimulatedPlaying {arg amplitude;
		if (this.isPlaying.not) {
			playSynth = Synth(\megaphonePlayer, [\buffer, buffer, \amp, amplitude, \loopDur, bufDur]);
			NodeWatcher.register(playSynth);
		}
		{
			warn("megaphone already playing")
		}
	}

	stopPlaying {
		if (simulated.not) {
			this.stopRealPlaying;
		}
		{
			this.stopSimulatedPlaying;
		}
	}

	stopRealPlaying {
		pythonAddr.sendMsg('/megaphone/play', 0); // LOW
	}

	stopSimulatedPlaying {
		if (this.isPlaying) {
			playSynth.free;
		} {
			warn("megaphone not playing")
		};
	}

	setPlayVolume {arg amp;
		if (simulated.not) {
			this.setRealPlayVolume(amp);
		}
		{
			this.setSimulatedPlayVolume(amp);
		}
	}

	setRealPlayVolume {arg amp;
		pythonAddr.sendMsg('/megaphone/volume', amp);
	}

	setSimulatedPlayVolume {arg amp;
		if (this.isPlaying) {
			playSynth.set(\amp, amp);
		} {
			warn("megaphone not playing")
		};
	}

	faceOut {
		this.setPosition(startingAngle);
	}

	faceIn {
		this.setPosition(startingAngle + (2pi/2));
	}

	faceNext {
		this.setPosition(startingAngle + (2pi * 0.25));
	}

	setPosition {arg targetAngle;
		if (simulated.not) {
			this.setRealPosition(targetAngle);
		}
		{
			this.setSimulatedPosition(targetAngle);
		}
	}

	setRealPosition {arg targetAngle;
		pythonAddr.sendMsg('/megaphone/position', targetAngle); // HIGH
	}

	setSimulatedPosition {arg targetAngle;
		var direction, condition;
		if (targetAngle > currentAngle) { // what if they are equal?
			direction = 1; // go forwards
			condition = { targetAngle < currentAngle };
		} {
			direction = -1; // go backwards
			condition = { targetAngle > currentAngle };
		};
		if (turner.notNil) { if (isTurning) {turner.stop}; };
		turner = Routine({
			isTurning = true;
			inf.do{
				currentAngle = currentAngle + (turnSpeed * direction);
				if (condition.value) {
					isTurning = false;
					currentAngle = targetAngle;
					turner.stop;
				};
				0.05.wait;
			};
		}).play;
	}

	waitUntilTurned {
		var c;
		c = Condition.new;
		fork {
			var r;
			r = Routine({
				inf.do{
					if (this.isTurning.not) {c.unhang; r.stop;};
					0.1.wait;
				};
			}).play;
		};
		c.hang;
	}

}
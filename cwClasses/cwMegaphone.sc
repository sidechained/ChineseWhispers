CWSharedRemoteMegaphone : CWRemoteMegaphone {

	var <sharedControlSpace;

	*new {arg index, node, simulated = false;
		^super.new(index, node, simulated).initSharedRemoteMegaphone;
	}

	initSharedRemoteMegaphone {
		sharedControlSpace = OSCDataSpace(node.addrBook, node.me, oscPath: '/sharedControlSpace');
	}

	// shared control:

	takeControl {
		this.takeControlOfPosition;
		this.takeControlOfRecording;
		this.takeControlOfPlayback;
		this.takeControlOfVolume;
		sharedControlSpace.put(\controlledBy, node.me.id);
	}

	relinquishControl {
		if (sharedControlSpace.at(\controlledBy) == node.me.id) {
			this.relinquishControlOfPosition;
			this.relinquishControlOfRecording;
			this.relinquishControlOfPlayback;
			this.relinquishControlOfPlayVolume;
			sharedControlSpace.put(\controlledBy, \reset); // cannot use nil as it gets converted to a 0 over network
		} {
			warn("you are not in control of this sound source");
		};

	}

	takeControlOfPosition {
		sharedControlSpace.put(\positionControlledBy, node.me.id);
	}

	takeControlOfRecording {
		sharedControlSpace.put(\recordingControlledBy, node.me.id);
	}

	takeControlOfPlayback {
		sharedControlSpace.put(\playbackControlledBy, node.me.id);
	}

	takeControlOfVolume {
		sharedControlSpace.put(\playVolumeControlledBy, node.me.id);
	}

	relinquishControlOfPosition {
		if (sharedControlSpace.at(\positionControlledBy) == node.me.id) {
			sharedControlSpace.put(\positionControlledBy, \reset);
		}
		{
			warn("you are not in control of this parameter");
		};
	}

	relinquishControlOfRecording {
		if (sharedControlSpace.at(\recordingControlledBy) == node.me.id) {
			sharedControlSpace.put(\recordingControlledBy, \reset);
		}
		{
			warn("you are not in control of this parameter");
		};
	}

	relinquishControlOfPlayback {
		if (sharedControlSpace.at(\playbackControlledBy) == node.me.id) {
			sharedControlSpace.put(\playbackControlledBy, \reset);
		}
		{
			warn("you are not in control of this parameter");
		};
	}

	relinquishControlOfPlayVolume {
		if (sharedControlSpace.at(\playVolumeControlledBy) == node.me.id) {
			sharedControlSpace.put(\playVolumeControlledBy, \reset);
		} {
			warn("you are not in control of this parameter");
		};
	}

	// shared actuation:

	setPosition {arg position;
		if (sharedControlSpace.at(\positionControlledBy) == node.me.id) {
			^super.setPosition(position);
		} {
			warn("you are not in control of this parameter");
		};
	}

	startRecording {
		if (sharedControlSpace.at(\recordingControlledBy) == node.me.id) {
			^super.startRecording;
		} {
			warn("you are not in control of this parameter");
		};
	}

	stopRecording {
		if (sharedControlSpace.at(\recordingControlledBy) == node.me.id) {
			^super.stopRecording;
		} {
			warn("you are not in control of this parameter");
		};
	}

	startPlaying {arg initialVolume;
		if (sharedControlSpace.at(\playbackControlledBy) == node.me.id) {
			^super.startPlaying(initialVolume);
		} {
			warn("you are not in control of this parameter");
		}
	}

	stopPlaying {
		if (sharedControlSpace.at(\playbackControlledBy) == node.me.id) {
			^super.stopPlaying;
		} {
			warn("you are not in control of this parameter");
		}
	}

	setPlayVolume {arg volume;
		if (sharedControlSpace.at(\playVolumeControlledBy) == node.me.id) {
			^super.setPlayVolume(volume);
		} {
			warn("you are not in control of this parameter");
		};
	}

}

CWRemoteMegaphone {

	// actuation

	var <index, <node, <name, <dataspace, <currentAngle;

	*new {arg index, node;
		^super.newCopyArgs(index, node).initRemoteMegaphone;
	}

	initRemoteMegaphone {
		// do only once this node is online
		var oscPath;
		currentAngle = 0;
		name = ('megaphone' ++ index).asSymbol;
		oscPath = '/' ++ name;
		dataspace = OSCDataSpace(node.addrBook, node.me, oscPath: oscPath);
	}

	faceLeft {arg position;
		this.setPosition(0);
	}

	faceForward {arg position;
		this.setPosition(90);
	}

	faceRight {arg position;
		this.setPosition(180);
	}

	setPosition {arg position;
		if (this.isOnline) {
			currentAngle = position;
			node.addrBook.sendName(name, \setPosition, position);
		} {
			inform("% is not online".format(name));
		}
	}

	startRecording {
		if (this.isOnline) {
			node.addrBook.sendName(name, \startRecording);
		} {
			inform("% is not online".format(name));
		}
	}

	stopRecording {
		if (this.isOnline) {
			node.addrBook.sendName(name, \stopRecording);
		} {
			inform("% is not online".format(name));
		}
	}

	startPlaying {arg initialVolume;
		if (this.isOnline) {
			node.addrBook.sendName(name, \startPlaying, initialVolume);
		} {
			inform("% is not online".format(name));
		}
	}

	stopPlaying {
		if (this.isOnline) {
			node.addrBook.sendName(name, \stopPlaying);
		} {
			inform("% is not online".format(name));
		}
	}

	setPlayVolume {arg volume;
		if (this.isOnline) {
			node.addrBook.sendName(name, \setPlayVolume, volume);
		} {
			inform("% is not online".format(name));
		}
	}

	//

	isOnline {
		^node.addrBook.atName(name) !? { node.addrBook.atName(name).online } ?? { false }
	}

	isRecording {
		// OSC converts booleans to 1's and 0's, so must convert back here
		^dataspace.at(\isRecording) !? {dataspace.at(\isRecording).asBoolean} ?? { false };
	}

	isPlaying {
		// OSC converts booleans to 1's and 0's, so must convert back here
		^dataspace.at(\isPlaying) !? {dataspace.at(\isPlaying).asBoolean} ?? { false };
	}

}

CWLocalMegaphone {

	// a networked megaphone
	// this will run on the beagleboard

	var index, simulated, megaphone, <utopian, <name, <dataspace;

	*new {arg index, simulated = false;
		^super.newCopyArgs(index, simulated).init;
	}

	init {
		if (simulated) {
			utopian = NMLUtopian(
				topology: \decentralised,
				hasServer: true, // server if simulated, none if not
				seesServers: false,
				sharesSynthDefs: false,
				verbose: false,
				doWhenMeAdded: {this.doWhenMeAdded},
				doWhenBooted: {megaphone = CWSimulatedMegaphone(index, utopian.node, utopian.server)}
			);
		}
		{
			utopian = NMLUtopian(
				topology: \decentralised,
				hasServer: false, // server if simulated, none if not
				seesServers: false,
				sharesSynthDefs: false,
				verbose: false,
				doWhenMeAdded: {this.doWhenMeAdded},
				doWhenBooted: nil
			);
			megaphone = CWRealMegaphone(index, utopian.node);
		};
	}

	doWhenMeAdded {
		name = ('megaphone' ++ index).asSymbol;
		// inform("registering with name: " ++ name);
		utopian.node.register(name);
		this.initResponders;
		this.initDataSpace;
	}

	initDataSpace {
		var oscPath;
		oscPath = '/' ++ name;
		inform("initialising data space for: " ++ oscPath);
		dataspace = OSCDataSpace(utopian.node.addrBook, utopian.node.me, oscPath);
	}

	initResponders {

		// provide option to free these?

		OSCFunc({arg msg;
			var position;
			# position = msg.drop(1);
			msg.postln;
			this.doSetPosition(position);
		}, '\setPosition', recvPort: utopian.node.me.addr.port);

		OSCFunc({arg msg;
			msg.postln;
			this.doStartRecording;
		}, '\startRecording', recvPort: utopian.node.me.addr.port);

		OSCFunc({arg msg;
			msg.postln;
			this.doStopRecording;
		}, '\stopRecording', recvPort: utopian.node.me.addr.port);

		OSCFunc({arg msg;
			var initialVolume;
			# initialVolume = msg.drop(1);
			msg.postln;
			this.doStartPlaying(initialVolume);
		}, '\startPlaying', recvPort: utopian.node.me.addr.port);

		OSCFunc({arg msg;
			msg.postln;
			this.doStopPlaying;
		}, '\stopPlaying', recvPort: utopian.node.me.addr.port);

		OSCFunc({arg msg;
			var volume;
			# volume = msg.drop(1);
			msg.postln;
			this.doSetPlayVolume(volume);
		}, '\setPlayVolume', recvPort: utopian.node.me.addr.port);

	}

	doSetPosition {arg position;
		// TODO ? set isTurning here?
		megaphone.doSetPosition(position);
	}

	doStartRecording {
		dataspace.put(\isRecording, true);
		megaphone.doStartRecording;
	}

	doStopRecording {
		dataspace.put(\isRecording, false);
		megaphone.doStopRecording;
	}

	doStartPlaying {arg initialVolume;
		dataspace.put(\isPlaying, true);
		megaphone.doStartPlaying(initialVolume);
	}

	doStopPlaying {
		dataspace.put(\isPlaying, false);
		megaphone.doStopPlaying;
	}

	doSetPlayVolume {arg volume;
		megaphone.doSetPlayVolume(volume);
	}

}

CWSimulatedMegaphone : CWAbstractMegaphone {

	// NOTE: simulated megaphone should run only after booting
	// NOTE: need for separate synthdeflibs
	// NOTE: stopped checking if synth nodes are playing

	var <server, <buffer, bufDur, recSynth, playSynth;

	*new {arg server;
		^super.newCopyArgs(server).init; // will this copy?
	}

	init {
		inform("running simulated megaphone");
		super.init;
		buffer = Buffer.alloc(server, Server.default.sampleRate * 10); // allocate a ten second buffer
		this.initSynthDefs;
	}

	initSynthDefs {
		SynthDef(\megaphoneRecorder, {arg buffer, recAmp = 1;
			var in;
			in = In.ar(0);
			in = in * recAmp;
			RecordBuf.ar(in, buffer, loop: 0, doneAction: 2); // stop once buffer is full
		}).add; // NOTE: need for separate synthdeflibs
		SynthDef(\megaphonePlayer, {arg buffer, amp = 1, loopDur = 10;
			// playback buffer contents, loop endlessly
			// dur should be set to the used length of the buffer
			// (which may or may not = its total length)
			// ...it will resets the buffer to the start
			var looper, out;
			looper = Impulse.ar(1/loopDur);
			out = PlayBuf.ar(1, buffer, trigger: looper, loop: 1);
			Out.ar(0, out);
		}).add; // NOTE: need for separate synthdeflibs
	}

	doStartRecording {
		var recAmp;
		recAmp = (currentAngle - startingAngle).linlin(0, 2pi/2, 1, 0.2); // recording volume determined by position
		recSynth = Synth(\megaphoneRecorder, [\buffer, buffer, \recAmp, recAmp], target: server, addAction: \addToTail);
		recStartTime = Main.elapsedTime;
	}

	doStopRecording {
		bufDur = Main.elapsedTime - recStartTime;
		recSynth.free;
	}

	doStartPlaying {arg initialVolume;
		playSynth = Synth(\megaphonePlayer, [\buffer, buffer, \amp, initialVolume, \loopDur, bufDur]);
	}

	doStopPlaying {
		playSynth.free;
	}

	doSetPlayVolume {arg volume;
		playSynth.set(\amp, volume);
	}

	doSetPosition {arg position;
		inform("position isn't used by simulated megaphone %".format(position))
	}

}

CWRealMegaphone : CWAbstractMegaphone {

	var pythonAddr;

	*new {
		^super.new.init; // will this copy?
	}

	init {
		inform("running real megaphone");
		pythonAddr = NetAddr("127.0.0.1", 9991);
		super.init;
	}

	doStartRecording {
		pythonAddr.sendMsg('/megaphone/record', 1); // HIGH
	}

	doStopRecording {
		pythonAddr.sendMsg('/megaphone/record', 0); // LOW
	}

	doStartPlaying {arg initialVolume;
		if (initialVolume.notNil) { this.doSetPlayVolume(initialVolume) };
		pythonAddr.sendMsg('/megaphone/play', 1); // HIGH
	}

	doStopPlaying {
		pythonAddr.sendMsg('/megaphone/play', 0); // LOW
	}

	doSetPlayVolume {arg volume;
		// should we be able to set play volume even when not playing?
		pythonAddr.sendMsg('/megaphone/playVolume', volume);
	}

	doSetPosition {arg position;
		pythonAddr.sendMsg('/megaphone/position', position);
	}

	// convenience methods

	doFaceLeft {arg position;
		this.doSetPosition(0);
	}

	doFaceForward {arg position;
		this.doSetPosition(90);
	}

	doFaceRight {arg position;
		this.doSetPosition(180);
	}
}

CWAbstractMegaphone {

	// TODO: recording volume should be set constantly, as can record whilst the megaphone is moving

	var startingAngle, <currentAngle = 0, turnSpeed, turner, isPlaying = false, isRecording = false, <isTurning = false;
	var recStartTime;

	// angle 0 = facing out
	// angle pi = facing in

	init {
		var angleSegment;
		angleSegment = 2pi/(5.rand + 1); // temp for now
		startingAngle = angleSegment;
		currentAngle = startingAngle;
		turnSpeed = 2pi / 100;
	}

	faceOut {
		this.calcPosition(startingAngle);
	}

	faceIn {
		this.calcPosition(startingAngle + (2pi/2));
	}

	faceNext {
		this.calcPosition(startingAngle + (2pi * 0.25));
	}

	calcPosition {arg targetAngle;
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
				this.setCurrentAngle(currentAngle + (turnSpeed * direction));
				if (condition.value) {
					isTurning = false;
					this.setCurrentAngle(targetAngle);
					turner.stop;
				};
				0.05.wait;
			};
		}).play;
	}

	setCurrentAngle {arg angle;
		currentAngle = angle;
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

	setPosition {arg position;
		this.doSetPosition(position);
	}

	startRecording {
		if (isRecording.not) {
			isRecording = true;
			// should turn to false after set amount of time
			// is there a way to get feedback about when recording stops?
			this.doStartRecording;
		}
		{
			warn("megaphone already recording")
		}
	}

	stopRecording {
		if (isRecording) {
			isRecording = false;
			this.doStopRecording;
		} {
			warn("megaphone recording already stopped")
		};
	}

	startPlaying {arg initialVolume;
		if (isPlaying.not) {
			isPlaying = true;
			this.doStartPlaying(initialVolume);
		}
		{
			warn("megaphone already playing")
		}
	}

	stopPlaying {
		if (isPlaying) {
			isPlaying = false;
			this.doStopPlaying;
		} {
			warn("megaphone not playing")
		};
	}

	setPlayVolume {arg volume;
		if (isPlaying) {
			this.doSetPlayVolume(volume);
		} {
			warn("megaphone not playing")
		};
	}

}

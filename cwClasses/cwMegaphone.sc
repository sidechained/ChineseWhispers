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

CWSharedRemoteMegaphone : CWRemoteMegaphone {

	var <sharedControlSpace;

	*new {arg index, node, simulated = false;
		^super.new(index, node, simulated).initSharedRemoteMegaphone;
	}

	initSharedRemoteMegaphone {
		sharedControlSpace = OSCDataSpace(node.addrBook, node.me, oscPath: '/sharedControlSpace'); // TODO: needs to be per megaphone
	}

	// shared control:

	takeControl {
		this.takeControlOfPosition(());
		this.takeControlOfRecording(());
		this.takeControlOfPlayback(());
		this.takeControlOfVolume(());
		sharedControlSpace.put(\controlledBy, node.me.id);
	}

	relinquishControl {
		if (sharedControlSpace.at(\controlledBy) == node.me.id) {
			this.relinquishControlOfPosition(());
			this.relinquishControlOfRecording(());
			this.relinquishControlOfPlayback(());
			this.relinquishControlOfVolume(());
			dataspace.put(\controlledBy, \reset); // cannot use nil as it gets converted to a 0 over network
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
		// TODO only if sound not playing
		if (sharedControlSpace.at(\playbackControlledBy) == node.me.id) {
			sharedControlSpace.put(\playbackControlledBy, \reset);
		}
		{
			warn("you are not in control of this parameter");
		};
	}

	relinquishControlOfVolume {
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
			warn("you are not in control of this megaphone");
		};
	}

	record {
		if (sharedControlSpace.at(\recordingControlledBy) == node.me.id) {
			^super.record;
		} {
			warn("you are not in control of this megaphone");
		};
	}

	stopRecording {
		if (sharedControlSpace.at(\recordingControlledBy) == node.me.id) {
			^super.stopRecording;
		} {
			warn("you are not in control of this megaphone");
		};
	}

	play {
		if (sharedControlSpace.at(\playbackControlledBy) == node.me.id) {
			^super.play;
		} {
			warn("you are not in control of this megaphone");
		}
	}

	stopPlaying {
		if (sharedControlSpace.at(\playbackControlledBy) == node.me.id) {
			^super.stopPlaying;
		} {
			warn("you are not in control of this megaphone");
		}
	}

	setPlayVolume {arg volume;
		if (sharedControlSpace.at(\playVolumeControlledBy) == node.me.id) {
			^super.setPlayVolume(volume);
		} {
			warn("you are not in control of this megaphone");
		};
	}

}

CWRemoteMegaphone {

	// actuation

	var index, <node, <name, <dataspace;

	*new {arg index, node;
		^super.newCopyArgs(index, node).initRemoteMegaphone;
	}

	initRemoteMegaphone {
		// do only once this node is online
		var oscPath;
		name = ('megaphone' ++ index).asSymbol;
		oscPath = '/' ++ name;
		dataspace = OSCDataSpace(node.addrBook, node.me, oscPath: oscPath);
	}

	setPosition {arg position;
		dataspace.put(\setPosition, position);
	}

	record {
		dataspace.put(\record);
	}

	stopRecording {
		dataspace.put(\stopRecording);
	}

	play {
		dataspace.put(\play);
	}

	stopPlaying {
		dataspace.put(\stopPlaying);
	}

	setPlayVolume {arg volume;
		dataspace.put(\setPlayVolume, volume);
	}

	//

	isOnline {
		^node.addrBook.atName(name) !? { node.addrBook.atName(name).online } ?? { false }
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
		}
	}

	doWhenMeAdded {
		name = ('megaphone' ++ index).asSymbol;
		// inform("registering with name: " ++ name);
		utopian.node.register(name);
		this.initDataSpace;
	}

	initDataSpace{
		var oscPath;
		oscPath = '/' ++ name;
		inform("initialising data space for: " ++ oscPath);
		dataspace = OSCDataSpace(utopian.node.addrBook, utopian.node.me, oscPath);
		dataspace.addDependant({arg dataspace, val, key, value;
			this.updateState(key, value);
		});
	}

	updateState {arg key, value;
		case
		// sound functions:
		{key == \setPosition } { // how about abstract positions...faceNext, etc?
			[\setPosition, value].postln;
			megaphone.setPosition(value);
		}
		{key == \faceIn } {
			megaphone.setPosition(0); // TODO: replace with real world value
		}
		{key == \faceOut } {
			megaphone.setPosition(2pi/2); // TODO: replace with real world value
		}
		{key == \faceNext } {
			megaphone.setPosition(2pi/4); // TODO: replace with real world value
		}
		{key == \record } {
			[\record].postln;
			megaphone.record;
		}
		{key == \stopRecording } {
			[\stopRecording].postln;
			megaphone.stopRecording;
		}
		{key == \play } {
			[\play].postln;
			megaphone.play;
		}
		{key == \stopPlaying } {
			[\stopPlaying].postln;
			megaphone.stopPlaying;
		}
		{key == \setPlayVolume } {
			[\setPlayVolume, value].postln;
			megaphone.setPlayVolume(value);
		}
	}

	//

	isPlaying {

	}

}

CWSimulatedMegaphone : CWAbstractMegaphone {

	// NOTE: simulated megaphone should run only after booting
	// NOTE: need for separate synthdeflibs

	var <server, <buffer, bufDur, recSynth, playSynth;

	*new {arg index, node, server, simulated = false;
		^super.new(index, node).initCWSimulatedMegaphone(server); // will this copy?
	}

	initCWSimulatedMegaphone {arg argServer;
		inform("running simulated megaphone");
		this.initAbstractMegaphone;
		server = argServer;
		buffer = Buffer.alloc(server, Server.default.sampleRate * 10); // allocate a ten second buffer
		this.initSynthDefs;
	}

	initSynthDefs {
		// TODO: could check to see if these exist first before adding:
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

	record {
		if (this.isRecording.not) {
			var recAmp;
			recAmp = (currentAngle - startingAngle).linlin(0, 2pi/2, 1, 0.2); // recording volume determined by position
			recSynth = Synth(\megaphoneRecorder, [\buffer, buffer, \recAmp, recAmp], target: server, addAction: \addToTail);
			recStartTime = Main.elapsedTime;
			// add to tail to ensure we record after playback (order of operations)
			NodeWatcher.register(recSynth); // clash?
		}
		{
			warn("megaphone already recording")
		}
	}

	stopRecording {
		if (this.isRecording) {
			bufDur = Main.elapsedTime - recStartTime;
			recSynth.free;
		} {
			warn("megaphone recording already stopped")
		};
	}

	play {arg amplitude;
		if (this.isPlaying.not) {
			playSynth = Synth(\megaphonePlayer, [\buffer, buffer, \amp, amplitude, \loopDur, bufDur]);
			NodeWatcher.register(playSynth); // clash?
		}
		{
			warn("megaphone already playing")
		}
	}

	stopPlaying {
		if (this.isPlaying) {
			playSynth.free;
		} {
			warn("megaphone not playing")
		};
	}

	setPlayVolume {arg amp;
		this.isPlaying.postln;
		if (this.isPlaying) {
			playSynth.set(\amp, amp);
		} {
			warn("megaphone not playing")
		};
	}

	setPosition {
		// position isn't reflected in simulated megaphone (only in GUI)
	}

	isRecording {
		// track synth, or simply set separate flags?
		if (recSynth.isNil) { ^false } { ^recSynth.isPlaying };
	}

	isPlaying {
		// track synth, or simply set separate flags?
		if (playSynth.isNil) { ^false } { ^playSynth.isPlaying };
	}


}

CWRealMegaphone : CWAbstractMegaphone {

	var pythonAddr, isPlaying, isRecording;

	*new {
		^super.new.initCWRealMegaphone; // will this copy?
	}

	initCWRealMegaphone {
		inform("running real megaphone");
		isPlaying = false;
		isRecording = false;
		this.initAbstractMegaphone;
		pythonAddr = NetAddr("127.0.0.1", 10000);
	}

	record {
		isRecording = true;
		// should turn to false after
		// is there a way to get feedback about when recording stops?
		pythonAddr.sendMsg('/megaphone/record', 1); // HIGH
	}

	stopRecording {
		isRecording = false;
		pythonAddr.sendMsg('/megaphone/record', 0); // LOW
	}

	play {
		isPlaying = true;
		pythonAddr.sendMsg('/megaphone/play', 1); // HIGH
	}

	stopPlaying {
		isPlaying = false;
		pythonAddr.sendMsg('/megaphone/play', 0); // LOW
	}

	setPlayVolume {arg amp;
		// should we be able to set play volume even when not playing?
		pythonAddr.sendMsg('/megaphone/volume', amp);
	}

	setPosition {arg targetAngle;
		pythonAddr.sendMsg('/megaphone/position', targetAngle); // HIGH
	}

}

CWAbstractMegaphone {

	// TODO: recording volume should be set constantly, as can record whilst the megaphone is moving

	var startingAngle, <currentAngle = 0, turnSpeed, turner, <isTurning = false;
	var recStartTime;

	// angle 0 = facing out
	// angle pi = facing in

	initAbstractMegaphone {
		var angleSegment;
		\initAbstractMegaphone.postln;
		angleSegment = 2pi/(5.rand + 1); // temp for now
		startingAngle = angleSegment;
		currentAngle = startingAngle;
		turnSpeed = 2pi / 100;
		[startingAngle, currentAngle].postln;
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
				currentAngle = currentAngle + (turnSpeed * direction);
				this.setPosition(currentAngle);
				if (condition.value) {
					isTurning = false;
					currentAngle = targetAngle;
					this.setPosition(currentAngle);
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

	// need equivalent real megaphone classes for these:
/*	isInUse {
		^(this.isRecording || this.isPlaying || isTurning );
	}*/

}
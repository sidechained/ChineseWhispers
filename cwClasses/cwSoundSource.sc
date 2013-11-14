CWSharedRemoteSoundSource : CWRemoteSoundSource {

	// this class manages shared control of the sound source
	// the sharedControlSpace exists only on laptops
	// control functions:

	var <sharedControlSpace;

	*new {arg index, node;
		^super.new(index, node).initSharedRemoteSoundSource;
	}

	initSharedRemoteSoundSource {
		sharedControlSpace = OSCDataSpace(node.addrBook, node.me, oscPath: '/sharedControlSpace');
	}

	// shared control:

	takeControl {
		// no checks here, can always grab control
		this.takeControlOfPlayback;
		this.takeControlOfVolume;
		sharedControlSpace.put(\controlledBy, node.me.id);
	}

	relinquishControl {
		if (sharedControlSpace.at(\controlledBy) == node.me.id) {
			this.relinquishControlOfPlayback;
			this.relinquishControlOfVolume;
			sharedControlSpace.put(\controlledBy, \reset);
			// cannot use nil as it gets converted to a 0 over network
		} {
			warn("you are not in control of this sound source");
		};
	}

	takeControlOfPlayback {
		// no checks here, can always grab control
		sharedControlSpace.put(\playbackControlledBy, node.me.id);
	}

	takeControlOfVolume {
		// no checks here, can always grab control
		sharedControlSpace.put(\volumeControlledBy, node.me.id);
	}

	relinquishControlOfPlayback {
		if (sharedControlSpace.at(\playbackControlledBy) == node.me.id) {
			sharedControlSpace.put(\playbackControlledBy, \reset);
		} {
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

	startPlaying {arg bufferNumber, initialVolume;
		if (sharedControlSpace.at(\playbackControlledBy) == node.me.id) {
			^super.startPlaying(bufferNumber, initialVolume);
		} {
			warn("you are not in control of this parameter");
		};
	}

	stopPlaying {
		if (sharedControlSpace.at(\playbackControlledBy) == node.me.id) {
			^super.stopPlaying;
		} {
			warn("you are not in control of this parameter");
		};
	}

	setPlayVolume {arg volume;
		if (sharedControlSpace.at(\playVolumeControlledBy) == node.me.id) {
			^super.setPlayVolume(volume);
		} {
			warn("you are not in control of this parameter");
		};
	}

}

CWRemoteSoundSource {

	// wrapper class which exposes functionality of a sound source to remote peers on the network
	// this will run on each laptop

	var <index, <node, <name, <dataspace;

	*new {arg index, node;
		^super.newCopyArgs(index, node).initRemoteSoundSource;
	}

	initRemoteSoundSource {
		// do only once this node is online
		var oscPath;
		name = ('soundSource' ++ index).asSymbol;
		oscPath = '/' ++ name;
		dataspace = OSCDataSpace(node.addrBook, node.me, oscPath: oscPath);
	}

	// actuation:

	startPlaying {arg bufferNumber, volume;
		if (this.isOnline) {
			if (this.isPlaying) { this.stopPlaying; inform("stopping existing sound")};
			node.addrBook.sendName(name, \startPlaying, bufferNumber, volume);
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

	// check state of local megaphone

	isOnline {
		^node.addrBook.atName(name) !? { node.addrBook.atName(name).online } ?? { false }
	}

	isPlaying {
		// OSC converts booleans to 1's and 0's, so must convert back here
		^dataspace.at(\isPlaying) !? {dataspace.at(\isPlaying).asBoolean} ?? { false };
	}

}

CWLocalSoundSource : CWSoundSource {

	// this will run on the beagleboard

	var index, <utopian, <name, <dataspace;

	*new {arg index, pathToSoundFiles;
		^super.new(pathToSoundFiles).init(index);
	}

	init {arg argIndex;
		// overrides CWSoundSource init (which will be called below once server has booted)
		index = argIndex;
		utopian = NMLUtopian(
			topology: \decentralised,
			hasServer: true,
			seesServers: false,
			sharesSynthDefs: false,
			verbose: false,
			doWhenMeAdded: {this.doWhenMeAdded},
			doWhenBooted: {this.doWhenBooted}
		);
	}

	doWhenMeAdded {
		name = ('soundSource' ++ index).asSymbol;
		utopian.node.register(name);
		inform("registering with name: " ++ name);
		this.initDataSpace;
		this.initResponders;
	}

	doWhenBooted {
		server = utopian.server;
		this.initAmplitudeResponder;
		this.initSynthDef;
		this.readBuffers;
	}

	initDataSpace{
		var oscPath;
		oscPath = '/soundSource' ++ index;
		inform("initialising data space for: " ++ oscPath);
		dataspace = OSCDataSpace(utopian.node.addrBook, utopian.node.me, oscPath);
	}

	initResponders {

		OSCFunc({arg msg;
			var bufferNumber, initialVolume;
			# bufferNumber, initialVolume = msg.drop(1);
			msg.postln;
			this.doStartPlaying(bufferNumber, initialVolume);
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

	doStartPlaying {arg initialVolume;
		dataspace.put(\isPlaying, true);
		super.doStartPlaying(initialVolume);
	}

	doStopPlaying {
		dataspace.put(\isPlaying, false);
		super.doStopPlaying;
	}

}

CWSoundSource {

	// abstract class at the moment (won't init when called)

	var pathToSoundFiles, server, <buffers, playSynth, <amplitude, isPlaying = false;

	*new {arg pathToSoundFiles, server;
		^super.newCopyArgs(pathToSoundFiles, server); // no init
	}

	initSynthDef {
		// TODO: could check to see if these exist first before adding:
		SynthDef(\soundSourcePlayer, {arg buffer, amp = 1;
			// inject a sound file into the system
			// should not loop
			var out;
			out = PlayBuf.ar(1, buffer, loop: 0, doneAction: 2);
			out = out * amp; // send Amplitude.kr pre or post amp scaling?
			SendReply.kr(Impulse.kr(20), '/soundSourceAmplitude', [Amplitude.kr(out)] );
			Out.ar(0, out);
		}).add;
	}

	initAmplitudeResponder {
		OSCFunc({arg msg;
			var amplitude;
			# amplitude = msg.drop(3);
		}, '/soundSourceAmplitude')
	}

	readBuffers {
		buffers = PathName(pathToSoundFiles).files.collect{arg filePathName;
			inform("reading: %".format(filePathName.fileName));
			Buffer.read(server, filePathName.fullPath);
		};
	}

	// checks:

	startPlaying {arg bufferNumber, initialVolume;
		if (isPlaying.not) {
			isPlaying = true;
			this.doStartPlaying(bufferNumber, initialVolume);
		} {
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

	// sound changing functions:

	doStartPlaying {arg bufferNumber, initialVolume = 1;
		playSynth = Synth(\soundSourcePlayer, [\buffer, bufferNumber, \amp, initialVolume], target: server);
	}

	doStopPlaying {
		playSynth.free;
	}

	doSetPlayVolume {arg volume;
		playSynth.set(\amp, volume);
	}

}
CWSharedRemoteSoundSource : CWRemoteSoundSource {

	// this will manage shared control of the resource

	// dataspace is the laptop dataspace
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
		this.takeControlOfPlayback(());
		this.takeControlOfVolume(());
		sharedControlSpace.put(\controlledBy, node.me.id);
	}

	relinquishControl {
		if (sharedControlSpace.at(\controlledBy) == node.me.id) {
			this.relinquishControlOfPlayback(());
			this.relinquishControlOfVolume(());
			sharedControlSpace.put(\controlledBy, \reset);
			// cannot use nil as it gets converted to a 0 over network
		} {
			warn("you are not in control of this sound source");
		};
	}

	takeControlOfPlayback {
		sharedControlSpace.put(\playbackControlledBy, node.me.id);
	}

	takeControlOfVolume {
		sharedControlSpace.put(\volumeControlledBy, node.me.id);
	}

	relinquishControlOfPlayback {
		// TODO only if sound not playing
		if (sharedControlSpace.at(\playbackControlledBy) == node.me.id) {
			sharedControlSpace.put(\playbackControlledBy, \reset);
		} {
			warn("you are not in control of this parameter");
		};
	}

	relinquishControlOfVolume {
		if (sharedControlSpace.at(\volumeControlledBy) == node.me.id) {
			sharedControlSpace.put(\volumeControlledBy, \reset);
		} {
			warn("you are not in control of this parameter");
		};
	}

	// shared actuation:

	play {arg bufferNumber, volume;
		if (sharedControlSpace.at(\playbackControlledBy) == node.me.id) {
			^super.play(bufferNumber, volume);
		} {
			warn("you are not in control of this parameter");
		};
	}

	stop {
		if (sharedControlSpace.at(\playbackControlledBy)  == node.me.id) {
			^super.stop;
		} {
			warn("you are not in control of this parameter");
		};
	}

	setVolume {arg volume;
		if (sharedControlSpace.at(\volumeControlledBy) == node.me.id) {
			^super.setVolume(volume);
		} {
			warn("you are not in control of this parameter");
		};
	}

}

CWRemoteSoundSource {

	// wrapper class which exposes functionality of a sound source to remote peers on the network
	// this will run on each laptop

	var index, <node, <name, <dataspace;

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

	play {arg bufferNumber, volume;
		dataspace.put(\play, [bufferNumber, volume].asBinaryArchive);
		// binary archiving is a workaround which make it possible send more than one value
	}

	stop {
		dataspace.put(\stop);
	}

	setVolume {arg volume;
		dataspace.put(\setVolume, volume);
	}

	// check state of local megaphone

	isOnline {
		^node.addrBook.atName(name) !? { node.addrBook.atName(name).online } ?? { false }
	}

	isPlaying {
		^dataspace.at(\isPlaying);
	}


}

CWLocalSoundSource : CWSoundSource {

	// this will run on the beagleboard

	var <utopian, <name, <dataspace;

	*new {arg index, pathToSoundFiles;
		^super.new(index, pathToSoundFiles).init;
	}

	init {
		// overrides CWSoundSource init
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
		// inform("registering with name: " ++ name);
		utopian.node.register(name);
		this.initDataSpace;
	}

	doWhenBooted {
		server = utopian.server;
		super.doWhenBooted;
	}

	initDataSpace{
		var oscPath;
		oscPath = '/soundSource' ++ index;
		inform("initialising data space for: " ++ oscPath);
		dataspace = OSCDataSpace(utopian.node.addrBook, utopian.node.me, oscPath);
		dataspace.addDependant({arg dataspace, val, key, value;
			this.updateState(key, value);
		});
	}

	updateState {arg key, value;
		case
		// sound functions:
		{ key == \play } {
			var bufferNumber, initialVolume;
			# bufferNumber, initialVolume = value.unarchive;
			this.startPlaying(bufferNumber, initialVolume);
		}
		{ key == \stop } { this.stopPlaying }
		{ key == \setVolume } { this.setVolume(value) }
	}

}

CWSoundSource {

	var index, pathToSoundFiles, server, <buffers, playSynth, <amplitude;

	*new {arg index, pathToSoundFiles ,server;
		^super.newCopyArgs(index, pathToSoundFiles, server);
		// .init;
	}

	// TODO: how to run this without messing with the init from CWLocalSoundSource

	// init {
	// 	\imhere.postln;
	// 	// server ?? { server = Server.default }; // use default if none given
	// 	fork {
	// 		server.boot;
	// 		server.bootSync;
	// 		this.doWhenBooted;
	// 	}
	// }

	doWhenBooted {
		this.initAmplitudeResponder;
		this.initSynthDef;
		this.readBuffers;
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
			# amplitude = msg.drop(3);
		}, '/soundSourceAmplitude')
	}

	readBuffers {
		buffers = PathName(pathToSoundFiles).files.collect{arg filePathName;
			inform("reading: %".format(filePathName.fileName));
			Buffer.read(server, filePathName.fullPath);
		};
	}

	isPlaying {
		if (playSynth.isNil) { ^false } { ^playSynth.isPlaying };
	}

	startPlaying {arg buffer, initialVolume = 1;
		if (this.isPlaying) { this.stopPlaying; };
		playSynth = Synth(\soundSourcePlayer, [\buffer, buffer, \amp, initialVolume], target: server);
		NodeWatcher.register(playSynth);
	}

	stopPlaying {
		if (this.isPlaying) { playSynth.free; } { warn("sound source not playing!") };
	}

	setVolume {arg volume;
		if (this.isPlaying) { playSynth.set(\amp, volume); } { warn("sound source not playing!") };
	}

}


CWRemoteSoundSource {

	// wrapper class to

	var index, <node, <dataspace;

	*new {arg index, node;
		^super.newCopyArgs(index, node).initObjectDataSpace;
	}

	initObjectDataSpace {
		// do only once this node is online
		var oscPath;
		oscPath = '\soundSource' ++ index;
		oscPath.postln;
		dataspace = OSCDataSpace(node.addrBook, node.me, oscPath: oscPath);
	}

	// control functions:

	takeControl {arg node;
		this.takeControlOfPlayback(());
		this.takeControlOfVolume(());
		dataspace.put(\controlledBy, node.me.id);
	}

	relinquishControl {
		this.relinquishControlOfPlayback(());
		this.relinquishControlOfVolume(());
		dataspace.put(\controlledBy, \reset);
		// cannot use nil as it gets converted to a 0 over network
	}

	takeControlOfPlayback {arg node;
		dataspace.put(\playbackControlledBy, node.me.id);
	}

	takeControlOfVolume {arg node;
		dataspace.put(\volumeControlledBy, node.me.id);
	}

	relinquishControlOfPlayback {arg node;
		dataspace.put(\playbackControlledBy, \reset);
	}

	relinquishControlOfVolume {arg node;
		dataspace.put(\volumeControlledBy, \reset);
	}

	// sound functions:

	setPlaybackState {arg playbackState;
		dataspace.put(\setPlaybackState, playbackState);
	}

	setVolume {arg volume;
		dataspace.put(\setVolume, volume);
	}

}

CWNetworkedSoundSource : CWSoundSource {

	var <utopian, name, <gui, <dataspace;
	var <controlledBy, <playbackControlledBy, <volumeControlledBy;

	*new {arg index, simulated = false, pathToSoundFiles;
		^super.new(index, simulated, pathToSoundFiles).init;
	}

	init {
		// overrides CWSoundSource init
		utopian = NMLUtopian(
			topology: \decentralised,
			hasServer: true,
			seesServers: false,
			sharesSynthDefs: false,
			verbose: false,
			doWhenMeAdded: {this.initCallBack}
			doWhenBooted: {this.doWhenBooted}
		);
	}

	initCallBack {
		utopian.node.addrBook.peers.postln;
		// name = 'soundSource' ++ index;
		// inform("registering with name: " ++ name);
		// utopian.node.register(name);
		// this.initDataSpace;
	}

	initDataSpace{
		var oscPath;
		oscPath = '\soundSource' ++ index;
		inform("initialising data space for: " ++ oscPath);
		dataspace = OSCDataSpace(utopian.node.addrBook, utopian.node.me, oscPath);
		dataspace.addDependant({arg dataspace, val, key, value;
			this.updateState(key, value);
		});
	}

	takeControl {
		this.takeControlOfPlayback(());
		this.takeControlOfVolume(());
		dataspace.put(\controlledBy, utopian.node.me.id);
	}

	relinquishControl {
		this.relinquishControlOfPlayback(());
		this.relinquishControlOfVolume(());
		dataspace.put(\controlledBy, \reset); // cannot use nil as it gets converted to a 0 over network
	}

	takeControlOfPlayback {
		dataspace.put(\playbackControlledBy, utopian.node.me.id);
	}

	takeControlOfVolume {
		dataspace.put(\volumeControlledBy, utopian.node.me.id);
	}

	relinquishControlOfPlayback {
		dataspace.put(\playbackControlledBy, \reset);
	}

	relinquishControlOfVolume {
		dataspace.put(\volumeControlledBy, \reset);
	}

	setPlaybackState {arg playbackState;
		\getPlaybackState.postln;
		if (playbackControlledBy == utopian.node.me.id) {
			dataspace.put(\setPlaybackState, playbackState);
		} {
			warn("you are not in control of this megaphone");
		};
	}

	setVolume {arg volume;
		if (volumeControlledBy == utopian.node.me.id) {
			dataspace.put(\setVolume, volume);
		} {
			warn("you are not in control of this megaphone");
		};
	}

	updateState {arg key, value;
		// shared control:
		case
		{
			(key == \controlledBy) ||
			(key == \playbackControlledBy)
		} {
			defer {
				gui.control(key, value);
			}
		}
		// sound functions:
		{key == \setPlaybackState } {
			var isPlaying, bufferNumber;
			# isPlaying, bufferNumber = value;
			if (isPlaying) {
				this.startPlaying(bufferNumber);
			} {
				this.stopPlaying;
			}
		}
		{key == \setVolume } {
			this.setPlayVolume(value);
		}
	}

}

CWSoundSource {

	var index, simulated, pathToSoundFiles, server, <buffers, playSynth, <amplitude;

	*new {arg index, simulated = false, pathToSoundFiles ,server;
		^super.newCopyArgs(index, simulated, pathToSoundFiles, server).init;
	}

	init {
		// server ?? { server = Server.default }; // use default if none given
		fork {
			server.boot;
			server.bootSync;
			this.doWhenBooted;
		}
	}

	doWhenBooted {
		this.receiveAmplitudeFromSynth;
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
			SendReply.kr(Impulse.kr(20), '/soundSourceAmplitude', [Amplitude.kr(out)] );
			Out.ar(0, out);
		}).add;

	}

	receiveAmplitudeFromSynth {
		OSCFunc({arg msg;
			# amplitude = msg.drop(3);
		}, '/soundSourceAmplitude')
	}

	readBuffers {
		pathToSoundFiles.postln;
		buffers = PathName(pathToSoundFiles).files.collect{arg filePathName;
			inform("reading: %".format(filePathName.fileName));
			Buffer.read(server, filePathName.fullPath);
		};
	}

	isPlaying {
		if (playSynth.isNil) { ^false } { ^playSynth.isPlaying };
	}

	startPlaying {arg buffer, amplitude = 1;
		this.stopPlaying;
		playSynth = Synth(\soundSourcePlayer, [\buffer, buffer, \amp, amplitude]);
		NodeWatcher.register(playSynth);
	}

	stopPlaying {
		if (this.isPlaying) { playSynth.free; };
	}

}


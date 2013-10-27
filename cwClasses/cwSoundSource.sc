CWSoundSource {

	var server, pathToSoundFiles, <buffers, playSynth, <amplitude;

	*new {arg server, pathToSoundFiles;
		^super.newCopyArgs(server, pathToSoundFiles).init;
	}

	init {
		if (server.serverRunning) {
			this.doWhenServerRunning;
		}
		{
			fork {
				inform("booting server...");
				server.boot;
				server.bootSync;
				this.doWhenServerRunning;
			};
		};
	}

	doWhenServerRunning {
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


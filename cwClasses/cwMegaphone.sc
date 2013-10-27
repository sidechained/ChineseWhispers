CWMegaphone {

	// TODO: recording volume should be set constantly, as can record whilst the megaphone is moving

	var server, startingAngle, <currentAngle = 0, turnSpeed, <buffer, bufDur, recSynth, recStartTime, playSynth, turner, <isTurning = false;

	// angle 0 = facing out
	// angle pi = facing in

	*new {arg server, startingAngle;
		^super.newCopyArgs(server, startingAngle).init;
	}

	init {
		currentAngle = startingAngle;
		turnSpeed = 2pi / 100;
		if (server.serverRunning) {
			this.doWhenServerRunning;
		}
		{
			fork {
				inform("booting server");
				server.boot;
				server.bootSync;
				this.doWhenServerRunning;
			};
		};
	}

	doWhenServerRunning {
		this.initSynthDefs;
		this.makeBuffer;
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

	makeBuffer {
		if (server.serverRunning) {
			buffer = Buffer.alloc(server, server.sampleRate * 10); // allocate a ten second buffer
		} {
			warn("server not running, cannot create megaphone buffer");
		};
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
		if (this.isRecording) {
			bufDur = Main.elapsedTime - recStartTime;
			bufDur.postln;
			recSynth.free;
		} {
			warn("megaphone recording already stopped")
		};
	}

	startPlaying {arg amplitude;
		if (this.isPlaying.not) {
			playSynth = Synth(\megaphonePlayer, [\buffer, buffer, \amp, amplitude, \loopDur, bufDur]);
			NodeWatcher.register(playSynth);
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
		if (this.isPlaying) {
			playSynth.set(\amp, amp);
		} {
			warn("megaphone not playing")
		};
	}

	faceOut {
		this.turnTo(startingAngle);
	}

	faceIn {
		this.turnTo(startingAngle + (2pi/2));
	}

	faceNext {
		this.turnTo(startingAngle + (2pi * 0.25));
	}

	turnTo {arg targetAngle;
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
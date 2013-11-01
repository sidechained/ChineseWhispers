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

	var myNode, <gui;
	var <dataSpace;
	var <controlledBy, <positionControlledBy, <recordingControlledBy, <playbackControlledBy, <volumeControlledBy;

	*new {arg argMyNode, index, noOfMegaphones;
		^super.new(index, noOfMegaphones).initNetworkedMegaphone(argMyNode);
	}

	initNetworkedMegaphone {arg argMyNode;
		myNode = argMyNode;
		gui = MegaphoneControlGUI.new(this);
		this.initDataSpace;
	}

	initDataSpace{
		dataSpace = OSCDataSpace(myNode.addrBook, myNode.me, '/megaphone' ++ index.asSymbol);
		dataSpace.addDependant({arg dataSpace, val, key, value;
			this.updateState(key, value);
		});
	}

	takeControl {
		this.takeControlOfPosition(());
		this.takeControlOfRecording(());
		this.takeControlOfPlayback(());
		this.takeControlOfVolume(());
		dataSpace.put(\controlledBy, myNode.me.id);
	}

	relinquishControl {
		this.relinquishControlOfPosition(());
		this.relinquishControlOfRecording(());
		this.relinquishControlOfPlayback(());
		this.relinquishControlOfVolume(());
		dataSpace.put(\controlledBy, \reset); // cannot use nil as it gets converted to a 0 over network
	}

	takeControlOfPosition {
		dataSpace.put(\positionControlledBy, myNode.me.id);
	}

	takeControlOfRecording {
		dataSpace.put(\recordingControlledBy, myNode.me.id);
	}

	takeControlOfPlayback {
		dataSpace.put(\playbackControlledBy, myNode.me.id);
	}

	takeControlOfVolume {
		dataSpace.put(\volumeControlledBy, myNode.me.id);
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
		if (positionControlledBy == myNode.me.id) {
			dataSpace.put(\setPosition, position);
		} {
			warn("you are not in control of this megaphone");
		};
	}

	setRecordState {arg recordState;
		if (recordingControlledBy == myNode.me.id) {
			dataSpace.put(\setRecordState, recordState);
		} {
			warn("you are not in control of this megaphone");
		};
	}

	setPlaybackState {arg playbackState;
		if (playbackControlledBy == myNode.me.id) {
			dataSpace.put(\setPlaybackState, playbackState);
		} {
			warn("you are not in control of this megaphone");
		};
	}

	setVolume {arg volume;
		if (volumeControlledBy == myNode.me.id) {
			dataSpace.put(\setVolume, volume);
		} {
			warn("you are not in control of this megaphone");
		};
	}

	updateState {arg key, value;
		case
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
		{key == \setPosition } {
			// how about abstract positions...faceNext, etc?
			this.turnTo(value);
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

MegaphoneControlGUI {

	var megaphone, <mainView, paramFuncArray, <controllingColorViewDict;
	var defaultBackgroundColor;

	*new {arg megaphone;
		^super.newCopyArgs(megaphone).init;
	}

	init {
		defaultBackgroundColor = Color.black;
		controllingColorViewDict = IdentityDictionary.new;
		paramFuncArray = [
			[\controlledBy, "all", { megaphone.takeControl }, { megaphone.relinquishControl } ],
			[\positionControlledBy, "pos", { megaphone.takeControlOfPosition }, { megaphone.relinquishControlOfPosition } ],
			[\recordingControlledBy, "rec", { megaphone.takeControlOfRecording }, { megaphone.relinquishControlOfRecording } ],
			[\playbackControlledBy, "play", { megaphone.takeControlOfPlayback }, { megaphone.relinquishControlOfPlayback } ],
			[\volumeControlledBy, "vol", { megaphone.takeControlOfVolume }, { megaphone.relinquishControlOfVolume } ]
		];
		mainView = this.makeMainView(megaphone);
	}

	makeRow {arg key, paramName, takeControlFunc, relinquishControlFunc;
		var allButton, rButton, controllingColorView;
		allButton =	Button()
		.fixedSize_(Size(43, 26))
		.states_([[paramName]])
		.action_(takeControlFunc);
		rButton = Button()
		.fixedSize_(Size(23, 26))
		.states_([["r"]])
		.action_(relinquishControlFunc);
		controllingColorView = View()
		.fixedSize_(Size(43, 26))
		.background_(defaultBackgroundColor);
		controllingColorViewDict.put(key, controllingColorView);
		^View().layout_(HLayout(*[allButton, rButton, controllingColorView]).spacing_(0).margins_(0))
	}

	makeMainView {
		var rows, mainView;
		rows = paramFuncArray.collect{arg paramFuncArray;
			var key, paramName, takeControlFunc, relinquishControlFunc;
			# key, paramName, takeControlFunc, relinquishControlFunc = paramFuncArray;
			this.makeRow(key, paramName, takeControlFunc, relinquishControlFunc)};
		mainView = View(nil, Rect(0, 0, 50, 100));
		mainView.layout_(VLayout(*rows).spacing_(4).margins_(0));
		^mainView;
	}

	mapIdToColor {arg id;
		if (id.isNil) {
			^Color.black;
		} {
			^Color.hsv(id, 1, 1); // TODO: need better colour mapping strategy than this!
		}
	}

	control {arg key, value;
		var color;
		if (value == \reset) {color = defaultBackgroundColor} { color = this.mapIdToColor(value) };
		controllingColorViewDict[key].background_(color);
	}

}

CWMegaphone {

	// TODO: recording volume should be set constantly, as can record whilst the megaphone is moving

	var index, noOfMegaphones, startingAngle, <currentAngle = 0, turnSpeed, <buffer, bufDur, recSynth, recStartTime, playSynth, turner, <isTurning = false;

	// angle 0 = facing out
	// angle pi = facing in

	*new {arg index, noOfMegaphones;
		^super.newCopyArgs(index, noOfMegaphones).initMegaphone;
	}

	initMegaphone {
		// server boot stuff should be in here, if the server isn't booted the instance of megaphone shouldn't be created at all
		var angleSegment;
		angleSegment = 2pi/noOfMegaphones;
		startingAngle = angleSegment * index;
		currentAngle = startingAngle;
		turnSpeed = 2pi / 100;
		// use default server here
		buffer = Buffer.alloc(Server.default, Server.default.sampleRate * 10); // allocate a ten second buffer
		this.initSynthDefs;
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
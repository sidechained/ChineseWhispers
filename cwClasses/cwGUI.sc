CWLaptop {

	// one utopian, many remote objects (megaphones and sound sources)

	var index, <utopian, <remoteMegaphones, <remoteSoundSources, <gui;
	// index won't make sense in the end

	*new {arg index;
		^super.newCopyArgs(index).init;
	}

	init {
		utopian = NMLUtopian(
			topology: \decentralised,
			hasServer: false,
			doWhenMeAdded: {this.doWhenMeAdded}
		);
	}

	doWhenMeAdded {
		// name the laptop (for ease of recongition in the addrBook)
		utopian.node.register('laptop' ++ index);
		// add megaphones:
		remoteMegaphones = 5.collect{arg index;
			CWSharedRemoteMegaphone(index, utopian.node);
		};
		// add sound sources:
		remoteSoundSources = 2.collect{arg index;
			CWSharedRemoteSoundSource(index, utopian.node);
		};
		gui = CWGUI(remoteMegaphones, remoteSoundSources);
	}

}

CWGUI {
	// draw the expected number of megaphones
	// have a colour for online, color for offline
	var remoteMegaphones, remoteSoundSources;
	var <node;
	var devicePositionDict;
	var onlineColor, offlineColor, playColor, recColor, stopColor;
	var displayPaneSize, controlPaneSize;
	var soundSourceSize, megaphoneSize, megaphoneLength, megaphoneMicSize, megaphoneHornSize;
	var megaphoneParamFuncArray, <megaphoneParamButtonDict;
	var soundSourceParamFuncArray, <soundSourceParamButtonDict;
	var defaultBackgroundColor;

	*new {arg remoteMegaphones, remoteSoundSources;
		^super.newCopyArgs(remoteMegaphones, remoteSoundSources).init;
	}

	init {
		onlineColor = Color.blue;
		offlineColor = Color.grey;
		playColor = Color.green;
		recColor = Color.red;
		stopColor = Color.black;
		displayPaneSize = Size(400, 400);
		controlPaneSize = Size(400, 400);
		soundSourceSize = displayPaneSize.width/10;
		megaphoneSize = displayPaneSize.width/10;
		megaphoneMicSize = megaphoneSize/10;
		megaphoneHornSize = megaphoneSize/5;

		devicePositionDict = ( // between 0 and 1
			soundSource0: 0.1,
			megaphone0: 0.23333333333333,
			megaphone1: 0.36666666666667,
			megaphone2: 0.5,
			megaphone3: 0.63333333333333,
			megaphone4: 0.76666666666667,
			soundSource1: 0.9
		);
		defer { this.makeGUI };
	}

	makeGUI {
		var gui, displayPane, controlPane, sharedControlPane;
		gui = View(nil, Rect(0, 0, 1200, 400));
		displayPane = this.makeDeviceDisplayPane.fixedSize_(displayPaneSize);
		//controlPane = this.makeControlPane.fixedSize_(200, 200);
		//sharedControlPane = this.makeSharedControlPane.fixedSize_(400, 100);
		//controlPane = View().fixedSize_(controlPaneSize).background_(Color.red);
		gui.layout_(HLayout(*[displayPane]) // , controlPane, sharedControlPane
			.spacing_(0)
			.margins_(0)
		);
		gui.front;
		^gui;
	}

	makeDeviceDisplayPane {
		var deviceDisplayPane;
		deviceDisplayPane = UserView();
		deviceDisplayPane.drawFunc_({
			// first, translate to center:
			// Pen.translate(size/2, deviceDisplayPane.bounds.height/2);
			// 1. draw megaphones:
			remoteMegaphones.do{arg remoteMegaphone;
				var xPos, isOnline, isPlaying, isRecording, currentAngle;
				xPos = devicePositionDict.at(remoteMegaphone.name).linlin(0, 1, 0, deviceDisplayPane.bounds.width);
				isOnline = remoteMegaphone.isOnline;
				isRecording = remoteMegaphone.isRecording;
				isPlaying = remoteMegaphone.isPlaying;
				currentAngle = 2pi*0.2; // remoteMegaphone.currentAngle
				//isTurning?
				this.drawMegaphone(xPos, isOnline, isRecording, isPlaying, currentAngle);
			};
			// 2. draw sound sources:
			remoteSoundSources.do{arg remoteSoundSource;
				var xPos, isOnline, isPlaying, amplitude;
				xPos = devicePositionDict.at(remoteSoundSource.name).linlin(0, 1, 0, deviceDisplayPane.bounds.width);
				isOnline = remoteSoundSource.isOnline;
				isPlaying = remoteSoundSource.isPlaying;
				//amplitude = remoteSoundSource.amplitude; // ignoring amp for now, only relevant for simulation
				this.drawSoundSource(xPos, isOnline, isPlaying);
			};
		});
		deviceDisplayPane.background_(Color.magenta(alpha:0.1));
		deviceDisplayPane.animate_(true);
		^deviceDisplayPane;
	}

	drawMegaphone {arg xPos, isOnline, isRecording, isPlaying, currentAngle; // isTurning?
		Pen.use{
			var start, end, color;
			// mag = 5;
			color = if (isOnline) {
				if (isPlaying) { playColor } {
					// if not playing, check if recording
					if (isRecording) { recColor } {
						// if not playing or recording then just use online color
						onlineColor
					};
				} { // offline
					offlineColor
				};
			};
			this.drawCentreBlob(xPos, 40, color);
			// draw centre blob (mic stand)
			this.drawBody(xPos, 40, currentAngle);
			this.drawHorn(xPos, 40, currentAngle);
			this.drawMic(xPos, 40, currentAngle);
		}
	}

	drawCentreBlob {arg x, y, color;
		var blobSize = 4;
		Pen.use{
			Pen.translate(x, y);
			Pen.fillColor_(color);
			Pen.fillOval(Rect(blobSize/2 * -1, blobSize/2 * -1, blobSize, blobSize));
		}
	}

	drawBody {arg x, y, currentAngle;
		Pen.use {
			var up, down;
			currentAngle = currentAngle;
			Pen.translate(x, y);
			up = Polar(megaphoneSize/2, (currentAngle - (2pi/2))%2pi).asComplex.asPoint;
			Pen.translate(up.x, up.y);
			down = Polar(megaphoneSize, currentAngle).asComplex.asPoint;
			Pen.line(down);
			Pen.strokeColor_(Color.grey(alpha: 0.3));
			Pen.stroke;
		};
	}

	drawHorn {arg x, y, currentAngle;
		// translate to start:
		Pen.use {
			var start, up, down;
			Pen.translate(x, y);
			start = Polar(megaphoneSize / 2, currentAngle).asComplex.asPoint;
			Pen.translate(start.x, start.y);
			up = Polar(megaphoneHornSize / 2, currentAngle + ((2pi/4)%2pi)).asComplex.asPoint;
			Pen.translate(up.x, up.y);
			down = Polar(megaphoneHornSize, currentAngle - ((2pi/4)%2pi)).asComplex.asPoint;
			Pen.line(down);
			Pen.strokeColor_(Color.grey(alpha: 0.3));
			Pen.stroke;
		};
	}

	drawMic {arg x, y, currentAngle;
		Pen.use {
			var end, up, down;
			Pen.translate(x, y);
			// translate to end:
			end = Polar(megaphoneSize / 2, currentAngle + ((2pi/2)%2pi)).asComplex.asPoint;
			Pen.translate(end.x, end.y);
			// translate along 90 degrees (up)
			up = Polar(megaphoneMicSize / 2, currentAngle + ((2pi/4)%2pi)).asComplex.asPoint;
			Pen.translate(up.x, up.y);
			// work out down and draw line to it
			down = Polar(megaphoneMicSize, currentAngle - ((2pi/4)%2pi)).asComplex.asPoint;
			Pen.line(down);
			Pen.strokeColor_(Color.blue);
			Pen.stroke;
		};
	}

	drawSoundSource {arg xPos, isOnline, isPlaying;
		var position, fillColor;
		fillColor = if (isOnline) {
			if (isPlaying) { playColor } { onlineColor } // play color overrides online color
		} {
			offlineColor
		};
		Pen.use{
			// (val: soundSource.amplitude.linlin(0, 1, 1, 0.2)
			Pen.translate(xPos, 40);
			Pen.fillColor_(fillColor);
			Pen.fillOval(Rect(soundSourceSize/2 * -1, soundSourceSize/2 * -1, soundSourceSize, soundSourceSize));
		}
	}

	/*	makeControlPane {
	var megaphoneControlRows, soundSourceControlRow, megaphoneControlPane;
	megaphoneControlRows = expectedMegaphoneNames.collect{arg megaphoneName;
	this.makeMegaphoneControlRow(megaphoneName);
	};
	expectedSoundSourceNames.collect{arg soundSourceName;
	this.makeSoundSourceControlRow(soundSourceName);
	};
	megaphoneControlPane = View().layout_(
	VLayout(*megaphoneControlRows ++ soundSourceControlRow)
	.spacing_(0)
	.margins_(0)
	);
	^megaphoneControlPane;
	}*/

	/*	makeMegaphoneControlRow {arg megaphoneName;
	var megaphone, megaphoneControlRow;
	megaphone = expectedDevices.at(megaphoneName);
	megaphoneControlRow = View();
	megaphoneControlRow.layout_(HLayout(
	StaticText().string_(megaphoneName),
	Button().states_([["face out"]]).action_({megaphone.dataspace.put(\faceOut)}),
	Button().states_([["face in"]]).action_({megaphone.dataspace.put(\faceIn)}),
	Button().states_([["face next"]]).action_({megaphone.dataspace.put(\faceNext)}),
	Button().states_([["rec"], ["rec", Color.white, Color.red(alpha: 0.2)]]).action_({arg butt;
	case
	{butt.value == 1} { megaphone.dataspace.put(\setRecordState, true) }
	{butt.value == 0} { megaphone.dataspace.put(\setRecordState, false) }
	}),
	Button().states_([["play"], ["play", Color.white, Color.red(alpha: 0.2)]]).action_({arg butt;
	case
	{butt.value == 1} { megaphone.dataspace.put(\setPlaybackState, true) }
	{butt.value == 0} { megaphone.dataspace.put(\setPlaybackState, false) }
	}),
	)
	.spacing_(0)
	.margins_(0)
	);
	^megaphoneControlRow;
	}*/

	/*	makeSoundSourceControlRow {arg soundSourceName;
	var soundSource, soundSourceControlRow;
	soundSource = expectedDevices.at(soundSourceName);
	soundSourceControlRow = View();
	soundSourceControlRow.layout_(HLayout(
	StaticText().string_("sndplyr:"),
	Button().states_([["playSF"], ["playSF", Color.white, Color.red(alpha: 0.2)]]).action_({arg butt;
	var soundSourceID;
	soundSourceID = node.addrBook.atName(soundSourceName).id;
	case
	{butt.value == 1} { soundSource.dataspace(soundSourceID, \setPlaybackState, true, 1) } TODO: replace with real worl value
	{butt.value == 0} { soundSource.dataspace(soundSourceID, \setPlaybackState, false) }
	});
	));
	^soundSourceControlRow;
	}*/

	/*	makeSharedControlPane {
	var megaphoneControlRows, soundSourceControlRows, megaphoneControlPane;
	megaphoneControlRows = expectedMegaphoneNames.collect{arg megaphoneName;
	this.makeMegaphoneSharedControlRow(megaphoneName);
	};
	soundSourceControlRows = expectedSoundSourceNames.collect{arg soundSourceName;
	this.makeSoundSourceSharedControlRow(soundSourceName);
	};
	megaphoneControlPane = View().layout_(
	HLayout(*megaphoneControlRows ++ soundSourceControlRows)
	.spacing_(0)
	.margins_(0)
	);
	^megaphoneControlPane;
	}*/

	/*	makeMegaphoneSharedControlRow {arg megaphoneName;
	var megaphone, rows, sharedControlPane;
	megaphone = expectedDevices.at(megaphoneName);
	defaultBackgroundColor = Color.black;
	megaphoneParamButtonDict = IdentityDictionary.new;
	megaphoneParamFuncArray = [
	[\positionControlledBy, "pos", {
	megaphone.dataspace.put(\takeControlOfPosition);
	}, {
	megaphone.dataspace.put(\relinquishControlOfPosition);
	} ],
	[\recordingControlledBy, "rec", {
	megaphone.dataspace.put(\takeControlOfRecording)
	}, {
	megaphone.dataspace.put(\relinquishControlOfRecording)
	} ],
	[\playbackControlledBy, "play", {
	megaphone.dataspace.put(\takeControlOfPlayback)
	}, {
	megaphone.dataspace.put(\relinquishControlOfPlayback)
	} ],
	[\volumeControlledBy, "vol", {
	megaphone.dataspace.put(\takeControlOfVolume)
	}, {
	megaphone.dataspace.put(\relinquishControlOfVolume)
	} ]
	];
	rows = megaphoneParamFuncArray.collect{arg paramFuncArray;
	var key, paramName, takeControlFunc, relinquishControlFunc;
	# key, paramName, takeControlFunc, relinquishControlFunc = paramFuncArray;
	this.makeMegaphoneParamRow(key, paramName, takeControlFunc, relinquishControlFunc)};
	sharedControlPane = View(nil, Rect(0, 0, 50, 100));
	^sharedControlPane.layout_(VLayout(*rows).spacing_(4).margins_(0));
	}*/

	/*	makeSoundSourceSharedControlRow {arg soundSourceName;
	var soundSource, rows, sharedControlPane;
	soundSource = expectedDevices.at(soundSourceName);
	soundSourceParamButtonDict = IdentityDictionary.new;
	soundSourceParamFuncArray = [
	[\playbackControlledBy, "play", {
	soundSource.dataspace.put(\takeControlOfPlayback)
	}, {
	soundSource.dataspace.put(\relinquishControlOfPlayback)
	} ],
	[\volumeControlledBy, "vol", {
	soundSource.dataspace.put(\takeControlOfVolume)
	}, {
	soundSource.dataspace.put(\relinquishControlOfVolume)
	} ]
	];
	rows = soundSourceParamFuncArray.collect{arg paramFuncArray;
	var key, paramName, takeControlFunc, relinquishControlFunc;
	# key, paramName, takeControlFunc, relinquishControlFunc = paramFuncArray;
	this.makeSoundSourceParamRow(key, paramName, takeControlFunc, relinquishControlFunc)};
	sharedControlPane = View(nil, Rect(0, 0, 50, 100));
	^sharedControlPane.layout_(VLayout(*rows).spacing_(4).margins_(0));
	}*/

	/*	makeMegaphoneParamRow {arg key, paramName, takeControlFunc, relinquishControlFunc;
	var paramButton, rButton, controllingColorView;
	paramButton = Button()
	.fixedSize_(Size(43, 26))
	.states_([[paramName]])
	.action_(takeControlFunc)
	.background_(Color.black); // default background color
	megaphoneParamButtonDict.put(key, paramButton); // hold this for later use
	rButton = Button()
	.fixedSize_(Size(23, 26))
	.states_([["r"]])
	.action_(relinquishControlFunc);
	controllingColorView = View()
	.fixedSize_(Size(43, 26))
	^View().layout_(HLayout(*[paramButton, rButton]).spacing_(0).margins_(0))
	}*/

	/*	makeSoundSourceParamRow {arg key, paramName, takeControlFunc, relinquishControlFunc;
	var paramButton, rButton, controllingColorView;
	paramButton = Button()
	.fixedSize_(Size(43, 26))
	.states_([[paramName]])
	.action_(takeControlFunc)
	.background_(Color.black); // default background color
	soundSourceParamButtonDict.put(key, paramButton); // hold this for later use
	rButton = Button()
	.fixedSize_(Size(23, 26))
	.states_([["r"]])
	.action_(relinquishControlFunc);
	controllingColorView = View()
	.fixedSize_(Size(43, 26))
	^View().layout_(HLayout(*[paramButton, rButton]).spacing_(0).margins_(0))
	}*/

	/*	mapIdToColor {arg id;
	if (id.isNil) {
	^Color.black;
	} {
	^Color.hsv(id, 1, 1); // TODO: need better colour mapping strategy than this!
	}
	}*/

	/*	control {arg key, value;
	var color;
	if (value == \reset) {color = defaultBackgroundColor} { color = this.mapIdToColor(value) };
	paramButtonDict[key].background_(color);
	}*/

}

/*makeGlobalRow {
Button()
.states_([["all"]])
.action({})
}

makeGlobalCol {

}

makeGUI {
gui = View(nil, Rect(0, 0, 500, 200));
gui.layout_(HLayout(*megaphones.collect{arg megaphone; megaphone.gui.mainView}));
gui.front.alwaysOnTop;
}

/*	[\controlledBy, "all", { megaphone.takeControl }, { megaphone.relinquishControl } ],

megaphones.do {

}*/

}*/
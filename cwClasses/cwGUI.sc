CWLaptop {

	// one utopian, many remote objects (megaphones and sound sources)

	var index, <utopian, <megaphones, <soundSources;
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
		//gui = CWGUI();
	}

	doWhenMeAdded {
		// name the laptop (for ease of recongition in the addrBook)
		utopian.node.register('laptop' ++ index);
		// add megaphones:
/*		megaphones = 5.collect{arg index;
			CWSharedRemoteMegaphone(index, utopian.node);
		};*/
		// add sound sources:
		soundSources = 1.collect{arg index;
			CWSharedRemoteSoundSource(index, utopian.node);
		};
		//
		//expectedDevices = megaphones ++ soundSources;
	}

}

/*

CWGUI {
	// draw the expected number of megaphones
	// have a colour for online, color for offline

	var <node;
	var expectedDevices, expectedDevicePositions;
	var onlineColor, offlineColor, controlledColor;
	var displayPaneSize, controlPaneSize;
	// control pane
	var megaphoneParamFuncArray, <megaphoneParamButtonDict;
	var soundSourceParamFuncArray, <soundSourceParamButtonDict;
	var defaultBackgroundColor;

	*new {
		^super.new.init;
	}

	initGUI {
		onlineColor = Color.green;
		offlineColor = Color.yellow;
		controlledColor = Color.blue;
		displayPaneSize = Size(400, 400);
		controlPaneSize = Size(400, 400);
		expectedDevices.keysValuesDo{arg key, value; value.put(\color, offlineColor); };
		// init default object colors (offline)
		expectedDevicePositions = ( // between 0 and 1
			soundSource0: 0.1,
			megaphone0: 0.23333333333333,
			megaphone1: 0.36666666666667,
			megaphone2: 0.5,
			megaphone3: 0.63333333333333,
			megaphone4: 0.76666666666667,
			soundSource1: 0.9
		);
		expectedDevicePositions.keysValuesDo{arg key, value;
			var newValue;
			newValue = value * displayPaneSize.width;
			expectedDevicePositions.put(key, newValue);
		};
		this.makeGUI;
	}

	makeGUI {
		var gui, displayPane, controlPane, sharedControlPane;
		gui = View(nil, Rect(0, 0, 1200, 400));
		displayPane = this.makeDeviceDisplayPane.fixedSize_(displayPaneSize);
		//controlPane = this.makeControlPane.fixedSize_(200, 200);
		//sharedControlPane = this.makeSharedControlPane.fixedSize_(400, 100);
		//controlPane = View().fixedSize_(controlPaneSize).background_(Color.red);
		gui.layout_(HLayout(*[displayPane, controlPane, sharedControlPane])
			.spacing_(0)
			.margins_(0)
		);
		gui.front;
		^gui;
	}

	makeDeviceDisplayPane {
		var deviceDisplayPane;
		deviceDisplayPane = UserView();
		deviceDisplayPane.drawFunc_({this.deviceDisplayPaneDrawFunc});
		deviceDisplayPane.animate_(true);
		^deviceDisplayPane;
	}

	deviceDisplayPaneDrawFunc {
		// first, translate to center:
		// Pen.translate(size/2, deviceDisplayPane.bounds.height/2);
/*		expectedSoundSourceNames.do{arg soundSourceName; this.drawSoundSource(soundSourceName);};
		expectedMegaphoneNames.do{arg megaphoneName; this.drawMegaphone(megaphoneName);};*/
	}

	drawMegaphone {arg megaphoneName;
		var megaphoneSize, megaphoneLength, micSize, hornSize;
		megaphoneSize = displayPaneSize.width/10;
		megaphoneLength = megaphoneSize/5;
		micSize = megaphoneSize/20;
		hornSize = megaphoneSize/10;
		Pen.use{
			var megaphone, mag, ang, start, end, strokeColor;
			megaphone = expectedDevices.at(megaphoneName);
			mag = megaphoneLength/0.9;
			ang = 0;
			Pen.translate(megaphone.position.x, megaphone.position.y);
			// begin to draw
			// #
			Pen.fillColor_(megaphone.color); Pen.fillOval(Rect(-2, -2, 4, 4));
			/*			Pen.use {
			var up, down;
			up = Polar(megaphoneSize, (ang - (2pi/2))%2pi).asComplex.asPoint;
			// translate along 90 degrees
			Pen.translate(up.x, up.y);
			// work out down
			down = Polar(megaphoneSize * 2, ang).asComplex.asPoint;
			Pen.line(down); Pen.strokeColor_(Color.grey(alpha: 0.3)); Pen.stroke;
			};
			// translate to start:
			start = Polar(megaphoneLength/2, megaphone.currentAngle).asComplex.asPoint;
			Pen.translate(start.x, start.y);
			Pen.use {
			var up, down;
			up = Polar(hornSize/2, megaphone.currentAngle + ((2pi/4)%2pi)).asComplex.asPoint;
			// translate along 90 degrees
			Pen.translate(up.x, up.y);
			// work out down
			down = Polar(hornSize, megaphone.currentAngle - ((2pi/4)%2pi)).asComplex.asPoint;
			Pen.line(down);
			Pen.strokeColor_(Color.blue);
			Pen.stroke;
			};
			// work out end:
			end = Polar(megaphoneLength, megaphone.currentAngle + ((2pi/2)%2pi)).asComplex.asPoint;
			// draw line from current position to end:
			Pen.line(end);
			Pen.strokeColor_(Color.black);
			if (megaphone.isRecording) {
			strokeColor = Color.red
			} {
			if (megaphone.isPlaying) {
			strokeColor = Color.green
			} {
			strokeColor = Color.grey;
			}
			};
			Pen.strokeColor_(strokeColor);
			Pen.stroke;
			// translate to end:
			Pen.translate(end.x, end.y);
			Pen.use {
			var up, down;
			up = Polar(micSize/2, megaphone.currentAngle + ((2pi/4)%2pi)).asComplex.asPoint;
			// translate along 90 degrees
			Pen.translate(up.x, up.y);
			// work out down
			down = Polar(micSize, megaphone.currentAngle - ((2pi/4)%2pi)).asComplex.asPoint;
			// red if recording, green if playing, black if neither
			Pen.line(down);
			Pen.strokeColor_(Color.blue);
			Pen.stroke;
			};*/
		};
	}

	// drawSoundSource {arg soundSourceName;
	// 	var position, fillColor;
	// 	var soundSourceSize, soundSource;
	// 	soundSourceSize = displayPaneSize.width/10;
	// 	soundSource = expectedDevices.at(soundSourceName);
	// 	Pen.use{
	// 		var fillColor;
	// 		fillColor = soundSource.color;
	// 		// (val: soundSource.amplitude.linlin(0, 1, 1, 0.2)
	// 		Pen.translate(soundSource.position.x, soundSource.position.y);
	// 		Pen.fillColor_(fillColor);
	// 		Pen.fillOval(Rect(soundSourceSize/2 * -1, soundSourceSize/2 * -1, soundSourceSize, soundSourceSize));
	// 	}
	// }
	//
	// makeControlPane {
	// 	var megaphoneControlRows, soundSourceControlRow, megaphoneControlPane;
	// 	megaphoneControlRows = expectedMegaphoneNames.collect{arg megaphoneName;
	// 		this.makeMegaphoneControlRow(megaphoneName);
	// 	};
	// 	expectedSoundSourceNames.collect{arg soundSourceName;
	// 		this.makeSoundSourceControlRow(soundSourceName);
	// 	};
	// 	megaphoneControlPane = View().layout_(
	// 		VLayout(*megaphoneControlRows ++ soundSourceControlRow)
	// 		.spacing_(0)
	// 		.margins_(0)
	// 	);
	// 	^megaphoneControlPane;
	// }
	//
	// makeMegaphoneControlRow {arg megaphoneName;
	// 	var megaphone, megaphoneControlRow;
	// 	megaphone = expectedDevices.at(megaphoneName);
	// 	megaphoneControlRow = View();
	// 	megaphoneControlRow.layout_(HLayout(
	// 		StaticText().string_(megaphoneName),
	// 		Button().states_([["face out"]]).action_({megaphone.dataspace.put(\faceOut)}),
	// 		Button().states_([["face in"]]).action_({megaphone.dataspace.put(\faceIn)}),
	// 		Button().states_([["face next"]]).action_({megaphone.dataspace.put(\faceNext)}),
	// 		Button().states_([["rec"], ["rec", Color.white, Color.red(alpha: 0.2)]]).action_({arg butt;
	// 			case
	// 			{butt.value == 1} { megaphone.dataspace.put(\setRecordState, true) }
	// 			{butt.value == 0} { megaphone.dataspace.put(\setRecordState, false) }
	// 		}),
	// 		Button().states_([["play"], ["play", Color.white, Color.red(alpha: 0.2)]]).action_({arg butt;
	// 			case
	// 			{butt.value == 1} { megaphone.dataspace.put(\setPlaybackState, true) }
	// 			{butt.value == 0} { megaphone.dataspace.put(\setPlaybackState, false) }
	// 		}),
	// 		)
	// 		.spacing_(0)
	// 		.margins_(0)
	// 	);
	// 	^megaphoneControlRow;
	// }
	//
	// makeSoundSourceControlRow {arg soundSourceName;
	// 	var soundSource, soundSourceControlRow;
	// 	soundSource = expectedDevices.at(soundSourceName);
	// 	soundSourceControlRow = View();
	// 	soundSourceControlRow.layout_(HLayout(
	// 		StaticText().string_("sndplyr:"),
	// 		Button().states_([["playSF"], ["playSF", Color.white, Color.red(alpha: 0.2)]]).action_({arg butt;
	// 			var soundSourceID;
	// 			soundSourceID = node.addrBook.atName(soundSourceName).id;
	// 			case
	// 			{butt.value == 1} { soundSource.dataspace(soundSourceID, \setPlaybackState, true, 1) } // TODO: replace with real worl value
	// 			{butt.value == 0} { soundSource.dataspace(soundSourceID, \setPlaybackState, false) }
	// 		});
	// 	));
	// 	^soundSourceControlRow;
	// }
	//
	// makeSharedControlPane {
	// 	var megaphoneControlRows, soundSourceControlRows, megaphoneControlPane;
	// 	megaphoneControlRows = expectedMegaphoneNames.collect{arg megaphoneName;
	// 		this.makeMegaphoneSharedControlRow(megaphoneName);
	// 	};
	// 	soundSourceControlRows = expectedSoundSourceNames.collect{arg soundSourceName;
	// 		this.makeSoundSourceSharedControlRow(soundSourceName);
	// 	};
	// 	megaphoneControlPane = View().layout_(
	// 		HLayout(*megaphoneControlRows ++ soundSourceControlRows)
	// 		.spacing_(0)
	// 		.margins_(0)
	// 	);
	// 	^megaphoneControlPane;
	// }
	//
	// makeMegaphoneSharedControlRow {arg megaphoneName;
	// 	var megaphone, rows, sharedControlPane;
	// 	megaphone = expectedDevices.at(megaphoneName);
	// 	defaultBackgroundColor = Color.black;
	// 	megaphoneParamButtonDict = IdentityDictionary.new;
	// 	megaphoneParamFuncArray = [
	// 		[\positionControlledBy, "pos", {
	// 			megaphone.dataspace.put(\takeControlOfPosition);
	// 			}, {
	// 				megaphone.dataspace.put(\relinquishControlOfPosition);
	// 		} ],
	// 		[\recordingControlledBy, "rec", {
	// 			megaphone.dataspace.put(\takeControlOfRecording)
	// 			}, {
	// 				megaphone.dataspace.put(\relinquishControlOfRecording)
	// 		} ],
	// 		[\playbackControlledBy, "play", {
	// 			megaphone.dataspace.put(\takeControlOfPlayback)
	// 			}, {
	// 				megaphone.dataspace.put(\relinquishControlOfPlayback)
	// 		} ],
	// 		[\volumeControlledBy, "vol", {
	// 			megaphone.dataspace.put(\takeControlOfVolume)
	// 			}, {
	// 				megaphone.dataspace.put(\relinquishControlOfVolume)
	// 		} ]
	// 	];
	// 	rows = megaphoneParamFuncArray.collect{arg paramFuncArray;
	// 		var key, paramName, takeControlFunc, relinquishControlFunc;
	// 		# key, paramName, takeControlFunc, relinquishControlFunc = paramFuncArray;
	// 	this.makeMegaphoneParamRow(key, paramName, takeControlFunc, relinquishControlFunc)};
	// 	sharedControlPane = View(nil, Rect(0, 0, 50, 100));
	// 	^sharedControlPane.layout_(VLayout(*rows).spacing_(4).margins_(0));
	// }
	//
	// makeSoundSourceSharedControlRow {arg soundSourceName;
	// 	var soundSource, rows, sharedControlPane;
	// 	soundSource = expectedDevices.at(soundSourceName);
	// 	soundSourceParamButtonDict = IdentityDictionary.new;
	// 	soundSourceParamFuncArray = [
	// 		[\playbackControlledBy, "play", {
	// 			soundSource.dataspace.put(\takeControlOfPlayback)
	// 			}, {
	// 				soundSource.dataspace.put(\relinquishControlOfPlayback)
	// 		} ],
	// 		[\volumeControlledBy, "vol", {
	// 			soundSource.dataspace.put(\takeControlOfVolume)
	// 			}, {
	// 				soundSource.dataspace.put(\relinquishControlOfVolume)
	// 		} ]
	// 	];
	// 	rows = soundSourceParamFuncArray.collect{arg paramFuncArray;
	// 		var key, paramName, takeControlFunc, relinquishControlFunc;
	// 		# key, paramName, takeControlFunc, relinquishControlFunc = paramFuncArray;
	// 	this.makeSoundSourceParamRow(key, paramName, takeControlFunc, relinquishControlFunc)};
	// 	sharedControlPane = View(nil, Rect(0, 0, 50, 100));
	// 	^sharedControlPane.layout_(VLayout(*rows).spacing_(4).margins_(0));
	// }
	//
	// makeMegaphoneParamRow {arg key, paramName, takeControlFunc, relinquishControlFunc;
	// 	var paramButton, rButton, controllingColorView;
	// 	paramButton = Button()
	// 	.fixedSize_(Size(43, 26))
	// 	.states_([[paramName]])
	// 	.action_(takeControlFunc)
	// 	.background_(Color.black); // default background color
	// 	megaphoneParamButtonDict.put(key, paramButton); // hold this for later use
	// 	rButton = Button()
	// 	.fixedSize_(Size(23, 26))
	// 	.states_([["r"]])
	// 	.action_(relinquishControlFunc);
	// 	controllingColorView = View()
	// 	.fixedSize_(Size(43, 26))
	// 	^View().layout_(HLayout(*[paramButton, rButton]).spacing_(0).margins_(0))
	// }
	//
	// makeSoundSourceParamRow {arg key, paramName, takeControlFunc, relinquishControlFunc;
	// 	var paramButton, rButton, controllingColorView;
	// 	paramButton = Button()
	// 	.fixedSize_(Size(43, 26))
	// 	.states_([[paramName]])
	// 	.action_(takeControlFunc)
	// 	.background_(Color.black); // default background color
	// 	soundSourceParamButtonDict.put(key, paramButton); // hold this for later use
	// 	rButton = Button()
	// 	.fixedSize_(Size(23, 26))
	// 	.states_([["r"]])
	// 	.action_(relinquishControlFunc);
	// 	controllingColorView = View()
	// 	.fixedSize_(Size(43, 26))
	// 	^View().layout_(HLayout(*[paramButton, rButton]).spacing_(0).margins_(0))
	// }
	//
	//
	// mapIdToColor {arg id;
	// 	if (id.isNil) {
	// 		^Color.black;
	// 	} {
	// 		^Color.hsv(id, 1, 1); // TODO: need better colour mapping strategy than this!
	// 	}
	// }

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

*/
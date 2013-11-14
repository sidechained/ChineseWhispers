CWLaptop {

	// one utopian, many remote objects (megaphones and sound sources)

	var index, pathToSoundFiles;
	var <name, <utopian, localSoundSource, <remoteMegaphones, <remoteSoundSources, <remoteLaptops, <gui;

	*new {arg index, pathToSoundFiles;
		^super.newCopyArgs(index, pathToSoundFiles).init;
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
		name = 'laptop' ++ index;
		utopian.node.register(name);
		// add local sound source:
		localSoundSource = CWLocalSoundSource(index, pathToSoundFiles); // use same index as laptop here
		// add megaphones:
		remoteMegaphones = 5.collect{arg index;
			CWRemoteMegaphone(index, utopian.node);
		};
		// add sound sources:
		remoteSoundSources = 2.collect{arg index;
			CWRemoteSoundSource(index, utopian.node);
		};
		// add laptops:
		remoteLaptops = 2.collect{arg index;
			CWRemoteLaptop(index, utopian.node);
		};
		gui = CWGUI(remoteMegaphones, remoteSoundSources, remoteLaptops, index);
	}

}


CWRemoteLaptop {

	var <index, <node, <name, <dataspace;

	*new {arg index, node;
		^super.newCopyArgs(index, node).initRemoteLaptop;
	}

	initRemoteLaptop {
		// do only once this node is online
		var oscPath;
		name = ('laptop' ++ index).asSymbol;
	}

	isOnline {
		^node.addrBook.atName(name) !? { node.addrBook.atName(name).online } ?? { false }
	}
}

CWGUI {
	// draw the expected number of megaphones
	// have a colour for online, color for offline
	var remoteMegaphones, remoteSoundSources, remoteLaptops, thisLaptopIndex;
	var <node;
	var devicePositionDict;
	var laptopColors, onlineColor, offlineColor, playColor, recColor, stopColor;
	var guiSize, displayPaneSize, controlPaneSize, sharedControlPaneSize;
	var soundSourceSize, megaphoneSize, megaphoneLength, megaphoneMicSize, megaphoneHornSize, laptopSize;
	var megaphoneParamFuncArray, <megaphoneParamButtonDict;
	var soundSourceParamFuncArray, <soundSourceParamButtonDict;
	var defaultBackgroundColor;

	*new {arg remoteMegaphones, remoteSoundSources, remoteLaptops, thisLaptopIndex;
		^super.newCopyArgs(remoteMegaphones, remoteSoundSources, remoteLaptops, thisLaptopIndex).init;
	}

	init {
		laptopColors = [Color.magenta, Color.cyan];
		onlineColor = Color.blue;
		offlineColor = Color.grey;
		playColor = Color.green;
		recColor = Color.red;
		stopColor = Color.black;
		guiSize = Size(1024, 300);
		displayPaneSize = Size(guiSize.width/2, guiSize.height);
		controlPaneSize = Size(guiSize.width/2, guiSize.height);
		//sharedControlPaneSize = Size(guiSize.width/3, guiSize.height);
		soundSourceSize = displayPaneSize.width/10;
		megaphoneSize = displayPaneSize.width/10;
		megaphoneMicSize = megaphoneSize/10;
		megaphoneHornSize = megaphoneSize/2.5;
		laptopSize = displayPaneSize.width/10;
		devicePositionDict = ( // between 0 and 1
			soundSource0: 0.1@0.2,
			megaphone0: 0.23333333333333@0.2,
			megaphone1: 0.36666666666667@0.2,
			megaphone2: 0.5@0.2,
			megaphone3: 0.63333333333333@0.2,
			megaphone4: 0.76666666666667@0.2,
			soundSource1: 0.9@0.2,
			laptop0: 0.25@0.8,
			laptop1: 0.75@0.8
		);
		defer { this.makeGUI };
	}

	makeGUI {
		var gui, displayPane, controlPane, sharedControlPane, yPos;
		yPos = thisLaptopIndex * guiSize.height;
		gui = View(nil, Rect(0, yPos, guiSize.width, guiSize.height));
		displayPane = this.makeDeviceDisplayPane.fixedSize_(displayPaneSize);
		controlPane = this.makeControlPane.fixedSize_(controlPaneSize).background_(Color.yellow(alpha:0.2));
		//sharedControlPane = this.makeSharedControlPane.fixedSize_(sharedControlPaneSize);
		gui.layout_(HLayout(*[displayPane, controlPane])
			.spacing_(0)
			.margins_(0)
		);
		gui.front;
		^gui;
	}

	// device pane:
	makeDeviceDisplayPane {
		var deviceDisplayPane;
		deviceDisplayPane = UserView();
		deviceDisplayPane.drawFunc_({
			// first, translate to center:
			// Pen.translate(size/2, deviceDisplayPane.bounds.height/2);
			// 1. draw megaphones:
			remoteMegaphones.do{arg remoteMegaphone;
				var xPos, yPos, isOnline, isPlaying, isRecording, currentAngle;
				xPos = devicePositionDict.at(remoteMegaphone.name).x.linlin(0, 1, 0, deviceDisplayPane.bounds.width);
				yPos = devicePositionDict.at(remoteMegaphone.name).y.linlin(0, 1, 0, deviceDisplayPane.bounds.width);
				isOnline = remoteMegaphone.isOnline;
				isRecording = remoteMegaphone.isRecording;
				isPlaying = remoteMegaphone.isPlaying;
				currentAngle = remoteMegaphone.currentAngle;
				currentAngle = currentAngle * (2pi/2) / 180; // degrees to radians
				this.drawMegaphone(xPos, yPos, isOnline, isRecording, isPlaying, currentAngle);
			};
			// 2. draw sound sources:
			remoteSoundSources.do{arg remoteSoundSource;
				var xPos, yPos, isOnline, isPlaying, amplitude, soundSourceIndex;
				xPos = devicePositionDict.at(remoteSoundSource.name).x.linlin(0, 1, 0, deviceDisplayPane.bounds.width);
				yPos = devicePositionDict.at(remoteSoundSource.name).y.linlin(0, 1, 0, deviceDisplayPane.bounds.height);
				isOnline = remoteSoundSource.isOnline;
				isPlaying = remoteSoundSource.isPlaying;
				//amplitude = remoteSoundSource.amplitude; // ignoring amp for now, only relevant for simulation
				soundSourceIndex = remoteSoundSource.index;
				this.drawSoundSource(xPos, yPos, isOnline, isPlaying, soundSourceIndex);
			};
			// 3. draw laptops
			remoteLaptops.do{arg remoteLaptop;
				var xPos, yPos, isOnline, laptopIndex;
				xPos = devicePositionDict.at(remoteLaptop.name).x.linlin(0, 1, 0, deviceDisplayPane.bounds.width);
				yPos = devicePositionDict.at(remoteLaptop.name).y.linlin(0, 1, 0, deviceDisplayPane.bounds.height);
				isOnline = remoteLaptop.isOnline;
				laptopIndex = remoteLaptop.index;
				this.drawLaptop(xPos, yPos, isOnline, laptopIndex);
			}
		});
		deviceDisplayPane.background_(Color.magenta(alpha:0.1));
		deviceDisplayPane.animate_(true);
		^deviceDisplayPane;
	}

	drawMegaphone {arg xPos, yPos, isOnline, isRecording, isPlaying, currentAngle; // isTurning?
		Pen.use{
			var start, end, onlineRecColor;
			// mag = 5;
			onlineRecColor = if (isOnline) {
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
			this.drawCentreBlob(xPos, yPos, onlineRecColor);
			// draw centre blob (mic stand)
			this.drawBody(xPos, yPos, currentAngle);
			this.drawHorn(xPos, yPos, currentAngle);
			this.drawMic(xPos, yPos, currentAngle);
		}
	}

	drawCentreBlob {arg x, y, onlineRecColor;
		var blobSize = 8;
		Pen.use{
			var ovalRect = Rect(blobSize/2 * -1, blobSize/2 * -1, blobSize, blobSize);
			Pen.translate(x, y);
			Pen.fillColor_(onlineRecColor);
			Pen.fillOval(ovalRect);
			// Pen.width_(2);
			// Pen.strokeColor_(laptopColors[controllingPlayerIndex]);
			// Pen.strokeOval(ovalRect);
			// Pen.stroke;
		}
	}

	drawBody {arg x, y, currentAngle;
		Pen.use {
			var up, down;
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

		Pen.use {
			var end, up, down;
			Pen.translate(x, y);
			// translate to end:
			end = Polar(megaphoneSize / 2, currentAngle + ((2pi/2)%2pi)).asComplex.asPoint;
			Pen.translate(end.x, end.y);
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
			var start, up, down;
			Pen.translate(x, y);
			// translate to start:
			start = Polar(megaphoneSize / 2, currentAngle).asComplex.asPoint;
			Pen.translate(start.x, start.y);
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

	drawSoundSource {arg xPos, yPos, isOnline, isPlaying, soundSourceIndex;
		var position, fillColor;
		fillColor = if (isOnline) {
			if (isPlaying) { playColor } { onlineColor } // play color overrides online color
		} {
			offlineColor
		};
		Pen.use{
			var ovalRect;
			ovalRect = Rect(soundSourceSize/2 * -1, soundSourceSize/2 * -1, soundSourceSize, soundSourceSize);
			Pen.translate(xPos, yPos);
			Pen.fillColor_(fillColor);
			Pen.fillOval(ovalRect);
			Pen.width_(4);
			Pen.strokeColor_(laptopColors[soundSourceIndex]);
			Pen.strokeOval(ovalRect);
			Pen.stroke;
		}
	}

	drawLaptop {arg xPos, yPos, isOnline, laptopIndex;
		var position, fillColor;
		fillColor = if (isOnline) {
			onlineColor
		} {
			offlineColor
		};
		Pen.use{
			// (val: soundSource.amplitude.linlin(0, 1, 1, 0.2)
			var rectRect;
			rectRect = Rect(laptopSize/2 * -1, laptopSize/2 * -1, laptopSize, laptopSize);
			Pen.translate(xPos, yPos);
			Pen.fillColor_(fillColor);
			Pen.fillRect(rectRect);
			Pen.width_(4);
			Pen.strokeColor_(laptopColors[laptopIndex]);
			Pen.strokeRect(rectRect);
			Pen.stroke;
		}
	}

	makeControlPane {
		var megaphoneControlRows, soundSourceControlRows, megaphoneControlPane;
		megaphoneControlRows = remoteMegaphones.collect{arg megaphone;
			this.makeMegaphoneControlRow(megaphone);
		};
		soundSourceControlRows = remoteSoundSources.collect{arg soundSource;
			this.makeSoundSourceControlRow(soundSource);
		};
		megaphoneControlPane = View().layout_(
			VLayout(*megaphoneControlRows ++ soundSourceControlRows)
			.spacing_(0)
			.margins_(0)
		);
		^megaphoneControlPane;
	}

	makeMegaphoneControlRow {arg megaphone;
		var megaphoneControlRow;
		megaphoneControlRow = View().layout_(HLayout(
			StaticText().string_(megaphone.name),
			Slider()
			.action_( {arg box; megaphone.setPosition(box.value * 180) } )
			.minWidth_(70)
			.maxHeight_(25)
			.orientation_(\horizontal),
			Button()
			.states_([["rec", Color.black, Color.red(alpha:0.1)], ["rec", Color.black, Color.green(alpha:0.1)]])
			.action_({arg butt; this.megaphoneToggleRecord(butt.value, megaphone) }
			),
			Button()
			.states_([["play", Color.black, Color.red(alpha:0.1)], ["play", Color.black, Color.green(alpha:0.1)]])
			.action_( {arg butt; this.megaphoneTogglePlay(butt.value, megaphone) }),
			Slider()
			.action_( {arg box; megaphone.setPlayVolume(box.value) } )
			.minWidth_(70)
			.maxHeight_(25)
			.orientation_(\horizontal),
		)
		.spacing_(0)
		.margins_(0)
		);
		^megaphoneControlRow;
	}

	megaphoneToggleRecord {arg buttValue, megaphone;
		case
		{buttValue == 1} { megaphone.startRecording }
		{buttValue == 0} { megaphone.stopRecording }
	}

	megaphoneTogglePlay {arg buttValue, megaphone;
		case
		{buttValue == 1} { megaphone.startPlaying }
		{buttValue == 0} { megaphone.stopPlaying }
	}

	makeSoundSourceControlRow {arg soundSource;
		var soundSourceControlRow, bufferNumber = 0;
		soundSourceControlRow = View().layout_(HLayout(
			StaticText().string_(soundSource.name),
			Button()
			.states_([["play", Color.black, Color.red(alpha:0.1)], ["play", Color.black, Color.green(alpha:0.1)]])
			.action_({arg butt; this.soundSourceTogglePlay(soundSource, butt.value, bufferNumber)}),
			NumberBox()
			.action_( {arg box; bufferNumber = box.value } ),
			Slider()
			.action_( {arg box; soundSource.setPlayVolume(box.value) } )
			.minWidth_(70)
			.maxHeight_(25)
			.orientation_(\horizontal),
		));
		^soundSourceControlRow;
	}

	soundSourceTogglePlay {arg soundSource, buttValue, bufferNumber;
		bufferNumber;
		case
		{buttValue == 1} { soundSource.startPlaying(bufferNumber) }  // enforce level?
		{buttValue == 0} { soundSource.stopPlaying }
	}

	makeSharedControlPane {
		var megaphoneControlRows, soundSourceControlRows, megaphoneControlPane;
		megaphoneControlRows = remoteMegaphones.collect{arg remoteMegaphone;
			this.makeMegaphoneSharedControlRow(remoteMegaphone);
		};
		soundSourceControlRows = remoteSoundSources.collect{arg remoteSoundSource;
			this.makeSoundSourceSharedControlRow(remoteSoundSource);
		};
		megaphoneControlPane = View().layout_(
			VLayout(*megaphoneControlRows ++ soundSourceControlRows)
			.spacing_(0)
			.margins_(0)
		);
		^megaphoneControlPane;
	}

	makeMegaphoneSharedControlRow {arg megaphone;
		var rows, sharedControlPane;
		megaphoneParamFuncArray = [
			[
				\positionControlledBy,
				"pos",
				{ megaphone.takeControlOfPosition },
				{ megaphone.relinquishControlOfPosition }
			],
			[
				\recordingControlledBy,
				"rec",
				{ megaphone.takeControlOfRecording },
				{ megaphone.relinquishControlOfRecording }
			],
			[
				\playbackControlledBy,
				"play",
				{ megaphone.takeControlOfPlayback },
				{ megaphone.relinquishControlOfPlayback }
			],
			[
				\volumeControlledBy,
				"vol",
				{ megaphone.takeControlOfVolume },
				{ megaphone.relinquishControlOfPlayVolume }
			]
		];
		rows = megaphoneParamFuncArray.collect{arg paramFuncArray;
			var key, paramName, takeControlFunc, relinquishControlFunc;
			# key, paramName, takeControlFunc, relinquishControlFunc = paramFuncArray;
			this.makeMegaphoneParamRow(key, paramName, takeControlFunc, relinquishControlFunc)};
		sharedControlPane = View(nil, Rect(0, 0, 50, 100));
		^sharedControlPane.layout_(HLayout(*rows).spacing_(4).margins_(0));
	}

	makeMegaphoneParamRow {arg key, paramName, takeControlFunc, relinquishControlFunc;
		var paramButton, rButton, controllingColorView;
		paramButton = Button()
		.fixedSize_(Size(43, 26))
		.states_([[paramName]])
		.action_(takeControlFunc)
		.background_(Color.black); // default background color
		rButton = Button()
		.fixedSize_(Size(23, 26))
		.states_([["r"]])
		.action_(relinquishControlFunc);
		controllingColorView = View()
		.fixedSize_(Size(43, 26))
		^View().layout_(HLayout(*[paramButton, rButton]).spacing_(0).margins_(0))
	}

	makeSoundSourceSharedControlRow {arg soundSource;
		var rows, sharedControlPane;
		soundSourceParamButtonDict = IdentityDictionary.new;
		soundSourceParamFuncArray = [
			[
				\playbackControlledBy,
				"play",
				{ soundSource.takeControlOfPlayback },
				{ soundSource.relinquishControlOfPlayback }
			],
			[
				\volumeControlledBy,
				"vol",
				{ soundSource.takeControlOfVolume },
				{ soundSource.relinquishControlOfPlayVolume }
			]
		];
		rows = soundSourceParamFuncArray.collect{arg paramFuncArray;
			var key, paramName, takeControlFunc, relinquishControlFunc;
			# key, paramName, takeControlFunc, relinquishControlFunc = paramFuncArray;
			this.makeSoundSourceParamRow(key, paramName, takeControlFunc, relinquishControlFunc)};
		sharedControlPane = View(nil, Rect(0, 0, 50, 100));
		^sharedControlPane.layout_(HLayout(*rows).spacing_(4).margins_(0));
	}

	makeSoundSourceParamRow {arg key, paramName, takeControlFunc, relinquishControlFunc;
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
	}

}
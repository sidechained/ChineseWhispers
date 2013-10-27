CWGUI {

	var megaphones, soundSource;

	*new {arg megaphones, soundSource;
		^super.newCopyArgs(megaphones, soundSource).init;
	}

	init {
		this.makeGUI;
	}

	makeGUI {
		var gui, displayPane, controlPane;
		gui = View(nil, Rect(0, 0, 800, 400));
		displayPane = this.makeMegaphoneDisplayPane(400).fixedSize_(400, 400);
		controlPane = this.makeMegaphoneControlPane.fixedSize_(400, 400);
		gui.layout_(HLayout(*[displayPane, controlPane])
			.spacing_(0)
			.margins_(0)
		);
		gui.front;
		^gui;
	}

	makeMegaphoneDisplayPane {arg size;
		var megaphoneDisplayPane, megaphoneLength, micSize, hornSize;
		megaphoneDisplayPane = UserView();
		megaphoneLength = size/5;
		micSize = size/20;
		hornSize = size/10;
		megaphoneDisplayPane.drawFunc_({
			// first, translate to center:
			Pen.translate(size/2, megaphoneDisplayPane.bounds.height/2);
			this.drawSoundSource(size);
			megaphones.do{arg megaphone, megaphoneIndex;
				Pen.use{
					var mag, ang;
					var mountPoint, start, end;
					var strokeColor;
					mag = megaphoneLength/0.9;
					ang = 2pi/megaphones.size * megaphoneIndex;
					mountPoint = Polar(mag, ang).asComplex.asPoint;
					Pen.translate(mountPoint.x, mountPoint.y);
					Pen.fillColor_(Color.red); Pen.fillOval(Rect(-2, -2, 4, 4));
					Pen.use {
						var up, down;
						up = Polar(size, (ang - (2pi/2))%2pi).asComplex.asPoint;
						// translate along 90 degrees
						Pen.translate(up.x, up.y);
						// work out down
						down = Polar(size * 2, ang).asComplex.asPoint;
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
					};
				};
			}
		});
		megaphoneDisplayPane.animate_(true);
		^megaphoneDisplayPane;
	}

	drawSoundSource {arg size;
		var soundFilePlayerSize, fillColor;
		soundFilePlayerSize = size/10;
		if (soundSource.isPlaying) { fillColor = Color.green(val: soundSource.amplitude.linlin(0, 1, 1, 0.2)); } { fillColor = Color.grey };
		Pen.fillColor_(fillColor);
		Pen.fillOval(Rect(soundFilePlayerSize/2 * -1, soundFilePlayerSize/2 * -1, soundFilePlayerSize, soundFilePlayerSize));
	}

	makeMegaphoneControlPane {
		var megaphoneControlRows, soundSourceControlRow, megaphoneControlPane;
		megaphoneControlRows = megaphones.collect{arg megaphone, megaphoneIndex;
			this.makeMegaphoneControlRow(megaphone, megaphoneIndex);
		};
		soundSourceControlRow = this.makeSoundSourceControlRow;
		megaphoneControlPane = View().layout_(
			VLayout(*megaphoneControlRows ++ soundSourceControlRow)
			.spacing_(0)
			.margins_(0)
		);
		^megaphoneControlPane;
	}

	makeMegaphoneControlRow {arg megaphone, megaphoneIndex;
		var megaphoneControlRow;
		megaphoneControlRow = View();
		megaphoneControlRow.layout_(HLayout(
			StaticText().string_("mega%:".format(megaphoneIndex)),
			Button().states_([["face out"]]).action_({megaphone.faceOut}),
			Button().states_([["face in"]]).action_({megaphone.faceIn;}),
			Button().states_([["face next"]]).action_({megaphone.faceNext;}),
			Button().states_([["rec"], ["rec", Color.white, Color.red(alpha: 0.2)]]).action_({arg butt;
				case
				{butt.value == 1} { megaphone.startRecording }
				{butt.value == 0} { megaphone.stopRecording }
			}),
			Button().states_([["play"], ["play", Color.white, Color.red(alpha: 0.2)]]).action_({arg butt;
				case
				{butt.value == 1} { megaphone.startPlaying }
				{butt.value == 0} { megaphone.stopPlaying }
			}),
		)
		.spacing_(0)
		.margins_(0)
		);
		^megaphoneControlRow;
	}

	makeSoundSourceControlRow {

		var soundSourceControlRow;
		soundSourceControlRow = View();
		soundSourceControlRow.layout_(HLayout(
			StaticText().string_("sndplyr:"),
			Button().states_([["playSF"], ["playSF", Color.white, Color.red(alpha: 0.2)]]).action_({arg butt;
				case
				{butt.value == 1} { soundSource.startPlaying(soundSource.buffers.choose) }
				{butt.value == 0} { soundSource.stopPlaying }
			});
		));
		^soundSourceControlRow;
	}

}
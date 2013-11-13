l = CWLaptop(0, "/Users/grahambooth/Desktop/Megaphone/beagleboard_soundfiles_final/soundfiles/");
l.utopian.node.addrBook.peers.printAll;"";
l.remoteMegaphones
(
Tdef(\player, {
	15.do{
		var which = l.remoteMegaphones[0];
		which.startPlaying;
		which.setPlayVolume(0.2);
		2.0.rand.wait;
		which.stopPlaying;
	}

}).play;
);


(
Tdef(\recorder, {
	l.remoteMegaphones.do{ |which|
		which.startRecording;
		2.0.rand.wait;
		which.stopRecording;
		1.0.rand.wait;
	};

	10.do{
		l.remoteMegaphones.choose.startPlaying.setPlayVolume(0.5.rand);
		1.wait;
	};

	100.do{
		l.remoteMegaphones.choose.setPlayVolume(0.5.rand);
		0.1.wait;
	};

	l.remoteMegaphones.do{ |which|
		which.setPlayVolume(0.01);
		2.0.rand.wait;
		which.stopPlaying;
		0.5.rand.wait;
	};

}).play;
)
(0..10)

(
Tdef(\leftToRight, {
	l.remoteMegaphones.do{arg remoteMegaphone; remoteMegaphone.setPosition(160)};
	0.5.wait;
	6.do{arg i;
		i.postln;
		if (i == 0) {
			l.remoteSoundSources[0].startPlaying((0..10).choose, 1);
			l.remoteMegaphones[0].startRecording;
			rrand(5, 8).wait;
			l.remoteSoundSources[0].stopPlaying;
			l.remoteMegaphones[0].stopRecording;
		} {
			l.remoteMegaphones[i-1].setPosition(180 - (i * 30)); 0.5.wait;
			l.remoteMegaphones[i-1].startPlaying(i.linlin(0, 5, 0.7, 0.001));
			if (i != 5) { l.remoteMegaphones[i].startRecording; };
			rrand(5, 8).wait;
			l.remoteMegaphones[i-1].stopPlaying;
			if (i != 5) { l.remoteMegaphones[i].stopRecording; };
		};
	};
	l.remoteMegaphones.do{arg i; i.stopPlaying};
}).play
)

Tdef(\leftToRight).stop; l.remoteMegaphones.do{arg i; i.stopPlaying};

(
Tdef(\recIt, {
	// record more than play
	var spacing, recTime, midPause, endPause;
	spacing = rrand(0.2, 0.2);
	recTime = 4;
	midPause = 8;
	endPause = 0.2;
	inf.do{arg i;
		var chosenOne, otherOnes;
		chosenOne = l.remoteMegaphones.choose;
			l.remoteMegaphones.do{arg megaphone;
				spacing.wait;
				megaphone.startRecording;
			};
			recTime.wait;
			l.remoteMegaphones.do{arg megaphone;
				spacing.wait;
				megaphone.stopRecording;
			};
			otherOnes = l.remoteMegaphones.copy;
			otherOnes.remove(chosenOne);
			otherOnes.do{arg otherMegaphone;
				spacing.wait;
				otherMegaphone.startPlaying(1); otherMegaphone.setPlayVolume(1);
			};
			midPause.wait;
			otherOnes.do{arg otherMegaphone;
				spacing.wait;
				otherMegaphone.stopPlaying;
			};
		endPause.wait;
	};
}).play
)

Tdef(\recIt).stop

(
Tdef(\rhythmicMovement, {
	inf.do{arg i;
		l.remoteMegaphones.do{arg remoteMegaphone; remoteMegaphone.setPosition(180.rand)};
		[0.5, 0.75, 1, 1, 1, 0.5, 0.7, 0.1][i%8].wait;
	};
}).play
)

Tdef(\rhythmicMovement).stop; l.remoteMegaphones.do{arg i; i.stopPlaying};


(
Tdef(\rhythmicMovementWithPlaying, {
	l.remoteSoundSources[0].stopPlaying;
	l.remoteMegaphones.do{arg remoteMegaphone; remoteMegaphone.stopPlaying};
	0.2.wait;
	inf.do{arg i;
		var chosenOne, recOrPlay;
		l.remoteMegaphones.do{arg remoteMegaphone; remoteMegaphone.setPosition(rrand(90, 90) - 0);};
		chosenOne = l.remoteMegaphones.choose;
		recOrPlay = [\record, \play].choose;
		case
		{ recOrPlay == \ssplay } { l.remoteSoundSources[0].startPlaying(10.rand, rrand(0.2, 1)); chosenOne.startRecording; }
		{ recOrPlay == \record } { chosenOne.startRecording; }
		{ recOrPlay == \play } { chosenOne.startPlaying; chosenOne.setPlayVolume(1); };
		([0.5, 0.75, 1, 1, 1, 0.5, 0.7, 0.1]) [i%8].wait;
		case
		{ recOrPlay == \ssplay } { l.remoteSoundSources[0].stopPlaying; chosenOne.stopRecording;}
		{ recOrPlay == \record } { chosenOne.stopRecording; }
		{ recOrPlay == \play } { chosenOne.stopPlaying; };
		0.1.wait;
	};
}).play
)

Tdef(\rhythmicMovementWithPlaying).stop; l.remoteMegaphones.do{arg remoteMegaphone; remoteMegaphone.setPosition(0);};

(
Tdef(\rhythmicMovement, {
	inf.do{arg i;
		l.remoteMegaphones.do{arg remoteMegaphone; remoteMegaphone.setPosition(180.rand)};
		[0.5, 0.75, 1, 1, 1, 0.5, 0.7, 0.1][i%8].wait;
	};
}).play
)

(
Tdef(\forwardMarch, {
	l.remoteMegaphones.do{arg remoteMegaphone; remoteMegaphone.setPosition(90)};
	0.5.wait;
	inf.do{arg
		var megaphone = l.remoteMegaphones.choose;
		megaphone.startPlaying;
		rrand(2, 4).wait;
		megaphone.stopPlaying;
		0.2.wait;
	}
}).play
)

Tdef(\forwardMarch).stop; l.remoteMegaphones.do{arg i; i.stopPlaying};

// set position of all:
l.remoteMegaphones.do{arg remoteMegaphone; remoteMegaphone.setPosition(180)};
//

l.remoteSoundSources[0].startPlaying(4, 0.8);
l.remoteSoundSources[0].stopPlaying
l.remoteMegaphones.do{arg i; i.stopPlaying; i.stopRecording; }
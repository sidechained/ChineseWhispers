// decisions, decisions:

// one sound source or two?
// two is better, with independent control of each
// code need to reference on the local one

l = CWLaptop(0, "/Users/grahambooth/Desktop/Megaphone/beagleboard_soundfiles_final/soundfiles/");
m = CWLaptop(1, "/Users/grahambooth/Desktop/Megaphone/beagleboard_soundfiles_final/soundfiles/");
l.utopian.node.addrBook.peers.printAll;"";

l.mySoundSource.index
m.mySoundSource.index
l.mySoundSource.startPlaying(5, 1)
l.mySoundSource.stopPlaying(5, 1)
m.mySoundSource.startPlaying(8, 1)
m.mySoundSource.stopPlaying(8, 1)

l

// notes - sound source needs to be louder (use proper speaker)

// pathnames here:

(
~sfd = (
	// a dictionary of soundfile names
	// can be used to quickly lookup sound file names
	// and their durations
	0: name: "female_beijing_forbidden_city.wav", dur:
	1:, name: "female_come_quickly_and_buy_one.wav", dur:
	2:, name: "female_da_jia_distored_shop_announcement.wav", dur:
	3:, name: "female_mai_piao.wav", dur:
	4:, name: "female_prerecorded_shop_announcement.wav", dur:
	5:, name: "male_gulou_shadow_show_echo_shortened.wav", dur:
	6:, name: "male_mutianyu_great_wall.wav", dur:
	7:, name: "male_street_megaphone_deep_voiced.wav", dur:
	8:, name: "male_tasty_snacks_for_sale.wav", dur:
	9:, name: "male_train_station_move_along1.wav", dur:
	10:, name: "male_train_station_move_along2.wav", dur:
)
)

(
PathName("/Users/grahambooth/Desktop/Megaphone/beagleboard_soundfiles_final/soundfiles/").files.do{arg file;
	file.postln;
};"";
)

(
[
	"/Users/grahambooth/Desktop/Megaphone/beagleboard_soundfiles_final/soundfiles/female_beijing_forbidden_city.wav",
	"/Users/grahambooth/Desktop/Megaphone/beagleboard_soundfiles_final/soundfiles/female_come_quickly_and_buy_one.wav",
	"/Users/grahambooth/Desktop/Megaphone/beagleboard_soundfiles_final/soundfiles/female_da_jia_distored_shop_announcement.wav",
	"/Users/grahambooth/Desktop/Megaphone/beagleboard_soundfiles_final/soundfiles/female_mai_piao.wav",
	"/Users/grahambooth/Desktop/Megaphone/beagleboard_soundfiles_final/soundfiles/female_prerecorded_shop_announcement.wav",
	"/Users/grahambooth/Desktop/Megaphone/beagleboard_soundfiles_final/soundfiles/male_gulou_shadow_show_echo_shortened.wav",
	"/Users/grahambooth/Desktop/Megaphone/beagleboard_soundfiles_final/soundfiles/male_mutianyu_great_wall.wav",
	"/Users/grahambooth/Desktop/Megaphone/beagleboard_soundfiles_final/soundfiles/male_street_megaphone_deep_voiced.wav",
	"/Users/grahambooth/Desktop/Megaphone/beagleboard_soundfiles_final/soundfiles/male_tasty_snacks_for_sale.wav",
	"/Users/grahambooth/Desktop/Megaphone/beagleboard_soundfiles_final/soundfiles/male_train_station_move_along1.wav",
	"/Users/grahambooth/Desktop/Megaphone/beagleboard_soundfiles_final/soundfiles/male_train_station_move_along2.wav"
].collect{arg pathName; f = SoundFile();
	f.openRead(pathName);
	f.duration;
}
)

// REMEMBER TO CHANGE NUMBER OF SOUND SOURCES, OR LET EACH PLAYER ACCESS THEIR OWN

l.remoteSoundSources[0].startPlaying(8, 4)
l.remoteSoundSources[0].stopPlaying

l.remoteMegaphones.do{arg remoteMegaphone; remoteMegaphone.faceLeft;};

// need a list of soundfiles up here somewhere

(
Tdef(\volley, {
	// turn forwards, record from the sound source, repeat the sound forwards
	var soundFileNumber;
	soundFileNumber = 1;
	l.remoteMegaphones.do{arg remoteMegaphone; remoteMegaphone.stopRecording; remoteMegaphone.faceForward;};
	l.remoteSoundSources[0].startPlaying(1, 1); // max volume
	l.remoteMegaphones.do{arg remoteMegaphone; remoteMegaphone.startRecording;};
	4.wait; // or the full length of the soundfile - would be good to have these in an array
	l.remoteSoundSources[0].stopPlaying;
	l.remoteMegaphones.do{arg remoteMegaphone; remoteMegaphone.stopRecording;};
	1.wait;
	1.do{
		var volume;
		volume = rrand(0.5, 1);
		// play them back forever, until the Tdef is stopped or restarted, when a new sound will be triggered
		l.remoteMegaphones.do{arg remoteMegaphone; remoteMegaphone.startPlaying(volume)};
	};
}).play
)

Tdef(\volley).stop; l.remoteMegaphones.do{arg remoteMegaphone; remoteMegaphone.stopPlaying};

(
Tdef(\volumeWave, {
	// modulates volume of megaphones, as some get louder others get quieter
	inf.do{arg i;
		var megaphones;
		megaphones = l.remoteMegaphones;
		megaphones.do{arg remoteMegaphone, remoteMegaphoneIndex;
			var volume;
			volume = ((i%100)/100 + (remoteMegaphoneIndex/5)) % 1;
			if (remoteMegaphoneIndex == 3) {volume.postln};
			remoteMegaphone.setPlayVolume(volume);
		};
		0.4.wait;
	};
}).play
)

Tdef(\volumeWave).stop // doesn't stop playing, only stop the volume wave being applied (stay at last setting)

(
Tdef(\muteUnmute, {
	//
	inf.do{
		a = { l.remoteMegaphones.choose.setPlayVolume(0); };
		b = { l.remoteMegaphones.choose.setPlayVolume(1); };
		[a, a, a, b].choose.value; // vary proportion of a to b to go from sparse to dense
		rrand(0.1, 0.2).wait;
	}
}).play
)

Tdef(\muteUnmute).stop

(
Tdef(\muteUnmuteRecord, {
	// not really successful
	inf.do{
		a = { l.remoteMegaphones.choose.setPlayVolume(0); };
		b = { l.remoteMegaphones.choose.setPlayVolume(1); };
		c = { fork { // set a megaphone recording
			var chosenOne;
			chosenOne = l.remoteMegaphones.choose;
			chosenOne.startRecording;
			2.wait;
			chosenOne.stopRecording;
		}
		};
		d = {
			fork {
				l.remoteSoundSources[0].startPlaying(4, 1);
				10.wait;
				l.remoteSoundSources[0].stopPlaying;
			}
		};
		[a, b, b, b, b, c, d].choose.value; // vary proportion of a to b to go from sparse to dense
		rrand(0.1, 5).wait;
	}
}).play
)

// stop one by one, randomly in a given time

l.remoteMegaphones.do{arg remoteMegaphone; remoteMegaphone.stopPlaying};

l.remoteMegaphones
coin

(
Tdef(\recordParallel, {
	// refill the buffers!
	var recTime, waitTimeBeforeRecording;
	recTime = 5;
	waitTimeBeforeRecording = rrand(0.2, 1.2); // nice to randomise this a bit
	l.remoteMegaphones.do{arg remoteMegaphone;
		fork { waitTimeBeforeRecording.wait; remoteMegaphone.startRecording; recTime.wait; remoteMegaphone.stopRecording; }
	};
}).play
)

Tdef(\recordParallel).stop; l.remoteMegaphones.do{arg remoteMegaphone; remoteMegaphone.stopRecording;};

)

(
Tdef(\playSpray, {
	// stop all, choose one at random then play on it for a number of repeats, then choose another...forever!
	var repeats, volume, waitTimeBetweenRepeats, sprayRange, endPause;
	repeats = 5;
	volume = { [1, 1, 0].choose }; // machine gun them with all ones
	//volume = { rrand(0.01, 0.5) }; // random range
	waitTimeBetweenRepeats = { rrand(1, 2) };
	sprayRange = [80, 100];
	endPause = 1; // time to wait before choosing a new megaphone
	l.remoteMegaphones.do{arg remoteMegaphone; remoteMegaphone.stopPlaying};
	inf.do{
		var which, coin;
		which = l.remoteMegaphones.choose; // choose at random
		coin = 2.rand.coin;
		repeats.do{arg index;
			if (coin) { index = abs(index - repeats) }; // randomly choose spray direction
			//if (2.rand.coin) { which.setPlayVolume(volume) } { which.setPlayVolume(0) }; // randomly mute
			which.setPosition(index.linlin(0, repeats, sprayRange[0], sprayRange[1]));
			which.startPlaying;
			which.setPlayVolume(volume.value);
			waitTimeBetweenRepeats.value.wait;
			which.stopPlaying;
		};
		endPause.wait;
	};
}).play;
);

Tdef(\playSpray).stop; l.remoteMegaphones.do{arg remoteMegaphone; remoteMegaphone.stopPlaying};

l.remoteMegaphones.do{arg remoteMegaphone; remoteMegaphone.faceRight};

(
Tdef(\recorder, {
	l.remoteMegaphones.do{arg which;
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
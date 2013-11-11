// sketch of a combined control/sharedcontrol row for a megaphone


(
z = [
	StaticText().string_("megaphone0"),
	Button().states_([["play"]]).maxHeight_(100),
	Button().states_([["stop"]]).maxHeight_(100),
	View().layout_(VLayout(
		StaticText().string_("position:"),
		NumberBox().align_(\center)
	)),
	View().layout_(VLayout(
		StaticText().string_("play vol:"),
		NumberBox()
		.align_(\center)
	))
];
c = z.collect{arg az;
	a = [ Button().states_([["t"]]), az, Button().states_([["r"]]) ];
	View().layout_(VLayout(*a).spacing_(0).margins_(0));
};
b !? { b.destroy}; b = View().layout_(HLayout(*c)).front.alwaysOnTop_(true);
)


(
~offlineColor = Color.grey();
~onlineColor = Color.green();
~freeColor = Color.white();
~controlledColor = Color.red();

~makeMegaphoneControlColumn = {arg index;
	var items;
	items = [
		View()
		.background_(~freeColor)
		.layout_(HLayout(
			StaticText().string_("megaphone" ++ index).align_(\center)
		)
		.spacing_(0).margins_(0)
		)
		,
		View()
		.background_(~freeColor)
		.layout_(HLayout(
			StaticText().string_("position:").minWidth_(70),
			NumberBox().align_(\center)
		)
		.spacing_(0).margins_(0)),
		View()
		.background_(~freeColor)
		.layout_(HLayout(
			StaticText().string_("rec:").minWidth_(70),
			Button().states_([["-"]])
		)
		.spacing_(0).margins_(0)
		)
		,
		View()
		.background_(~freeColor)
		.layout_(HLayout(
			StaticText().string_("play:").minWidth_(70),
			Button().states_([["-"]])
		)
		.spacing_(0).margins_(0)
		)
		,
		View()
		.background_(~freeColor)
		.layout_(HLayout(
			StaticText().string_("play vol:").minWidth_(70),
			NumberBox()
			.align_(\center)
		)
		.spacing_(0).margins_(0)
		)
	];
	View()
	.background_(~offlineColor)
	.layout_(VLayout(*items).spacing_(0).margins_(5))
};

~setPositionControlledByBackground = {arg megaphoneIndex;
	~sharedMegaphoneControlColumns[megaphoneIndex].children[1].background_(~controlledColor);
};

~resetRecordingControlledByBackground = {arg megaphoneIndex;
	~sharedMegaphoneControlColumns[megaphoneIndex].children[1].background_(~freeColor);
};

~setRecordingControlledByBackground = {arg megaphoneIndex;
	~sharedMegaphoneControlColumns[megaphoneIndex].children[2].background_(~controlledColor);
};

~resetRecordingControlledByBackground = {arg megaphoneIndex;
	~sharedMegaphoneControlColumns[megaphoneIndex].children[2].background_(~freeColor);
};

~setPlaybackControlledByBackground = {arg megaphoneIndex;
	~sharedMegaphoneControlColumns[megaphoneIndex].children[3].background_(~controlledColor);
};

~resetPlaybackControlledByBackground = {arg megaphoneIndex;
	~sharedMegaphoneControlColumns[megaphoneIndex].children[3].background_(~freeColor);
};

~setPlayVolumeControlledByBackground = {arg megaphoneIndex;
	~sharedMegaphoneControlColumns[megaphoneIndex].children[4].background_(~controlledColor);
};

~resetPlayVolumeControlledByBackground = {arg megaphoneIndex;
	~sharedMegaphoneControlColumns[megaphoneIndex].children[4].background_(~freeColor);
};

~addDependancy = {arg sharedRemoteMegaphone;
	sharedRemoteMegaphone.sharedControlSpace.addDependant({arg dataSpace, val, key, value;
		[dataSpace, key, value].postln;
		case
		{ key == \controlledBy } {
			if ((value == \reset).not) {
				~setPositionControlledByBackground.value(sharedRemoteMegaphone.index);
				~setRecordingControlledByBackground.value(sharedRemoteMegaphone.index);
				~setPlaybackControlledByBackground.value(sharedRemoteMegaphone.index);
				~setPlayVolumeControlledByBackground.value(sharedRemoteMegaphone.index);
			}
			{
				~resetPositionControlledByBackground.value(sharedRemoteMegaphone.index);
				~resetRecordingControlledByBackground.value(sharedRemoteMegaphone.index);
				~resetPlaybackControlledByBackground.value(sharedRemoteMegaphone.index);
				~resetPlayVolumeControlledByBackground.value(sharedRemoteMegaphone.index);
			};
		}
		{ key == \positionControlledBy } {
			if ((value == \reset).not) {
				var megaphoneIndex;
				megaphoneIndex = sharedRemoteMegaphone.index;
				~setPositionControlledByBackground.value(megaphoneIndex);
			}
			{
				var megaphoneIndex;
				megaphoneIndex = sharedRemoteMegaphone.index;
				~resetPositionControlledByBackground.value(megaphoneIndex);
			};
		}
		{ key == \recordingControlledBy } {
			if ((value == \reset).not) {
				var megaphoneIndex;
				megaphoneIndex = sharedRemoteMegaphone.index;
				~setRecordingControlledByBackground.value(megaphoneIndex);
			}
			{
				var megaphoneIndex;
				megaphoneIndex = sharedRemoteMegaphone.index;
				~setRecordingControlledByBackground.value(megaphoneIndex);
			};
		}
		{ key == \playbackControlledBy } {
			if ((value == \reset).not) {
				var megaphoneIndex;
				megaphoneIndex = sharedRemoteMegaphone.index;
				~setPlaybackControlledByBackground.value(megaphoneIndex);
			}
			{
				var megaphoneIndex;
				megaphoneIndex = sharedRemoteMegaphone.index;
				~resetPlaybackControlledByBackground.value(megaphoneIndex);
			};
		}
		{ key == \playVolumeControlledBy } {
			if ((value == \reset).not) {
				var megaphoneIndex;
				megaphoneIndex = sharedRemoteMegaphone.index;
				~setPlayVolumeControlledByBackground.value(megaphoneIndex);
			}
			{
				var megaphoneIndex;
				megaphoneIndex = sharedRemoteMegaphone.index;
				~resetPlayVolumeControlledByBackground.value(megaphoneIndex);
			};
		}

	})
};

~goOnline = {arg index;
	~megaphoneControlColumns[index].background_(~onlineColor);
};

~goOffline = {arg index;
	~megaphoneControlColumns[index].background_(~offlineColor);
};

~makeGUI = {

	~sharedRemoteMegaphones = 5.collect{arg index;
		CWSharedRemoteMegaphone(index, ~utopian.node);
	};

	~sharedMegaphoneControlColumns = ~sharedRemoteMegaphones.collect{arg sharedRemoteMegaphone;
		~addDependancy.value(sharedRemoteMegaphone);
		~makeMegaphoneControlColumn.value(sharedRemoteMegaphone.index);
	};

	v !? {v.destroy};
	v = View(nil, Rect(0, 0, 800, 200))
	.layout_(HLayout(*~sharedMegaphoneControlColumns))
	.front
	.alwaysOnTop_(true)
};

// main:

~utopian = NMLUtopian(
	topology: \decentralised,
	hasServer: false,
	doWhenMeAdded: {
		defer{
			~makeGUI.value;
		}
	}
);
)


~sharedRemoteMegaphones[0].sharedControlSpace.put(\positionControlledBy, 1)
~sharedRemoteMegaphones[0].sharedControlSpace.put(\positionControlledBy, \reset)

~sharedRemoteMegaphones[0].sharedControlSpace.put(\recordingControlledBy, 1)
~sharedRemoteMegaphones[0].sharedControlSpace.put(\recordingControlledBy, \reset)

~sharedRemoteMegaphones[1].sharedControlSpace.put(\playbackControlledBy, 1)
~sharedRemoteMegaphones[0].sharedControlSpace.put(\playbackControlledBy, \reset)

~sharedRemoteMegaphones[0].sharedControlSpace.put(\playVolumeControlledBy, 1)
~sharedRemoteMegaphones[0].sharedControlSpace.put(\playVolumeControlledBy, \reset)



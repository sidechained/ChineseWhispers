(
// 1. first let's start the resources:
var sfp;
sfp = "/Users/grahambooth/Desktop/Megaphone/china_soundfiles/china_announcements_edited/_chosen/";
l = [];
Routine({
/*	2.do{arg index;
		// a. boot the local sources
		var local;
		local = CWLocalSoundSource(index, true, pathToSoundFiles: sfp);
		~local = ~local.add(local);
		2.wait;
	};
	5.do{arg index;
		// a. boot the local sources
		var local;
		local = CWLocalSoundSource(index, true, pathToSoundFiles: sfp);
		~local = ~local.add(local);
		2.wait;
	};	*/
	1.do{arg index;
		var laptop;
		// b. boot their remote counterparts
		laptop = CWLaptop(index);
		~laptops = ~laptops.add(laptop);
		2.wait;
	};
}).play;
)

~laptops[0].soundSources[0].isOnline
~laptops[0].utopian.node.addrBook.atName(\soundSource0)
)
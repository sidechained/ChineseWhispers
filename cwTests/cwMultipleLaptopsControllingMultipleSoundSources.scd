// * Test for multiple laptops in shared control of multiple sound sources, using:
// - two local sound sources (which do the actual sound playback)
// - two laptops, each running two remote sound sources (which connect to the local sound sources

(
// 1. first let's start the resources:
var sfp;
sfp = "/Users/grahambooth/Desktop/Megaphone/china_soundfiles/china_announcements_edited/_chosen/";
l = [];
Routine({
	2.do{arg index;
		// a. boot the local sources
		var local;
		local = CWLocalSoundSource(index, true, pathToSoundFiles: sfp);
		~local = ~local.add(local);
		2.wait;
	};
	2.do{arg index;
		var laptop;
		// b. boot their remote counterparts
		laptop = CWLaptop(index);
		~laptops = ~laptops.add(laptop);
		2.wait;
	};
}).play;
)

// 2. now try to control them:
~laptops[0].utopian.node.addrBook.peers.printAll;""; // each laptop should see four peers, two laptops, two soundSources
~laptops[1].utopian.node.addrBook.peers.printAll;"";
// we can get and interrogate the objects a laptop expects to see (CWRemoteSoundSource's)
~laptops[0].soundSources
~laptops[0].soundSources[0].dataspace.keys // parameters the local and remote sound sources share (sound making)
// first laptop takes control of sound source 0:
~laptops[0].soundSources[0].takeControl;
// this should be reflected by the sharedControlSpace of each laptop
// (sharedControlSpace represents the parameters the laptops share about this sound source)
~laptops[0].soundSources[0].sharedControlSpace.getPairs;
~laptops[0].soundSources[0].sharedControlSpace.getPairs;
 // the number given is the player's nodeID, can crossref it in the address book
~laptops[0].utopian.node.addrBook.peers.printAll;""; // look for laptop0
// now we have control of the sound source, we can play it (specify buffer and optional volume)
~laptops[0].soundSources[0].playBuffer(0, 1)
// stop the buffer
~laptops[0].soundSources[0].stopBuffer
// if the other laptop tries to play it they won't be able to
~laptops[1].soundSources[0].playBuffer(0, 1)
// ...unless they take control of it
~laptops[1].soundSources[0].takeControl;
~laptops[1].soundSources[0].playBuffer(0, 1);
~laptops[1].soundSources[0].stopBuffer;
// now the first laptop should be prevented from playing it
~laptops[0].soundSources[0].playBuffer(0, 1)
// let's try to take control of the second sound source with the 2nd laptop
~laptops[1].soundSources[1].takeControl;
~laptops[1].soundSources[1].playBuffer(5, 1);
~laptops[1].soundSources[1].stopBuffer
~laptops[0].soundSources[1].playBuffer(5, 1);

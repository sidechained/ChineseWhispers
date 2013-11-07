// * Test for multiple laptops in shared control of multiple sound sources, using:
// - two local sound sources (which do the actual sound playback)
// - two laptops, each running two remote sound sources (which connect to the local sound sources

(
// 1. first let's start the resources:
l = [];
Routine({
	2.do{arg index;
		// a. boot the local sources
		var local;
		local = CWLocalMegaphone(index, simulated: true);
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
~laptops[0].megaphones
~laptops[0].megaphones[0].dataspace.keys // parameters the local and remote sound sources share (sound making)
// first laptop takes control of sound source 0:
~laptops[0].megaphones[0].takeControl;
// this should be reflected by the sharedControlSpace of each laptop
// (sharedControlSpace represents the parameters the laptops share about this sound source)
~laptops[0].megaphones[0].sharedControlSpace.getPairs;
~laptops[0].megaphones[1].sharedControlSpace.getPairs;
 // the number given is the player's nodeID, can crossref it in the address book
~laptops[0].utopian.node.addrBook.peers.printAll;""; // look for laptop0
// now we have control of the sound source, we can play it (specify buffer and optional volume)
~laptops[0].megaphones[0].record;
~laptops[0].megaphones[0].stopRecording;
~laptops[0].megaphones[0].play;
~laptops[0].megaphones[0].stopPlaying;
~laptops[0].megaphones[0].setPlayVolume(0.5);
~laptops[0].megaphones[0].setPosition(10);
// stop the buffer
~laptops[0].megaphones[0].stopRecording;
// if the other laptop tries to play it they won't be able to
~laptops[1].megaphones[0].record;
// ...unless they take control of it
~laptops[1].megaphones[0].takeControl;
~laptops[1].megaphones[0].record;
~laptops[1].megaphones[0].stopRecording;
// now the first laptop should be prevented from playing it
~laptops[0].megaphones[0].record
// let's try to take control of the second sound source with the 2nd laptop
~laptops[1].megaphones[1].takeControl;
~laptops[1].megaphones[1].record;
~laptops[1].megaphones[1].stopRecording
~laptops[0].megaphones[1].record;

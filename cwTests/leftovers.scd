		dataspace.addDependant({arg dataspace, val, key, value;
			this.updateState(key, value);
		});

		// shared control:
		{ (key == \controlledBy) || (key == \playbackControlledBy) } { defer { gui.control(key, value) } }


		dataSpace.addDependant({arg dataSpace, val, key, value;
			this.updateState(key, value);
		});

		// shared control:
		{
			(key == \controlledBy) ||
			(key == \positionControlledBy) ||
			(key == \recordingControlledBy) ||
			(key == \playbackControlledBy) ||
			(key == \volumeControlledBy)
		} {
			defer {
				gui.control(key, value);
			}
		}
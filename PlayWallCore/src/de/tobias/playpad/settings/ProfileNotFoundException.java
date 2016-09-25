package de.tobias.playpad.settings;

import de.tobias.playpad.profile.ref.ProfileReference;

public class ProfileNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	private ProfileReference ref;

	public ProfileNotFoundException(ProfileReference ref) {
		super(ref.getName());
		this.ref = ref;
	}

	public ProfileReference getRef() {
		return ref;
	}
}

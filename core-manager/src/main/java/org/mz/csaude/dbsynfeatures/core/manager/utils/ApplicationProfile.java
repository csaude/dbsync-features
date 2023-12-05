package org.mz.csaude.dbsynfeatures.core.manager.utils;

public final class ApplicationProfile {
	
	private ApplicationProfile() {
	}
	
	public static final String REMOTE = "remote";
	public static final String CENTRAL = "central";
	
	public static boolean isCentral(String profile) {
		return profile.equals(ApplicationProfile.CENTRAL);
	}
	
	public static boolean isRemote(String profile) {
		return profile.equals(ApplicationProfile.REMOTE);
	}
}
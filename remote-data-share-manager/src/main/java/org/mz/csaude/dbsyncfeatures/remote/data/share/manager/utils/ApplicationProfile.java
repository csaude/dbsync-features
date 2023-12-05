package org.mz.csaude.dbsyncfeatures.remote.data.share.manager.utils;

public final class ApplicationProfile {
	
	private ApplicationProfile() {
	}
	
	public static final String PUBLISHER = "publisher";
	public static final String CONSUMER = "consumer";
	
	
	public static boolean isCentral(String profile) {
		return profile.equals(ApplicationProfile.CONSUMER);
	}
	
	public static boolean isRemote(String profile) {
		return profile.equals(ApplicationProfile.PUBLISHER);
	}
	
	
}
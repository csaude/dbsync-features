package org.mz.csaude.dbsyncfeatures.core.manager.utils;

import java.util.ArrayList;
import java.util.List;

public final class ApplicationProfile {
	
	private ApplicationProfile() {
	}
	
	public static final String REMOTE = "remote";
	
	public static final String CENTRAL = "central";
	
	public static final String DATA_SHARE_REMOTE = "ds-remote";
	
	public static final String DATA_SHARE_CENTRAL = "ds-central";
	
	public static boolean isDataShareCentral(String profile) {
		return profile.equals(ApplicationProfile.DATA_SHARE_CENTRAL);
	}
	
	public static boolean isDataShareRemote(String profile) {
		return profile.equals(ApplicationProfile.DATA_SHARE_REMOTE);
	}
	
	public static boolean isCentral(String[] profiles) {
		
		List<String> centralProfiles = new ArrayList<>();
		
		centralProfiles.add(CENTRAL);
		centralProfiles.add(DATA_SHARE_CENTRAL);
		
		for (String profile : profiles) {
			if (centralProfiles.contains(profile))
				return true;
			;
		}
		
		return false;
	}
	
	public static boolean isRemote(String[] profiles) {
		List<String> remoteProfiles = new ArrayList<>();
		
		remoteProfiles.add(REMOTE);
		remoteProfiles.add(DATA_SHARE_REMOTE);
		
		for (String profile : profiles) {
			if (remoteProfiles.contains(profile))
				return true;
		}
		
		return false;
	}
}

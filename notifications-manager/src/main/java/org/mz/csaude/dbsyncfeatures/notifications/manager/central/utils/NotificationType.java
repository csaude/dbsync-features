package org.mz.csaude.dbsyncfeatures.notifications.manager.central.utils;


public enum NotificationType {
	DBSYNC_INITIAL_SETUP,
	UPDATE_TRY,
	LIQUIBASE_UNLOCK,
	DBSYNC_SHUTDOWN,
	LOCATION_HARMONIZATION_STARTED,
	LOCATION_HARMONIZATION_FINISHED,
	UNKNOWN_NOTIFICATION
}

package org.eclipse.egit.ui.internal.cairh;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;
import org.eclipse.egit.ui.Activator;

public class EGitUILogger {
	private static ILog logger = null;

	private static String plugin_id = "";
	static {
		logger = Activator.getDefault().getLog();
		plugin_id = Activator.getPluginId();
	}

	public static void logCancel(String message, Throwable exception) {
		logger.log(new Status(Status.CANCEL, plugin_id, Status.CANCEL, message, exception));
	}

	public static void logError(String message, Throwable exception) {
		logger.log(new Status(Status.ERROR, plugin_id, Status.ERROR, message, exception));
	}

	public static void logInfo(String message, Throwable exception) {
		logger.log(new Status(Status.INFO, plugin_id, Status.INFO, message, exception));
	}

	public static void logOk(String message, Throwable exception) {
		logger.log(new Status(Status.OK, plugin_id, Status.OK, message, exception));
	}

	public static void logWarning(String message, Throwable exception) {
		logger.log(new Status(Status.WARNING, plugin_id, Status.WARNING, message, exception));
	}
}

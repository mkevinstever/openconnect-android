package de.robv.android.xposed;


/**
 * Hook the initialization of Java-based command-line tools (like pm)
 *
 * <p>Starting with Xposed's app_process version 55, this will not be called unless a
 * special file (conf/enable_for_tools) is created in the data directory of the installer.
 * You should not (and usually don't need to) implement this in your module.
 */
@Deprecated
public interface IXposedHookCmdInit extends IXposedMod {
	/**
	 * Called very early during startup of a command-line tool
	 * @param startClassName The startup class
	 * @throws Throwable everything is caught, but will prevent further initialization of the module
	 */
	public void initCmdApp(StartupParam startupParam) throws Throwable;

	public static class StartupParam {
		public String modulePath;
		public String startClassName;
	}
}

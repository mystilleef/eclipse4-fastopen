package com.laboki.eclipse.plugin.fastopen;

import com.laboki.eclipse.plugin.fastopen.instance.Instance;
import com.laboki.eclipse.plugin.fastopen.main.Services;
import com.laboki.eclipse.plugin.fastopen.task.AsyncTask;

public enum Plugin implements Instance {
	INSTANCE;

	private final static Services SERVICES = new Services();

	@Override
	public Instance
	start() {
		new AsyncTask() {

			@Override
			public void
			execute() {
				Plugin.SERVICES.start();
			}
		}.start();
		return this;
	}

	@Override
	public Instance
	stop() {
		Plugin.SERVICES.stop();
		return this;
	}
}

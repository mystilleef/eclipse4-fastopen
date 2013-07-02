package com.laboki.eclipse.plugin.fastopen;

import com.laboki.eclipse.plugin.fastopen.main.Services;
import com.laboki.eclipse.plugin.fastopen.task.AsyncTask;

public enum Plugin implements Instance {
	INSTANCE;

	private final static Services SERVICES = new Services();

	@Override
	public Instance begin() {
		new AsyncTask() {

			@Override
			public void asyncExecute() {
				Plugin.SERVICES.begin();
			}
		}.begin();
		return this;
	}

	@Override
	public Instance end() {
		new AsyncTask() {

			@Override
			public void asyncExecute() {
				Plugin.SERVICES.end();
			}
		}.begin();
		return this;
	}
}

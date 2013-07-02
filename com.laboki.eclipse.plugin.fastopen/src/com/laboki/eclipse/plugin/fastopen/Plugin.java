package com.laboki.eclipse.plugin.fastopen;

import com.laboki.eclipse.plugin.fastopen.main.Services;
import com.laboki.eclipse.plugin.fastopen.task.Task;

public enum Plugin implements Instance {
	INSTANCE;

	private final static Services SERVICES = new Services();

	@Override
	public Instance begin() {
		new Task() {

			@Override
			public void execute() {
				Plugin.SERVICES.begin();
			}
		}.begin();
		return this;
	}

	@Override
	public Instance end() {
		new Task() {

			@Override
			public void execute() {
				Plugin.SERVICES.end();
			}
		}.begin();
		return this;
	}
}

package com.laboki.eclipse.plugin.fastopen;

import com.laboki.eclipse.plugin.fastopen.opener.Services;

public enum Plugin implements Instance {
	INSTANCE;

	private final static Services SERVICES = new Services();

	@Override
	public Instance begin() {
		new Task() {

			@Override
			public void asyncExec() {
				Plugin.SERVICES.begin();
			}
		}.begin();
		return this;
	}

	@Override
	public Instance end() {
		new Task() {

			@Override
			public void asyncExec() {
				Plugin.SERVICES.end();
			}
		}.begin();
		return this;
	}
}

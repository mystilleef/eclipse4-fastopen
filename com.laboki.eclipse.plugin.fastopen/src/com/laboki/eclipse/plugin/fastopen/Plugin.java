package com.laboki.eclipse.plugin.fastopen;

import com.laboki.eclipse.plugin.fastopen.opener.EditorContext;
import com.laboki.eclipse.plugin.fastopen.opener.Services;

public enum Plugin implements Instance {
	INSTANCE;

	private final static Services SERVICES = new Services();

	@Override
	public Instance begin() {
		EditorContext.asyncExec(new Task() {

			@Override
			public void asyncExec() {
				Plugin.SERVICES.begin();
			}
		});
		return this;
	}

	@Override
	public Instance end() {
		EditorContext.asyncExec(new Task() {

			@Override
			public void asyncExec() {
				Plugin.SERVICES.end();
			}
		});
		return this;
	}
}

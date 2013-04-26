package com.laboki.eclipse.plugin.fastopen;

import com.laboki.eclipse.plugin.fastopen.opener.EditorContext;
import com.laboki.eclipse.plugin.fastopen.opener.Services;

public enum Opener implements Runnable, Instance {
	INSTANCE;

	private final static Services SERVICES = new Services();

	@Override
	public void run() {
		this.begin();
	}

	@Override
	public Instance begin() {
		EditorContext.asyncExec(new Task() {

			@Override
			public void asyncExec() {
				Opener.SERVICES.begin();
			}
		});
		return this;
	}

	@Override
	public Instance end() {
		EditorContext.asyncExec(new Task() {

			@Override
			public void asyncExec() {
				Opener.SERVICES.end();
			}
		});
		return this;
	}
}

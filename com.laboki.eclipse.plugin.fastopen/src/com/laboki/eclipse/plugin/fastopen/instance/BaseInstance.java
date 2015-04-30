package com.laboki.eclipse.plugin.fastopen.instance;

public abstract class BaseInstance implements Instance {

	protected BaseInstance() {}

	@Override
	public Instance
	start() {
		return this;
	}

	@Override
	public Instance
	stop() {
		return this;
	}
}

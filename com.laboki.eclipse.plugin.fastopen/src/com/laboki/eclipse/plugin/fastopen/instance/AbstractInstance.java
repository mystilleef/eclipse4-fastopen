package com.laboki.eclipse.plugin.fastopen.instance;

public abstract class AbstractInstance implements Instance {

	protected AbstractInstance() {}

	@Override
	public Instance
	begin() {
		return this;
	}

	@Override
	public Instance
	end() {
		return this;
	}
}

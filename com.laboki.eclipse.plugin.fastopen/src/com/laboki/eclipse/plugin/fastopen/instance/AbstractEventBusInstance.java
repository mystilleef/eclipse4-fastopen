package com.laboki.eclipse.plugin.fastopen.instance;

import com.laboki.eclipse.plugin.fastopen.main.EventBus;

public abstract class AbstractEventBusInstance implements Instance {

	protected final EventBus eventBus;

	protected AbstractEventBusInstance(final EventBus eventBus) {
		this.eventBus = eventBus;
	}

	@Override
	public Instance begin() {
		EventBus.register(this);
		return this;
	}

	@Override
	public Instance end() {
		EventBus.unregister(this);
		return this;
	}
}

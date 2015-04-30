package com.laboki.eclipse.plugin.fastopen.instance;

import com.laboki.eclipse.plugin.fastopen.main.EventBus;

public class EventBusInstance implements Instance {

	@Override
	public Instance
	start() {
		EventBus.register(this);
		return this;
	}

	@Override
	public Instance
	stop() {
		EventBus.unregister(this);
		return this;
	}
}

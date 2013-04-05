package com.laboki.eclipse.plugin.fastopen.opener.listeners;

abstract class AbstractOpenerListener implements IOpenerListener {

	private boolean isListening;

	protected AbstractOpenerListener() {}

	@Override
	public void start() {
		if (this.isListening) return;
		this.add();
		this.isListening = true;
	}

	@Override
	public void stop() {
		if (!this.isListening) return;
		this.remove();
		this.isListening = false;
	}

	protected void add() {}

	protected void remove() {}
}

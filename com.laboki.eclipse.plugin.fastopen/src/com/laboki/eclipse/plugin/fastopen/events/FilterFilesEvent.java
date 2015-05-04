package com.laboki.eclipse.plugin.fastopen.events;

public final class FilterFilesEvent {

	private final String string;

	public FilterFilesEvent(final String string) {
		this.string = string;
	}

	public String
	getString() {
		return this.string;
	}
}

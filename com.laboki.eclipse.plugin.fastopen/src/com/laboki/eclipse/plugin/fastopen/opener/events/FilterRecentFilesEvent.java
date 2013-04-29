package com.laboki.eclipse.plugin.fastopen.opener.events;


public final class FilterRecentFilesEvent {

	private final String string;

	public FilterRecentFilesEvent(final String string) {
		this.string = string;
	}

	public String getString() {
		return this.string;
	}
}

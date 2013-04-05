package com.laboki.eclipse.plugin.fastopen.events;

import lombok.Getter;

public final class FilterRecentFilesEvent {

	@Getter private final String string;

	public FilterRecentFilesEvent(final String string) {
		this.string = string;
	}
}

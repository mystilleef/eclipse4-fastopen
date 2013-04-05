package com.laboki.eclipse.plugin.fastopen.opener.ui;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.laboki.eclipse.plugin.fastopen.Task;
import com.laboki.eclipse.plugin.fastopen.events.FileResourcesEvent;
import com.laboki.eclipse.plugin.fastopen.events.ShowFastOpenDialogEvent;
import com.laboki.eclipse.plugin.fastopen.opener.EditorContext;

public final class Window {

	@SuppressWarnings("static-method")
	@Subscribe
	@AllowConcurrentEvents
	public void showDialog(@SuppressWarnings("unused") final ShowFastOpenDialogEvent event) {
		EditorContext.asyncExec(new Task("") {

			@Override
			public void execute() {
				System.out.println("Showing window");
			}
		});
	}

	@SuppressWarnings("static-method")
	@Subscribe
	@AllowConcurrentEvents
	public void printResources(@SuppressWarnings("unused") final FileResourcesEvent event) {
		EditorContext.asyncExec(new Task("") {

			@Override
			public void execute() {
				// System.out.println("+++++++++++++++++++++++++++++++++++");
				// for (final File file : event.getFiles())
				// System.out.println(file.getName());
				// System.out.println("+++++++++++++++++++++++++++++++++++");
			}
		});
	}
}

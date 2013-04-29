package com.laboki.eclipse.plugin.fastopen;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.laboki.eclipse.plugin.fastopen.opener.EditorContext;
import com.laboki.eclipse.plugin.fastopen.opener.events.ShowFastOpenDialogEvent;

public final class Handler extends AbstractHandler {

	public Handler() {}

	@Override
	public Object execute(final ExecutionEvent arg0) throws ExecutionException {
		EditorContext.asyncExec(new Task() {

			@Override
			public void execute() {
				EventBus.post(new ShowFastOpenDialogEvent());
			}
		});
		return null;
	}
}

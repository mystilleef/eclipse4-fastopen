package com.laboki.eclipse.plugin.fastopen.task;

import com.laboki.eclipse.plugin.fastopen.main.EditorContext;

public abstract class AsyncTask extends BaseTask {

	@Override
	protected TaskJob
	newTaskJob() {
		return new TaskJob() {

			@Override
			protected void
			runTask() {
				EditorContext.asyncExec(() -> AsyncTask.this.execute());
			}
		};
	}

	@Override
	public abstract void
	execute();
}

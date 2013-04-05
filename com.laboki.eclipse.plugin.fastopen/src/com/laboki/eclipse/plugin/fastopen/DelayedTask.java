package com.laboki.eclipse.plugin.fastopen;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.laboki.eclipse.plugin.fastopen.opener.EditorContext;

public abstract class DelayedTask extends Job implements Runnable {

	private final int timeInMilliSeconds;

	public DelayedTask(final String name, final int timeInMilliSeconds) {
		super(name);
		this.timeInMilliSeconds = timeInMilliSeconds;
	}

	@Override
	public void run() {
		this.schedule(this.timeInMilliSeconds);
	}

	@Override
	protected IStatus run(final IProgressMonitor arg0) {
		EditorContext.asyncExec(new Runnable() {

			@Override
			public void run() {
				DelayedTask.this.execute();
			}
		});
		return Status.OK_STATUS;
	}

	protected void execute() {}
}

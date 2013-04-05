package com.laboki.eclipse.plugin.fastopen;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.laboki.eclipse.plugin.fastopen.opener.EditorContext;

public abstract class Task extends Job implements Runnable {

	public Task(final String name) {
		super(name);
		this.setPriority(Job.INTERACTIVE);
	}

	@Override
	public void run() {
		this.schedule();
	}

	@Override
	protected IStatus run(final IProgressMonitor arg0) {
		EditorContext.asyncExec(new Runnable() {

			@Override
			public void run() {
				Task.this.execute();
			}
		});
		return Status.OK_STATUS;
	}

	protected void execute() {}
}

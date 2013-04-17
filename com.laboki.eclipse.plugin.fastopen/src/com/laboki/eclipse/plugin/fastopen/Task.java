package com.laboki.eclipse.plugin.fastopen;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.laboki.eclipse.plugin.fastopen.opener.EditorContext;

public abstract class Task extends Job implements Runnable {

	private final String name;

	public Task(final String name) {
		super(name);
		this.name = name;
		this.setPriority(Job.INTERACTIVE);
	}

	@Override
	public boolean belongsTo(final Object family) {
		return this.name.equals(family);
	}

	@Override
	public void run() {
		this.schedule();
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		if (monitor.isCanceled()) return Status.CANCEL_STATUS;
		this.asyncExec();
		return Status.OK_STATUS;
	}

	private void asyncExec() {
		EditorContext.asyncExec(new Runnable() {

			@Override
			public void run() {
				Task.this.execute();
			}
		});
	}

	protected void execute() {}
}

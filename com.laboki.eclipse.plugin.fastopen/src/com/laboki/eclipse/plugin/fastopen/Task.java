package com.laboki.eclipse.plugin.fastopen;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.laboki.eclipse.plugin.fastopen.opener.EditorContext;

public abstract class Task extends Job implements Runnable {

	public static final int TASK_INTERACTIVE = Job.INTERACTIVE;
	public static final int TASK_SHORT = Job.SHORT;
	public static final int TASK_LONG = Job.LONG;
	public static final int TASK_BUILD = Job.BUILD;
	public static final int TASK_DECORATE = Job.DECORATE;
	private final int delayTime;
	private final String name;

	public Task() {
		super("");
		this.name = "";
		this.delayTime = 0;
		this.setPriority(Task.TASK_INTERACTIVE);
	}

	public Task(final String name) {
		super(name);
		this.name = name;
		this.delayTime = 0;
		this.setPriority(Task.TASK_INTERACTIVE);
	}

	public Task(final String name, final int delayTime) {
		super(name);
		this.name = name;
		this.delayTime = delayTime;
		this.setPriority(Task.TASK_DECORATE);
	}

	public Task(final String name, final int delayTime, final int priority) {
		super(name);
		this.name = name;
		this.delayTime = delayTime;
		this.setPriority(priority);
	}

	@Override
	public boolean belongsTo(final Object family) {
		return this.name.equals(family);
	}

	@Override
	public void run() {
		this.setUser(false);
		this.setSystem(true);
		this.schedule(this.delayTime);
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		if (monitor.isCanceled()) return Status.CANCEL_STATUS;
		this.runTask();
		return Status.OK_STATUS;
	}

	private void runTask() {
		this.execute();
		this.runExec();
		this.postExecute();
	}

	private void runExec() {
		this.runAsyncExec();
		this.runSyncExec();
	}

	protected void execute() {}

	private void runAsyncExec() {
		EditorContext.asyncExec(new Runnable() {

			@Override
			public void run() {
				EditorContext.flushEvents();
				Task.this.asyncExec();
			}
		});
	}

	protected void asyncExec() {}

	private void runSyncExec() {
		EditorContext.syncExec(new Runnable() {

			@Override
			public void run() {
				EditorContext.flushEvents();
				Task.this.syncExec();
			}
		});
	}

	protected void syncExec() {}

	protected void postExecute() {}
}

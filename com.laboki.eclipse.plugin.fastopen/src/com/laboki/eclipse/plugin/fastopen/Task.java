package com.laboki.eclipse.plugin.fastopen;

public class Task extends AbstractTask implements Instance {

	public Task() {
		super("", 0, AbstractTask.TASK_INTERACTIVE);
	}

	public Task(final String name) {
		super(name, 0, AbstractTask.TASK_INTERACTIVE);
	}

	public Task(final String name, final int delayTime) {
		super(name, delayTime, AbstractTask.TASK_DECORATE);
	}

	public Task(final String name, final int delayTime, final int priority) {
		super(name, delayTime, priority);
	}

	@Override
	public Instance begin() {
		this.run();
		return this;
	}

	@Override
	public Instance end() {
		this.cancel();
		return this;
	}
}

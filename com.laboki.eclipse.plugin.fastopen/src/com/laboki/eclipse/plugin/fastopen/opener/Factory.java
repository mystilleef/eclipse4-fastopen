package com.laboki.eclipse.plugin.fastopen.opener;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;

import com.laboki.eclipse.plugin.fastopen.EventBus;
import com.laboki.eclipse.plugin.fastopen.Instance;
import com.laboki.eclipse.plugin.fastopen.opener.events.PartActivationEvent;

public enum Factory implements Instance {
	INSTANCE;

	private static final IPartService PART_SERVICE = EditorContext.getPartService();
	private final PartListener partListener = new PartListener();

	@Override
	public Instance begin() {
		Factory.startRecentFilesMonitor(Factory.PART_SERVICE.getActivePart());
		Factory.PART_SERVICE.addPartListener(this.partListener);
		return this;
	}

	@Override
	public Instance end() {
		Factory.PART_SERVICE.removePartListener(this.partListener);
		return this;
	}

	public static void startRecentFilesMonitor(final IWorkbenchPart part) {
		if (EditorContext.isInvalidPart(part)) return;
		EventBus.post(new PartActivationEvent());
	}

	private final class PartListener implements IPartListener {

		public PartListener() {}

		@Override
		public void partActivated(final IWorkbenchPart part) {
			Factory.startRecentFilesMonitor(part);
		}

		@Override
		public void partClosed(final IWorkbenchPart part) {}

		@Override
		public void partBroughtToTop(final IWorkbenchPart part) {
			Factory.startRecentFilesMonitor(part);
		}

		@Override
		public void partDeactivated(final IWorkbenchPart part) {}

		@Override
		public void partOpened(final IWorkbenchPart part) {
			Factory.startRecentFilesMonitor(part);
		}
	}
}

package com.laboki.eclipse.plugin.fastopen.opener;

import lombok.ToString;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;

import com.laboki.eclipse.plugin.fastopen.EventBus;
import com.laboki.eclipse.plugin.fastopen.events.PartActivationEvent;

@ToString
public final class Factory implements Runnable {

	private static final IPartService PART_SERVICE = EditorContext.getPartService();
	private final PartListener partListener = new PartListener();

	public Factory() {
		Factory.PART_SERVICE.addPartListener(this.partListener);
	}

	@Override
	public void run() {
		EditorContext.instance();
		Factory.startRecentFilesMonitor(Factory.PART_SERVICE.getActivePart());
	}

	public static void startRecentFilesMonitor(final IWorkbenchPart part) {
		if (Factory.isInvalidPart(part)) return;
		EventBus.post(new PartActivationEvent());
	}

	private static boolean isInvalidPart(final IWorkbenchPart part) {
		return !Factory.isValidPart(part);
	}

	private static boolean isValidPart(final IWorkbenchPart part) {
		if (part == null) return false;
		if (part instanceof IEditorPart) return true;
		return false;
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
		public void partBroughtToTop(final IWorkbenchPart part) {}

		@Override
		public void partDeactivated(final IWorkbenchPart part) {}

		@Override
		public void partOpened(final IWorkbenchPart part) {}
	}
}

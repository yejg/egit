/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Thomas Wolf <thomas.wolf@paranor.ch> - Bug 477248
 *******************************************************************************/
package org.eclipse.egit.ui.internal.revision;

import java.io.InputStream;
import java.net.URI;
import java.util.Date;

import org.eclipse.core.resources.IEncodedStorage;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.egit.core.AdapterUtils;
import org.eclipse.egit.ui.internal.PreferenceBasedDateFormatter;
import org.eclipse.egit.ui.internal.UIText;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * An Editor input for file revisions
 */
public class FileRevisionEditorInput extends PlatformObject implements
		IWorkbenchAdapter, IStorageEditorInput {

	private final Object fileRevision;

	private final IStorage storage;

	/**
	 * @param revision
	 *            the file revision
	 * @param monitor
	 * @return a file revision editor input
	 * @throws CoreException
	 */
	public static FileRevisionEditorInput createEditorInputFor(
			IFileRevision revision, IProgressMonitor monitor)
			throws CoreException {
		IStorage storage = revision.getStorage(monitor);
		return new FileRevisionEditorInput(revision, storage);
	}

	private static IStorage wrapStorage(final IStorage storage,
			final String charset) {
		if (charset == null)
			return storage;
		if (storage instanceof IFileState) {
			return new IFileState() {
				@Override
				public Object getAdapter(Class adapter) {
					return AdapterUtils.adapt(storage, adapter);
				}

				@Override
				public boolean isReadOnly() {
					return storage.isReadOnly();
				}

				@Override
				public String getName() {
					return storage.getName();
				}

				@Override
				public IPath getFullPath() {
					return storage.getFullPath();
				}

				@Override
				public InputStream getContents() throws CoreException {
					return storage.getContents();
				}

				@Override
				public String getCharset() throws CoreException {
					return charset;
				}

				@Override
				public boolean exists() {
					return ((IFileState) storage).exists();
				}

				@Override
				public long getModificationTime() {
					return ((IFileState) storage).getModificationTime();
				}
			};
		}

		return new IEncodedStorage() {
			@Override
			public Object getAdapter(Class adapter) {
				return AdapterUtils.adapt(storage, adapter);
			}

			@Override
			public boolean isReadOnly() {
				return storage.isReadOnly();
			}

			@Override
			public String getName() {
				return storage.getName();
			}

			@Override
			public IPath getFullPath() {
				return storage.getFullPath();
			}

			@Override
			public InputStream getContents() throws CoreException {
				return storage.getContents();
			}

			@Override
			public String getCharset() throws CoreException {
				return charset;
			}
		};
	}

	/**
	 * @param revision
	 *            the file revision
	 * @param storage
	 *            the contents of the file revision
	 */
	public FileRevisionEditorInput(Object revision, IStorage storage) {
		Assert.isNotNull(revision);
		Assert.isNotNull(storage);
		this.fileRevision = revision;
		this.storage = storage;
	}

	/**
	 * @param state
	 *            the file state
	 */
	public FileRevisionEditorInput(IFileState state) {
		this(state, state);
	}

	/**
	 * @param revision
	 * @param storage
	 * @param charset
	 */
	public FileRevisionEditorInput(Object revision, IStorage storage,
			String charset) {
		this(revision, wrapStorage(storage, charset));
	}

	@Override
	public IStorage getStorage() throws CoreException {
		return storage;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public String getName() {
		IFileRevision rev = AdapterUtils.adapt(this, IFileRevision.class);
		if (rev != null) {
			return NLS.bind(
					UIText.FileRevisionEditorInput_NameAndRevisionTitle,
					new String[] { rev.getName(), rev.getContentIdentifier() });
		}
		IFileState state = AdapterUtils.adapt(this, IFileState.class);
		if (state != null) {
			return state.getName() + ' ' + PreferenceBasedDateFormatter.create()
					.formatDate(new Date(state.getModificationTime()));
		}
		return storage.getName();

	}

	@Override
	public IPersistableElement getPersistable() {
		// can't persist
		return null;
	}

	@Override
	public String getToolTipText() {
		return storage.getFullPath().toString();
	}

	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) {
			return this;
		}
		if (adapter == IStorage.class) {
			return storage;
		}
		Object object = super.getAdapter(adapter);
		if (object != null) {
			return object;
		}
		return AdapterUtils.adapt(fileRevision, adapter);
	}

	@Override
	public Object[] getChildren(Object o) {
		return new Object[0];
	}

	@Override
	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}

	@Override
	public String getLabel(Object o) {
		IFileRevision rev = AdapterUtils.adapt(this, IFileRevision.class);
		if (rev != null)
			return rev.getName();
		return storage.getName();
	}

	@Override
	public Object getParent(Object o) {
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (obj instanceof FileRevisionEditorInput) {
			FileRevisionEditorInput other = (FileRevisionEditorInput) obj;
			return (other.fileRevision.equals(this.fileRevision));
		}
		return false;
	}

	@Override
	public int hashCode() {
		return fileRevision.hashCode();
	}

	/**
	 * @return the revision
	 */
	public IFileRevision getFileRevision() {
		if (fileRevision instanceof IFileRevision) {
			return (IFileRevision) fileRevision;
		}
		return null;
	}

	/**
	 * @return the revision
	 */
	public URI getURI() {
		if (fileRevision instanceof IFileRevision) {
			IFileRevision fr = (IFileRevision) fileRevision;
			return fr.getURI();
		}
		return null;
	}
}

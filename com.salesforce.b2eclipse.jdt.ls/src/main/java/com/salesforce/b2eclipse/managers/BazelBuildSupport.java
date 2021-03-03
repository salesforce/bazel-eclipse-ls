package com.salesforce.b2eclipse.managers;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ls.core.internal.ExtensionsExtractor;
import org.eclipse.jdt.ls.core.internal.IConstants;
import org.eclipse.jdt.ls.core.internal.IProjectImporter;
import org.eclipse.jdt.ls.core.internal.managers.IBuildSupport;
import org.eclipse.jdt.ls.core.internal.managers.ProjectsManager.CHANGE_TYPE;

import com.salesforce.b2eclipse.BazelNature;

@SuppressWarnings("restriction")
public class BazelBuildSupport implements IBuildSupport{
	@Override
	public boolean applies(IProject project) {
		try {
			return project != null && project.hasNature(BazelNature.BAZEL_NATURE_ID);

		} catch (CoreException e) {
			return false;
		}
	}
	
	@Override
	public void update(IProject project, boolean force, IProgressMonitor monitor) throws CoreException {
		try {
			updateInternal(project, force, monitor);
			
		} catch (CoreException ex) {
			throw ex;
			
		} catch (AssertionFailedException ex) {
			// Bazel can't work with the provided project.
			// Just skip it.
			
		} catch (NoSuchElementException ex) {
			// No bazel importers found. This is definitely illegal situation
			// which is not clear how to solve right now.
			// Just skip it.
		}
		
	}
	
	protected void updateInternal(IProject project, boolean force, IProgressMonitor monitor) throws CoreException {
		Assert.isTrue(applies(project));
		
		final IProjectImporter importer = obtainBazelImporter().get();
		
		Assert.isTrue(importer.applies(monitor));
		
		importer.importToWorkspace(monitor);
	}

	@Override
	public boolean isBuildFile(IResource resource) {
		return true;
	}
	
	@Override
	public boolean isBuildLikeFileName(String fileName) {
		return fileName.equals("BUILD");
	}

	@Override
    public List<String> getWatchPatterns() {
        return Arrays.asList("**/BUILD", "**/WORKSPACE");
    }
	
	@Override
	public boolean fileChanged(IResource resource, CHANGE_TYPE changeType, IProgressMonitor monitor) throws CoreException {
		return true;
	}

	///////
	
	private Optional<IProjectImporter> obtainBazelImporter() {
		return ExtensionsExtractor.<IProjectImporter>extractExtensions(IConstants.PLUGIN_ID, "importers")
					.stream()
					.filter(importer -> importer instanceof BazelProjectImporter)
					.findFirst();
	}
}

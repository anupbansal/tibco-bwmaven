/**
 * (C) Copyright 2011-2015 FastConnect SAS
 * (http://www.fastconnect.fr/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.fastconnect.factory.tibco.bw.maven.deployment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

import fr.fastconnect.factory.tibco.bw.maven.compile.CompileEARMojo;
import fr.fastconnect.factory.tibco.bw.maven.packaging.AbstractPackagingMojo;

/**
 * <p>
 * This class inherits from {@link CompileEARMojo} because it will use the
 * EAR generated by this class to deploy it on a TIBCO domain.
 * </p>
 *
 * @author Mathieu Debove
 *
 */
public abstract class AbstractBWDeployMojo extends AbstractPackagingMojo {
	
	/**
	 * Name of the project once deployed in TIBCO domain
	 */
	@Parameter ( property = deployedProjectNameProperty, required = true )
	protected String deployedProjectName;

	/**
	 * TIBCO domain name
	 */
	@Parameter ( property = domainNameProperty, required = true)
	protected String domainName;

	/**
	 * TIBCO domain username
	 */
	@Parameter ( property = domainUsernameProperty, required = true)
	protected String domainUsername;

	/**
	 * TIBCO domain password
	 */
	@Parameter ( property = domainPasswordProperty, required = true)
	protected String domainPassword;

	@Parameter ( property = "forceAppManageGoals", defaultValue = "false" )
	protected Boolean forceAppManageGoals;

	@Override
	protected String getArtifactFileExtension() {
		return BWEAR_EXTENSION;
	}

	public boolean skip() {
		return (super.skip() || !getProject().getPackaging().startsWith(BWEAR_TYPE)) && !forceAppManageGoals; // startsWith to include bw-ear-deploy
	}

	/**
	 * <p>
	 * For each AppManage command a common set of arguments is required (domain,
	 * project name, ...)
	 * </p>
	 *
	 * @return a list with common arguments for AppManage.
	 */
	public ArrayList<String> commonArguments() {
		ArrayList<String> arguments = new ArrayList<String>();

		arguments.add("-app");
		arguments.add(deployedProjectName);
		arguments.add("-domain");
		arguments.add(domainName);
		arguments.add("-user");
		arguments.add(domainUsername);
		arguments.add("-pw");
		arguments.add(domainPassword);

		return arguments;
	}

	public abstract String getInitMessage();
	public abstract String getFailureMessage();
	public abstract void postAction() throws MojoExecutionException;

	public abstract ArrayList<String> arguments();

	public void execute() throws MojoExecutionException {
		if (skip()) {
			getLog().info(SKIPPING);
			return;
		}

		checkAppManage();

		try {
			getLog().info(getInitMessage());

			ArrayList<String> arguments = arguments();

			ArrayList<File> tras = new ArrayList<File>();
			tras.add(tibcoAppManageTRAPath);

			launchTIBCOBinary(tibcoAppManagePath, tras, arguments, directory, getFailureMessage());

			postAction();
		} catch (IOException e) {
			throw new MojoExecutionException(getFailureMessage(), e);
		}
	}

}

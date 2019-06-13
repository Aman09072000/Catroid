/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2019 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * An additional term exception under section 7 of the GNU Affero
 * General Public License, version 3, is available at
 * http://developer.catrobat.org/license_additional_term
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.catrobat.catroid.test.io.asynctask;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.catrobat.catroid.common.DefaultProjectHandler;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.io.asynctask.ProjectRenameTask;
import org.catrobat.catroid.test.utils.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ProjectRenameTaskTest {

	private final String PROJECT_NAME = "testProject";
	private final String RENAMED_PROJECT_NAME = "renamedTestProject";
	private final String SLASH_RENAMED_PROJECT = "renamed/TestProject";
	private final String SLASH_ENCODED_RENAMED_PROJECT = "renamed%2FTestProject";

	private Project defaultProject;

	@Before
	public void setUp() throws IOException {
		TestUtils.deleteProjects(PROJECT_NAME, RENAMED_PROJECT_NAME, SLASH_ENCODED_RENAMED_PROJECT);
		defaultProject = DefaultProjectHandler.createAndSaveDefaultProject(PROJECT_NAME,
				InstrumentationRegistry.getTargetContext());
	}

	@After
	public void tearDown() throws IOException {
		TestUtils.deleteProjects(PROJECT_NAME, RENAMED_PROJECT_NAME, SLASH_ENCODED_RENAMED_PROJECT);
	}

	@Test
	public void projectRenameTaskTest() throws IOException {
		File renamedDirectory = ProjectRenameTask.task(defaultProject.getDirectory(), RENAMED_PROJECT_NAME);
		assertEquals(RENAMED_PROJECT_NAME, renamedDirectory.getName());
	}

	@Test
	public void projectRenameSlashTaskTest() throws IOException {
		File renamedDirectory = ProjectRenameTask.task(defaultProject.getDirectory(), SLASH_RENAMED_PROJECT);
		assertEquals(SLASH_ENCODED_RENAMED_PROJECT, renamedDirectory.getName());
	}
}

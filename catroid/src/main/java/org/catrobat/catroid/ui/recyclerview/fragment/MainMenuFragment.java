/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2018 The Catrobat Team
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

package org.catrobat.catroid.ui.recyclerview.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.common.Constants;
import org.catrobat.catroid.common.ProjectData;
import org.catrobat.catroid.content.backwardcompatibility.ProjectMetaDataParser;
import org.catrobat.catroid.io.ProjectAndSceneScreenshotLoader;
import org.catrobat.catroid.io.XstreamSerializer;
import org.catrobat.catroid.io.asynctask.ProjectLoadTask;
import org.catrobat.catroid.ui.ProjectActivity;
import org.catrobat.catroid.ui.ProjectListActivity;
import org.catrobat.catroid.ui.ProjectUploadActivity;
import org.catrobat.catroid.ui.WebViewActivity;
import org.catrobat.catroid.ui.recyclerview.RVButton;
import org.catrobat.catroid.ui.recyclerview.adapter.ButtonAdapter;
import org.catrobat.catroid.ui.recyclerview.dialog.NewProjectDialogFragment;
import org.catrobat.catroid.ui.recyclerview.viewholder.ButtonVH;
import org.catrobat.catroid.utils.FileMetaDataExtractor;
import org.catrobat.catroid.utils.ToastUtil;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import dagger.android.support.AndroidSupportInjection;

import static org.catrobat.catroid.common.Constants.CODE_XML_FILE_NAME;
import static org.catrobat.catroid.common.Constants.EXTRA_PROJECT_NAME;
import static org.catrobat.catroid.common.FlavoredConstants.DEFAULT_ROOT_DIRECTORY;

public class MainMenuFragment extends Fragment implements
		ButtonAdapter.OnItemClickListener,
		ProjectLoadTask.ProjectLoadListener {

	public static final String TAG = MainMenuFragment.class.getSimpleName();

	@Inject
	ProjectManager projectManager;

	@Retention(RetentionPolicy.SOURCE)
	@IntDef({NEW, PROGRAMS, HELP, EXPLORE, UPLOAD})
	@interface ButtonId {
	}

	private static final int NEW = 1;
	private static final int PROGRAMS = 2;
	private static final int HELP = 3;
	private static final int EXPLORE = 4;
	private static final int UPLOAD = 5;

	private static final int CURRENTTHUMBNAILSIZE = 150;
	private static final int CONTINUE = R.id.current_project;

	private View parent;
	private RecyclerView recyclerView;
	private ButtonAdapter buttonAdapter;
	private File projectDir;

	@Override
	public void onAttach(Context context) {
		AndroidSupportInjection.inject(this);
		super.onAttach(context);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		parent = inflater.inflate(R.layout.landing_page, container, false);
		recyclerView = parent.findViewById(R.id.recycler_view);
		setShowProgressBar(true);
		return parent;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		List<RVButton> items = getItems();
		buttonAdapter = new ButtonAdapter(items) {

			@NonNull
			@Override
			public ButtonVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
				View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vh_button, parent, false);
				int itemHeight = parent.getHeight() / items.size();
				view.setMinimumHeight(itemHeight);
				return new ButtonVH(view);
			}
		};
		buttonAdapter.setOnItemClickListener(this);
		recyclerView.setAdapter(buttonAdapter);

		parent.findViewById(R.id.current_project).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onProgramClick(v);
			}
		});

		projectDir = new File(DEFAULT_ROOT_DIRECTORY, FileMetaDataExtractor
				.encodeSpecialCharsForFileSystem(projectManager.getCurrentProjectName()));
		loadProjectImage();

		setShowProgressBar(false);
	}

	private List<RVButton> getItems() {
		List<RVButton> items = new ArrayList<>();

		items.add(new RVButton(NEW, ContextCompat.getDrawable(getActivity(), R.drawable.ic_main_menu_new),
				getString(R.string.main_menu_new)));
		items.add(new RVButton(PROGRAMS, ContextCompat.getDrawable(getActivity(), R.drawable.ic_main_menu_programs),
				getString(R.string.main_menu_programs)));
		items.add(new RVButton(HELP, ContextCompat.getDrawable(getActivity(), R.drawable.ic_main_menu_help),
				getString(R.string.main_menu_help)));
		items.add(new RVButton(EXPLORE, ContextCompat.getDrawable(getActivity(), R.drawable.ic_main_menu_community),
				getString(R.string.main_menu_web)));
		items.add(new RVButton(UPLOAD, ContextCompat.getDrawable(getActivity(), R.drawable.ic_main_menu_upload),
				getString(R.string.main_menu_upload)));
		return items;
	}

	@Override
	public void onResume() {
		super.onResume();
		setShowProgressBar(false);
		buttonAdapter.items.get(0).subtitle = projectManager.getCurrentProjectName();
		buttonAdapter.notifyDataSetChanged();
		loadProjectImage();
		projectDir = new File(DEFAULT_ROOT_DIRECTORY, FileMetaDataExtractor
				.encodeSpecialCharsForFileSystem(projectManager.getCurrentProjectName()));

		String projectName = getActivity().getIntent().getStringExtra(EXTRA_PROJECT_NAME);
		if (projectName != null) {
			getActivity().getIntent().removeExtra(EXTRA_PROJECT_NAME);
			loadDownloadedProject(projectName);
		}
	}

	private void loadDownloadedProject(String name) {
		File projectDir = new File(DEFAULT_ROOT_DIRECTORY, FileMetaDataExtractor.encodeSpecialCharsForFileSystem(name));
		new ProjectLoadTask(projectDir, getContext())
				.setListener(this)
				.execute();
	}

	public void setShowProgressBar(boolean show) {
		parent.findViewById(R.id.progress_bar).setVisibility(show ? View.VISIBLE : View.GONE);
		recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
	}

	@Override
	public void onItemClick(@ButtonId int id) {
		switch (id) {
			case NEW:
				new NewProjectDialogFragment()
						.show(getFragmentManager(), NewProjectDialogFragment.TAG);
				break;
			case PROGRAMS:
				setShowProgressBar(true);
				startActivity(new Intent(getActivity(), ProjectListActivity.class));
				break;
			case HELP:
				setShowProgressBar(true);
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.CATROBAT_HELP_URL)));
				break;
			case EXPLORE:
				setShowProgressBar(true);
				startActivity(new Intent(getActivity(), WebViewActivity.class));
				break;
			case UPLOAD:
				setShowProgressBar(true);
				Intent intent = new Intent(getActivity(), ProjectUploadActivity.class)
						.putExtra(ProjectUploadActivity.PROJECT_DIR,
								new File(DEFAULT_ROOT_DIRECTORY, FileMetaDataExtractor
										.encodeSpecialCharsForFileSystem(projectManager.getCurrentProjectName())));
				startActivity(intent);
				break;
		}
	}

	public void onProgramClick(View view) {
		switch (view.getId()) {
			case CONTINUE:
				setShowProgressBar(true);

				new ProjectLoadTask(projectDir, getContext())
						.setListener(this)
						.execute();
				break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onLoadFinished(boolean success) {
		if (success) {
			Intent intent = new Intent(getActivity(), ProjectActivity.class);
			intent.putExtra(ProjectActivity.EXTRA_FRAGMENT_POSITION, ProjectActivity.FRAGMENT_SCENES);
			startActivity(intent);
		} else {
			setShowProgressBar(false);
			ToastUtil.showError(getActivity(), R.string.error_load_project);
		}
	}

	private ProjectData getCurrentProject() {
		List<ProjectData> items = new ArrayList<>();

		for (File projectDir : DEFAULT_ROOT_DIRECTORY.listFiles()) {
			File xmlFile = new File(projectDir, CODE_XML_FILE_NAME);
			if (!xmlFile.exists()) {
				continue;
			}

			ProjectMetaDataParser metaDataParser = new ProjectMetaDataParser(xmlFile);

			try {
				items.add(metaDataParser.getProjectMetaData());
			} catch (IOException e) {
				Log.e(TAG, "Well, that's awkward.", e);
			}
		}
		if (items.size() == 0) {
			return null;
		}
		Collections.sort(items, new Comparator<ProjectData>() {
			@Override
			public int compare(ProjectData project1, ProjectData project2) {
				return Long.compare(project2.getLastUsed(), project1.getLastUsed());
			}
		});

		return items.get(0);
	}

	private void loadProjectImage() {
		ProjectData currentProject = getCurrentProject();
		if (currentProject == null) {
			return;
		}
		TextView titleView = parent.findViewById(R.id.title_view);
		titleView.setText(currentProject.getName());
		ProjectAndSceneScreenshotLoader loader =
				new ProjectAndSceneScreenshotLoader(CURRENTTHUMBNAILSIZE, CURRENTTHUMBNAILSIZE);

		String sceneName = XstreamSerializer.extractDefaultSceneNameFromXml(currentProject.getDirectory());
		loader.loadAndShowScreenshot(currentProject.getName(), sceneName, false,
				parent.findViewById(R.id.image_view));
	}
}

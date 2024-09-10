package com.labnex.app.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.labnex.app.R;
import com.labnex.app.bottomsheets.BranchesBottomSheet;
import com.labnex.app.bottomsheets.ProjectLabelsBottomSheet;
import com.labnex.app.bottomsheets.ProjectMembersBottomSheet;
import com.labnex.app.bottomsheets.ProjectMilestonesBottomSheet;
import com.labnex.app.bottomsheets.ProjectReleasesBottomSheet;
import com.labnex.app.bottomsheets.ProjectWikisBottomSheet;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.databinding.ActivityProjectDetailBinding;
import com.labnex.app.helpers.Markdown;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.helpers.TextDrawable.ColorGenerator;
import com.labnex.app.helpers.TextDrawable.TextDrawable;
import com.labnex.app.helpers.Utils;
import com.labnex.app.interfaces.BottomSheetListener;
import com.labnex.app.models.projects.Projects;
import com.labnex.app.models.repository.FileContents;
import com.vdurmont.emoji.EmojiParser;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class ProjectDetailActivity extends BaseActivity
		implements BottomSheetListener, BranchesBottomSheet.UpdateInterface {

	private ActivityProjectDetailBinding binding;
	public ProjectsContext projectsContext;
	private int projectId;
	private String branch;
	private String README;
	private String source;

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		binding = ActivityProjectDetailBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		BranchesBottomSheet.setUpdateListener(ProjectDetailActivity.this);

		projectsContext = ProjectsContext.fromIntent(getIntent());
		projectId = projectsContext.getProjectId();

		Bundle bsBundle = new Bundle();

		if (getIntent().getStringExtra("source") != null) {

			source = getIntent().getStringExtra("source");

			/*if (Objects.requireNonNull(source).equalsIgnoreCase("starred")) {
				binding.projectsText.setText(R.string.starred_projects);
			}*/
		} else {
			source = "";
		}

		binding.filesMainFrame.setOnClickListener(
				releases -> {
					ProjectsContext project =
							new ProjectsContext(projectsContext.getProject(), ctx);
					Intent intent = project.getIntent(ctx, FilesBrowserActivity.class);
					intent.putExtra("source", "project");
					intent.putExtra("projectId", projectId);
					intent.putExtra("branch", branch);
					ctx.startActivity(intent);
				});

		binding.commitsMainFrame.setOnClickListener(
				issues -> {
					ProjectsContext project =
							new ProjectsContext(projectsContext.getProject(), ctx);
					Intent intent = project.getIntent(ctx, CommitsActivity.class);
					intent.putExtra("source", "project");
					intent.putExtra("projectId", projectId);
					intent.putExtra("branch", branch);
					ctx.startActivity(intent);
				});

		binding.issuesMainFrame.setOnClickListener(
				issues -> {
					ProjectsContext project =
							new ProjectsContext(projectsContext.getProject(), ctx);
					Intent intent = project.getIntent(ctx, IssuesActivity.class);
					intent.putExtra("source", "project");
					intent.putExtra("id", projectId);
					ctx.startActivity(intent);
				});

		binding.mergeRequestsMainFrame.setOnClickListener(
				mr -> {
					ProjectsContext project =
							new ProjectsContext(projectsContext.getProject(), ctx);
					Intent intent = project.getIntent(ctx, MergeRequestsActivity.class);
					intent.putExtra("source", "mr");
					intent.putExtra("projectId", projectId);
					ctx.startActivity(intent);
				});

		binding.releasesMainFrame.setOnClickListener(
				releases -> {
					bsBundle.putInt("projectId", projectId);
					ProjectReleasesBottomSheet bottomSheet = new ProjectReleasesBottomSheet();
					bottomSheet.setArguments(bsBundle);
					bottomSheet.show(getSupportFragmentManager(), "projectReleasesBottomSheet");
				});

		binding.bottomAppBar.setNavigationOnClickListener(bottomAppBar -> finish());

		binding.bottomAppBar.setOnMenuItemClickListener(
				menuItem -> {
					if (menuItem.getItemId() == R.id.project_members) {
						bsBundle.putInt("projectId", projectId);
						ProjectMembersBottomSheet bottomSheet = new ProjectMembersBottomSheet();
						bottomSheet.setArguments(bsBundle);
						bottomSheet.show(getSupportFragmentManager(), "projectMembersBottomSheet");
					}
					if (menuItem.getItemId() == R.id.project_labels) {
						bsBundle.putInt("projectId", projectId);
						ProjectLabelsBottomSheet bottomSheet = new ProjectLabelsBottomSheet();
						bottomSheet.setArguments(bsBundle);
						bottomSheet.show(getSupportFragmentManager(), "projectLabelsBottomSheet");
					}
					if (menuItem.getItemId() == R.id.project_wiki) {
						bsBundle.putInt("projectId", projectId);
						ProjectWikisBottomSheet bottomSheet = new ProjectWikisBottomSheet();
						bottomSheet.setArguments(bsBundle);
						bottomSheet.show(getSupportFragmentManager(), "projectWikisBottomSheet");
					}
					if (menuItem.getItemId() == R.id.project_milestones) {
						bsBundle.putInt("projectId", projectId);
						ProjectMilestonesBottomSheet bottomSheet =
								new ProjectMilestonesBottomSheet();
						bottomSheet.setArguments(bsBundle);
						bottomSheet.show(
								getSupportFragmentManager(), "projectMilestonesBottomSheet");
					}
					return false;
				});

		binding.newIssue.setOnClickListener(
				accounts -> {
					ProjectsContext project =
							new ProjectsContext(
									projectsContext.getProjectName(),
									projectsContext.getPath(),
									projectsContext.getProjectId(),
									ctx);
					Intent intent = project.getIntent(ctx, CreateIssueActivity.class);
					ctx.startActivity(intent);
				});

		binding.switchBranch.setOnClickListener(
				branches -> {
					bsBundle.putInt("projectId", projectId);
					bsBundle.putString("source", "project_detail");
					BranchesBottomSheet bottomSheet = new BranchesBottomSheet();
					bottomSheet.setArguments(bsBundle);
					bottomSheet.show(getSupportFragmentManager(), "branchesBottomSheet");
				});

		getProjectInfo();
	}

	@Override
	public void updateDataListener(String str, String type) {

		branch = str;
		binding.branchTitle.setText(str);
	}

	private void getProjectInfo() {

		Call<Projects> call = RetrofitClient.getApiInterface(ctx).getProjectInfo(projectId);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Projects> call, @NonNull Response<Projects> response) {

						Projects projectDetails = response.body();

						if (response.isSuccessful()) {

							if (response.code() == 200) {

								assert projectDetails != null;

								projectsContext.setProject(projectDetails);
								projectsContext.setBranchRef(
										projectsContext.getProject().getDefaultBranch());

								binding.progressBar.setVisibility(View.GONE);
								binding.basicInfoFrame.setVisibility(View.VISIBLE);
								binding.codeSection.setVisibility(View.VISIBLE);
								binding.branchSection.setVisibility(View.VISIBLE);

								if (projectDetails.isArchived()) {
									binding.archivedProjectFrame.setVisibility(View.VISIBLE);
								}

								if (!projectDetails.isArchived()
										&& projectDetails.isIssuesEnabled()
										&& !Objects.equals(source, "most_visited")) {
									binding.newIssue.setVisibility(View.VISIBLE);
								}

								ColorGenerator generator = ColorGenerator.MATERIAL;
								int color = generator.getColor(projectDetails.getName());
								String firstCharacter =
										String.valueOf(projectDetails.getName().charAt(0));

								TextDrawable drawable =
										TextDrawable.builder()
												.beginConfig()
												.useFont(Typeface.DEFAULT)
												.fontSize(16)
												.toUpperCase()
												.width(28)
												.height(28)
												.endConfig()
												.buildRoundRect(firstCharacter, color, 8);

								if (projectDetails.getAvatarUrl() != null
										&& projectDetails
												.getVisibility()
												.equalsIgnoreCase("public")) {

									Glide.with(ctx)
											.load(projectDetails.getAvatarUrl())
											.diskCacheStrategy(DiskCacheStrategy.ALL)
											.placeholder(R.drawable.ic_spinner)
											.centerCrop()
											.into(binding.projectAvatar);
								} else {
									binding.projectAvatar.setImageDrawable(drawable);
								}

								binding.projectName.setText(projectDetails.getName());
								binding.projectPath.setText(projectDetails.getPathWithNamespace());
								if (projectDetails.getDescription() != null
										&& !projectDetails.getDescription().isEmpty()) {
									binding.projectDescription.setVisibility(View.VISIBLE);
									binding.projectDescription.setText(
											projectDetails.getDescription());
								}

								binding.projectStars.setText(
										getResources()
												.getQuantityString(
														R.plurals.project_stars,
														projectDetails.getStarCount(),
														Utils.numberFormatter(
																projectDetails.getStarCount())));
								binding.projectForks.setText(
										getResources()
												.getQuantityString(
														R.plurals.project_forks,
														projectDetails.getForksCount(),
														Utils.numberFormatter(
																projectDetails.getForksCount())));
								binding.issuesOpenCount.setText(
										Utils.numberFormatter(projectDetails.getOpenIssuesCount()));

								binding.branchTitle.setText(projectDetails.getDefaultBranch());
								branch = projectDetails.getDefaultBranch();

								binding.copyProjectUrl.setOnClickListener(
										copy ->
												Utils.copyToClipboard(
														ctx,
														ProjectDetailActivity.this,
														projectDetails.getWebUrl(),
														getString(R.string.copy_url_message)));

								if (projectDetails.getReadmeUrl() != null) {
									README =
											projectDetails
													.getReadmeUrl()
													.substring(
															projectDetails.getReadmeUrl().length()
																	- 9);
									loadProjectReadme();
								}
							}
						}
					}

					@Override
					public void onFailure(@NonNull Call<Projects> call, @NonNull Throwable t) {
						Snackbar.info(
								ProjectDetailActivity.this,
								findViewById(R.id.bottom_app_bar),
								getString(R.string.generic_server_response_error));
					}
				});
	}

	private void loadProjectReadme() {

		Call<FileContents> call =
				RetrofitClient.getApiInterface(ctx)
						.getProjectFileContent(
								projectId, README, projectsContext.getProject().getDefaultBranch());

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<FileContents> call,
							@NonNull Response<FileContents> response) {

						FileContents readmeFile = response.body();

						if (response.isSuccessful()) {

							if (response.code() == 200) {

								assert readmeFile != null;
								binding.projectReadmeSection.setVisibility(View.VISIBLE);

								Markdown.render(
										ctx,
										EmojiParser.parseToUnicode(
												Utils.decodeBase64(readmeFile.getContent())),
										binding.readme,
										projectsContext);
							}
						}
					}

					@Override
					public void onFailure(@NonNull Call<FileContents> call, @NonNull Throwable t) {
						Snackbar.info(
								ProjectDetailActivity.this,
								findViewById(R.id.bottom_app_bar),
								getString(R.string.generic_server_response_error));
					}
				});
	}

	@Override
	public void onButtonClicked(String text) {}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
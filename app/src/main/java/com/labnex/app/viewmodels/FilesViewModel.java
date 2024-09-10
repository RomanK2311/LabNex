package com.labnex.app.viewmodels;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.labnex.app.R;
import com.labnex.app.adapters.FilesAdapter;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.models.repository.Tree;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class FilesViewModel extends ViewModel {

	private MutableLiveData<List<Tree>> mutableList;
	private MutableLiveData<String> next;

	public LiveData<List<Tree>> getFiles(
			Context ctx,
			int id,
			String ref,
			String pageToken,
			String path,
			int resultLimit,
			Activity activity,
			BottomAppBar bottomAppBar) {

		mutableList = new MutableLiveData<>();
		loadInitialList(ctx, id, ref, pageToken, path, resultLimit, activity, bottomAppBar);

		return mutableList;
	}

	public LiveData<String> getLink() {

		next = new MutableLiveData<>();
		return next;
	}

	public void loadInitialList(
			Context ctx,
			int id,
			String ref,
			String pageToken,
			String path,
			int resultLimit,
			Activity activity,
			BottomAppBar bottomAppBar) {

		Call<List<Tree>> call =
				RetrofitClient.getApiInterface(ctx).getFiles(id, ref, "", path, resultLimit);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Tree>> call,
							@NonNull Response<List<Tree>> response) {

						if (response.code() == 200) {
							mutableList.postValue(response.body());
							if (response.headers().get("Link") != null) {
								next.postValue(response.headers().get("Link"));
							}
						} else if (response.code() == 401) {

							Snackbar.info(
									ctx, activity.findViewById(android.R.id.content),
									bottomAppBar, ctx.getString(R.string.not_authorized));
						} else if (response.code() == 403) {

							Snackbar.info(
									ctx,
									activity.findViewById(android.R.id.content),
									bottomAppBar,
									ctx.getString(R.string.access_forbidden_403));
						} else {

							Snackbar.info(
									ctx, activity.findViewById(android.R.id.content),
									bottomAppBar, ctx.getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Tree>> call, @NonNull Throwable t) {
						Snackbar.info(
								ctx,
								activity.findViewById(android.R.id.content),
								bottomAppBar,
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}

	public void loadMore(
			Context ctx,
			int id,
			String ref,
			String pageToken,
			String path,
			int resultLimit,
			FilesAdapter adapter,
			Activity activity,
			BottomAppBar bottomAppBar) {

		Call<List<Tree>> call =
				RetrofitClient.getApiInterface(ctx).getFiles(id, ref, pageToken, path, resultLimit);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Tree>> call,
							@NonNull Response<List<Tree>> response) {

						if (response.isSuccessful()) {

							List<Tree> list = mutableList.getValue();
							assert list != null;
							assert response.body() != null;

							if (!response.body().isEmpty()) {

								list.addAll(response.body());
								adapter.updateList(list);

								if (response.headers().get("Link") != null) {
									next.postValue(response.headers().get("Link"));
								} else {
									adapter.setMoreDataAvailable(false);
								}
							} else {
								adapter.setMoreDataAvailable(false);
							}
						} else {
							Snackbar.info(
									ctx,
									activity.findViewById(android.R.id.content),
									bottomAppBar,
									ctx.getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Tree>> call, @NonNull Throwable t) {
						Snackbar.info(
								ctx,
								activity.findViewById(android.R.id.content),
								bottomAppBar,
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}
}
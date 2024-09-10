package com.labnex.app.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.labnex.app.R;
import com.labnex.app.contexts.AccountContext;
import com.labnex.app.core.CoreApplication;
import com.labnex.app.helpers.AppSettingsInit;
import com.labnex.app.helpers.SharedPrefDB;
import com.labnex.app.helpers.Utils;
import java.util.Locale;

/**
 * @author mmarif
 */
public abstract class BaseActivity extends AppCompatActivity {

	protected SharedPrefDB sharedPrefDB;
	protected Context ctx = this;
	protected Context appCtx;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		this.appCtx = getApplicationContext();
		this.sharedPrefDB = SharedPrefDB.getInstance(appCtx);

		// UserAccountsApi userAccountsApi = BaseApi.getInstance(ctx, UserAccountsApi.class);

		switch (Integer.parseInt(
				AppSettingsInit.getSettingsValue(ctx, AppSettingsInit.APP_THEME_KEY))) {
			case 0:
				setTheme(R.style.AppThemeDark);
				break;
			case 1:
				setTheme(R.style.AppThemeLight);
				break;
			case 2:
				setTheme(R.style.AppTheme);
				break;
			default:
				setTheme(R.style.AppTheme);
				break;
		}

		String[] locale =
				AppSettingsInit.getSettingsValue(ctx, AppSettingsInit.APP_LOCALE_KEY).split("\\|");

		if (locale[0].equals("0")) {
			Utils.setAppLocale(getResources(), Locale.getDefault().getLanguage());
		} else {
			Utils.setAppLocale(getResources(), locale[1]);
		}
	}

	public void onResume() {
		super.onResume();

		if (Boolean.parseBoolean(
						AppSettingsInit.getSettingsValue(ctx, AppSettingsInit.APP_BIOMETRIC_KEY))
				&& !Boolean.parseBoolean(
						AppSettingsInit.getSettingsValue(
								ctx, AppSettingsInit.APP_BIOMETRIC_LIFE_CYCLE_KEY))) {

			Intent unlockIntent = new Intent(ctx, BiometricLockActivity.class);
			ctx.startActivity(unlockIntent);
		}
	}

	public AccountContext getAccount() {
		return ((CoreApplication) getApplication()).currentAccount;
	}
}
package com.google.android.settings.gestures.columbus;

import android.app.ActivityManager;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.homepage.contextualcards.ContextualCardManager$$ExternalSyntheticLambda3;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.widget.SelectorWithWidgetPreference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.function.Supplier;

public class ColumbusAppListPreferenceController extends BasePreferenceController implements SelectorWithWidgetPreference.OnClickListener, LifecycleObserver, OnStart {
    static final String COLUMBUS_LAUNCH_APP_SECURE_KEY = "columbus_launch_app";
    private static final String TAG = "ColumbusAppListPrefCtrl";
    private int mCurrentUser;
    private final LauncherApps mLauncherApps;
    private final MetricsFeatureProvider mMetricsFeatureProvider;
    private final String mOpenAppValue;
    private PreferenceCategory mPreferenceCategory;

    @Override
    public Class getBackgroundWorkerClass() {
        return super.getBackgroundWorkerClass();
    }

    @Override
    public IntentFilter getIntentFilter() {
        return super.getIntentFilter();
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return super.getSliceHighlightMenuRes();
    }

    @Override
    public boolean hasAsyncUpdate() {
        return super.hasAsyncUpdate();
    }

    @Override
    public boolean isPublicSlice() {
        return super.isPublicSlice();
    }

    @Override
    public boolean isSliceable() {
        return super.isSliceable();
    }

    @Override
    public boolean useDynamicSliceSummary() {
        return super.useDynamicSliceSummary();
    }

    public ColumbusAppListPreferenceController(Context context, String str) {
        super(context, str);
        mLauncherApps = (LauncherApps) mContext.getSystemService(LauncherApps.class);
        mOpenAppValue = mContext.getString(R.string.columbus_setting_action_launch_value);
        mMetricsFeatureProvider = FeatureFactory.getFeatureFactory().getMetricsFeatureProvider();
    }

    @Override
    public int getAvailabilityStatus() {
        return ColumbusPreferenceController.isColumbusSupported(mContext) ? 0 : 3;
    }

    @Override
    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        if (isAvailable()) {
            mCurrentUser = ActivityManager.getCurrentUser();
            mPreferenceCategory = (PreferenceCategory) preferenceScreen.findPreference(getPreferenceKey());
            updateAppList();
        }
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        int preferenceCount = mPreferenceCategory.getPreferenceCount();
        if (preferenceCount == 0) {
            return;
        }
        String stringForUser = Settings.Secure.getStringForUser(mContext.getContentResolver(), COLUMBUS_LAUNCH_APP_SECURE_KEY, mCurrentUser);
        for (int i = 0; i < preferenceCount; i++) {
            Preference preference2 = mPreferenceCategory.getPreference(i);
            if (preference2 instanceof ColumbusRadioButtonPreference) {
                ColumbusRadioButtonPreference columbusRadioButtonPreference = (ColumbusRadioButtonPreference) preference2;
                columbusRadioButtonPreference.setChecked(TextUtils.equals(stringForUser, columbusRadioButtonPreference.getKey()));
            }
        }
    }

    @Override
    public void onStart() {
        updateAppList();
    }

    @Override
    public void onRadioButtonClicked(SelectorWithWidgetPreference selectorWithWidgetPreference) {
        if (selectorWithWidgetPreference instanceof ColumbusRadioButtonPreference) {
            ColumbusRadioButtonPreference columbusRadioButtonPreference = (ColumbusRadioButtonPreference) selectorWithWidgetPreference;
            Settings.Secure.putStringForUser(mContext.getContentResolver(), "columbus_action", mOpenAppValue, mCurrentUser);
            Settings.Secure.putStringForUser(mContext.getContentResolver(), COLUMBUS_LAUNCH_APP_SECURE_KEY, columbusRadioButtonPreference.getKey(), mCurrentUser);
            Settings.Secure.putStringForUser(mContext.getContentResolver(), "columbus_launch_app_shortcut", columbusRadioButtonPreference.getKey(), mCurrentUser);
            mMetricsFeatureProvider.action(mContext, 1757, columbusRadioButtonPreference.getKey());
            updateState(mPreferenceCategory);
        }
    }

    private void updateAppList() {
        PreferenceCategory preferenceCategory = mPreferenceCategory;
        if (preferenceCategory == null) {
            return;
        }
        preferenceCategory.removeAll();

        List<LauncherActivityInfo> activityList = mLauncherApps.getActivityList(null, UserHandle.of(mCurrentUser));
        activityList.sort(Comparator.comparing(new Function<LauncherActivityInfo, String>() {
            @Override
            public String apply(LauncherActivityInfo launcherActivityInfo) {
                return lambda$updateAppList$0(launcherActivityInfo);
            }
        }));

        List<ShortcutInfo> queryForShortcuts = queryForShortcuts();
        for (final LauncherActivityInfo launcherActivityInfo : activityList) {
            ArrayList<ShortcutInfo> arrayList = (ArrayList<ShortcutInfo>) queryForShortcuts.stream().filter(new Predicate<ShortcutInfo>() {
                @Override
                public boolean test(ShortcutInfo shortcutInfo) {
                    return lambda$updateAppList$1(launcherActivityInfo, shortcutInfo);
                }
            }).collect(Collectors.toCollection(ArrayList::new));

            final Bundle bundle = new Bundle();
            bundle.putParcelable(COLUMBUS_LAUNCH_APP_SECURE_KEY, launcherActivityInfo.getComponentName());
            bundle.putParcelableArrayList("columbus_app_shortcuts", arrayList);

            makeRadioPreference(launcherActivityInfo.getComponentName().flattenToString(),
                    launcherActivityInfo.getLabel(),
                    launcherActivityInfo.getIcon(DisplayMetrics.DENSITY_DEVICE_STABLE),
                    arrayList.isEmpty() ? null : new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            lambda$updateAppList$2(bundle, view);
                        }
                    });
        }
    }

    public static String lambda$updateAppList$0(LauncherActivityInfo launcherActivityInfo) {
        return launcherActivityInfo.getLabel().toString();
    }

    public static boolean lambda$updateAppList$1(LauncherActivityInfo launcherActivityInfo, ShortcutInfo shortcutInfo) {
        return shortcutInfo.getPackage().equals(launcherActivityInfo.getComponentName().getPackageName());
    }

    public void lambda$updateAppList$2(Bundle bundle, View view) {
        new SubSettingLauncher(mContext).setDestination(ColumbusGestureLaunchAppShortcutSettingsFragment.class.getName()).setSourceMetricsCategory(1871).setExtras(bundle).launch();
    }

    private List queryForShortcuts() {
        List<ShortcutInfo> list;
        LauncherApps.ShortcutQuery shortcutQuery = new LauncherApps.ShortcutQuery();
        shortcutQuery.setQueryFlags(9);
        try {
            list = mLauncherApps.getShortcuts(shortcutQuery, UserHandle.of(mCurrentUser));
        } catch (IllegalStateException | SecurityException e) {
            Log.e(TAG, "Failed to query for shortcuts", e);
            list = null;
        }
        return list == null ? new ArrayList() : list;
    }

    private void makeRadioPreference(String str, CharSequence charSequence, Drawable drawable, View.OnClickListener onClickListener) {
        ColumbusRadioButtonPreference columbusRadioButtonPreference = new ColumbusRadioButtonPreference(mPreferenceCategory.getContext());
        columbusRadioButtonPreference.setKey(str);
        columbusRadioButtonPreference.setTitle(charSequence);
        columbusRadioButtonPreference.setIcon(drawable);
        columbusRadioButtonPreference.setOnClickListener(this);
        columbusRadioButtonPreference.setExtraWidgetOnClickListener(onClickListener);
        mPreferenceCategory.addPreference(columbusRadioButtonPreference);
    }
}

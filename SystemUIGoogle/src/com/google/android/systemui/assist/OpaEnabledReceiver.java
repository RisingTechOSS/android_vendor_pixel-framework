/*
 * Copyright (C) 2022 The PixelExperience Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.systemui.assist;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.dagger.SysUISingleton;
import com.android.systemui.dagger.qualifiers.Background;
import com.android.systemui.dagger.qualifiers.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import lineageos.providers.LineageSettings;

@SysUISingleton
public class OpaEnabledReceiver {
    private final Executor mBgExecutor;
    private final ContentObserver mContentObserver;
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private final Executor mFgExecutor;
    private final OpaEnabledSettings mOpaEnabledSettings;
    private final List<OpaEnabledListener> mListeners = new ArrayList();
    private boolean mIsAGSAAssistant;
    private boolean mIsLongPressHomeEnabled;
    private boolean mIsOpaEligible;
    private boolean mIsOpaEnabled;

    @Inject
    public OpaEnabledReceiver(
            Context context, 
            @Main Executor fgExecutor,
            @Background Executor bgExecutor,
            OpaEnabledSettings opaEnabledSettings) {
        mContext = context;
        mContentResolver = context.getContentResolver();
        mContentObserver = new AssistantContentObserver(context);
        mFgExecutor = fgExecutor;
        mBgExecutor = bgExecutor;
        mOpaEnabledSettings = opaEnabledSettings;
        updateOpaEnabledState(false);
        registerContentObserver();
    }

    public void addOpaEnabledListener(OpaEnabledListener opaEnabledListener) {
        mListeners.add(opaEnabledListener);
        opaEnabledListener.onOpaEnabledReceived(mContext, mIsOpaEligible, mIsAGSAAssistant, mIsOpaEnabled, mIsLongPressHomeEnabled);
    }

    public void onUserSwitching(int i) {
        updateOpaEnabledState(true);
        mContentResolver.unregisterContentObserver(mContentObserver);
        registerContentObserver();
    }

    private void updateOpaEnabledState(final boolean z) {
        mBgExecutor.execute(() -> {
            mIsOpaEligible = mOpaEnabledSettings.isOpaEligible();
            mIsAGSAAssistant = mOpaEnabledSettings.isAgsaAssistant();
            mIsOpaEnabled = mOpaEnabledSettings.isOpaEnabled();
            mIsLongPressHomeEnabled = mOpaEnabledSettings.isLongPressHomeEnabled();
            if (z) {
                mFgExecutor.execute(() -> dispatchOpaEnabledState(mContext));
            }
        });
    }

    public void dispatchOpaEnabledState() {
        dispatchOpaEnabledState(mContext);
    }

    private void dispatchOpaEnabledState(Context context) {
        Log.i("OpaEnabledReceiver", "Dispatching OPA eligble = " + mIsOpaEligible + "; AGSA = " + mIsAGSAAssistant + "; OPA enabled = " + mIsOpaEnabled);
        for (int i = 0; i < mListeners.size(); i++) {
            mListeners.get(i).onOpaEnabledReceived(context, mIsOpaEligible, mIsAGSAAssistant, mIsOpaEnabled, mIsLongPressHomeEnabled);
        }
    }

    private void registerContentObserver() {
        mContentResolver.registerContentObserver(LineageSettings.System.getUriFor(
                LineageSettings.System.KEY_HOME_LONG_PRESS_ACTION), false, mContentObserver,
                UserHandle.USER_ALL);
    }

    private class AssistantContentObserver extends ContentObserver {
        public AssistantContentObserver(Context context) {
            super(new Handler(context.getMainLooper()));
        }

        @Override
        public void onChange(boolean z, Uri uri) {
            updateOpaEnabledState(true);
        }
    }
}

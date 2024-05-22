/*
 * Copyright (C) 2024 RisingOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.systemui.columbus.actions;

import android.app.ActivityOptions;
import android.app.IActivityManager;
import android.app.IUserSwitchObserver;
import android.app.SynchronousUserSwitchObserver;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;

import com.google.android.systemui.columbus.gates.Gate;
import com.google.android.systemui.columbus.gates.SilenceAlertsDisabled;
import com.google.android.systemui.columbus.sensors.GestureSensor;

import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;

public abstract class DeskClockAction extends Action {
    public static final Companion Companion = new Companion(null);
    private boolean alertFiring;
    private final BroadcastReceiver alertReceiver;
    private final Gate.Listener gateListener;
    private boolean receiverRegistered;
    private final SilenceAlertsDisabled silenceAlertsDisabled;
    private final IUserSwitchObserver userSwitchCallback;

    public static final class Companion {
        private Companion() {
        }

        public Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    public DeskClockAction(Context context, SilenceAlertsDisabled silenceAlertsDisabled, IActivityManager iActivityManager) {
        super(context, null, 2, null);
        this.silenceAlertsDisabled = silenceAlertsDisabled;

        this.gateListener = new Gate.Listener() {
            @Override
            public void onGateChanged(Gate gate) {
                updateBroadcastReceiver();
            }
        };

        this.alertReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    String action = intent.getAction();
                    if (Intrinsics.areEqual(action, getAlertAction())) {
                        alertFiring = true;
                    } else if (Intrinsics.areEqual(action, getDoneAction())) {
                        alertFiring = false;
                    }
                }
                updateAvailable();
            }
        };

        this.userSwitchCallback = new SynchronousUserSwitchObserver() {
            @Override
            public void onUserSwitching(int userId) throws RemoteException {
                updateBroadcastReceiver();
            }
        };

        silenceAlertsDisabled.registerListener(gateListener);
        try {
            iActivityManager.registerUserSwitchObserver(userSwitchCallback, "Columbus/DeskClockAct");
        } catch (RemoteException e) {
            Log.e("Columbus/DeskClockAct", "Failed to register user switch observer", e);
        }

        updateBroadcastReceiver();
    }

    private void updateAvailable() {
        setAvailable(alertFiring);
    }

    private void updateBroadcastReceiver() {
        alertFiring = false;
        if (receiverRegistered) {
            getContext().unregisterReceiver(alertReceiver);
            receiverRegistered = false;
        }

        if (!silenceAlertsDisabled.isBlocking()) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(getAlertAction());
            intentFilter.addAction(getDoneAction());
            getContext().registerReceiverAsUser(alertReceiver, UserHandle.CURRENT, intentFilter, "com.android.systemui.permission.SEND_ALERT_BROADCASTS", null, Context.RECEIVER_EXPORTED);
            receiverRegistered = true;
        }

        updateAvailable();
    }

    public abstract Intent createDismissIntent();

    public abstract String getAlertAction();

    public abstract String getDoneAction();

    @Override
    public void onTrigger(GestureSensor.DetectionProperties detectionProperties) {
        Intent dismissIntent = createDismissIntent();
        ActivityOptions options = ActivityOptions.makeBasic();
        options.setDisallowEnterPictureInPictureWhileLaunching(true);
        dismissIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        dismissIntent.putExtra("android.intent.extra.REFERRER", Uri.parse("android-app://" + getContext().getPackageName()));

        try {
            getContext().startActivityAsUser(dismissIntent, options.toBundle(), UserHandle.CURRENT);
        } catch (ActivityNotFoundException e) {
            Log.e("Columbus/DeskClockAct", "Failed to dismiss alert", e);
        }

        alertFiring = false;
        updateAvailable();
    }

    @Override
    public String toString() {
        return super.toString() + " [receiverRegistered -> " + receiverRegistered + ']';
    }
}


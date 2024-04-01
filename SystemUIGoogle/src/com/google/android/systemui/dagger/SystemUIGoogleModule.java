/*
 * Copyright (C) 2023 The PixelExperience Project
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

package com.google.android.systemui.dagger;

import static com.android.systemui.Dependency.ALLOW_NOTIFICATION_LONG_PRESS_NAME;
import static com.android.systemui.Dependency.LEAK_REPORT_EMAIL_NAME;

import android.content.Context;
import android.hardware.SensorPrivacyManager;
import android.os.Handler;
import android.os.PowerManager;

import androidx.annotation.Nullable;

import com.android.systemui.qs.QsEventLogger;
import com.android.keyguard.KeyguardViewController;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.controls.controller.ControlsTileResourceConfiguration;
import com.android.systemui.dagger.SysUISingleton;
import com.android.systemui.dagger.qualifiers.Background;
import com.android.systemui.dagger.qualifiers.Main;
import com.android.systemui.demomode.DemoModeController;
import com.android.systemui.dock.DockManager;
import com.android.systemui.dock.DockManagerImpl;
import com.android.systemui.doze.DozeHost;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.lineage.LineageModule;
import com.android.systemui.media.dagger.MediaModule;
import com.android.systemui.navigationbar.gestural.GestureModule;
import com.android.systemui.plugins.BcSmartspaceDataPlugin;
import com.android.systemui.plugins.qs.QSFactory;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.power.EnhancedEstimates;
import com.android.systemui.qs.dagger.QSModule;
import com.android.systemui.qs.tileimpl.QSFactoryImpl;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsImplementation;
import com.android.systemui.screenshot.ReferenceScreenshotModule;
import com.android.systemui.shade.NotificationShadeWindowControllerImpl;
import com.android.systemui.shade.ShadeExpansionStateManager;
import com.android.systemui.settings.UserContentResolverProvider;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.events.StatusBarEventsModule;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationLockscreenUserManagerImpl;
import com.android.systemui.statusbar.NotificationShadeWindowController;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.notification.collection.provider.VisualStabilityProvider;
import com.android.systemui.statusbar.notification.collection.render.GroupMembershipManager;
import com.android.systemui.statusbar.phone.DozeServiceHost;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.policy.AccessibilityManagerWrapper;
import com.android.systemui.statusbar.policy.AospPolicyModule;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.DeviceProvisionedControllerImpl;
import com.android.systemui.statusbar.policy.IndividualSensorPrivacyController;
import com.android.systemui.statusbar.policy.IndividualSensorPrivacyControllerImpl;
import com.android.systemui.statusbar.policy.SensorPrivacyController;
import com.android.systemui.statusbar.policy.SensorPrivacyControllerImpl;
import com.android.systemui.volume.dagger.VolumeModule;
import com.android.systemui.rotationlock.RotationLockModule;
import com.android.systemui.statusbar.policy.AospPolicyModule;

import com.android.systemui.battery.BatterySaverModule;
import com.android.systemui.settings.dagger.MultiUserUtilsModule;
import com.android.systemui.statusbar.phone.fragment.CollapsedStatusBarFragmentStartableModule;
import com.android.systemui.shade.ShadeModule;
import com.android.systemui.scene.SceneContainerFrameworkModule;
import com.android.systemui.navigationbar.NavigationBarControllerModule;
import com.android.systemui.statusbar.phone.HeadsUpModule;
import com.android.systemui.wallpapers.dagger.WallpaperModule;
import com.android.systemui.statusbar.KeyboardShortcutsModule;

import com.google.android.systemui.NotificationLockscreenUserManagerGoogle;
import com.google.android.systemui.controls.GoogleControlsTileResourceConfigurationImpl;
import com.google.android.systemui.power.dagger.PowerModuleGoogle;
import com.google.android.systemui.qs.dagger.QSModuleGoogle;
import com.google.android.systemui.qs.tileimpl.QSFactoryImplGoogle;
import com.google.android.systemui.qs.tileimpl.GoogleQSModule;
import com.google.android.systemui.reversecharging.ReverseChargingController;
import com.google.android.systemui.reversecharging.dagger.ReverseChargingModule;
import com.google.android.systemui.smartspace.BcSmartspaceDataProvider;
import com.google.android.systemui.smartspace.dagger.SmartspaceGoogleModule;
import com.google.android.systemui.statusbar.dagger.StartCentralSurfacesGoogleModule;
import com.google.android.systemui.statusbar.KeyguardIndicationControllerGoogle;
import com.google.android.systemui.statusbar.policy.BatteryControllerImplGoogle;
import com.google.android.systemui.statusbar.policy.dagger.SystemUIGooglePolicyModule;

import javax.inject.Named;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

import dagger.Lazy;

@Module(includes = {
        CollapsedStatusBarFragmentStartableModule.class,
        GestureModule.class,
        HeadsUpModule.class,
        MediaModule.class,
        MultiUserUtilsModule.class,
        NavigationBarControllerModule.class,
        ShadeModule.class,
        ReferenceScreenshotModule.class,
        RotationLockModule.class,
        SceneContainerFrameworkModule.class,
        VolumeModule.class,
        WallpaperModule.class,
        KeyboardShortcutsModule.class,
        GoogleQSModule.class,
        PowerModuleGoogle.class,
        QSModuleGoogle.class,
        StartCentralSurfacesGoogleModule.class,
        SmartspaceGoogleModule.class,
        ReverseChargingModule.class,
        SystemUIGooglePolicyModule.class,
        LineageModule.class
})

public abstract class SystemUIGoogleModule {

    @SysUISingleton
    @Provides
    @Named(LEAK_REPORT_EMAIL_NAME)
    static String provideLeakReportEmail() {
        return "";
    }

    @Binds
    abstract NotificationLockscreenUserManager bindNotificationLockscreenUserManager(
            NotificationLockscreenUserManagerGoogle notificationLockscreenUserManager);

    @Provides
    @SysUISingleton
    static SensorPrivacyController provideSensorPrivacyController(
            SensorPrivacyManager sensorPrivacyManager) {
        SensorPrivacyController spC = new SensorPrivacyControllerImpl(sensorPrivacyManager);
        spC.init();
        return spC;
    }

    @Provides
    @SysUISingleton
    static IndividualSensorPrivacyController provideIndividualSensorPrivacyController(
            SensorPrivacyManager sensorPrivacyManager) {
        IndividualSensorPrivacyController spC = new IndividualSensorPrivacyControllerImpl(
                sensorPrivacyManager);
        spC.init();
        return spC;
    }

    @SysUISingleton
    @Provides
    @Named(ALLOW_NOTIFICATION_LONG_PRESS_NAME)
    static boolean provideAllowNotificationLongPress() {
        return true;
    }

    @Provides
    @SysUISingleton
    static Recents provideRecents(Context context, RecentsImplementation recentsImplementation,
            CommandQueue commandQueue) {
        return new Recents(context, recentsImplementation, commandQueue);
    }

    @SysUISingleton
    @Provides
    static DeviceProvisionedController bindDeviceProvisionedController(
            DeviceProvisionedControllerImpl deviceProvisionedController) {
        deviceProvisionedController.init();
        return deviceProvisionedController;
    }

    @Binds
    abstract KeyguardViewController bindKeyguardViewController(
            StatusBarKeyguardViewManager statusBarKeyguardViewManager);

    @Binds
    abstract NotificationShadeWindowController bindNotificationShadeController(
            NotificationShadeWindowControllerImpl notificationShadeWindowController);

    @Binds
    abstract DozeHost provideDozeHost(DozeServiceHost dozeServiceHost);

    @Binds
    abstract KeyguardIndicationController bindKeyguardIndicationControllerGoogle(KeyguardIndicationControllerGoogle keyguardIndicationControllerGoogle);

    @Binds
    abstract DockManager bindDockManager(DockManagerImpl dockManager);

    /** */
    @Binds
    @SysUISingleton
    public abstract QSFactory bindQSFactoryGoogle(QSFactoryImplGoogle qsFactoryImpl);

    @Binds
    abstract ControlsTileResourceConfiguration bindControlsTileResourceConfiguration(GoogleControlsTileResourceConfigurationImpl configuration);

    @Provides
    @SysUISingleton
    static BcSmartspaceDataPlugin provideBcSmartspaceDataPlugin() {
        return new BcSmartspaceDataProvider();
    }
}

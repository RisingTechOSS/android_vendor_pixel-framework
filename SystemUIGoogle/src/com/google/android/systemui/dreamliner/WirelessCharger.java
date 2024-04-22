/*
 * Copyright (C) 2022 The PixelExperience Project
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

package com.google.android.systemui.dreamliner;

import android.os.Bundle;
import java.util.ArrayList;

public interface WirelessCharger {

    interface AlignInfoListener {
        void onAlignInfoChanged(DockAlignInfo dockAlignInfo);
    }

    interface ChallengeCallback {
        void onCallback(int i, ArrayList<Byte> arrayList);
    }

    interface GetFanInformationCallback {
        void onCallback(int i, Bundle bundle);
    }

    interface GetFanSimpleInformationCallback {
        void onCallback(int i, Bundle bundle);
    }

    interface GetFeaturesCallback {
        void onCallback(int i, long j);
    }

    interface GetInformationCallback {
        void onCallback(int i, DockInfo dockInfo);
    }

    interface GetWpcAuthCertificateCallback {
        void onCallback(int i, ArrayList<Byte> arrayList);
    }

    interface GetWpcAuthChallengeResponseCallback {
        void onCallback(int i, byte b, byte b2, byte b3, ArrayList<Byte> arrayList, ArrayList<Byte> arrayList2);
    }

    interface GetWpcAuthDigestsCallback {
        void onCallback(int i, byte b, byte b2, ArrayList<byte[]> arrayList);
    }

    interface IsDockPresentCallback {
        void onCallback(boolean z, byte b, byte b2, boolean z2, int i);
    }

    interface KeyExchangeCallback {
        void onCallback(int i, byte b, ArrayList<Byte> arrayList);
    }

    interface SetFanCallback {
        void onCallback(int i, Bundle bundle);
    }

    interface SetFeaturesCallback {
        void onCallback(int i);
    }

    void asyncIsDockPresent(IsDockPresentCallback isDockPresentCallback);

    void challenge(byte b, byte[] bArr, ChallengeCallback challengeCallback);

    void getFanInformation(byte b, GetFanInformationCallback getFanInformationCallback);

    int getFanLevel();

    void getFanSimpleInformation(byte b, GetFanSimpleInformationCallback getFanSimpleInformationCallback);

    void getFeatures(long j, GetFeaturesCallback getFeaturesCallback);

    void getInformation(GetInformationCallback getInformationCallback);

    void getWpcAuthCertificate(byte b, short s, short s2, GetWpcAuthCertificateCallback getWpcAuthCertificateCallback);

    void getWpcAuthChallengeResponse(byte b, byte[] bArr, GetWpcAuthChallengeResponseCallback getWpcAuthChallengeResponseCallback);

    void getWpcAuthDigests(byte b, GetWpcAuthDigestsCallback getWpcAuthDigestsCallback);

    void keyExchange(byte[] bArr, KeyExchangeCallback keyExchangeCallback);

    void registerAlignInfo(AlignInfoListener alignInfoListener);

    void setFan(byte b, byte b2, int i, SetFanCallback setFanCallback);

    void setFeatures(long j, long j2, SetFeaturesCallback setFeaturesCallback);
}

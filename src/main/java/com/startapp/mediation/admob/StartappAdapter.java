/*
 * Copyright 2020 StartApp Inc
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * if you take (copy/paste) this file to your own project
 * change this package path to your own as well
 * <p>
 * if you take (copy/paste) this file to your own project
 * change this package path to your own as well
 * <p>
 * if you take (copy/paste) this file to your own project
 * change this package path to your own as well
 */

/* TODO
 * if you take (copy/paste) this file to your own project
 * change this package path to your own as well
 */
package com.startapp.mediation.admob;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.mediation.Adapter;
import com.google.android.gms.ads.mediation.InitializationCompleteCallback;
import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.MediationConfiguration;
import com.google.android.gms.ads.mediation.MediationRewardedAd;
import com.google.android.gms.ads.mediation.MediationRewardedAdCallback;
import com.google.android.gms.ads.mediation.MediationRewardedAdConfiguration;
import com.google.android.gms.ads.mediation.NativeMediationAdRequest;
import com.google.android.gms.ads.mediation.UnifiedNativeAdMapper;
import com.google.android.gms.ads.mediation.VersionInfo;
import com.google.android.gms.ads.mediation.customevent.CustomEventBanner;
import com.google.android.gms.ads.mediation.customevent.CustomEventBannerListener;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitial;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitialListener;
import com.google.android.gms.ads.mediation.customevent.CustomEventNative;
import com.google.android.gms.ads.mediation.customevent.CustomEventNativeListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.startapp.mediation.common.StartAppMediationExtras;
import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.ads.banner.BannerBase;
import com.startapp.sdk.ads.banner.BannerListener;
import com.startapp.sdk.ads.banner.Mrec;
import com.startapp.sdk.ads.banner.banner3d.Banner3D;
import com.startapp.sdk.ads.nativead.NativeAdDetails;
import com.startapp.sdk.ads.nativead.NativeAdDisplayListener;
import com.startapp.sdk.ads.nativead.NativeAdInterface;
import com.startapp.sdk.ads.nativead.NativeAdPreferences;
import com.startapp.sdk.ads.nativead.StartAppNativeAd;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.startapp.sdk.adsbase.adlisteners.VideoListener;
import com.startapp.sdk.adsbase.model.AdPreferences;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Keep
@SuppressWarnings("deprecation")
public class StartappAdapter extends Adapter implements CustomEventInterstitial, CustomEventBanner, MediationRewardedAd, CustomEventNative {
    private static final String LOG_TAG = StartappAdapter.class.getSimpleName();

    // region Lifecycle
    @Override
    public void onDestroy() {
        removeFromParent(bannerContainer);
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onResume() {
    }
    // endregion

    //region Extras
    public static class Extras extends StartAppMediationExtras {
        boolean is3DBanner() {
            return isBanner3d();
        }

        Extras(
                @NonNull MediationAdRequest mediationAdRequest,
                @Nullable Bundle customEventExtras,
                @Nullable String serverParameter
        ) {
            super(customEventExtras, serverParameter, false, false);
            setKeywords(getAdPreferences(), mediationAdRequest);
            setLocation(getAdPreferences(), mediationAdRequest);
        }

        @SuppressWarnings("ConstantConditions")
        Extras(
                @NonNull NativeMediationAdRequest mediationAdRequest,
                @Nullable Bundle customEventExtras,
                @Nullable String serverParameter
        ) {
            super(customEventExtras, serverParameter, true, mediationAdRequest.getNativeAdOptions() != null && !mediationAdRequest.getNativeAdOptions().shouldReturnUrlsForImageAssets());
            setKeywords(getAdPreferences(), mediationAdRequest);
            setLocation(getAdPreferences(), mediationAdRequest);
        }

        Extras(@NonNull MediationRewardedAdConfiguration configuration) {
            super(configuration.getMediationExtras(), configuration.getServerParameters().getString("parameter"), false, false);

            if (configuration.getLocation() != null) {
                getAdPreferences().setLongitude(configuration.getLocation().getLongitude());
                getAdPreferences().setLatitude(configuration.getLocation().getLatitude());
            }
        }

        @SuppressWarnings("ConstantConditions")
        private static void setKeywords(@NonNull AdPreferences prefs, @NonNull MediationAdRequest request) {
            Set<String> keywords = request.getKeywords();
            if (keywords == null) {
                return;
            }

            StringBuilder sb = new StringBuilder();
            for (String keyWord : keywords) {
                sb.append(keyWord).append(",");
            }

            prefs.setKeywords(sb.substring(0, sb.length() - 1));
        }

        @SuppressWarnings("ConstantConditions")
        private static void setLocation(@NonNull AdPreferences prefs, @NonNull MediationAdRequest request) {
            Location location = request.getLocation();
            if (location == null) {
                return;
            }

            prefs.setLongitude(request.getLocation().getLongitude());
            prefs.setLatitude(request.getLocation().getLatitude());
        }

        @SuppressWarnings("unused")
        public static class Builder extends StartAppMediationExtras.Builder {
            // none
        }
    }
    //endregion

    //region Utils
    private static final AtomicBoolean isInitialized = new AtomicBoolean(false);

    private static void initializeIfNecessary(@NonNull Context context, @Nullable String appId, boolean testAds) {
        if (appId == null || appId.trim().length() < 1) {
            Log.e("StartAppSDK", "App ID not found\n" +
                    "+-----------------------------------------------------------------------+\n" +
                    "|                S   T   A   R   T   A   P   P                          |\n" +
                    "| - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - |\n" +
                    "| AdMob Mediation is configured wrong, App ID not found                 |\n" +
                    "| Put your App ID as Parameter for Custom Event:                        |\n" +
                    "|                                                                       |\n" +
                    "|              { startappAppId : 'YOUR_APP_ID' }                        |\n" +
                    "|                                                                       |\n" +
                    "| https://support.start.io/hc/en-us/articles/360005100893-AdMob-Adapter |\n" +
                    "+-----------------------------------------------------------------------+\n");
            return;
        }

        if (!isInitialized.getAndSet(true)) {
            StartAppSDK.setTestAdsEnabled(testAds);
            StartAppAd.disableSplash();
            StartAppAd.enableConsent(context, false);
            StartAppSDK.addWrapper(context, "AdMob", BuildConfig.VERSION_NAME);
            StartAppSDK.init(context, appId, false);
        }
    }

    private static void removeFromParent(@Nullable View view) {
        if (view == null || view.getParent() == null) {
            return;
        }

        if (view.getParent() instanceof ViewGroup) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }
    //endregion

    //region Interstitial
    @Nullable
    private StartAppAd interstitial;

    @Nullable
    private CustomEventInterstitialListener interstitialListener;

    @Override
    public void requestInterstitialAd(
            @NonNull Context context,
            @NonNull final CustomEventInterstitialListener listener,
            @Nullable String serverParameter,
            @NonNull MediationAdRequest mediationAdRequest,
            @Nullable Bundle customEventExtras
    ) {
        final Extras extras = new Extras(mediationAdRequest, customEventExtras, serverParameter);

        initializeIfNecessary(context, extras.getAppId(), mediationAdRequest.isTesting());

        interstitialListener = listener;
        interstitial = new StartAppAd(context);
        final AdEventListener loadListener = new AdEventListener() {
            @Override
            public void onReceiveAd(@NonNull Ad ad) {
                listener.onAdLoaded();
            }

            @Override
            public void onFailedToReceiveAd(@Nullable Ad ad) {
                String message = ad != null ? ad.getErrorMessage() : null;
                Log.v(LOG_TAG, "ad loading failed: " + message);
                listener.onAdFailedToLoad(messageToError(message));
            }
        };

        if (extras.getAdMode() == null) {
            interstitial.loadAd(extras.getAdPreferences(), loadListener);
        } else {
            interstitial.loadAd(extras.getAdMode(), extras.getAdPreferences(), loadListener);
        }
    }

    @Override
    public void showInterstitial() {
        if (interstitial == null) {
            return;
        }

        interstitial.showAd(new AdDisplayListener() {
            @Override
            public void adHidden(@NonNull Ad ad) {
                if (interstitialListener == null) {
                    return;
                }

                interstitialListener.onAdClosed();
            }

            @Override
            public void adDisplayed(@NonNull Ad ad) {
                if (interstitialListener == null) {
                    return;
                }

                interstitialListener.onAdOpened();
            }

            @Override
            public void adClicked(@NonNull Ad ad) {
                if (interstitialListener == null) {
                    return;
                }

                interstitialListener.onAdClicked();
            }

            @Override
            public void adNotDisplayed(@NonNull Ad ad) {
            }
        });
    }
    //endregion

    //region Banner
    @Nullable
    private FrameLayout bannerContainer;

    @Override
    public void requestBannerAd(
            @NonNull Context context,
            @NonNull final CustomEventBannerListener listener,
            @Nullable String serverParameter,
            @NonNull AdSize adSize,
            @NonNull MediationAdRequest mediationAdRequest,
            @Nullable Bundle customEventExtras
    ) {
        if (!(context instanceof Activity)) {
            return;
        }

        bannerContainer = new FrameLayout(context);
        BannerListener loadListener = new BannerListener() {
            @Override
            public void onReceiveAd(@NonNull View view) {
                listener.onAdLoaded(bannerContainer);
            }

            @Override
            public void onFailedToReceiveAd(@NonNull View view) {
                listener.onAdFailedToLoad(AdRequest.ERROR_CODE_NO_FILL);
            }

            @Override
            public void onImpression(@NonNull View view) {
            }

            @Override
            public void onClick(@NonNull View view) {
                listener.onAdClicked();
                listener.onAdOpened();
            }
        };

        BannerBase banner = loadBanner(context, serverParameter, adSize, mediationAdRequest, customEventExtras, loadListener);
        // force banner to calculate its view size
        bannerContainer.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        bannerContainer.addView(banner, new FrameLayout.LayoutParams(
                adSize.getWidthInPixels(context),
                adSize.getHeightInPixels(context),
                Gravity.CENTER));
    }

    @NonNull
    private BannerBase loadBanner(
            @NonNull Context context,
            @Nullable String serverParameter,
            @NonNull AdSize adSize,
            @NonNull MediationAdRequest mediationAdRequest,
            @Nullable Bundle customEventExtras,
            @NonNull BannerListener loadListener
    ) {
        Extras extras = new Extras(mediationAdRequest, customEventExtras, serverParameter);
        initializeIfNecessary(context, extras.getAppId(), mediationAdRequest.isTesting());

        final Activity activity = (Activity) context;
        final BannerBase result;

        if (adSize.equals(AdSize.MEDIUM_RECTANGLE)) {
            result = new Mrec(activity, extras.getAdPreferences(), loadListener);
        } else if (extras.is3DBanner()) {
            result = new Banner3D(activity, extras.getAdPreferences(), loadListener);
        } else {
            result = new Banner(activity, extras.getAdPreferences(), loadListener);
        }

        result.loadAd(adSize.getWidth(), adSize.getHeight());
        return result;
    }
    //endregion

    //region Rewarded
    @Nullable
    private StartAppAd rewarded;

    @Nullable
    private MediationRewardedAdCallback rewardedListener;

    @Override
    public void initialize(
            @NonNull Context context,
            @NonNull InitializationCompleteCallback completeCallback,
            @NonNull List<MediationConfiguration> list
    ) {
        completeCallback.onInitializationSucceeded();
    }

    @Override
    @NonNull
    public VersionInfo getVersionInfo() {
        final String[] parts = BuildConfig.VERSION_NAME.split("\\.");
        if (parts.length < 3) {
            return new VersionInfo(0, 0, 1);
        }

        try {
            return new VersionInfo(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        } catch (Exception e) {
            return new VersionInfo(0, 0, 1);
        }
    }

    @Override
    @NonNull
    public VersionInfo getSDKVersionInfo() {
        String version = null;

        try {
            version = (String) StartAppSDK.class.getDeclaredMethod("getVersion").invoke(null);
        } catch (Throwable ex) {
            // ignore
        }

        if (version == null) {
            try {
                version = (String) Class.forName("com.startapp.sdk.GeneratedConstants")
                        .getDeclaredField("INAPP_VERSION")
                        .get(null);
            } catch (Throwable ex) {
                // ignore
            }
        }

        if (version == null) {
            return new VersionInfo(0, 0, 1);
        }

        final String[] parts = version.split("\\.");
        if (parts.length < 3) {
            return new VersionInfo(0, 0, 1);
        }

        try {
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            int patch = Integer.parseInt(leadingDigits(parts[2]));
            return new VersionInfo(major, minor, patch);
        } catch (Throwable e) {
            return new VersionInfo(0, 0, 1);
        }
    }

    private static String leadingDigits(String input) {
        for (int i = 0, n = input.length(); i < n; ++i) {
            char c = input.charAt(i);

            if (c < '0' || c > '9') {
                return input.substring(0, i);
            }
        }

        return input;
    }

    @Override
    public void loadRewardedAd(
            @NonNull MediationRewardedAdConfiguration adConfiguration,
            @NonNull final MediationAdLoadCallback<MediationRewardedAd, MediationRewardedAdCallback> loadCallback
    ) {
        Context context = adConfiguration.getContext();
        Extras extras = new Extras(adConfiguration);
        initializeIfNecessary(context, extras.getAppId(), adConfiguration.isTestRequest());

        rewarded = new StartAppAd(context);
        rewarded.setVideoListener(new VideoListener() {
            @Override
            public void onVideoCompleted() {
                if (rewardedListener != null) {
                    rewardedListener.onVideoComplete();
                    rewardedListener.onUserEarnedReward(new StartappRewardItem());
                }
            }
        });

        final AdEventListener loadListener = new AdEventListener() {
            @Override
            public void onReceiveAd(@NonNull Ad ad) {
                rewardedListener = loadCallback.onSuccess(StartappAdapter.this);
            }

            @Override
            public void onFailedToReceiveAd(@Nullable Ad ad) {
                String message = ad != null ? ad.getErrorMessage() : null;
                Log.v(LOG_TAG, "ad loading failed: " + message);
                loadCallback.onFailure(messageToError(message));
            }
        };

        rewarded.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, extras.getAdPreferences(), loadListener);
    }

    @Override
    public void showAd(@NonNull Context context) {
        if (rewarded == null) {
            return;
        }

        rewarded.showAd(new AdDisplayListener() {
            @Override
            public void adHidden(Ad ad) {
                if (rewardedListener != null) {
                    rewardedListener.onAdClosed();
                }
            }

            @Override
            public void adDisplayed(Ad ad) {
                if (rewardedListener != null) {
                    rewardedListener.onAdOpened();
                    rewardedListener.onVideoStart();
                    rewardedListener.reportAdImpression();
                }
            }

            @Override
            public void adClicked(Ad ad) {
                if (rewardedListener != null) {
                    rewardedListener.reportAdClicked();
                }
            }

            @Override
            public void adNotDisplayed(Ad ad) {
                if (rewardedListener != null) {
                    String message = ad.getErrorMessage();
                    rewardedListener.onAdFailedToShow(
                            new AdError(0, message != null ? message : "adNotDisplayed", "io.start"));
                }
            }
        });
    }

    public static class StartappRewardItem implements RewardItem {
        @NonNull
        @Override
        public String getType() {
            return "";
        }

        @Override
        public int getAmount() {
            return 1;
        }
    }
    //endregion

    //region Native
    @Override
    public void requestNativeAd(
            @NonNull final Context context,
            @NonNull final CustomEventNativeListener listener,
            @Nullable String serverParameter,
            @NonNull NativeMediationAdRequest mediationAdRequest,
            @Nullable Bundle customEventExtras
    ) {
        Extras extras = new Extras(mediationAdRequest, customEventExtras, serverParameter);
        initializeIfNecessary(context, extras.getAppId(), mediationAdRequest.isTesting());

        final StartAppNativeAd startappAds = new StartAppNativeAd(context);
        final NativeAdPreferences prefs = (NativeAdPreferences) extras.getAdPreferences();

        startappAds.loadAd(prefs, new AdEventListener() {
            @Override
            public void onReceiveAd(@NonNull Ad ad) {
                final ArrayList<NativeAdDetails> ads = startappAds.getNativeAds();
                if (ads != null && !ads.isEmpty()) {
                    listener.onAdLoaded(new NativeMapper(context, ads.get(0), prefs.isContentAd(), listener));
                } else {
                    Log.v(LOG_TAG, "ad loading failed: no fill");
                    listener.onAdFailedToLoad(messageToError("204"));
                }
            }

            @Override
            public void onFailedToReceiveAd(@Nullable Ad ad) {
                final String message = ad != null ? ad.getErrorMessage() : null;
                Log.v(LOG_TAG, "ad loading failed: " + message);
                listener.onAdFailedToLoad(messageToError(message));
            }
        });
    }

    private static class NativeMapper extends UnifiedNativeAdMapper {
        @NonNull
        private final NativeAdDetails details;

        @NonNull
        private final WeakReference<CustomEventNativeListener> listener;

        NativeMapper(
                @NonNull Context context,
                @NonNull NativeAdDetails details,
                boolean isContentAd,
                @NonNull CustomEventNativeListener listener
        ) {
            this.details = details;
            this.listener = new WeakReference<>(listener);

            setHasVideoContent(false);
            setHeadline(details.getTitle());
            setBody(details.getDescription());
            setCallToAction(details.getCallToAction());
            setStarRating((double) details.getRating());

            if (!isContentAd) {
                if (!TextUtils.isEmpty(details.getImageUrl())) {
                    final Uri uri = Uri.parse(details.getImageUrl());
                    if (uri != null) {
                        setImages(Collections.<NativeAd.Image>singletonList(new MappedImage(context, uri, details.getImageBitmap())));
                    }
                }

                if (!TextUtils.isEmpty(details.getSecondaryImageUrl())) {
                    final Uri uri = Uri.parse(details.getSecondaryImageUrl());
                    if (uri != null) {
                        setIcon(new MappedImage(context, uri, details.getSecondaryImageBitmap()));
                    }
                }
            }

            setOverrideClickHandling(true);
            setOverrideImpressionRecording(true);
        }

        @Override
        public void trackViews(
                @NonNull View containerView,
                @NonNull Map<String, View> clickableAssetViews,
                @NonNull Map<String, View> nonclickableAssetViews
        ) {
            List<View> clickableViews = new ArrayList<>(clickableAssetViews.values());
            details.registerViewForInteraction(containerView, clickableViews, new NativeAdDisplayListener() {
                @Override
                public void adHidden(@NonNull NativeAdInterface nativeAdInterface) {
                    CustomEventNativeListener callbacks = listener.get();
                    if (callbacks != null) {
                        callbacks.onAdClosed();
                    }
                }

                @Override
                public void adDisplayed(@NonNull NativeAdInterface nativeAdInterface) {
                    CustomEventNativeListener callbacks = listener.get();
                    if (callbacks != null) {
                        callbacks.onAdImpression();
                    }
                }

                @Override
                public void adClicked(@NonNull NativeAdInterface nativeAdInterface) {
                    CustomEventNativeListener callbacks = listener.get();
                    if (callbacks != null) {
                        callbacks.onAdClicked();
                        callbacks.onAdOpened();
                    }
                }

                @Override
                public void adNotDisplayed(@NonNull NativeAdInterface nativeAdInterface) {
                }
            });
        }

        @Override
        public void untrackView(@NonNull View view) {
            details.unregisterView();
        }
    }

    private static class MappedImage extends NativeAd.Image {
        @NonNull
        private final Context context;

        @NonNull
        private final Uri uri;

        @Nullable
        private final Bitmap bitmap;

        MappedImage(@NonNull Context context, @NonNull Uri uri, @Nullable Bitmap bitmap) {
            this.context = context;
            this.uri = uri;
            this.bitmap = bitmap;
        }

        @NonNull
        @Override
        public Drawable getDrawable() {
            if (bitmap == null) {
                return new ColorDrawable(Color.TRANSPARENT);
            }

            return new BitmapDrawable(context.getResources(), bitmap);
        }

        @Override
        @NonNull
        public Uri getUri() {
            return uri;
        }

        @Override
        public double getScale() {
            return 1.0;
        }
    }
    //endregion

    @NonNull
    private static AdError messageToError(@Nullable String message) {
        boolean isNoFill = message != null && (message.contains("204") || message.contains("Empty Response"));
        return new AdError(isNoFill
                ? AdRequest.ERROR_CODE_MEDIATION_NO_FILL
                : AdRequest.ERROR_CODE_INTERNAL_ERROR,
                isNoFill ? "No Fill" : "Internal error",
                "io.start");
    }
}

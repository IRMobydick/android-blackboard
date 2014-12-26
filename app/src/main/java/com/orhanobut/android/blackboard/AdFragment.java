package com.orhanobut.android.blackboard;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

/**
 * @author Orhan Obut
 */
public class AdFragment extends Fragment {

    private InterstitialAd interstitial;

    public static Fragment newInstance(){
        return new AdFragment();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ad, container, false);
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
//        AdView mAdView = (AdView) getView().findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);

        // Create the interstitial.
        interstitial = new InterstitialAd(getActivity());
        interstitial.setAdUnitId(AdRequest.DEVICE_ID_EMULATOR);

        // Create ad request.
        AdRequest adRequest = new AdRequest.Builder().build();

        interstitial.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
            }
        });
        
        // Begin loading your interstitial.
        interstitial.loadAd(adRequest);

    }

    // Invoke displayInterstitial() when you are ready to display an interstitial.
    public void displayInterstitial() {
        if (interstitial.isLoaded()) {
            interstitial.show();
        }
    }

}

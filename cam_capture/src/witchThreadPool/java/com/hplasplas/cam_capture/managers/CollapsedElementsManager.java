/*
 * Copyright © 2018. Dmitry Starkin Contacts: t0506803080@gmail.com
 *
 * This file is part of cam_capture
 *
 *     cam_capture is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *    cam_capture is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with cam_capture  If not, see <http://www.gnu.org/licenses/>.
 */
package com.hplasplas.cam_capture.managers;

import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hplasplas.cam_capture.R;
import com.hplasplas.cam_capture.activitys.CamCapture;
import com.hplasplas.cam_capture.util.MainHandler;

import static com.hplasplas.cam_capture.setting.Constants.BOTTOM_PANEL_IDLE_TIME;
import static com.hplasplas.cam_capture.setting.Constants.DEBUG;
import static com.hplasplas.cam_capture.setting.Constants.FAB_ANIMATION_DURATION;
import static com.hplasplas.cam_capture.setting.Constants.MESSAGE_PANEL_MUST_HIDE;

/**
 * Created by StarkinDG on 29.03.2017.
 */

public class CollapsedElementsManager implements View.OnTouchListener {
    
    private final String TAG = getClass().getSimpleName();
    
    private CamCapture mActivity;
    private TextView mFilesInFolderText;
    private FloatingActionButton mButton;
    private CardView mFilesInFolderTextCard;
    private BottomSheetBehavior<LinearLayout> mBottomSheetBehavior;
    
    public CollapsedElementsManager(CamCapture activity) {
        
        mActivity = activity;
        if (mActivity != null) {
            findViews();
            adjustViews();
        }
    }
    
    private void findViews() {
        
        mFilesInFolderText = (TextView) mActivity.findViewById(R.id.files_in_folder);
        mFilesInFolderTextCard = (CardView) mActivity.findViewById(R.id.files_in_folder_card);
        mButton = (FloatingActionButton) mActivity.findViewById(R.id.fab_photo);
        mButton.setOnTouchListener(this);
        LinearLayout bottomPanel = (LinearLayout) mActivity.findViewById(R.id.photo_list_container);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomPanel);
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) mActivity.findViewById(R.id.main_window);
        coordinatorLayout.setOnTouchListener(this);
    }
    
    private void adjustViews() {
        
        if (mButton != null && mBottomSheetBehavior != null) {
            mButton.setOnClickListener(v -> {
                enableButton(false);
                mActivity.makePhoto();
            });
            
            mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    
                    if (BottomSheetBehavior.STATE_HIDDEN == newState) {
                        mButton.animate().scaleX(0).scaleY(0).setDuration(FAB_ANIMATION_DURATION).start();
                        mFilesInFolderTextCard.animate().scaleX(0).scaleY(0).setDuration(FAB_ANIMATION_DURATION).start();
                        enableButton(false);
                    } else if (BottomSheetBehavior.STATE_EXPANDED == newState) {
                        mButton.animate().scaleX(1).scaleY(1).setDuration(FAB_ANIMATION_DURATION).start();
                        mFilesInFolderTextCard.animate().scaleX(1).scaleY(1).setDuration(FAB_ANIMATION_DURATION).start();
                        enableButton(true);
                    }
                }
                
                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    
                }
            });
        }
    }
    
    public void enableButton(boolean state) {
        
        if (mButton != null) {
            mButton.setEnabled(state);
        }
    }
    
    private void changeBottomPanelVisibility() {
        
        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            showBottomPanel();
        } else {
            hideBottomPanel();
        }
    }
    
    public void hideBottomPanel() {
        
        stopTimer();
        if (mActivity != null && mBottomSheetBehavior != null) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }
    
    private void showBottomPanel() {
        
        restartTimer();
        if (mBottomSheetBehavior != null) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }
    
    private void setInterfaceElementsScale(float scale) {
        if(scale < 1){
            enableButton(false);
        } else {
            enableButton(true);
        }
        mButton.setScaleX(scale);
        mButton.setScaleY(scale);
        mFilesInFolderTextCard.setScaleX(scale);
        mFilesInFolderTextCard.setScaleY(scale);
    }
    
    public void setRightVisibilityInterfaceElements() {
        
        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            setInterfaceElementsScale(0);
        } else {
            setInterfaceElementsScale(1);
        }
    }
    
    public void startTimerIfNeed() {
        
        if (mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            restartTimer();
        }
    }
    
    public void setFilesInFolderText(int filesInFolder) {
        
        if (mActivity != null && mFilesInFolderText != null) {
            mFilesInFolderText.setText(mActivity.getResources().getString(R.string.files_in_folder, filesInFolder));
        }
    }
    
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        
        if (DEBUG) {
            Log.d(TAG, "onTouch: ");
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (v.getId() != R.id.fab_photo) {
                changeBottomPanelVisibility();
                return true;
            } else {
                restartTimer();
                return false;
            }
        }
        return false;
    }
    
    public void restartTimer() {
        
        MainHandler handler = MainHandler.getInstance();
        handler.removeMessages(MESSAGE_PANEL_MUST_HIDE);
        Message message = handler.obtainMessage(MESSAGE_PANEL_MUST_HIDE, this);
        handler.sendMessageDelayed(message, BOTTOM_PANEL_IDLE_TIME);
    }
    
    public void stopTimer() {
        
        MainHandler.getInstance().removeMessages(MESSAGE_PANEL_MUST_HIDE);
    }
}
// Copyright 2013 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.ui.autofill;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;

import org.chromium.ui.DropdownAdapter;
import org.chromium.ui.DropdownItem;
import org.chromium.ui.DropdownPopupWindow;
import org.chromium.ui.base.ViewAndroidDelegate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * The Autofill suggestion popup that lists relevant suggestions.
 */
public class AutofillPopup extends DropdownPopupWindow implements AdapterView.OnItemClickListener {

    /**
     * Constants defining types of Autofill suggestion entries.
     * Has to be kept in sync with enum in WebAutofillClient.h
     *
     * Not supported: MenuItemIDWarningMessage, MenuItemIDClearForm, and
     * MenuItemIDAutofillOptions.
     */
    private static final int ITEM_ID_AUTOCOMPLETE_ENTRY = 0;
    private static final int ITEM_ID_PASSWORD_ENTRY = -2;
    private static final int ITEM_ID_SEPARATOR_ENTRY = -3;
    private static final int ITEM_ID_DATA_LIST_ENTRY = -6;

    private final Context mContext;
    private final AutofillPopupDelegate mAutofillCallback;
    private List<AutofillSuggestion> mSuggestions;


    /**
     * An interface to handle the touch interaction with an AutofillPopup object.
     */
    public interface AutofillPopupDelegate {
        /**
         * Requests the controller to hide AutofillPopup.
         */
        public void requestHide();

        /**
         * Handles the selection of an Autofill suggestion from an AutofillPopup.
         * @param listIndex The index of the selected Autofill suggestion.
         */
        public void suggestionSelected(int listIndex);
    }

    /**
     * Creates an AutofillWindow with specified parameters.
     * @param context Application context.
     * @param viewAndroidDelegate View delegate used to add and remove views.
     * @param autofillCallback A object that handles the calls to the native AutofillPopupView.
     */
    public AutofillPopup(Context context, ViewAndroidDelegate viewAndroidDelegate,
            AutofillPopupDelegate autofillCallback) {
        super(context, viewAndroidDelegate);
        mContext = context;
        mAutofillCallback = autofillCallback;

        setOnItemClickListener(this);
    }

    /**
     * Filters the Autofill suggestions to the ones that we support and shows the popup.
     * @param suggestions Autofill suggestion data.
     */
    public void filterAndShow(AutofillSuggestion[] suggestions) {
        mSuggestions = new ArrayList<AutofillSuggestion>(Arrays.asList(suggestions));
        // Remove the AutofillSuggestions with IDs that are not supported by Android
        ArrayList<DropdownItem> cleanedData = new ArrayList<DropdownItem>();
        HashSet<Integer> separators = new HashSet<Integer>();
        for (int i = 0; i < suggestions.length; i++) {
            int itemId = suggestions[i].mUniqueId;
            if (itemId > 0 || itemId == ITEM_ID_AUTOCOMPLETE_ENTRY ||
                    itemId == ITEM_ID_PASSWORD_ENTRY || itemId == ITEM_ID_DATA_LIST_ENTRY) {
                cleanedData.add(suggestions[i]);
            } else if (itemId == ITEM_ID_SEPARATOR_ENTRY) {
                separators.add(cleanedData.size());
            }
        }
        setAdapter(new DropdownAdapter(mContext, cleanedData, separators));
        show();
    }

    /**
     * Overrides the default dismiss behavior to request the controller to dismiss the view.
     */
    @Override
    public void dismiss() {
        mAutofillCallback.requestHide();
    }

    /**
     * Hides the popup.
     */
    public void hide() {
        super.dismiss();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DropdownAdapter adapter = (DropdownAdapter) parent.getAdapter();
        int listIndex = mSuggestions.indexOf(adapter.getItem(position));
        assert listIndex > -1;
        mAutofillCallback.suggestionSelected(listIndex);
    }
}

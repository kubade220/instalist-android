/*
 * Copyright 2016 Tino Siegmund, Michael Wodniok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.noorganization.instalist.view.touchlistener.sidebar;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import org.noorganization.instalist.R;
import org.noorganization.instalist.presenter.IListController;
import org.noorganization.instalist.presenter.implementation.ControllerFactory;
import org.noorganization.instalist.model.ShoppingList;
import org.noorganization.instalist.view.utils.ViewUtils;

public class OnSubmitClickListenerWithChildData implements View.OnClickListener {

    private ViewSwitcher mViewSwitcher;
    private EditText     mNameEditText;
    private String       mShoppingListId;

    private IListController mListController;

    public OnSubmitClickListenerWithChildData(Context _context, ViewSwitcher _ViewSwitcher,
                                              EditText _NameEditText, String _ShoppingListId) {
        mViewSwitcher = _ViewSwitcher;
        mNameEditText = _NameEditText;
        mShoppingListId = _ShoppingListId;
        mListController = ControllerFactory.getListController(_context);
    }

    @Override
    public void onClick(View _View) {
        if (! ViewUtils.checkEditTextIsFilled(mNameEditText)) {
            return;
        }

        ShoppingList oldShoppingList, newShoppingList;
        String       insertedText;
        Context  context = _View.getContext();

        insertedText = mNameEditText.getText().toString();
        oldShoppingList = mListController.getListById(mShoppingListId);
        newShoppingList = mListController.renameList(oldShoppingList, insertedText);

        if (newShoppingList == null) {
            Toast.makeText(_View.getContext(), context.getString(R.string.shopping_list_not_found), Toast.LENGTH_SHORT).show();
            return;
        } else if (newShoppingList.equals(oldShoppingList)) {
           /* if (newShoppingList.mName.compareTo(insertedText) != 0) {

                Toast.makeText(context, context.getString(R.string.list_exists), Toast.LENGTH_SHORT).show();
                mNameEditText.setError(context.getString(R.string.list_exists));
                return;
            }*/
        }

        ViewUtils.removeSoftKeyBoard(context, mNameEditText);
        // TODO: remove this when callback for this kind is there.
        // ((IBaseActivity) context).updateList(newShoppingList);
        mViewSwitcher.showNext();
    }
}
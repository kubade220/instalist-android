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

package org.noorganization.instalist.view.fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import org.noorganization.instalist.R;
import org.noorganization.instalist.model.ListEntry;
import org.noorganization.instalist.model.ShoppingList;
import org.noorganization.instalist.presenter.IListController;
import org.noorganization.instalist.presenter.event.ListItemChangedMessage;
import org.noorganization.instalist.presenter.implementation.ControllerFactory;
import org.noorganization.instalist.view.activity.MainShoppingListView;
import org.noorganization.instalist.view.customview.AmountPicker;
import org.noorganization.instalist.view.decoration.DividerItemListDecoration;
import org.noorganization.instalist.view.event.ActivityStateMessage;
import org.noorganization.instalist.view.event.ShoppingListOverviewFragmentActiveEvent;
import org.noorganization.instalist.view.event.ShoppingListSelectedMessage;
import org.noorganization.instalist.view.event.ToolbarChangeMessage;
import org.noorganization.instalist.view.interfaces.IBaseActivity;
import org.noorganization.instalist.view.interfaces.IFragment;
import org.noorganization.instalist.view.listadapter.ShoppingItemListAdapter;
import org.noorganization.instalist.view.sorting.shoppingList.AlphabeticalListEntryComparator;
import org.noorganization.instalist.view.sorting.shoppingList.PriorityListEntryComparator;
import org.noorganization.instalist.view.touchlistener.OnRecyclerItemTouchListener;
import org.noorganization.instalist.view.utils.PreferencesManager;
import org.noorganization.instalist.view.utils.ViewUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import de.greenrobot.event.EventBus;

/**
 * This fragment handles the display of the content of an ShoppingList. This includes managing the
 * listadapter for the shoppingList items and all click events.
 */
public class ShoppingListOverviewFragment extends BaseFragment implements IFragment {

    private static final String LOG_TAG = ShoppingListOverviewFragment.class.toString();

    private ShoppingList mCurrentShoppingList;
    private boolean mHandlingProductSelectedMessages;

    private Context mContext;

    private FloatingActionButton mAddButton;

    private LinearLayoutManager mLayoutManager;

    private ShoppingItemListAdapter mShoppingItemListAdapter;
    private RecyclerView mRecyclerView;

    private IListController mListController;

    private IBaseActivity mBaseActivityInterface;

    private ActionMode mActionMode; // usage of support.v7.ActionMode!


    private static String PREFERENCES_NAME = "SHOPPING_LIST_FRAGMENT";

    private static String SORT_MODE = "SORT_MODE";
    /**
     * Contains the mapping from a Integer to comperators.
     */
    private Map<Integer, Comparator> mMapComperable;

    private static Integer SORT_BY_NAME = 0;
    private static Integer SORT_BY_PRIORITY = 1;

    /**
     * Used to inflate the actionbar.
     */
    private ActionMode.Callback mActionModeCallback;

    private View mRootView;

    /**
     * Listener for Callback of ActionMode when editing an ListEntry.
     */
    private class OnShoppingListItemActionModeListener implements ActionMode.Callback {

        private Context mContext;
        private View mView;
        private String mListEntryId;
        /**
         * The {@link android.os.PowerManager.WakeLock} to let the screen on when in shopping mode.
         */
        private PowerManager.WakeLock mWakeLock;

        /**
         * Constructor of OnShoppingListItemActionModeListener.
         *
         * @param _Context     the context of the Fragment.
         * @param _View        the View of the selected element. Used to read the data from it.
         * @param _ListEntryId the Id of the clicked ListEntry.
         */
        public OnShoppingListItemActionModeListener(Context _Context, View _View, String _ListEntryId) {
            mContext = _Context;
            mView = _View;
            mListEntryId = _ListEntryId;
        }

        /**
         * Checks if any editable field of the editview has a focus.
         *
         * @return true if a component has focus, false otherwise.
         */
        public boolean hasFocus() {
            boolean hasFocus = false;
            // hasFocus |= mAmountPicker.hasFocus();
            return hasFocus;
        }

        public void clearFocus() {
            // mAmountPicker.clearFocus();
        }

        @Override
        public boolean onCreateActionMode(ActionMode _Mode, Menu _Menu) {
            mAddButton.setVisibility(View.GONE);
            _Menu.clear();
            MenuInflater menuInflater = _Mode.getMenuInflater();
            menuInflater.inflate(R.menu.menu_contextual_actionmode_options, _Menu);

            ListEntry listEntry = getListEntryById(mListEntryId);
            _Mode.setTitle(listEntry.mProduct.mName);
            return true;
        }

        // called after onCreateActionMode
        @Override
        public boolean onPrepareActionMode(ActionMode _Mode, Menu _Menu) {
            return true;
        }

        // called when user selected an item.
        @Override
        public boolean onActionItemClicked(ActionMode _Mode, MenuItem _Item) {
            ListEntry entry = getListEntryById(mListEntryId);

            switch (_Item.getItemId()) {
                case R.id.menu_add_action:
                    int position = mShoppingItemListAdapter.getPositionForId(mListEntryId);

                    View view = mLayoutManager.findViewByPosition(position);
                    AmountPicker amountPicker = (AmountPicker) view.findViewById(R.id.list_product_shopping_product_amount_edit);
                    if (amountPicker == null) {
                        Log.e(LOG_TAG, "mAmountPicker is null.");
                        return true;
                    }

                    float value = amountPicker.getValue();
                    if (value == 0.0f) {
                        // TODO: some error messaging
                        return true;
                    }
                    entry.mAmount = value;
                    // entry.mUnit = unit;

                    ControllerFactory.getListController(mContext).addOrChangeItem(mCurrentShoppingList, entry.mProduct, value);
                    _Mode.finish();
                    break;
                case R.id.menu_cancel_action:
                    _Mode.finish();
                    break;
                case R.id.menu_delete_action:
                    ControllerFactory.getListController(mContext).removeItem(mListController.getEntryById(mListEntryId));
                    _Mode.finish();
                    break;
                default:
                    return false;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode _Mode) {
            mShoppingItemListAdapter.resetEditModeView();
            mView.setSelected(false);
            mActionMode = null;
            mAddButton.setVisibility(View.VISIBLE);
        }

        /**
         * Gets the ListEntry by an id. If it is null then an @Link{NullPointerException} will be thrown.
         *
         * @param _Id the id of the ListEntry.
         * @return the ListEntry.
         */
        private ListEntry getListEntryById(String _Id) {
            ListEntry listEntry = ControllerFactory.getListController(mContext).getEntryById(_Id);
            if (listEntry == null) {
                Log.e(LOG_TAG, "ListEntry is not defined.");
                throw new NullPointerException("ListEntry is not defined.");
            }
            return listEntry;
        }
    }


    // --------------------------------------------------------------------------------------------


    public ShoppingListOverviewFragment() {
    }

    /**
     * Creates an instance of an ShoppingListOverviewFragment.
     *
     * @param _ListId id of the @Link{ShoppingList} that should be shown.
     * @return the new instance of this fragment.
     */
    public static ShoppingListOverviewFragment newInstance(String _ListId) {

        ShoppingListOverviewFragment fragment = new ShoppingListOverviewFragment();
        Bundle args = new Bundle();
        args.putString(MainShoppingListView.KEY_LISTID, _ListId);
        fragment.setArguments(args);
        return fragment;
    }

    // --------------------------------------------------------------------------------------------


    @Override
    protected void onAttachToContext(Context _Context) {
        mContext = _Context;

        try {
            mBaseActivityInterface = (IBaseActivity) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " has no IBaseActivity interface attached.");
        }

        mListController = ControllerFactory.getListController(mContext);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        // get bundle args to get the listname that should be shown
        Bundle bundle = this.getArguments();

        mMapComperable = new WeakHashMap<>();
        mMapComperable.put(0, new AlphabeticalListEntryComparator());
        mMapComperable.put(1, new PriorityListEntryComparator());


        if (bundle == null) {
            return;
        }

        String listId = bundle.getString(MainShoppingListView.KEY_LISTID);
        mCurrentShoppingList = mListController.getListById(listId);

        // assign other listname if none is assigned
        if (mCurrentShoppingList == null) {
            List<ShoppingList> mShoppingLists = mListController.getAllLists();
            if (mShoppingLists.size() > 0) {
                mCurrentShoppingList = mShoppingLists.get(0);
                //mBaseActivityInterface.setToolbarTitle(mCurrentShoppingList.mName);
            }
        }
        EventBus.getDefault().post(new ShoppingListSelectedMessage(mCurrentShoppingList));
        mHandlingProductSelectedMessages = true;
    }


    // --------------------------------------------------------------------------------------------


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // swtich which action item was pressed
        switch (id) {
            case R.id.list_items_sort_by_priority:
                mShoppingItemListAdapter.sortByComparator(mMapComperable.get(SORT_BY_PRIORITY));
                PreferencesManager.getInstance().setValue(SORT_MODE, SORT_BY_PRIORITY);
                break;
            case R.id.list_items_sort_by_name:
                mShoppingItemListAdapter.sortByComparator(mMapComperable.get(SORT_BY_NAME));
                PreferencesManager.getInstance().setValue(SORT_MODE, SORT_BY_NAME);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // set the title in "main" activity so that the current list name is shown on the actionbar

        //mBaseActivityInterface.setToolbarTitle(mCurrentShoppingList.mName);


        mRootView = getView();
        if (mRootView == null) {
            throw new NullPointerException("Root view is null. Probably some initialization went wrong.");
        }
        mRootView.setFocusableInTouchMode(true);
        // set focus to this view to get key events.
        mRootView.requestFocus();

        mRootView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View _View, int _KeyCode, KeyEvent _Event) {
                // onKey gets 2 calls when key was pressed ( up and down) only use the up action.
                if (_Event.getAction() == MotionEvent.ACTION_UP) {
                    // only call back button when back button was released
                    if (_KeyCode == KeyEvent.KEYCODE_BACK) {
                        if (mActionMode != null) {
                            mActionMode.finish();
                            return true;
                        }
                    }
                }
                return false;
            }
        });
    }


    // --------------------------------------------------------------------------------------------


    @Override
    public void onPause() {
        super.onPause();
        mAddButton.setOnClickListener(null);
        EventBus.getDefault().post(new ShoppingListOverviewFragmentActiveEvent(false));
        mBaseActivityInterface.unregisterFragment(this);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    // --------------------------------------------------------------------------------------------


    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);

        mBaseActivityInterface.registerFragment(this);
        // init
        mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.fragment_shopping_list);

        mBaseActivityInterface.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        mShoppingItemListAdapter = new ShoppingItemListAdapter(getActivity(), mListController.listAllListEntries(mCurrentShoppingList.mUUID, mCurrentShoppingList.mCategory != null ? mCurrentShoppingList.mCategory.mUUID : null));
        int sortIndex = PreferencesManager.getInstance().getIntValue(SORT_MODE);
        if (sortIndex >= 0) {
            mShoppingItemListAdapter.sortByComparator(mMapComperable.get(sortIndex));
        } else {
            // set it by default to sort by name
            mShoppingItemListAdapter.sortByComparator(mMapComperable.get(SORT_BY_NAME));
            PreferencesManager.getInstance().setValue(SORT_MODE, SORT_BY_NAME);
        }
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(mContext);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView.setLayoutManager(mLayoutManager);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mRecyclerView.addItemDecoration(new DividerItemListDecoration(getResources().getDrawable(R.drawable.list_divider, mContext.getTheme())));
        } else {
            mRecyclerView.addItemDecoration(new DividerItemListDecoration(getResources().getDrawable(R.drawable.list_divider)));
        }
        mRecyclerView.setAdapter(mShoppingItemListAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mRecyclerView.addOnItemTouchListener(new OnRecyclerItemTouchListener(mContext, mRecyclerView) {

            private void toggleStrike(ListEntry _Entry) {
                if (_Entry.mStruck) {
                    mListController.unstrikeItem(_Entry);
                } else {
                    mListController.strikeItem(_Entry);
                }
            }

            @Override
            public void onSwipeRight(View _ChildView, int _Position) {
                super.onSwipeRight(_ChildView, _Position);
                ListEntry entry = mListController.getEntryById(mShoppingItemListAdapter.getItem(_Position).mUUID);
                toggleStrike(entry);
            }

            @Override
            public void onSwipeLeft(View _ChildView, int _Position) {
                super.onSwipeLeft(_ChildView, _Position);
                ListEntry entry = mListController.getEntryById(mShoppingItemListAdapter.getItem(_Position).mUUID);
                toggleStrike(entry);

            }

            @Override
            public void onSingleTap(View _ChildView, int _Position) {
                super.onSingleTap(_ChildView, _Position);
                if (mActionMode != null) {
                    if (_Position < 0 || mShoppingItemListAdapter.getItemViewType(_Position) != ShoppingItemListAdapter.ViewType.EDIT_MODE_VIEW) {
                        // only close edit field when this view is not currently in edit mode or the user clicked not on an listelement
                        mActionMode.finish();
                    }
                }
                // ListEntry entry = ListEntry.findById(ListEntry.class, mShoppingItemListAdapter.getItemId(_Position));
                // Toast.makeText(mContext, "Item selected: " + entry.mProduct.mName, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongPress(View _ChildView, int _Position) {
                super.onLongPress(_ChildView, _Position);
                if (mActionMode != null) {
                    mActionMode.finish();
                }

                // return if there was a long press on a position where no listitem is specified.
                if (_Position < 0) {
                    return;
                }

                mShoppingItemListAdapter.setToEditMode(_Position);

                mActionModeCallback = new OnShoppingListItemActionModeListener(mContext, _ChildView, mShoppingItemListAdapter.getItem(_Position).mUUID);
                // Start the CAB using the Callback defined above
                mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
                _ChildView.setSelected(true);
            }
        });

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // reset selected items ... (lazy resetting!)
                ViewUtils.addFragment(getActivity(),
                        ProductListDialogFragment.newInstance(mCurrentShoppingList.mUUID));
            }
        });

        String title = (mCurrentShoppingList == null ? "" : mCurrentShoppingList.mName);
        EventBus.getDefault().post(new ToolbarChangeMessage(false, title));
        EventBus.getDefault().post(new ShoppingListOverviewFragmentActiveEvent(true));
    }

    // --------------------------------------------------------------------------------------------


    @Override
    public View onCreateView(LayoutInflater _Inflater, ViewGroup _Container, Bundle _SavedInstanceState) {
        super.onCreateView(_Inflater, _Container, _SavedInstanceState);

        View view = _Inflater.inflate(R.layout.fragment_main_shopping_list_view, _Container, false);
        mAddButton = (FloatingActionButton) view.findViewById(R.id.add_item_main_list_view);
        return view;
    }


    public void onEvent(ActivityStateMessage _message) {
        if (_message.mActivity == getActivity()) {
            mHandlingProductSelectedMessages = (_message.mState == ActivityStateMessage.State.RESUMED);
        }
    }
    /*
        public void onEvent(ProductSelectMessage _message) {
            if (mHandlingProductSelectedMessages) {
                Map<Product, Float> productAmounts = _message.mProducts;
                IListController controller = ControllerFactory.getListController(mContext);
                for (Product currentProduct : productAmounts.keySet()) {
                    controller.addOrChangeItem(mCurrentShoppingList, currentProduct,
                            productAmounts.get(currentProduct), true);
                }
            }
        }
    */

    public void onEvent(ListItemChangedMessage _message) {
        /*if(!_message.mEntry.mList.equals(mCurrentShoppingList)){
            return;
        }*/
        Log.d(getClass().getCanonicalName(), "ListItemChangedMessage: " + _message.mChange.toString()
                + "; " + _message.mEntry.toString());
        switch (_message.mChange) {
            case CHANGED:
                onListItemUpdated(_message.mEntry);
                break;
            case CREATED:
                onListItemAdded(_message.mEntry);
                break;
            case DELETED:
                onListItemDeleted(_message.mEntry);
                break;
        }

    }

    /**
     * Updates the adapter in the shoppinglistadapter with the given item.
     *
     * @param _Entry the item that should be deleted.
     */
    public void onListItemUpdated(ListEntry _Entry) {
        mShoppingItemListAdapter.updateListEntry(_Entry);
    }

    /**
     * Removes the given item from the containing listarray in the shoppinglistadapter.
     *
     * @param _Entry the item that should be deleted.
     */
    public void onListItemDeleted(ListEntry _Entry) {
        mShoppingItemListAdapter.removeListEntry(_Entry.mUUID);
    }

    /**
     * Adds the given listentry to the listentry adapter.
     *
     * @param _Entry The entry that should be added to the list.
     */
    public void onListItemAdded(ListEntry _Entry) {
        if (_Entry.mList.mUUID.equals(mCurrentShoppingList.mUUID)) {
            mShoppingItemListAdapter.addListEntry(_Entry.mUUID);
        }
    }

    @Override
    public void onShoppingListRemoved(ShoppingList _ShoppingList) {
        if (mCurrentShoppingList.equals(_ShoppingList)) {
            mBaseActivityInterface.setToolbarTitle(mContext.getResources().getString(R.string.shopping_list_not_choosen));
            ViewUtils.removeFragment(getActivity(), this);
        }
    }
}
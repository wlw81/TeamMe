package de.pasligh.android.teamme.databinding;
import de.pasligh.android.teamme.R;
import de.pasligh.android.teamme.BR;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class ActivityGameCreatorBindingImpl extends ActivityGameCreatorBinding  {

    @Nullable
    private static final android.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = null;
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.gameCreatorCL, 1);
        sViewsWithIds.put(R.id.playerSelectionBlankTV2, 2);
        sViewsWithIds.put(R.id.playerSelectionRV2, 3);
        sViewsWithIds.put(R.id.newGameFAB, 4);
        sViewsWithIds.put(R.id.gameCreatorAB, 5);
        sViewsWithIds.put(R.id.gameCreatorTB, 6);
        sViewsWithIds.put(R.id.PreSelectionTextView, 7);
        sViewsWithIds.put(R.id.SportTextView, 8);
        sViewsWithIds.put(R.id.PlayerTeamSelectionLinearLayout, 9);
        sViewsWithIds.put(R.id.TeamCountNumberPicker, 10);
        sViewsWithIds.put(R.id.TeamsTextView, 11);
        sViewsWithIds.put(R.id.PlayerCountNumberPicker, 12);
        sViewsWithIds.put(R.id.additionalPlayerCountTextView, 13);
        sViewsWithIds.put(R.id.navigation_view, 14);
    }
    // views
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers

    public ActivityGameCreatorBindingImpl(@Nullable android.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 15, sIncludes, sViewsWithIds));
    }
    private ActivityGameCreatorBindingImpl(android.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 0
            , (android.widget.NumberPicker) bindings[12]
            , (android.widget.LinearLayout) bindings[9]
            , (android.widget.TextView) bindings[7]
            , (android.widget.AutoCompleteTextView) bindings[8]
            , (android.widget.NumberPicker) bindings[10]
            , (android.widget.TextView) bindings[11]
            , (android.widget.TextView) bindings[13]
            , (android.support.design.widget.AppBarLayout) bindings[5]
            , (android.support.design.widget.CoordinatorLayout) bindings[1]
            , (android.support.v4.widget.DrawerLayout) bindings[0]
            , (android.support.v7.widget.Toolbar) bindings[6]
            , (android.support.design.widget.NavigationView) bindings[14]
            , (android.support.design.widget.FloatingActionButton) bindings[4]
            , (android.widget.TextView) bindings[2]
            , (android.support.v7.widget.RecyclerView) bindings[3]
            );
        this.gameCreatorDL.setTag(null);
        setRootTag(root);
        // listeners
        invalidateAll();
    }

    @Override
    public void invalidateAll() {
        synchronized(this) {
                mDirtyFlags = 0x2L;
        }
        requestRebind();
    }

    @Override
    public boolean hasPendingBindings() {
        synchronized(this) {
            if (mDirtyFlags != 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean setVariable(int variableId, @Nullable Object variable)  {
        boolean variableSet = true;
        if (BR.playerAssignments == variableId) {
            setPlayerAssignments((java.util.List) variable);
        }
        else {
            variableSet = false;
        }
            return variableSet;
    }

    public void setPlayerAssignments(@Nullable java.util.List PlayerAssignments) {
        this.mPlayerAssignments = PlayerAssignments;
    }

    @Override
    protected boolean onFieldChange(int localFieldId, Object object, int fieldId) {
        switch (localFieldId) {
        }
        return false;
    }

    @Override
    protected void executeBindings() {
        long dirtyFlags = 0;
        synchronized(this) {
            dirtyFlags = mDirtyFlags;
            mDirtyFlags = 0;
        }
        // batch finished
    }
    // Listener Stub Implementations
    // callback impls
    // dirty flag
    private  long mDirtyFlags = 0xffffffffffffffffL;
    /* flag mapping
        flag 0 (0x1L): playerAssignments
        flag 1 (0x2L): null
    flag mapping end*/
    //end
}
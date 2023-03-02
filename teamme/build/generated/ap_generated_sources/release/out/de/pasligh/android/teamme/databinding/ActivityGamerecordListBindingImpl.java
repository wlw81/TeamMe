package de.pasligh.android.teamme.databinding;
import de.pasligh.android.teamme.R;
import de.pasligh.android.teamme.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class ActivityGamerecordListBindingImpl extends ActivityGamerecordListBinding  {

    @Nullable
    private static final androidx.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = null;
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.gamerecord_list, 2);
    }
    // views
    @NonNull
    private final android.widget.LinearLayout mboundView0;
    @NonNull
    private final android.widget.TextView mboundView1;
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers

    public ActivityGamerecordListBindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 3, sIncludes, sViewsWithIds));
    }
    private ActivityGamerecordListBindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 0
            , null
            , (androidx.recyclerview.widget.RecyclerView) bindings[2]
            );
        this.mboundView0 = (android.widget.LinearLayout) bindings[0];
        this.mboundView0.setTag(null);
        this.mboundView1 = (android.widget.TextView) bindings[1];
        this.mboundView1.setTag(null);
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
        if (BR.records == variableId) {
            setRecords((java.util.List) variable);
        }
        else {
            variableSet = false;
        }
            return variableSet;
    }

    public void setRecords(@Nullable java.util.List Records) {
        this.mRecords = Records;
        synchronized(this) {
            mDirtyFlags |= 0x1L;
        }
        notifyPropertyChanged(BR.records);
        super.requestRebind();
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
        int recordsSize = 0;
        java.util.List<?> records = mRecords;
        boolean recordsSizeInt0 = false;
        int recordsSizeInt0ViewGONEViewVISIBLE = 0;

        if ((dirtyFlags & 0x3L) != 0) {



                if (records != null) {
                    // read records.size()
                    recordsSize = records.size();
                }


                // read records.size() > 0
                recordsSizeInt0 = (recordsSize) > (0);
            if((dirtyFlags & 0x3L) != 0) {
                if(recordsSizeInt0) {
                        dirtyFlags |= 0x8L;
                }
                else {
                        dirtyFlags |= 0x4L;
                }
            }


                // read records.size() > 0 ? View.GONE : View.VISIBLE
                recordsSizeInt0ViewGONEViewVISIBLE = ((recordsSizeInt0) ? (android.view.View.GONE) : (android.view.View.VISIBLE));
        }
        // batch finished
        if ((dirtyFlags & 0x3L) != 0) {
            // api target 1

            this.mboundView1.setVisibility(recordsSizeInt0ViewGONEViewVISIBLE);
        }
    }
    // Listener Stub Implementations
    // callback impls
    // dirty flag
    private  long mDirtyFlags = 0xffffffffffffffffL;
    /* flag mapping
        flag 0 (0x1L): records
        flag 1 (0x2L): null
        flag 2 (0x3L): records.size() > 0 ? View.GONE : View.VISIBLE
        flag 3 (0x4L): records.size() > 0 ? View.GONE : View.VISIBLE
    flag mapping end*/
    //end
}
package de.pasligh.android.teamme;

import android.databinding.DataBinderMapper;
import android.databinding.DataBindingComponent;
import android.databinding.ViewDataBinding;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import de.pasligh.android.teamme.databinding.ActivityGameCreatorBindingImpl;
import de.pasligh.android.teamme.databinding.ActivityGamerecordListBindingImpl;
import de.pasligh.android.teamme.databinding.ActivityGamerecordListBindingW900dpImpl;
import de.pasligh.android.teamme.databinding.ActivityPlayerListBindingImpl;
import de.pasligh.android.teamme.databinding.ActivityPlayerListBindingW900dpImpl;
import java.lang.IllegalArgumentException;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.RuntimeException;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataBinderMapperImpl extends DataBinderMapper {
  private static final int LAYOUT_ACTIVITYGAMECREATOR = 1;

  private static final int LAYOUT_ACTIVITYGAMERECORDLIST = 2;

  private static final int LAYOUT_ACTIVITYPLAYERLIST = 3;

  private static final SparseIntArray INTERNAL_LAYOUT_ID_LOOKUP = new SparseIntArray(3);

  static {
    INTERNAL_LAYOUT_ID_LOOKUP.put(de.pasligh.android.teamme.R.layout.activity_game_creator, LAYOUT_ACTIVITYGAMECREATOR);
    INTERNAL_LAYOUT_ID_LOOKUP.put(de.pasligh.android.teamme.R.layout.activity_gamerecord_list, LAYOUT_ACTIVITYGAMERECORDLIST);
    INTERNAL_LAYOUT_ID_LOOKUP.put(de.pasligh.android.teamme.R.layout.activity_player_list, LAYOUT_ACTIVITYPLAYERLIST);
  }

  @Override
  public ViewDataBinding getDataBinder(DataBindingComponent component, View view, int layoutId) {
    int localizedLayoutId = INTERNAL_LAYOUT_ID_LOOKUP.get(layoutId);
    if(localizedLayoutId > 0) {
      final Object tag = view.getTag();
      if(tag == null) {
        throw new RuntimeException("view must have a tag");
      }
      switch(localizedLayoutId) {
        case  LAYOUT_ACTIVITYGAMECREATOR: {
          if ("layout/activity_game_creator_0".equals(tag)) {
            return new ActivityGameCreatorBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_game_creator is invalid. Received: " + tag);
        }
        case  LAYOUT_ACTIVITYGAMERECORDLIST: {
          if ("layout/activity_gamerecord_list_0".equals(tag)) {
            return new ActivityGamerecordListBindingImpl(component, view);
          }
          if ("layout-w900dp/activity_gamerecord_list_0".equals(tag)) {
            return new ActivityGamerecordListBindingW900dpImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_gamerecord_list is invalid. Received: " + tag);
        }
        case  LAYOUT_ACTIVITYPLAYERLIST: {
          if ("layout-w900dp/activity_player_list_0".equals(tag)) {
            return new ActivityPlayerListBindingW900dpImpl(component, view);
          }
          if ("layout/activity_player_list_0".equals(tag)) {
            return new ActivityPlayerListBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_player_list is invalid. Received: " + tag);
        }
      }
    }
    return null;
  }

  @Override
  public ViewDataBinding getDataBinder(DataBindingComponent component, View[] views, int layoutId) {
    if(views == null || views.length == 0) {
      return null;
    }
    int localizedLayoutId = INTERNAL_LAYOUT_ID_LOOKUP.get(layoutId);
    if(localizedLayoutId > 0) {
      final Object tag = views[0].getTag();
      if(tag == null) {
        throw new RuntimeException("view must have a tag");
      }
      switch(localizedLayoutId) {
      }
    }
    return null;
  }

  @Override
  public int getLayoutId(String tag) {
    if (tag == null) {
      return 0;
    }
    Integer tmpVal = InnerLayoutIdLookup.sKeys.get(tag);
    return tmpVal == null ? 0 : tmpVal;
  }

  @Override
  public String convertBrIdToString(int localId) {
    String tmpVal = InnerBrLookup.sKeys.get(localId);
    return tmpVal;
  }

  @Override
  public List<DataBinderMapper> collectDependencies() {
    ArrayList<DataBinderMapper> result = new ArrayList<DataBinderMapper>(1);
    result.add(new com.android.databinding.library.baseAdapters.DataBinderMapperImpl());
    return result;
  }

  private static class InnerBrLookup {
    static final SparseArray<String> sKeys = new SparseArray<String>(4);

    static {
      sKeys.put(0, "_all");
      sKeys.put(1, "playerAssignments");
      sKeys.put(2, "players");
      sKeys.put(3, "records");
    }
  }

  private static class InnerLayoutIdLookup {
    static final HashMap<String, Integer> sKeys = new HashMap<String, Integer>(5);

    static {
      sKeys.put("layout/activity_game_creator_0", de.pasligh.android.teamme.R.layout.activity_game_creator);
      sKeys.put("layout/activity_gamerecord_list_0", de.pasligh.android.teamme.R.layout.activity_gamerecord_list);
      sKeys.put("layout-w900dp/activity_gamerecord_list_0", de.pasligh.android.teamme.R.layout.activity_gamerecord_list);
      sKeys.put("layout-w900dp/activity_player_list_0", de.pasligh.android.teamme.R.layout.activity_player_list);
      sKeys.put("layout/activity_player_list_0", de.pasligh.android.teamme.R.layout.activity_player_list);
    }
  }
}

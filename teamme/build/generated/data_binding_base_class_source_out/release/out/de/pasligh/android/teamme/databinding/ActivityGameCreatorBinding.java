// Generated by data binding compiler. Do not edit!
package de.pasligh.android.teamme.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.databinding.Bindable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import de.pasligh.android.teamme.R;
import java.lang.Deprecated;
import java.lang.Object;
import java.util.List;

public abstract class ActivityGameCreatorBinding extends ViewDataBinding {
  @NonNull
  public final NumberPicker PlayerCountNumberPicker;

  @NonNull
  public final LinearLayout PlayerTeamSelectionLinearLayout;

  @NonNull
  public final TextView PreSelectionTextView;

  @NonNull
  public final AutoCompleteTextView SportTextView;

  @NonNull
  public final NumberPicker TeamCountNumberPicker;

  @NonNull
  public final TextView TeamsTextView;

  @NonNull
  public final TextView additionalPlayerCountTextView;

  @NonNull
  public final AppBarLayout gameCreatorAB;

  @NonNull
  public final CoordinatorLayout gameCreatorCL;

  @NonNull
  public final DrawerLayout gameCreatorDL;

  @NonNull
  public final Toolbar gameCreatorTB;

  @NonNull
  public final NavigationView navigationView;

  @NonNull
  public final FloatingActionButton newGameFAB;

  @NonNull
  public final TextView playerSelectionBlankTV2;

  @NonNull
  public final RecyclerView playerSelectionRV2;

  @Bindable
  protected List mPlayerAssignments;

  protected ActivityGameCreatorBinding(Object _bindingComponent, View _root, int _localFieldCount,
      NumberPicker PlayerCountNumberPicker, LinearLayout PlayerTeamSelectionLinearLayout,
      TextView PreSelectionTextView, AutoCompleteTextView SportTextView,
      NumberPicker TeamCountNumberPicker, TextView TeamsTextView,
      TextView additionalPlayerCountTextView, AppBarLayout gameCreatorAB,
      CoordinatorLayout gameCreatorCL, DrawerLayout gameCreatorDL, Toolbar gameCreatorTB,
      NavigationView navigationView, FloatingActionButton newGameFAB,
      TextView playerSelectionBlankTV2, RecyclerView playerSelectionRV2) {
    super(_bindingComponent, _root, _localFieldCount);
    this.PlayerCountNumberPicker = PlayerCountNumberPicker;
    this.PlayerTeamSelectionLinearLayout = PlayerTeamSelectionLinearLayout;
    this.PreSelectionTextView = PreSelectionTextView;
    this.SportTextView = SportTextView;
    this.TeamCountNumberPicker = TeamCountNumberPicker;
    this.TeamsTextView = TeamsTextView;
    this.additionalPlayerCountTextView = additionalPlayerCountTextView;
    this.gameCreatorAB = gameCreatorAB;
    this.gameCreatorCL = gameCreatorCL;
    this.gameCreatorDL = gameCreatorDL;
    this.gameCreatorTB = gameCreatorTB;
    this.navigationView = navigationView;
    this.newGameFAB = newGameFAB;
    this.playerSelectionBlankTV2 = playerSelectionBlankTV2;
    this.playerSelectionRV2 = playerSelectionRV2;
  }

  public abstract void setPlayerAssignments(@Nullable List playerAssignments);

  @Nullable
  public List getPlayerAssignments() {
    return mPlayerAssignments;
  }

  @NonNull
  public static ActivityGameCreatorBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot) {
    return inflate(inflater, root, attachToRoot, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.activity_game_creator, root, attachToRoot, component)
   */
  @NonNull
  @Deprecated
  public static ActivityGameCreatorBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot, @Nullable Object component) {
    return ViewDataBinding.<ActivityGameCreatorBinding>inflateInternal(inflater, R.layout.activity_game_creator, root, attachToRoot, component);
  }

  @NonNull
  public static ActivityGameCreatorBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.activity_game_creator, null, false, component)
   */
  @NonNull
  @Deprecated
  public static ActivityGameCreatorBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable Object component) {
    return ViewDataBinding.<ActivityGameCreatorBinding>inflateInternal(inflater, R.layout.activity_game_creator, null, false, component);
  }

  public static ActivityGameCreatorBinding bind(@NonNull View view) {
    return bind(view, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.bind(view, component)
   */
  @Deprecated
  public static ActivityGameCreatorBinding bind(@NonNull View view, @Nullable Object component) {
    return (ActivityGameCreatorBinding)bind(component, view, R.layout.activity_game_creator);
  }
}
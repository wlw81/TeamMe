// Generated by data binding compiler. Do not edit!
package de.pasligh.android.teamme.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.Bindable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;
import de.pasligh.android.teamme.R;
import java.lang.Deprecated;
import java.lang.Object;
import java.util.List;

public abstract class ActivityPlayerListBinding extends ViewDataBinding {
  /**
   * This binding is not available in all configurations.
   * <p>
   * Present:
   * <ul>
   *   <li>layout-w900dp/</li>
   * </ul>
   *
   * Absent:
   * <ul>
   *   <li>layout/</li>
   * </ul>
   */
  @Nullable
  public final FrameLayout playerDetailContainer;

  @NonNull
  public final RecyclerView playerList;

  @Bindable
  protected List mPlayers;

  protected ActivityPlayerListBinding(Object _bindingComponent, View _root, int _localFieldCount,
      FrameLayout playerDetailContainer, RecyclerView playerList) {
    super(_bindingComponent, _root, _localFieldCount);
    this.playerDetailContainer = playerDetailContainer;
    this.playerList = playerList;
  }

  public abstract void setPlayers(@Nullable List players);

  @Nullable
  public List getPlayers() {
    return mPlayers;
  }

  @NonNull
  public static ActivityPlayerListBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot) {
    return inflate(inflater, root, attachToRoot, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.activity_player_list, root, attachToRoot, component)
   */
  @NonNull
  @Deprecated
  public static ActivityPlayerListBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot, @Nullable Object component) {
    return ViewDataBinding.<ActivityPlayerListBinding>inflateInternal(inflater, R.layout.activity_player_list, root, attachToRoot, component);
  }

  @NonNull
  public static ActivityPlayerListBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.activity_player_list, null, false, component)
   */
  @NonNull
  @Deprecated
  public static ActivityPlayerListBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable Object component) {
    return ViewDataBinding.<ActivityPlayerListBinding>inflateInternal(inflater, R.layout.activity_player_list, null, false, component);
  }

  public static ActivityPlayerListBinding bind(@NonNull View view) {
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
  public static ActivityPlayerListBinding bind(@NonNull View view, @Nullable Object component) {
    return (ActivityPlayerListBinding)bind(component, view, R.layout.activity_player_list);
  }
}

/*
 * Copyright 2021 The Android Open Source Project
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

package io.material.catalog.preferences;

import io.material.catalog.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import dagger.android.support.AndroidSupportInjection;
import io.material.catalog.preferences.CatalogPreference.Option;
import javax.inject.Inject;

/**
 * A fragment to display a bottom sheet for preferences settings.
 */
public class CatalogPreferencesDialogFragment extends BottomSheetDialogFragment
    implements HasAndroidInjector {
  @Inject
  BaseCatalogPreferences preferences;

  @Inject DispatchingAndroidInjector<Object> childFragmentInjector;

  @Override
  public void onAttach(Context context) {
    AndroidSupportInjection.inject(this);
    super.onAttach(context);
  }

  @Override
  public AndroidInjector<Object> androidInjector() {
    return childFragmentInjector;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    // Do not restore states. All buttons will be checked according to preference settings.
    return super.onCreateDialog(null);
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    LinearLayout container =
        (LinearLayout) layoutInflater.inflate(
            R.layout.mtrl_preferences_dialog, viewGroup, false);
    for (CatalogPreference catalogPreference : preferences.getPreferences()) {
      container.addView(createPreferenceView(layoutInflater, container, catalogPreference));
    }
    return container;
  }

  private View createPreferenceView(
      @NonNull LayoutInflater layoutInflater,
      @NonNull ViewGroup rootView,
      @NonNull CatalogPreference preference) {
    boolean isEnabled = preference.isEnabled();
    View view =
        layoutInflater.inflate(R.layout.mtrl_preferences_dialog_preference, rootView, false);
    TextView description = view.findViewById(R.id.preference_description);
    description.setEnabled(isEnabled);
    description.setText(preference.description);
    MaterialButtonToggleGroup buttonToggleGroup = view.findViewById(R.id.preference_options);
    for (Option option : preference.getOptions()) {
      MaterialButton button = createOptionButton(layoutInflater, buttonToggleGroup, option);
      button.setEnabled(isEnabled);
      buttonToggleGroup.addView(button);
    }
    buttonToggleGroup.check(preference.getSelectedOption(view.getContext()).id);
    buttonToggleGroup.setEnabled(isEnabled);
    if (isEnabled) {
      buttonToggleGroup.addOnButtonCheckedListener(
          (group, checkedId, isChecked) -> {
            if (isChecked) {
              preference.setSelectedOption(group.getContext(), checkedId);
            }
          });
    }
    return view;
  }

  private MaterialButton createOptionButton(
      @NonNull LayoutInflater layoutInflater,
      @NonNull ViewGroup rootView,
      @NonNull Option option) {
    MaterialButton button =
        (MaterialButton) layoutInflater.inflate(
            R.layout.mtrl_preferences_dialog_option_button, rootView, false);
    button.setId(option.id);
    button.setIconResource(option.icon);
    button.setText(option.description);
    return button;
  }
}

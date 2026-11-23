package com.antiarnaque.app;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.antiarnaque.app.ui.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/** Test instrumenté minimal : l'application démarre et affiche la navigation. */
@RunWith(AndroidJUnit4.class)
public final class SmokeTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void bottomNavigationIsDisplayed() {
        onView(withId(R.id.bottom_navigation)).check(matches(isDisplayed()));
    }
}

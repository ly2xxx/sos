package com.ly2xxx.sos;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ly2xxx.sos.model.EmergencyContact;

import java.util.List;

public class EmergencyContactAdapter extends RecyclerView.Adapter<EmergencyContactAdapter.ViewHolder> {
    
    private List<EmergencyContactsActivity.CountryEmergencyContact> contacts;

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
        EmergencyContactsActivity.CountryEmergencyContact countryContact = contacts.get(position);
        String countryName = countryContact.getCountryName();
        EmergencyContact contact = countryContact.getEmergencyContact();
        
        holder.countryName.setText(countryName);
        
        // Set emergency numbers with icons
        holder.policeNumber.setText("🚔 " + contact.getPolice());
        holder.ambulanceNumber.setText("🚑 " + contact.getAmbulance());
        holder.fireNumber.setText("🚒 " + contact.getFire());
        holder.generalNumber.setText("🆘 " + contact.getGeneral());
        
        // Set click listeners for calling
        holder.policeNumber.setOnClickListener(v -> makeCall(v, contact.getPolice(), "Police"));
        holder.ambulanceNumber.setOnClickListener(v -> makeCall(v, contact.getAmbulance(), "Ambulance"));
        holder.fireNumber.setOnClickListener(v -> makeCall(v, contact.getFire(), "Fire"));
        holder.generalNumber.setOnClickListener(v -> makeCall(v, contact.getGeneral(), "Emergency"));
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    private void makeCall(View view, String number, String serviceType) {
        if (number != null && !number.isEmpty()) {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + number));
            
            try {
                view.getContext().startActivity(callIntent);
                Toast.makeText(view.getContext(), 
                        "Calling " + serviceType + ": " + number, 
                        Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(view.getContext(), 
                        "Unable to make call", 
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(view.getContext(), 
                    "Emergency number not available", 
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView countryName;
        TextView policeNumber;
        TextView ambulanceNumber;
        TextView fireNumber;
        TextView generalNumber;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            countryName = itemView.findViewById(R.id.tv_country_name);
            policeNumber = itemView.findViewById(R.id.tv_police_number);
            ambulanceNumber = itemView.findViewById(R.id.tv_ambulance_number);
            fireNumber = itemView.findViewById(R.id.tv_fire_number);
            generalNumber = itemView.findViewById(R.id.tv_general_number);
        }
    }
}

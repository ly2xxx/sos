package com.ly2xxx.sos;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ly2xxx.sos.model.EmergencyContact;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EmergencyContactsActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private EditText searchEditText;
    private EmergencyContactAdapter adapter;
    private List<CountryEmergencyContact> allContacts;
    private List<CountryEmergencyContact> filteredContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);
        
        setupToolbar();
        initViews();
        loadEmergencyContacts();
        setupRecyclerView();
        setupSearch();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Emergency Contacts");
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        searchEditText = findViewById(R.id.search_edit_text);
        
        allContacts = new ArrayList<>();
        filteredContacts = new ArrayList<>();
    }

    private void loadEmergencyContacts() {
        try {
            AssetManager assetManager = getAssets();
            InputStream inputStream = assetManager.open("emergency_contacts.json");
            
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            
            String jsonString = new String(buffer, "UTF-8");
            JSONObject jsonObject = new JSONObject(jsonString);
            
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String countryKey = keys.next();
                JSONObject contactData = jsonObject.getJSONObject(countryKey);
                
                String countryName = countryKey.replace("_", " ");
                String police = contactData.getString("police");
                String ambulance = contactData.getString("ambulance");
                String fire = contactData.getString("fire");
                String general = contactData.getString("general");
                
                EmergencyContact contact = new EmergencyContact(police, ambulance, fire, general);
                CountryEmergencyContact countryContact = new CountryEmergencyContact(countryName, contact);
                allContacts.add(countryContact);
            }
            
            // Sort countries alphabetically
            allContacts.sort((c1, c2) -> c1.getCountryName().compareToIgnoreCase(c2.getCountryName()));
            filteredContacts.addAll(allContacts);
            
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading emergency contacts", Toast.LENGTH_LONG).show();
        }
    }

    private void setupRecyclerView() {
        adapter = new EmergencyContactAdapter(filteredContacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterContacts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterContacts(String query) {
        filteredContacts.clear();
        
        if (query.isEmpty()) {
            filteredContacts.addAll(allContacts);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (CountryEmergencyContact contact : allContacts) {
                if (contact.getCountryName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredContacts.add(contact);
                }
            }
        }
        
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Inner class to hold country name and emergency contact
    public static class CountryEmergencyContact {
        private String countryName;
        private EmergencyContact emergencyContact;

        public CountryEmergencyContact(String countryName, EmergencyContact emergencyContact) {
            this.countryName = countryName;
            this.emergencyContact = emergencyContact;
        }

        public String getCountryName() {
            return countryName;
        }

        public EmergencyContact getEmergencyContact() {
            return emergencyContact;
        }
    }
}

package ara.renault.com.fuel_level;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ara.renault.com.fuel_gauge.Car;
import ara.renault.com.fuel_gauge.FuelListener;
import ara.renault.com.fuel_gauge.NativeGaugeFragment;

public class MainActivity extends AppCompatActivity implements FuelListener, ValueEventListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_active);


        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            NativeGaugeFragment nativeGaugeFragment = NativeGaugeFragment.newInstance(100, 800);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, nativeGaugeFragment).commit();
        }
    }

    @Override
    public void onValueChanged(Car car) {

    }

    @Override
    public void onStart() {
        super.onStart();
        setUpListener(NativeGaugeFragment.CAR);

    }

    @Override
    public void onStop() {
        super.onStop();
        removeListener(NativeGaugeFragment.CAR);

    }

    public void setUpListener(final String key) {
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(key);
        // Read from the database
        myRef.addValueEventListener(this);
    }

    public void removeListener(final String key) {
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(key);
        // Read from the database
        myRef.removeEventListener(this);
    }


    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        // This method is called once with the initial value and again
        // whenever data at this location is updated.
        String key = dataSnapshot.getKey();
        Log.d(TAG, "Value from " + key);
        Car car = dataSnapshot.getValue(Car.class);
        NativeGaugeFragment nativeGaugeFragment = (NativeGaugeFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (nativeGaugeFragment != null)
            nativeGaugeFragment.displayCar(car);

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.e(TAG, "Failed to read value.", databaseError.toException());
    }


}

package ara.renault.com.fuel_level;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.FirebaseApp;

import ara.renault.com.fuel_gauge.Car;
import ara.renault.com.fuel_gauge.FuelListener;
import ara.renault.com.fuel_gauge.NativeGaugeFragment;

public class MainActivity extends AppCompatActivity implements FuelListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_active);


        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            NativeGaugeFragment nativeGaugeFragment = NativeGaugeFragment.newInstance(100,800);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, nativeGaugeFragment).commit();
        }
    }

    @Override
    public void onValueChanged(Car car) {

    }
}

package ara.renault.com.fuel_gauge;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ara.renault.com.fuel_gauge.views.GaugeView;

public class NativeGaugeFragment extends Fragment implements ValueEventListener {

    private static final String TAG = NativeGaugeFragment.class.getSimpleName();
    private static final String FUEL = "fuel";
    private static final String MILEAGE = "mileage";

    private static final String CAR = "car";

    private static final long DURATION_NEEDLE = 5000;
    private static final long DURATION_FLICKER = 1000;

    //Animation
    private static final int FUEL_LOW_LIMIT = 11;

    GaugeView gauge_view;
    ConstraintLayout mileage;
    TextView mileView;
    Button button;
    Map<String, View> tagMap;
    FuelListener mListener;


    public static NativeGaugeFragment newInstance(int fuel, int mileage) {
        NativeGaugeFragment f = new NativeGaugeFragment();
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt(FUEL, fuel);
        args.putInt(MILEAGE, mileage);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (FuelListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement FuelListener");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_native_active, container, false);
        gauge_view = (GaugeView) rootView.findViewById(R.id.gauge_view);
        //init unless you do not want the needle
        mileage = (ConstraintLayout) rootView.findViewById(R.id.mileage);
        mileView = (TextView) rootView.findViewById(R.id.tv_status_value);
        button = (Button) rootView.findViewById(R.id.reinit);
        tagMap = new HashMap<>();
        tagMap.put(FUEL, gauge_view);
        tagMap.put(MILEAGE, mileage);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FirebaseApp.initializeApp(getContext());
        triggerAnimations();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                triggerAnimations();
            }
        });
    }

    public void triggerAnimations() {
        int fuelStart = getArguments().getInt(FUEL);

        ObjectAnimator needleAnim = ObjectAnimator.ofFloat(gauge_view, "targetValue", fuelStart, FUEL_LOW_LIMIT);
        needleAnim.setDuration(DURATION_NEEDLE);

        ValueAnimator flickerAnim = ValueAnimator.ofInt(Color.RED, Color.WHITE, Color.RED);
        flickerAnim.setRepeatMode(ValueAnimator.REVERSE);
        flickerAnim.setRepeatCount(1);
        flickerAnim.setDuration(DURATION_FLICKER);
        flickerAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                //TODO:dangerous
                int color = Integer.parseInt(String.valueOf(animation.getAnimatedValue()));
                gauge_view.setColor(color);
                gauge_view.requestLayout();
            }

        });

        flickerAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mListener != null)
                    mListener.onValueChanged(makeCar());
            }
        });

        ValueAnimator counterAnim = new ValueAnimator();
        int limitMileage = getArguments().getInt(MILEAGE);
        counterAnim.setObjectValues(0, limitMileage);
        counterAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                String mileage = getResources().getString(R.string.mileage_value, String.valueOf(animation.getAnimatedValue()));
                mileView.setText(mileage);
            }
        });
        counterAnim.setDuration(DURATION_NEEDLE);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(needleAnim).with(counterAnim).before(flickerAnim);
        animatorSet.start();
    }


    @SuppressWarnings("unused")
    public void setUpValues() {
        int fuelLevel = getArguments().getInt(FUEL, 50);
        int mileageLevel = getArguments().getInt(MILEAGE, 80);
        gauge_view.setTargetValue(fuelLevel);
        Resources res = getResources();
        String mileage = res.getString(R.string.mileage_value, String.valueOf(mileageLevel));
        mileView.setText(mileage);
    }

    @Override
    public void onStart() {
        super.onStart();
        setUpListener(CAR);

    }

    @Override
    public void onStop() {
        super.onStop();
        removeListener(CAR);

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
        displayCar(car);
        if (mListener != null)
            mListener.onValueChanged(makeCar());
    }

    public void displayCar(Car car) {
        gauge_view.setVisibility(car.isFuelV() ? View.VISIBLE : View.GONE);
        mileage.setVisibility(car.isMileageV() ? View.VISIBLE : View.GONE);
        String mileage = getResources().getString(R.string.mileage_value, String.valueOf(car.getMileage()));
        mileView.setText(mileage);
        gauge_view.setTargetValue(car.getFuel());
    }

    public Car makeCar() {
        Car car = new Car();
        car.setFuelV(mileage.getVisibility() == View.VISIBLE);
        car.setMileageV(mileView.getVisibility() == View.VISIBLE);
        car.setFuel((int) gauge_view.getmTargetValue());
        if (!TextUtils.isEmpty(mileView.getText())) {
            Pattern p = Pattern.compile("-?\\d+");
            Matcher m = p.matcher(mileView.getText().toString());
            if (m.find()) {
                car.setMileage(Integer.parseInt(m.group()));
            }

        }
        return car;
    }


    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.e(TAG, "Failed to read value.", databaseError.toException());
    }


}

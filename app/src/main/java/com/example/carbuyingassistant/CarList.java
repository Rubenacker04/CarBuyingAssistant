package com.example.carbuyingassistant;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CarList extends AppCompatActivity {

    private static final String TAG = "CarListActivity";
    private double latitude;
    private double longitude;
    private String keywords;
    private RecyclerView carRecyclerView;
    private CarAdapter carAdapter;
    private ArrayList<Car> carList = new ArrayList<>();

    public static class Car {
        String makeAndModel;
        int year;
        int miles;
        double distanceFromUser;
        int mpgHighway;
        int mpgCity;
        int imageResId;

        public Car(String makeAndModel, int year, int miles, double distanceFromUser, int mpgHighway, int mpgCity, int imageResId) {
            this.makeAndModel = makeAndModel;
            this.year = year;
            this.miles = miles;
            this.distanceFromUser = distanceFromUser;
            this.mpgHighway = mpgHighway;
            this.mpgCity = mpgCity;
            this.imageResId = imageResId;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_list);

        Intent intent = getIntent();
        latitude = intent.getDoubleExtra("EXTRA_LATITUDE", 0.0);
        longitude = intent.getDoubleExtra("EXTRA_LONGITUDE", 0.0);
        keywords = intent.getStringExtra("EXTRA_KEYWORDS");

        Log.d(TAG, "Received Latitude: " + latitude);
        Log.d(TAG, "Received Longitude: " + longitude);
        Log.d(TAG, "Received Keywords: " + (keywords != null ? keywords : "None"));

        setupRecyclerView();
        createDummyCars();      // Will be replaced by method for receiving/populating car data from API

        carAdapter.notifyDataSetChanged(); // Update the CarList
    }

    /**
     * Sets up scrollable list of cars, and dividers between each of them.
     */
    private void setupRecyclerView() {
        carRecyclerView = findViewById(R.id.car_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        carRecyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration divider = new DividerItemDecoration(carRecyclerView.getContext(), layoutManager.getOrientation());
        carRecyclerView.addItemDecoration(divider);

        carAdapter = new CarAdapter(carList, this);
        carRecyclerView.setAdapter(carAdapter);
    }

    /**
     * Creates fake entries for cars to show CarList for now.
     * Will be replaced by a method to pull car jsons from the API.
     */
    private void createDummyCars() {
        carList.clear();

        carList.add(new Car("Ford Focus", 2016, 62500, 14, 34, 27, R.drawable.compact));
        carList.add(new Car("Toyota Camry", 2018, 48000, 22, 36, 32, R.drawable.sedan));
        carList.add(new Car("Toyota Corolla", 2015, 71000, 29, 33, 28, R.drawable.sedan));
        carList.add(new Car("Chevrolet Tahoe", 2012, 112000, 43, 28, 24, R.drawable.suv));
        carList.add(new Car("Nissan Maxima", 2020, 32500, 55, 36, 32, R.drawable.sedan));
        carList.add(new Car("Honda Civic", 2019, 22000, 60, 40, 31, R.drawable.compact));
        carList.add(new Car("Chevrolet Malibu", 2017, 55000, 68, 36, 27, R.drawable.sedan));
        carList.add(new Car("Ford F150", 2020, 15000, 75, 34, 23, R.drawable.truck));
    }


    /**
     * RecyclerView Adapter and ViewHolder for scrollable list
     */
    public static class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {

        private final List<Car> cars;
        private final Context context;

        public CarAdapter(List<Car> cars, Context context) {
            this.cars = cars;
            this.context = context;
        }

        @NonNull
        @Override
        public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_car, parent, false);
            return new CarViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
            Car currentCar = cars.get(position);

            // Pull data into each Car list item
            String title = String.format(Locale.getDefault(), "%d %s",
                    currentCar.year, currentCar.makeAndModel);
            String distance = String.format(Locale.getDefault(), "%.0f mi away", currentCar.distanceFromUser);
            String details = String.format(Locale.getDefault(), "%,d Miles Driven, %d/%d MPG",
                    currentCar.miles, currentCar.mpgCity, currentCar.mpgHighway);

            holder.carTitle.setText(title);
            holder.carDistance.setText(distance);
            holder.carDetails.setText(details);
            holder.carImage.setImageResource(currentCar.imageResId);

            // Set up button clicks for each Car list item
            holder.marketReportButton.setOnClickListener(v ->
                    Toast.makeText(context, "Market Report for " + currentCar.makeAndModel, Toast.LENGTH_SHORT).show()
            );

            holder.sellerSiteButton.setOnClickListener(v ->
                    Toast.makeText(context, "Seller Site for " + currentCar.makeAndModel, Toast.LENGTH_SHORT).show()
            );
        }

        @Override
        public int getItemCount() {
            return cars.size();
        }

        /**
         * Car list item ViewHolder
         */
        public static class CarViewHolder extends RecyclerView.ViewHolder {
            TextView carTitle;
            TextView carDistance;
            TextView carDetails;
            ImageView carImage;
            MaterialButton marketReportButton;
            MaterialButton sellerSiteButton;

            public CarViewHolder(@NonNull View itemView) {
                super(itemView);
                carTitle = itemView.findViewById(R.id.car_title);
                carDistance = itemView.findViewById(R.id.car_distance);
                carDetails = itemView.findViewById(R.id.car_details);
                carImage = itemView.findViewById(R.id.car_image);
                marketReportButton = itemView.findViewById(R.id.market_report_button);
                sellerSiteButton = itemView.findViewById(R.id.seller_site_button);
            }
        }
    }
}

package org.techtown.diary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.techtown.diary.fragment.GraphFragment;
import org.techtown.diary.fragment.ListFragment;
import org.techtown.diary.fragment.WriteFragment;
import org.techtown.diary.helper.OnTabItemSelectedListener;

public class MainActivity extends AppCompatActivity implements OnTabItemSelectedListener {
    // 상수
    private static final String LOG = "MainActivity";

    // 프래그먼트
    private ListFragment listFragment;
    private WriteFragment writeFragment;
    private GraphFragment graphFragment;

    // UI
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listFragment = new ListFragment();
        writeFragment = new WriteFragment();
        graphFragment = new GraphFragment();

        bottomNavigationView = (BottomNavigationView)findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();

                switch(item.getItemId()) {
                    case R.id.tab1:
                        transaction.replace(R.id.container, listFragment);
                        transaction.commit();

                        return true;
                    case R.id.tab2:
                        transaction.replace(R.id.container, writeFragment);
                        transaction.commit();

                        return true;
                    case R.id.tab3:
                        transaction.replace(R.id.container, graphFragment);
                        transaction.commit();

                        return true;
                }

                return false;
            }
        });
        onTabSelected(0);
    }

    @Override
    public void onTabSelected(int position) {
        switch(position) {
            case 0:
                bottomNavigationView.setSelectedItemId(R.id.tab1);
                break;
            case 1:
                bottomNavigationView.setSelectedItemId(R.id.tab2);
                break;
            case 2:
                bottomNavigationView.setSelectedItemId(R.id.tab3);
                break;
            default:
                Log.e(LOG, "ERROR : Tab Position ERROR..");
        }
    }

    @Override
    public void onBackPressed() {
        if(bottomNavigationView.getSelectedItemId() == R.id.tab1) {
            super.onBackPressed();
        } else {
            bottomNavigationView.setSelectedItemId(R.id.tab1);
        }
    }
}
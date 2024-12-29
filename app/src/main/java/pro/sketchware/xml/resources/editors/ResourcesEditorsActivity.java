package pro.sketchware.xml.resources.editors;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayoutMediator;

import pro.sketchware.databinding.ResourcesEditorsActivityBinding;
import pro.sketchware.xml.resources.editors.fragments.ColorEditor;
import pro.sketchware.xml.resources.editors.fragments.StringEditor;
import pro.sketchware.xml.resources.editors.fragments.StylesEditor;

public class ResourcesEditorsActivity extends AppCompatActivity {

    private ResourcesEditorsActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ResourcesEditorsActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topAppBar);

        setupViewPager();

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("strings.xml");
                    break;
                case 1:
                    tab.setText("colors.xml");
                    break;
                case 2:
                    tab.setText("styles.xml");
                    break;
                // TODO: add themes.xml
            }
        }).attach();

        binding.fab.setOnClickListener(view -> {

        });
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);
    }

    private static class ViewPagerAdapter extends FragmentStateAdapter {

        public ViewPagerAdapter(@NonNull AppCompatActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return switch (position) {
                case 0 -> new StringEditor();
                case 1 -> new ColorEditor();
                case 2 -> new StylesEditor();
                default -> throw new IllegalArgumentException("Invalid position");
            };
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}

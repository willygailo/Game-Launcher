package com.gamebooster.app.ui.screens;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.gamebooster.app.R;
import com.gamebooster.app.config.CfgProfileManager;
import com.gamebooster.app.config.CompetitiveCfgProfile;
import com.gamebooster.app.core.AppExecutors;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

/**
 * CfgProfilesFragment — Controller UI for the Competitive CFG Profile System.
 *
 * Provides tabbed per-game profile management (MLBB / PUBGM / CODM / ALL) with:
 *   - FPS target buttons (165, 144, 120, 90)
 *   - Super Fast Touch 165Hz toggle
 *   - Force System Hz toggle
 *   - Single-tap "APPLY & SAVE VIA SHIZUKU" button per game
 *   - Global "APPLY ALL GAMES" quick controls
 */
public class CfgProfilesFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TextView tvStatus;
    private Button btnAll165, btnAll144, btnAll120, btnAll90;

    private static final String[] GAME_KEYS = {
            CompetitiveCfgProfile.GAME_MLBB,
            CompetitiveCfgProfile.GAME_PUBGM,
            CompetitiveCfgProfile.GAME_CODM,
            CompetitiveCfgProfile.GAME_ALL
    };

    private static final String[] GAME_TITLES = {
            "Mobile Legends",
            "PUBG / BGMI",
            "Call of Duty",
            "All Games"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cfg_profiles, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabLayout  = view.findViewById(R.id.tab_cfg_games);
        viewPager  = view.findViewById(R.id.viewpager_cfg_games);
        tvStatus   = view.findViewById(R.id.tv_cfg_status);
        btnAll165  = view.findViewById(R.id.btn_all_165);
        btnAll144  = view.findViewById(R.id.btn_all_144);
        btnAll120  = view.findViewById(R.id.btn_all_120);
        btnAll90   = view.findViewById(R.id.btn_all_90);

        setupViewPager();
        setupGlobalButtons();
    }

    private void setupViewPager() {
        CfgPagerAdapter adapter = new CfgPagerAdapter();
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) ->
                tab.setText(GAME_TITLES[position])
        ).attach();
    }

    private void setupGlobalButtons() {
        btnAll165.setOnClickListener(v -> applyGlobalFps(165));
        btnAll144.setOnClickListener(v -> applyGlobalFps(144));
        btnAll120.setOnClickListener(v -> applyGlobalFps(120));
        btnAll90.setOnClickListener (v -> applyGlobalFps(90));
    }

    private void applyGlobalFps(int fps) {
        tvStatus.setText("Applying " + fps + " FPS profile to all games via Shizuku...");
        tvStatus.setTextColor(Color.parseColor("#00FFCC"));

        AppExecutors.getInstance().executeCommand(() -> {
            int count = CfgProfileManager.applyAllGames(requireContext(), fps, true, true);
            AppExecutors.getInstance().postToMainThread(() -> {
                if (isAdded()) {
                    tvStatus.setText("✓ Successfully updated " + count + " game packages to " + fps + " FPS/Hz!");
                    tvStatus.setTextColor(Color.parseColor("#00FF88"));
                    Toast.makeText(requireContext(), "Applied " + fps + " FPS to all games!", Toast.LENGTH_SHORT).show();
                    // Refresh current page
                    viewPager.getAdapter().notifyItemChanged(viewPager.getCurrentItem());
                }
            });
        });
    }

    // ─── Adapter for ViewPager2 ───────────────────────────────────────────────

    private class CfgPagerAdapter extends RecyclerView.Adapter<CfgCardViewHolder> {

        @NonNull
        @Override
        public CfgCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_cfg_game_card, parent, false);
            return new CfgCardViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull CfgCardViewHolder holder, int position) {
            holder.bind(GAME_KEYS[position], GAME_TITLES[position]);
        }

        @Override
        public int getItemCount() {
            return GAME_KEYS.length;
        }
    }

    // ─── ViewHolder for per-game card ─────────────────────────────────────────

    private class CfgCardViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvIcon, tvTitle, tvPackages, tvSavedBadge, tvSummary, tvLog;
        private final Button btn165, btn144, btn120, btn90, btnApplySave;
        private final Switch switchSuperTouch, switchForceHz;

        private int selectedFps = 165;
        private String gameKey;

        public CfgCardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon          = itemView.findViewById(R.id.tv_game_icon);
            tvTitle         = itemView.findViewById(R.id.tv_game_title);
            tvPackages      = itemView.findViewById(R.id.tv_game_packages);
            tvSavedBadge    = itemView.findViewById(R.id.tv_saved_fps_badge);
            tvSummary       = itemView.findViewById(R.id.tv_profile_summary);
            tvLog           = itemView.findViewById(R.id.tv_apply_log);

            btn165          = itemView.findViewById(R.id.btn_fps_165);
            btn144          = itemView.findViewById(R.id.btn_fps_144);
            btn120          = itemView.findViewById(R.id.btn_fps_120);
            btn90           = itemView.findViewById(R.id.btn_fps_90);
            btnApplySave    = itemView.findViewById(R.id.btn_apply_save);

            switchSuperTouch = itemView.findViewById(R.id.switch_super_touch);
            switchForceHz   = itemView.findViewById(R.id.switch_force_hz);
        }

        public void bind(String key, String title) {
            this.gameKey = key;
            tvTitle.setText(title);

            // Icon & package description
            switch (key) {
                case CompetitiveCfgProfile.GAME_MLBB:
                    tvIcon.setText("⚔️");
                    tvPackages.setText("com.mobile.legends, com.vng.mlbbvn");
                    break;
                case CompetitiveCfgProfile.GAME_PUBGM:
                    tvIcon.setText("🪂");
                    tvPackages.setText("com.tencent.ig, com.pubg.imobile, com.vng.pubgmobile");
                    break;
                case CompetitiveCfgProfile.GAME_CODM:
                    tvIcon.setText("🎯");
                    tvPackages.setText("com.activision.callofduty.shooter, com.garena.game.codm");
                    break;
                default:
                    tvIcon.setText("🌐");
                    tvPackages.setText("All supported game versions & variants");
                    break;
            }

            // Load saved profile
            CompetitiveCfgProfile saved = CfgProfileManager.loadProfile(requireContext(), key);
            selectedFps = saved.getTargetFps();
            switchSuperTouch.setChecked(saved.isSuperFastTouchEnabled());
            switchForceHz.setChecked(saved.isForceWriteSystemHz());

            updateFpsButtonSelection(selectedFps);
            updateSummaryText();
            tvSavedBadge.setText("SAVED\n" + saved.getTargetFps() + "fps");

            // FPS button listeners
            btn165.setOnClickListener(v -> selectFps(165));
            btn144.setOnClickListener(v -> selectFps(144));
            btn120.setOnClickListener(v -> selectFps(120));
            btn90.setOnClickListener (v -> selectFps(90));

            // Switch listeners
            switchSuperTouch.setOnCheckedChangeListener((b, isChecked) -> updateSummaryText());
            switchForceHz.setOnCheckedChangeListener((b, isChecked) -> updateSummaryText());

            // Apply & Save listener
            btnApplySave.setOnClickListener(v -> applyAndSave());
        }

        private void selectFps(int fps) {
            this.selectedFps = fps;
            updateFpsButtonSelection(fps);
            updateSummaryText();
        }

        private void updateFpsButtonSelection(int selected) {
            int activeBg   = Color.parseColor("#FF0055");
            int inactiveBg = Color.parseColor("#1E2430");

            btn165.setBackgroundColor(selected == 165 ? activeBg : inactiveBg);
            btn144.setBackgroundColor(selected == 144 ? activeBg : inactiveBg);
            btn120.setBackgroundColor(selected == 120 ? activeBg : inactiveBg);
            btn90.setBackgroundColor (selected == 90  ? activeBg : inactiveBg);
        }

        private void updateSummaryText() {
            String text = "Profile: " + selectedFps + " FPS" +
                    " · Super Touch " + (switchSuperTouch.isChecked() ? "ON" : "OFF") +
                    " · Force Hz " + (switchForceHz.isChecked() ? "ON" : "OFF");
            tvSummary.setText(text);
        }

        private void applyAndSave() {
            tvLog.setText("Executing Shizuku force-write for " + gameKey + " @ " + selectedFps + " FPS...");
            tvLog.setTextColor(Color.parseColor("#00FFCC"));

            final CompetitiveCfgProfile profile = new CompetitiveCfgProfile(
                    gameKey,
                    selectedFps,
                    switchSuperTouch.isChecked(),
                    switchForceHz.isChecked()
            );

            AppExecutors.getInstance().executeCommand(() -> {
                int count = CfgProfileManager.applyProfile(requireContext(), gameKey, profile);
                AppExecutors.getInstance().postToMainThread(() -> {
                    if (isAdded()) {
                        tvLog.setText("✓ Done! Updated " + count + " packages to " + selectedFps + " FPS/Hz via Shizuku.");
                        tvLog.setTextColor(Color.parseColor("#00FF88"));
                        tvSavedBadge.setText("SAVED\n" + selectedFps + "fps");
                        Toast.makeText(requireContext(), titleCase(gameKey) + " updated to " + selectedFps + " FPS!", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        private String titleCase(String s) {
            return s == null ? "" : s.toUpperCase();
        }
    }
}

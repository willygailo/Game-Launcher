package com.gamebooster.app.ui.layout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gamebooster.app.R;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.core.EngineUIHelper;
import com.gamebooster.app.games.GameAppInfo;
import com.gamebooster.app.games.GameManagerRepository;

import java.util.ArrayList;
import java.util.List;

public class GamesFragment extends Fragment {

    private TextView tvEngineStatus;
    private TextView tvHeader;
    private RecyclerView rvGames;
    private GamesAdapter adapter;
    private final List<GameAppInfo> gameList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_games, container, false);

        tvEngineStatus = view.findViewById(R.id.tv_engine_status);
        Button btnBoost = view.findViewById(R.id.btn_boost_ram);
        tvHeader = view.findViewById(R.id.tv_games_header);
        rvGames = view.findViewById(R.id.rv_games_list);

        rvGames.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GamesAdapter(getContext(), gameList);
        rvGames.setAdapter(adapter);

        btnBoost.setOnClickListener(v -> {
            if (getContext() != null) {
                btnBoost.setEnabled(false);
                AppExecutors.getInstance().executeCommand(() -> {
                    String resultMsg = GameManagerRepository.boostRamAndOptimize(getContext());
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            btnBoost.setEnabled(true);
                            Toast.makeText(getContext(), resultMsg, Toast.LENGTH_LONG).show();
                        }
                    });
                });
            }
        });

        EngineUIHelper.refreshEngineStatus(tvEngineStatus);
        loadInstalledGames();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        EngineUIHelper.refreshEngineStatus(tvEngineStatus);
        loadInstalledGames();
    }

    private void loadInstalledGames() {
        if (getContext() == null) return;

        // Offload package scanning to AppExecutors scanIO pool
        AppExecutors.getInstance().executeScan(() -> {
            List<GameAppInfo> installedGames = GameManagerRepository.getInstalledGames(getContext());

            AppExecutors.getInstance().postToMainThread(() -> {
                if (!isAdded() || getContext() == null) return;

                if (tvHeader != null) {
                    tvHeader.setText("INSTALLED GAMES DETECTED (" + installedGames.size() + "):");
                }
                gameList.clear();
                gameList.addAll(installedGames);
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            });
        });
    }
}

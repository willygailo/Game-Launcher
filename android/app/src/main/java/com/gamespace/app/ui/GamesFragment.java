package com.gamespace.app.ui;

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

import com.gamespace.app.R;
import com.gamespace.app.core.GameAppInfo;
import com.gamespace.app.data.GameManagerRepository;

import com.gamespace.app.utils.EngineUIHelper;

import java.util.List;

public class GamesFragment extends Fragment {

    private TextView tvEngineStatus;
    private TextView tvHeader;
    private RecyclerView rvGames;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_games, container, false);

        tvEngineStatus = view.findViewById(R.id.tv_engine_status);
        Button btnBoost = view.findViewById(R.id.btn_boost_ram);
        tvHeader = view.findViewById(R.id.tv_games_header);
        rvGames = view.findViewById(R.id.rv_games_list);

        rvGames.setLayoutManager(new LinearLayoutManager(getContext()));

        btnBoost.setOnClickListener(v -> {
            if (getContext() != null) {
                String resultMsg = GameManagerRepository.boostRamAndOptimize(getContext());
                Toast.makeText(getContext(), resultMsg, Toast.LENGTH_LONG).show();
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
        if (getContext() != null && tvHeader != null && rvGames != null) {
            List<GameAppInfo> installedGames = GameManagerRepository.getInstalledGames(getContext());
            tvHeader.setText("INSTALLED GAMES DETECTED (" + installedGames.size() + "):");
            GamesAdapter adapter = new GamesAdapter(getContext(), installedGames);
            rvGames.setAdapter(adapter);
        }
    }
}
